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
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonRecordQuery;
import dk.magenta.datafordeler.cpr.records.person.data.CustodyDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.ParentDataRecord;
import dk.magenta.datafordeler.geo.GeoLookupService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.*;

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

    private Logger log = LogManager.getLogger(CprRecordFamilyRelationService.class.getCanonicalName());

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
            personQuery.setPersonnummer(cprNummer);

            OffsetDateTime now = OffsetDateTime.now();
            personQuery.setRegistrationAt(now);
            personQuery.setEffectAt(now);

            this.applyAreaRestrictionsToQuery(personQuery, user);

            List<PersonEntity> personEntities = QueryManager.getAllEntities(session, personQuery, PersonEntity.class);
            PersonEntity personEntity = null;
            if (!personEntities.isEmpty()) {
                personEntity = personEntities.get(0);
            }

            if(personEntity==null) {
                throw new HttpNotFoundException("No entity with CPR number " + cprNummer + " was found");
            }

            String fatherPnr = personEntity.getFather().current().get(0).getCprNumber();
            String motherPnr = personEntity.getMother().current().get(0).getCprNumber();

            personQuery.setPersonnummer(fatherPnr);
            PersonEntity father = QueryManager.getAllEntitiesAsStream(session, personQuery, PersonEntity.class).findFirst().orElse(null);
            personQuery.setPersonnummer(motherPnr);
            PersonEntity mother = QueryManager.getAllEntitiesAsStream(session, personQuery, PersonEntity.class).findFirst().orElse(null);

            Boolean motherhasCustody = true;
            Boolean fatherhasCustody = true;
            List<CustodyDataRecord> currentCustodyList = personEntity.getCustody().current();
            if(currentCustodyList.size() != 0) {
                motherhasCustody = currentCustodyList.stream().anyMatch(r -> r.getRelationType()==3 && r.getRelationPnr().equals(cprNummer));
                fatherhasCustody = currentCustodyList.stream().anyMatch(r -> r.getRelationType()==4 && r.getRelationPnr().equals(cprNummer));
            }

            String hql = "SELECT personEntity " +
                    "FROM "+ PersonEntity.class.getCanonicalName()+" personEntity "+
                    "JOIN "+ ParentDataRecord.class.getCanonicalName() + " mother ON mother."+ParentDataRecord.DB_FIELD_ENTITY+"=personEntity."+"id"+" "+
                    "JOIN "+ ParentDataRecord.class.getCanonicalName() + " father ON father."+ParentDataRecord.DB_FIELD_ENTITY+"=personEntity."+"id"+" "+
                    " WHERE mother."+ParentDataRecord.DB_FIELD_CPR_NUMBER+"=:motherPnr"+
                    " AND father."+ParentDataRecord.DB_FIELD_CPR_NUMBER+"=:fatherPnr";

            Query siblingQuery = session.createQuery(hql);
            siblingQuery.setParameter("motherPnr", motherPnr);
            siblingQuery.setParameter("fatherPnr", fatherPnr);
            List<PersonEntity> siblingList = siblingQuery.getResultList();
            Object obj = personOutputWrapper.wrapRecordResultFilteredInfo(personEntity, father, mother, siblingList);
            return obj.toString();
        } catch(Exception e) {
            e.printStackTrace();
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
