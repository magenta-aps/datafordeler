package dk.magenta.datafordeler.combined;

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
import dk.magenta.datafordeler.cpr.data.person.PersonCustodyRelationsManager;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonRecordQuery;
import dk.magenta.datafordeler.cpr.records.person.data.CustodyDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.ParentDataRecord;
import dk.magenta.datafordeler.geo.GeoLookupService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Lookup the family-relation of a person.
 * The service finds the parents of a person, and information about if the parents has custody over the children.
 * The service also deliveres a list of siblings of the requested person
 */
@RestController
@RequestMapping("/combined/familyRelation/1")
public class CprRecordFamilyRelationService {

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private DafoUserManager dafoUserManager;

    @Autowired
    private CprPlugin cprPlugin;

    @Autowired
    protected MonitorService monitorService;

    private final Logger log = LogManager.getLogger(CprRecordFamilyRelationService.class.getCanonicalName());

    @Autowired
    private PersonOutputWrapper personOutputWrapper;

    @PostConstruct
    public void init() {
    }

    @RequestMapping(method = RequestMethod.GET, path = "/cpr/{cprNummer}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String getSingle(@PathVariable("cprNummer") String cprNummer, HttpServletRequest request)
            throws AccessDeniedException, InvalidTokenException, HttpNotFoundException, InvalidCertificateException {

        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.info(
                "Incoming REST request for CprRecordFamilyRelationService with cprNummer " + cprNummer
        );
        this.checkAndLogAccess(loggerHelper);
        loggerHelper.urlInvokePersistablelogs("CprRecordFamilyRelationService");

        try (final Session session = sessionManager.getSessionFactory().openSession()) {
            GeoLookupService lookupService = new GeoLookupService(sessionManager);
            personOutputWrapper.setLookupService(lookupService);
            PersonRecordQuery personQuery = new PersonRecordQuery();
            personQuery.setParameter(PersonRecordQuery.PERSONNUMMER, cprNummer);

            OffsetDateTime now = OffsetDateTime.now();
            personQuery.setRegistrationAt(now);
            personQuery.setEffectAt(now);

            this.applyAreaRestrictionsToQuery(personQuery, user);

            List<PersonEntity> personEntities = QueryManager.getAllEntities(session, personQuery, PersonEntity.class);
            PersonEntity personEntity = null;
            if (!personEntities.isEmpty()) {
                personEntity = personEntities.get(0);
            } else {
                throw new HttpNotFoundException("No entity with CPR number " + cprNummer + " was found");
            }

            ParentDataRecord fatherRec = personEntity.getFather().current().stream().findFirst().orElse(null);
            String fatherPnr = null;
            PersonEntity fatherEntity = null;
            if (fatherRec != null) {
                fatherPnr = fatherRec.getCprNumber();
                personQuery.setParameter(PersonRecordQuery.PERSONNUMMER, fatherPnr);
                fatherEntity = QueryManager.getAllEntitiesAsStream(session, personQuery, PersonEntity.class).findFirst().orElse(null);
            }
            ParentDataRecord motherRec = personEntity.getMother().current().stream().findFirst().orElse(null);
            String motherPnr = null;
            PersonEntity motherEntity = null;
            if (motherRec != null) {
                motherPnr = motherRec.getCprNumber();
                personQuery.setParameter(PersonRecordQuery.PERSONNUMMER, motherPnr);
                motherEntity = QueryManager.getAllEntitiesAsStream(session, personQuery, PersonEntity.class).findFirst().orElse(null);
            }

            Boolean motherhasCustody = true;
            Boolean fatherhasCustody = true;
            List<CustodyDataRecord> currentCustodyList = personEntity.getCustody().current();
            if (LocalDateTime.now().minusYears(18).isAfter(PersonCustodyRelationsManager.findNewestUnclosed(personEntity.getBirthTime().current()).getBirthDatetime())) {
                motherhasCustody = false;
                fatherhasCustody = false;
            } else if (currentCustodyList.size() != 0) {
                motherhasCustody = currentCustodyList.stream().anyMatch(r -> r.getRelationType() == 3);
                fatherhasCustody = currentCustodyList.stream().anyMatch(r -> r.getRelationType() == 4);
            }

            String hql = "SELECT personEntity " +
                    "FROM " + PersonEntity.class.getCanonicalName() + " personEntity " +
                    "JOIN " + ParentDataRecord.class.getCanonicalName() + " mother ON mother." + ParentDataRecord.DB_FIELD_ENTITY + "=personEntity." + "id" + " " +
                    "JOIN " + ParentDataRecord.class.getCanonicalName() + " father ON father." + ParentDataRecord.DB_FIELD_ENTITY + "=personEntity." + "id" + " " +
                    " WHERE mother." + ParentDataRecord.DB_FIELD_CPR_NUMBER + "=:motherPnr" +
                    " AND father." + ParentDataRecord.DB_FIELD_CPR_NUMBER + "=:fatherPnr";

            Query siblingQuery = session.createQuery(hql);
            siblingQuery.setParameter("motherPnr", motherPnr);
            siblingQuery.setParameter("fatherPnr", fatherPnr);
            List<PersonEntity> siblingList = siblingQuery.getResultList();
            Object obj = personOutputWrapper.wrapRecordResultFilteredInfo(personEntity, fatherEntity, fatherhasCustody, motherEntity, motherhasCustody, siblingList);
            return obj.toString();
        } catch (Exception e) {
            throw new HttpNotFoundException("No entity with CPR number " + cprNummer + " was found");
        }
    }

    protected void checkAndLogAccess(LoggerHelper loggerHelper) throws AccessDeniedException {
        try {
            loggerHelper.getUser().checkHasSystemRole(CprRolesDefinition.READ_CPR_ROLE);
        } catch (AccessDeniedException e) {
            loggerHelper.info("Access denied: " + e.getMessage());
            throw (e);
        }
    }

    protected void applyAreaRestrictionsToQuery(PersonRecordQuery query, DafoUserDetails user) throws InvalidClientInputException {
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
