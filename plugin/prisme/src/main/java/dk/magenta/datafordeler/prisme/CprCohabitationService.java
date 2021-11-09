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
        if (cprs.size() > 100) {
            throw new QueryBuildException("Maximum 100 numbers is allowed");
        }

        OffsetDateTime now = OffsetDateTime.now();
        personQuery.setRegistrationAt(now);
        personQuery.setEffectAt(now);

        ObjectNode obj = objectMapper.createObjectNode();

        try (Session session = sessionManager.getSessionFactory().openSession()) {

            personQuery.applyFilters(session);
            this.applyAreaRestrictionsToQuery(personQuery, user);
            GeoLookupService lookupService = new GeoLookupService(sessionManager);

            List<PersonEntity> personEntities = QueryManager.getAllEntities(session, personQuery, PersonEntity.class);
            if(personEntities.size() != cprNumbers.size()) {
                throw new HttpNotFoundException(cprs.toString());
            }

            AddressDataRecord firstAddress = FilterUtilities.findNewestUnclosed(personEntities.get(0).getAddress());

            int municipalityCode =  firstAddress.getMunicipalityCode();
            int roadCode =  firstAddress.getRoadCode();
            GeoLookupDTO lookup = lookupService.doLookup(municipalityCode, roadCode);
            String houseNumber =  firstAddress.getHouseNumber();
            String door =  firstAddress.getDoor();
            String floor =  firstAddress.getFloor();
            String bnr =  firstAddress.getBuildingNumber();

            List<PersonEntity> matchingEntities = personEntities.stream().filter(item ->
                    FilterUtilities.findNewestUnclosed(item.getAddress().current()).getMunicipalityCode()==municipalityCode &&
                            FilterUtilities.findNewestUnclosed(item.getAddress().current()).getRoadCode()==roadCode &&
                            FilterUtilities.findNewestUnclosed(item.getAddress().current()).getHouseNumber().equals(houseNumber) &&
                            FilterUtilities.findNewestUnclosed(item.getAddress().current()).getDoor().equals(door) &&
                            FilterUtilities.findNewestUnclosed(item.getAddress().current()).getFloor().equals(floor) &&
                            FilterUtilities.findNewestUnclosed(item.getAddress().current()).getBuildingNumber().equals(bnr)
            ).collect(Collectors.toList());

            int counter = 1;
            LocalDate personBirthDate = null;
            OffsetDateTime lastMovingTimestamp = null;

            for(PersonEntity personEntity : matchingEntities) {
                personBirthDate = personEntity.getBirthTime().current().get(0).getBirthDatetime().toLocalDate();
                OffsetDateTime personMovingTimestamp = personEntity.getEvent().stream().filter(event -> "A01".equals(event.getEventId()) ||
                        "A05".equals(event.getEventId())).map(u -> u.getTimestamp()).max(OffsetDateTime::compareTo).orElse(null);
                //If there is no timestamp of actual movings, use the last timestamp of a new address
                if(personMovingTimestamp==null) {
                    personMovingTimestamp = personEntity.getAddress().getFirstCurrent().getEffectFrom();
                }
                //Store the newest timestamp in order to find the last moving
                if(personMovingTimestamp!=null && (lastMovingTimestamp==null || lastMovingTimestamp.isBefore(personMovingTimestamp))) {
                    lastMovingTimestamp = personMovingTimestamp;
                }
            }
            boolean allPersonsHasSameAddress = matchingEntities.size()==cprNumbers.size() && !lookup.isAdministrativ();
            obj.put("Cohabitation", allPersonsHasSameAddress);
            obj.put("ResidentDate", allPersonsHasSameAddress ? Optional.ofNullable(lastMovingTimestamp).map(OffsetDateTime::toLocalDate).map(LocalDate::toString).orElse(personBirthDate.toString()) : null);
            for(String cpr : cprNumbers) {
                obj.put("cpr"+counter, cpr);
                counter++;
            }
        }


        return obj;
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
