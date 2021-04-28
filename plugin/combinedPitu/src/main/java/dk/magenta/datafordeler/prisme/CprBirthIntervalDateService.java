package dk.magenta.datafordeler.prisme;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.MonitorService;
import dk.magenta.datafordeler.core.arearestriction.AreaRestriction;
import dk.magenta.datafordeler.core.arearestriction.AreaRestrictionType;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.exception.InvalidCertificateException;
import dk.magenta.datafordeler.core.exception.InvalidTokenException;
import dk.magenta.datafordeler.core.exception.QueryBuildException;
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
@RequestMapping("/combined/cpr/birthIntervalDate/1")
public class CprBirthIntervalDateService {

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

    private Logger log = LogManager.getLogger(CprBirthIntervalDateService.class.getCanonicalName());

    @PostConstruct
    public void init() {
    }

    @GetMapping("/search")
    public Envelope findAll(HttpServletRequest request, @RequestParam MultiValueMap<String, String> requestParams, HttpServletResponse response) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException, QueryBuildException {

        String birthAfter = requestParams.getFirst("birth.GTE");
        String birthBefore = requestParams.getFirst("birth.LTE");
        String pageSize = requestParams.getFirst("pageSize");
        String page = requestParams.getFirst("page");
        List<String>  municipalitycodes = Optional.ofNullable(requestParams.get("municipalitycode")).orElse(new ArrayList<>());
        List<String>  localitycodes = Optional.ofNullable(requestParams.get("localitycode")).orElse(new ArrayList<>());

        List<String> municipalitycodeNumbers = new ArrayList<String>();
        for (String municipalitycode : municipalitycodes) {
            List<String> municipalitycodeList = Arrays.asList(municipalitycode.split(","));
            for (String municipality : municipalitycodeList) {
                municipalitycodeNumbers.add(municipality);
            }
        }

        List<String> localitycodeNumbers = new ArrayList<String>();
        for (String localitycode : localitycodes) {
            List<String> localitycodeList = Arrays.asList(localitycode.split(","));
            for (String municipality : localitycodeList) {
                localitycodeNumbers.add(municipality);
            }
        }

        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);

        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.urlInvokePersistablelogs("CprBirthIntervalDateService");
        this.checkAndLogAccess(loggerHelper);

        OffsetDateTime now = OffsetDateTime.now();

        PersonRecordQuery personQuery = new PersonRecordQuery();
        if(pageSize != null) {
            personQuery.setPageSize(pageSize);
        }
        if(page != null) {
            personQuery.setPage(page);
        }

        personQuery.setRegistrationAt(now);
        personQuery.setEffectAt(now);
        personQuery.setKommunekoder(municipalitycodeNumbers);
        if(birthAfter!=null) {
            personQuery.setBirthTimeAfter(dk.magenta.datafordeler.core.fapi.Query.parseDateTime(birthAfter).toLocalDateTime());
        }
        if(birthBefore!=null) {
            personQuery.setBirthTimeBefore(dk.magenta.datafordeler.core.fapi.Query.parseDateTime(birthBefore).toLocalDateTime());
        }

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Envelope envelope = new Envelope();
            envelope.setRequestTimestamp(user.getCreationTime());
            envelope.setUsername(user.toString());

            personQuery.applyFilters(session);
            this.applyAreaRestrictionsToQuery(personQuery, user);

            List<String> personEntities =QueryManager.getAllEntities(session, personQuery, PersonEntity.class).stream().map(item -> item.getPersonnummer()).collect(Collectors.toList());;

            envelope.setPageSize(personQuery.getPageSize());
            envelope.setPage(personQuery.getPage());
            envelope.setPath(request.getServletPath());
            envelope.setResponseTimestamp(OffsetDateTime.now());

            envelope.setResults(personEntities);
            setHeaders(response);
            loggerHelper.urlResponsePersistablelogs(HttpStatus.OK.value(), "CprBirthIntervalDateService done");
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
        }
        catch (AccessDeniedException e) {
            loggerHelper.info("Access denied: " + e.getMessage());
            throw(e);
        }
    }

    protected void applyAreaRestrictionsToQuery(PersonRecordQuery query, DafoUserDetails user) {
        Collection<AreaRestriction> restrictions = user.getAreaRestrictionsForRole(CprRolesDefinition.READ_CPR_ROLE);
        AreaRestrictionDefinition areaRestrictionDefinition = this.cprPlugin.getAreaRestrictionDefinition();
        AreaRestrictionType municipalityType = areaRestrictionDefinition.getAreaRestrictionTypeByName(CprAreaRestrictionDefinition.RESTRICTIONTYPE_KOMMUNEKODER);
        for (AreaRestriction restriction : restrictions) {
            if (restriction.getType() == municipalityType) {
                query.addKommunekode(restriction.getValue());
            }
        }
    }
}
