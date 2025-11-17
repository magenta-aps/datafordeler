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
import dk.magenta.datafordeler.core.util.LoggerHelper;
import dk.magenta.datafordeler.cpr.CprAreaRestrictionDefinition;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonRecordQuery;
import dk.magenta.datafordeler.geo.GeoLookupService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

/**
 * Get the history of adresses on a specific person
 */
@RestController
@RequestMapping("/prisme/cpr/addresshistory/1")
public class CprAdressHistoryService {

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

    private final Logger log = LogManager.getLogger(CprAdressHistoryService.class.getCanonicalName());

    @Autowired
    private PersonAdressHistoryOutputWrapperPrisme personOutputWrapper;

    @PostConstruct
    public void init() {
        this.monitorService.addAccessCheckPoint("/prisme/cpr/addresshistory/1/1234");
    }

    @RequestMapping(method = RequestMethod.GET, path = {"/{cprNummer}","/{cprNummer}/"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public String getSingle(@PathVariable("cprNummer") String cprNummer, HttpServletRequest request)
            throws AccessDeniedException, InvalidTokenException, JsonProcessingException, HttpNotFoundException, InvalidCertificateException, InvalidClientInputException {

        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.info(
                "Incoming REST request for PrismeCprService with cprNummer " + cprNummer
        );
        this.checkAndLogAccess(loggerHelper);
        loggerHelper.urlInvokePersistablelogs("CprAdressHistoryService");

        try (final Session session = sessionManager.getSessionFactory().openSession()) {
            GeoLookupService lookupService = new GeoLookupService(sessionManager);
            personOutputWrapper.setLookupService(lookupService);

            PersonRecordQuery personQuery = new PersonRecordQuery();
            personQuery.setParameter(PersonRecordQuery.PERSONNUMMER, cprNummer);

            personQuery.applyFilters(session);
            this.applyAreaRestrictionsToQuery(personQuery, user);

            List<PersonEntity> personEntities = QueryManager.getAllEntities(session, personQuery, PersonEntity.class);

            if (!personEntities.isEmpty()) {
                PersonEntity personEntity = personEntities.get(0);
                loggerHelper.urlResponsePersistablelogs(HttpStatus.OK.value(), "CprAdressHistoryService done");
                return objectMapper.writeValueAsString(personOutputWrapper.wrapRecordResult(personEntity, personQuery));
            }
            loggerHelper.urlResponsePersistablelogs(HttpStatus.NOT_FOUND.value(), "CprAdressHistoryService done");
            throw new HttpNotFoundException("No entity with CPR number " + cprNummer + " was found");
        }
    }


    protected void checkAndLogAccess(LoggerHelper loggerHelper) throws AccessDeniedException {
        loggerHelper.logRequest();
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
