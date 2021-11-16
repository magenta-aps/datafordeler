package dk.magenta.datafordeler.prisme;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import dk.magenta.datafordeler.cpr.records.person.data.AddressDataRecord;
import dk.magenta.datafordeler.geo.GeoLookupDTO;
import dk.magenta.datafordeler.geo.GeoLookupService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

    @GetMapping("/search")
    public ObjectNode findAll(HttpServletRequest request, @RequestParam MultiValueMap<String, String> requestParams) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException, QueryBuildException, HttpNotFoundException {

        List<String> cprs = requestParams.get("cpr");

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
        if (cprs.size() > 2) {
            throw new QueryBuildException("Maximum 2 numbers is allowed");
        }

        OffsetDateTime now = OffsetDateTime.now();
        personQuery.setRegistrationAt(now);

        try (Session session = sessionManager.getSessionFactory().openSession()) {

            personQuery.applyFilters(session);
            this.applyAreaRestrictionsToQuery(personQuery, user);
            GeoLookupService lookupService = new GeoLookupService(sessionManager);

            List<PersonEntity> personEntities = QueryManager.getAllEntities(session, personQuery, PersonEntity.class);
            if (personEntities.size() != cprNumbers.size()) {
                throw new HttpNotFoundException(cprs.toString());
            }

            List<AddressDataRecord> firstAddList = FilterUtilities.sortRecordsOnEffect(personEntities.get(0).getAddress()
                    .stream().filter( adress -> !adress.isUndone()).collect(Collectors.toList()));
            List<AddressDataRecord> secondAddList = FilterUtilities.sortRecordsOnEffect(personEntities.get(1).getAddress()
                    .stream().filter( adress -> !adress.isUndone()).collect(Collectors.toList()));

            if (!this.compareAdresses(firstAddList.get(0), secondAddList.get(0))) {
                System.out.println("NOTEQUAL");
                return constructResponse(cprNumbers, false, null);
            }

            System.out.println("SAME");

            OffsetDateTime theFirstMatchingOne = findFirstCommonAdress(firstAddList, secondAddList);

            return constructResponse(cprNumbers, true, Optional.ofNullable(theFirstMatchingOne).map(OffsetDateTime::toLocalDate).map(LocalDate::toString).get());

        }
    }

    private ObjectNode constructResponse(List<String> cprNumbers, boolean cohabitation, String residentDate) {
        ObjectNode obj = objectMapper.createObjectNode();

        obj.put("Cohabitation", cohabitation);
        obj.put("ResidentDate", residentDate);
        int counter = 1;
        for(String cpr : cprNumbers) {
            obj.put("cpr" + counter, cpr);
            counter++;
        }
        return obj;
    }

    private OffsetDateTime findFirstCommonAdress(List<AddressDataRecord> adressList1, List<AddressDataRecord> adressList2) {

        OffsetDateTime commonAdressTime = null;
        for (int i = 0; i < Math.min(adressList1.size(), adressList2.size()); i++) {
            AddressDataRecord adress1 = adressList1.get(i);
            AddressDataRecord adress2 = adressList2.get(i);
            if (this.compareAdresses(adress1, adress2) &&
                    adress1.getEffectFrom().equals(adress2.getEffectFrom())) {
                commonAdressTime = adress1.getEffectFrom();
            } else {
                return commonAdressTime;
            }
        }
        return commonAdressTime;
    }


    private boolean compareAdresses(AddressDataRecord adress1, AddressDataRecord adress2) {
        return adress1.getMunicipalityCode()==adress2.getMunicipalityCode() &&
                adress1.getRoadCode()==adress2.getRoadCode() &&
                adress1.getHouseNumber().equals(adress2.getHouseNumber()) &&
                adress1.getDoor().equals(adress2.getDoor()) &&
                adress1.getFloor().equals(adress2.getFloor()) &&
                adress1.getBuildingNumber().equals(adress2.getBuildingNumber());
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
                query.addKommunekodeRestriction(restriction.getValue());
            }
        }
    }
}
