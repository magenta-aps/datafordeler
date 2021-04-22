package dk.magenta.datafordeler.prisme;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.MonitorService;
import dk.magenta.datafordeler.core.arearestriction.AreaRestriction;
import dk.magenta.datafordeler.core.arearestriction.AreaRestrictionType;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.*;
import dk.magenta.datafordeler.core.plugin.AreaRestrictionDefinition;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.FinalWrapper;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import dk.magenta.datafordeler.cpr.CprAreaRestrictionDefinition;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonRecordQuery;
import dk.magenta.datafordeler.cpr.records.person.data.AddressDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.PersonStatusDataRecord;
import dk.magenta.datafordeler.geo.GeoLookupService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Get the history of cohabitation
 */
@RestController
@RequestMapping("/prisme/cpr/cohabitationinformation/1")
public class CprCohabitationService {

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

    private Logger log = LogManager.getLogger(CprCohabitationService.class.getCanonicalName());

    @PostConstruct
    public void init() {
    }

    @GetMapping("/cpr")
    public StreamingResponseBody findAll(HttpServletRequest request, @RequestParam MultiValueMap<String, String> requestParams) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException, QueryBuildException {

        List<String> cprs = requestParams.get("cpr");
        String allowDirect = requestParams.getFirst("allowDirect");

        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);

        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.urlInvokePersistablelogs("CprRecordCombinedPersonLookupService");
        this.checkAndLogAccess(loggerHelper);

        PersonRecordQuery personQuery = new PersonRecordQuery();

        List<String> cprNumbers = new ArrayList<String>();

        for (String cpr : cprs) {
            List<String> subCprList = Arrays.asList(cpr.split(","));
            for (String subCpr : subCprList) {
                cprNumbers.add(subCpr);
            }
        }

        personQuery.setPersonnumre(cprNumbers);
        if (cprs.size() > 100) {
            throw new QueryBuildException("Maximum 100 numbers is allowed");
        }

        OffsetDateTime now = OffsetDateTime.now();
        personQuery.setRegistrationAt(now);
        personQuery.setEffectAt(now);

            try (Session session = sessionManager.getSessionFactory().openSession()) {

                personQuery.applyFilters(session);
                this.applyAreaRestrictionsToQuery(personQuery, user);

                Stream<PersonEntity> personEntities = QueryManager.getAllEntitiesAsStream(session, personQuery, PersonEntity.class);
                AddressDataRecord firstAddress = FilterUtilities.findNewestUnclosed(personEntities.findFirst().get().getAddress());

                int munipialicity =  firstAddress.getMunicipalityCode();
                int road =  firstAddress.getRoadCode();
                String houseNumber =  firstAddress.getHouseNumber();
                String door =  firstAddress.getDoor();
                String floor =  firstAddress.getFloor();
                String bnr =  firstAddress.getBuildingNumber();

                List<PersonEntity> matchingEntities = personEntities.filter(item ->
                                FilterUtilities.findNewestUnclosed(item.getAddress().current()).getMunicipalityCode()==munipialicity &&
                                FilterUtilities.findNewestUnclosed(item.getAddress().current()).getRoadCode()==road &&
                                FilterUtilities.findNewestUnclosed(item.getAddress().current()).getHouseNumber()==houseNumber &&
                                FilterUtilities.findNewestUnclosed(item.getAddress().current()).getDoor()==door &&
                                FilterUtilities.findNewestUnclosed(item.getAddress().current()).getFloor()==floor &&
                                FilterUtilities.findNewestUnclosed(item.getAddress().current()).getBuildingNumber()==bnr
                        ).collect(Collectors.toList());

                System.out.println(matchingEntities);




            }

            return null;

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
