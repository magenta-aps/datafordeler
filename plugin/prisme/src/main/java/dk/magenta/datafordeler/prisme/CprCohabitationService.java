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
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    private final Logger log = LogManager.getLogger(CprCohabitationService.class.getCanonicalName());

    @PostConstruct
    public void init() {
    }

    @GetMapping("/search")
    public ObjectNode findAll(HttpServletRequest request, @RequestParam MultiValueMap<String, String> requestParams) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException, QueryBuildException, HttpNotFoundException, InvalidClientInputException {

        List<String> cprs = requestParams.get("cpr");

        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);

        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.urlInvokePersistablelogs("CprRecordCombinedPersonLookupService");
        this.checkAndLogAccess(loggerHelper);

        PersonRecordQuery personQuery = new PersonRecordQuery();

        List<String> cprNumbers = new ArrayList<String>();

        for (String cpr : cprs) {
            String[] subCprList = cpr.split(",");
            for (String subCpr : subCprList) {
                cprNumbers.add(subCpr);
            }
        }

        personQuery.setParameter(PersonRecordQuery.PERSONNUMMER, cprNumbers);
        if (cprs.size() > 2) {
            throw new QueryBuildException("Maximum 2 numbers is allowed");
        }

        OffsetDateTime now = OffsetDateTime.now();
        personQuery.setRegistrationAt(now);

        try (Session session = sessionManager.getSessionFactory().openSession()) {

            personQuery.applyFilters(session);
            this.applyAreaRestrictionsToQuery(personQuery, user);

            List<PersonEntity> personEntities = QueryManager.getAllEntities(session, personQuery, PersonEntity.class);
            if (personEntities.size() != cprNumbers.size()) {
                throw new HttpNotFoundException(cprs.toString());
            }

            List<AddressDataRecord> firstAddList = FilterUtilities.sortRecordsOnEffect(personEntities.get(0).getAddress()
                    .stream().filter(adress -> !adress.isUndone()).collect(Collectors.toList()));
            List<AddressDataRecord> secondAddList = FilterUtilities.sortRecordsOnEffect(personEntities.get(1).getAddress()
                    .stream().filter(adress -> !adress.isUndone()).collect(Collectors.toList()));

            if (!this.compareAdresses(firstAddList.get(0), secondAddList.get(0)) ||
                    firstAddList.get(0).getEffectTo() != null ||
                    secondAddList.get(0).getEffectTo() != null) {
                return constructResponse(cprNumbers, false, null);
            }

            OffsetDateTime theFirstMatchingOne = findFirstCommonAdress(firstAddList, secondAddList);

            return constructResponse(cprNumbers, true, Optional.ofNullable(theFirstMatchingOne).map(OffsetDateTime::toLocalDate).map(LocalDate::toString).get());

        }
    }

    private ObjectNode constructResponse(List<String> cprNumbers, boolean cohabitation, String residentDate) {
        ObjectNode obj = objectMapper.createObjectNode();

        obj.put("Cohabitation", cohabitation);
        obj.put("ResidentDate", residentDate);
        int counter = 1;
        for (String cpr : cprNumbers) {
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
            if (this.compareAdresses(adress1, adress2)) {
                if (Equality.cprDomainEqualDate(adress1.getEffectFrom(), adress2.getEffectFrom())) {
                    // Save the timestamp an iterate to find out if there is earlier common adresses
                    commonAdressTime = adress1.getEffectFrom();
                } else {
                    // find the time when the last of the two persons moved in
                    if (adress1.getEffectFrom().isBefore(adress2.getEffectFrom())) {
                        return adress2.getEffectFrom();
                    } else {
                        return adress1.getEffectFrom();
                    }
                }
            } else {
                return commonAdressTime;
            }
        }
        return commonAdressTime;
    }


    private boolean compareAdresses(AddressDataRecord adress1, AddressDataRecord adress2) {
        return adress1.getMunicipalityCode() == adress2.getMunicipalityCode() &&
                adress1.getRoadCode() == adress2.getRoadCode() &&
                Objects.equals(adress1.getHouseNumber(), adress2.getHouseNumber()) &&
                Objects.equals(adress1.getDoor(), adress2.getDoor()) &&
                Objects.equals(adress1.getFloor(), adress2.getFloor()) &&
                Objects.equals(adress1.getBuildingNumber(), adress2.getBuildingNumber());
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
