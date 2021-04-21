package dk.magenta.datafordeler.prisme;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.MonitorService;
import dk.magenta.datafordeler.core.arearestriction.AreaRestriction;
import dk.magenta.datafordeler.core.arearestriction.AreaRestrictionType;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.exception.HttpNotFoundException;
import dk.magenta.datafordeler.core.exception.InvalidCertificateException;
import dk.magenta.datafordeler.core.exception.InvalidTokenException;
import dk.magenta.datafordeler.core.plugin.AreaRestrictionDefinition;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import dk.magenta.datafordeler.cpr.CprAreaRestrictionDefinition;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonRecordQuery;
import dk.magenta.datafordeler.cpr.records.person.data.PersonStatusDataRecord;
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

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
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

    private Logger log = LogManager.getLogger(CprResidentService.class.getCanonicalName());

    @PostConstruct
    public void init() {
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{cprNummer}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResidentItem getSingle(@PathVariable("cprNummer") String cprNummer, HttpServletRequest request)
            throws AccessDeniedException, InvalidTokenException, JsonProcessingException, HttpNotFoundException, InvalidCertificateException {

        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.info(
                "Incoming REST request for residentinformation with cprNummer " + cprNummer
        );
        this.checkAndLogAccess(loggerHelper);
        loggerHelper.urlInvokePersistablelogs("residentinformation");

        try(final Session session = sessionManager.getSessionFactory().openSession();) {
            PersonRecordQuery personQuery = new PersonRecordQuery();
            personQuery.setPersonnummer(cprNummer);
            personQuery.applyFilters(session);
            this.applyAreaRestrictionsToQuery(personQuery, user);
            List<PersonEntity> personEntities = QueryManager.getAllEntities(session, personQuery, PersonEntity.class);

            if (!personEntities.isEmpty()) {

                PersonEntity personEntity = personEntities.get(0);
                List<PersonStatusDataRecord> statusList = FilterUtilities.sortRecordsOnEffect(personEntity.getStatus());
                ResidentItem residentInfo = new ResidentItem(cprNummer, false, null);
                for(PersonStatusDataRecord status : statusList) {

                    if(status.getStatus()==5 || status.getStatus()==7) {
                        residentInfo.setTimestamp(status.getEffectFrom().toLocalDate());
                        residentInfo.setResidentInGL(true);
                    } else {
                        //If a status for the person is found where the person is not living in greenland return the last values of the person living in greenland
                        loggerHelper.urlResponsePersistablelogs(HttpStatus.OK.value(), "residentinformation done");
                        return residentInfo;
                    }
                }
                residentInfo.setTimestamp(FilterUtilities.findNewestUnclosed(personEntity.getBirthTime().current()).getBirthDatetime().toLocalDate());
                loggerHelper.urlResponsePersistablelogs(HttpStatus.OK.value(), "residentinformation done");
                return residentInfo;
            }
            loggerHelper.urlResponsePersistablelogs(HttpStatus.NOT_FOUND.value(), "residentinformation done");
            throw new HttpNotFoundException("No entity with CPR number " + cprNummer + " was found");
        }
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
