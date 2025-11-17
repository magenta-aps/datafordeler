package dk.magenta.datafordeler.prisme;

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
import dk.magenta.datafordeler.core.util.Equality;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import dk.magenta.datafordeler.cpr.CprAreaRestrictionDefinition;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonRecordQuery;
import dk.magenta.datafordeler.cpr.records.person.data.AddressDataRecord;
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

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Get the history of adresses on a specific person
 */
@RestController
@RequestMapping("/prisme/cpr/residentinformation/1")
public class CprResidentService {

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

    private final Logger log = LogManager.getLogger(CprResidentService.class.getCanonicalName());

    @PostConstruct
    public void init() {
    }

    @RequestMapping(method = RequestMethod.GET, path = {"/{cprNummer}", "/{cprNummer}/"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResidentItem getSingle(@PathVariable("cprNummer") String cprNummer, HttpServletRequest request)
            throws AccessDeniedException, InvalidTokenException, HttpNotFoundException, InvalidCertificateException, InvalidClientInputException {

        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user, this.getClass());
        loggerHelper.info(
                "Incoming REST request for residentinformation with cprNummer " + cprNummer
        );
        this.checkAndLogAccess(loggerHelper);
        loggerHelper.urlInvokePersistablelogs("residentinformation");

        try (final Session session = sessionManager.getSessionFactory().openSession()) {
            PersonRecordQuery personQuery = new PersonRecordQuery();
            personQuery.setParameter(PersonRecordQuery.PERSONNUMMER, cprNummer);
            OffsetDateTime now = OffsetDateTime.now();
            personQuery.setRegistrationAt(now);
            personQuery.applyFilters(session);
            this.applyAreaRestrictionsToQuery(personQuery, user);
            List<PersonEntity> personEntities = QueryManager.getAllEntities(session, personQuery, PersonEntity.class);

            if (!personEntities.isEmpty()) {

                PersonEntity personEntity = personEntities.get(0);
                List<AddressDataRecord> addList = FilterUtilities.sortRecordsOnEffect(personEntity.getAddress());
                ResidentItem residentInfo = new ResidentItem(cprNummer, false, null);
                OffsetDateTime lastEffectFrom = null;

                //Iterate backward through municipality of the person, store when the first danish address is found
                for (AddressDataRecord add : addList) {

                    // If newest adressrecord is not active, this citizen lives outside DK and GL, return false
                    if (lastEffectFrom == null && add.getEffectTo() != null) {
                        loggerHelper.urlResponsePersistablelogs(HttpStatus.OK.value(), "residentinformation done");
                        return residentInfo;
                    }

                    // Municipalitycode=900 to support adresses from before the merging of municipalitynumbers
                    if (add.getMunicipalityCode() > 900 && (lastEffectFrom == null || Equality.cprDomainEqualDate(lastEffectFrom, add.getEffectTo()))) {
                        lastEffectFrom = add.getEffectFrom();
                        residentInfo.setDato(add.getEffectFrom().toLocalDate());
                        residentInfo.setBorIGL(true);
                    } else {
                        //If a status for the person is found where the person is not living in greenland return the last values of the person living in greenland
                        loggerHelper.urlResponsePersistablelogs(HttpStatus.OK.value(), "residentinformation done");
                        return residentInfo;
                    }
                }
                loggerHelper.urlResponsePersistablelogs(HttpStatus.OK.value(), "residentinformation done");
                return residentInfo;
            }
            loggerHelper.urlResponsePersistablelogs(HttpStatus.NOT_FOUND.value(), "residentinformation done");
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
