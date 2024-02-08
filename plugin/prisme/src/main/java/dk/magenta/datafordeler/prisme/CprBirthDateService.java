package dk.magenta.datafordeler.prisme;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.MonitorService;
import dk.magenta.datafordeler.core.arearestriction.AreaRestriction;
import dk.magenta.datafordeler.core.arearestriction.AreaRestrictionType;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.exception.InvalidCertificateException;
import dk.magenta.datafordeler.core.exception.InvalidParameterException;
import dk.magenta.datafordeler.core.exception.InvalidTokenException;
import dk.magenta.datafordeler.core.fapi.Envelope;
import dk.magenta.datafordeler.core.plugin.AreaRestrictionDefinition;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import dk.magenta.datafordeler.cpr.CprAreaRestrictionDefinition;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonRecordQuery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Get the history of cohabitation
 */
@RestController
@RequestMapping("/prisme/cpr/under18years/1")
public class CprBirthDateService {

    @Autowired
    SessionManager sessionManager;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private DafoUserManager dafoUserManager;

    @Autowired
    private CprPlugin cprPlugin;

    @Autowired
    protected MonitorService monitorService;

    private final Logger log = LogManager.getLogger(CprBirthDateService.class.getCanonicalName());

    @PostConstruct
    public void init() {
    }

    @GetMapping("/search")
    public Envelope findAll(HttpServletRequest request, @RequestParam MultiValueMap<String, String> requestParams, HttpServletResponse response) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException, InvalidParameterException {

        String updatedSince = requestParams.getFirst("updatedSince");
        String pageSize = requestParams.getFirst("pageSize");
        String page = requestParams.getFirst("page");
        List<String> municipalitycodes = Optional.ofNullable(requestParams.get("municipalitycode")).orElse(new ArrayList<>());
        List<String> municipalitycodeNumbers = new ArrayList<String>();

        for (String municipalitycode : municipalitycodes) {
            String[] municipalitycodeList = municipalitycode.split(",");
            for (String municipality : municipalitycodeList) {
                municipalitycodeNumbers.add(municipality);
            }
        }

        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);

        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.urlInvokePersistablelogs("CprBirthDateService");
        this.checkAndLogAccess(loggerHelper);

        OffsetDateTime now = OffsetDateTime.now();

        OffsetDateTime offsetTimestampGTE = null;
        if (updatedSince != null) {
            offsetTimestampGTE = dk.magenta.datafordeler.core.fapi.Query.parseDateTime(updatedSince);
        }

        PersonRecordQuery personQuery = new PersonRecordQuery();
        if (pageSize != null) {
            int pageSizeInt = 10;
            pageSizeInt = Integer.valueOf(pageSize);
            if (pageSizeInt > 1000) {
                throw new InvalidParameterException("pageSize");
            }
            personQuery.setPageSize(pageSize);
        }
        if (page != null) {
            personQuery.setPage(page);
        }

        personQuery.setRegistrationAt(now);
        personQuery.setEffectAt(now);
        personQuery.setBirthTimeAfter(LocalDateTime.now().minusYears(18));
        personQuery.setParameter(PersonRecordQuery.KOMMUNEKODE, municipalitycodeNumbers);
        personQuery.setRecordAfter(offsetTimestampGTE);

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Envelope envelope = new Envelope();
            envelope.setRequestTimestamp(user.getCreationTime());
            envelope.setUsername(user.toString());
            this.applyAreaRestrictionsToQuery(personQuery, user);

            List<String> personEntities = QueryManager.getAllEntities(session, personQuery, PersonEntity.class).stream().map(item -> item.getPersonnummer()).collect(Collectors.toList());

            envelope.setPageSize(personQuery.getPageSize());
            envelope.setPage(personQuery.getPage());
            envelope.setPath(request.getServletPath());
            envelope.setResponseTimestamp(OffsetDateTime.now());

            envelope.setResults(personEntities);
            setHeaders(response);
            loggerHelper.urlResponsePersistablelogs(HttpStatus.OK.value(), "CprBirthDateService done");
            return envelope;
        }

    }

    private static void setHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Content-Type", "application/json; charset=utf-8");
        response.setStatus(200);
    }


    protected void checkAndLogAccess(LoggerHelper loggerHelper) throws AccessDeniedException {
        try {
            loggerHelper.getUser().checkHasSystemRole(CprRolesDefinition.READ_CPR_ROLE);
        } catch (AccessDeniedException e) {
            loggerHelper.info("Access denied: " + e.getMessage());
            throw (e);
        }
    }

    protected void applyAreaRestrictionsToQuery(PersonRecordQuery query, DafoUserDetails user) {
        Collection<AreaRestriction> restrictions = user.getAreaRestrictionsForRole(CprRolesDefinition.READ_CPR_ROLE);
        AreaRestrictionDefinition areaRestrictionDefinition = this.cprPlugin.getAreaRestrictionDefinition();
        AreaRestrictionType municipalityType = areaRestrictionDefinition.getAreaRestrictionTypeByName(CprAreaRestrictionDefinition.RESTRICTIONTYPE_KOMMUNEKODER);
        for (AreaRestriction restriction : restrictions) {
            if (restriction.getType() == municipalityType) {
                query.addKommunekodeRestriction(restriction.getValue());
            }
        }
    }
}
