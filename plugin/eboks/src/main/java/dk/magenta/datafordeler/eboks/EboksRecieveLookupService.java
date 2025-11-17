package dk.magenta.datafordeler.eboks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.MonitorService;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.exception.AccessRequiredException;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonRecordQuery;
import dk.magenta.datafordeler.cpr.records.person.data.BirthTimeDataRecord;
import dk.magenta.datafordeler.cvr.access.CvrRolesDefinition;
import dk.magenta.datafordeler.cvr.query.CompanyRecordQuery;
import dk.magenta.datafordeler.cvr.records.AddressMunicipalityRecord;
import dk.magenta.datafordeler.cvr.records.AddressRecord;
import dk.magenta.datafordeler.cvr.records.CompanyRecord;
import dk.magenta.datafordeler.eboks.utils.FilterUtilities;
import dk.magenta.datafordeler.ger.data.company.CompanyEntity;
import dk.magenta.datafordeler.ger.data.company.CompanyQuery;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Webservice for finding out if a cpr- or cvr- number is allowed to recieve e-post
 * <p>
 * Persons that should not recieve eboks-letters
 * Persons that is under 15 years old
 * Persons that is dead
 * Persons that has had any adress in greenland either current or historic since 2017
 */
@RestController
@RequestMapping("/eboks/recipient")
public class EboksRecieveLookupService {

    @Autowired
    SessionManager sessionManager;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private DafoUserManager dafoUserManager;

    @Autowired
    protected MonitorService monitorService;

    private final Logger log = LogManager.getLogger(EboksRecieveLookupService.class.getCanonicalName());


    @PostConstruct
    public void init() {
        this.monitorService.addAccessCheckPoint("/eboks/recipient/lookup?cpr=1111&cvr=1111");

    }

    @RequestMapping(method = RequestMethod.GET, path = {"/{lookup}","/{lookup}/"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public String getSingle(@RequestParam(value = "cpr", required = false, defaultValue = "") List<String> cprs, @RequestParam(value = "cvr", required = false, defaultValue = "") List<String> cvrs, HttpServletRequest request)
            throws DataFordelerException, JsonProcessingException {

        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user, this.getClass());
        this.checkAndLogAccess(loggerHelper);

        PersonRecordQuery personQuery = new PersonRecordQuery();
        personQuery.setPageSize(Integer.MAX_VALUE);

        try (Session session = sessionManager.getSessionFactory().openSession()) {

            ArrayList<FailResult> failedCprs = new ArrayList<FailResult>();
            ArrayList<FailResult> failedCvrs = new ArrayList<FailResult>();
            ArrayNode validCprList = objectMapper.createArrayNode();

            if (cprs != null && !cprs.isEmpty()) {
                personQuery.setParameter(PersonRecordQuery.PERSONNUMMER, cprs);
                // The date that the eboks-system was initiated
                // This date is relevant in order to find out if the person can be excluded from recieving eboks-letters for not beeing from greenland.
                // A person that has not had adress in greenland since 8. June 2017, gan not recieve eboks-letters
                OffsetDateTime eboxStart = OffsetDateTime.of(2017, 6, 8, 0, 0, 0, 0, ZoneOffset.UTC);

                OffsetDateTime now = OffsetDateTime.now();
                personQuery.setRegistrationAt(now);
                // All records that has an effecttointerval that is after 8. June 2017 has tried living after that date
                personQuery.setEffectFromBefore(now);
                personQuery.setEffectToAfter(eboxStart);

                personQuery.applyFilters(session);
                Stream<PersonEntity> personEntities = QueryManager.getAllEntitiesAsStream(session, personQuery, PersonEntity.class);

                personEntities.forEach((k) -> {
                    BirthTimeDataRecord birthtime = FilterUtilities.findNewestUnclosedCpr(k.getBirthTime());
                    LocalDateTime fifteenYearsAgo = LocalDateTime.now().minusYears(15);
                    if (fifteenYearsAgo.isBefore(birthtime.getBirthDatetime())) {
                        failedCprs.add(new FailResult(k.getPersonnummer(), FailState.MINOR));
                    } else if (FilterUtilities.findNewestUnclosedCpr(k.getStatus()).getStatus() == 90) {
                        failedCprs.add(new FailResult(k.getPersonnummer(), FailState.DEAD));
                    } else if (k.getAddress().isEmpty() || k.getAddress().stream().anyMatch(address -> address.getMunicipalityCode() > 950)) {
                        validCprList.add(k.getPersonnummer());
                    } else {
                        failedCprs.add(new FailResult(k.getPersonnummer(), FailState.NOTFROMGREENLAND));
                    }
                    cprs.remove(k.getPersonnummer());
                });
            }

            ArrayNode cvrList = objectMapper.createArrayNode();

            if (cvrs != null && !cvrs.isEmpty()) {
                CompanyRecordQuery query = new CompanyRecordQuery();
                query.setPageSize(Integer.MAX_VALUE);
                query.setParameter(CompanyRecordQuery.CVRNUMMER, cvrs);
                Stream<CompanyRecord> companyEntities = QueryManager.getAllEntitiesAsStream(session, query, CompanyRecord.class);

                companyEntities.forEach((k) -> {
                    String cvrNumber = Integer.toString(k.getCvrNumber());

                    AddressRecord adress = FilterUtilities.findNewestUnclosedCvr(k.getLocationAddress());
                    if (adress == null) {
                        adress = FilterUtilities.findNewestCvr(k.getPostalAddress().currentRegistration());
                    }

                    String status = k.getMetadata().getCompanyStatusRecord(k).getStatus();
                    if (!"NORMAL".equals(status) && !"Aktiv".equals(status) && !"Fremtid".equals(status)) {
                        failedCvrs.add(new FailResult(cvrNumber, FailState.CEASED));
                    } else if (!this.companyFromGreenland(adress)) {
                        failedCvrs.add(new FailResult(cvrNumber, FailState.NOTFROMGREENLAND));
                    } else {
                        cvrList.add(cvrNumber);
                    }

                    cvrs.remove(cvrNumber);
                });
            }
            //Find the company as a ger company, if CVR-company does not exist
            if (cvrs != null && !cvrs.isEmpty()) {
                Collection<CompanyEntity> companyEntities = this.gerCompanyLookup(session, cvrs);
                if (!companyEntities.isEmpty()) {
                    companyEntities.forEach((k) -> {
                        String gerNo = Integer.toString(k.getGerNr());
                        if (k.getMunicipalityCode() >= 950) {
                            cvrList.add(gerNo);
                        } else {
                            failedCvrs.add(new FailResult(gerNo, FailState.NOTFROMGREENLAND));
                        }
                        cvrs.remove(gerNo);
                    });
                }
            }

            ObjectNode returnValue = objectMapper.createObjectNode();
            ObjectNode validValues = objectMapper.createObjectNode();
            validValues.set("cpr", validCprList);
            validValues.set("cvr", cvrList);

            ObjectNode invalidValues = objectMapper.createObjectNode();

            ArrayNode failedCvr = objectMapper.createArrayNode();
            ArrayNode failedCpr = objectMapper.createArrayNode();

            cprs.stream().forEach((item) -> {
                ObjectNode node = objectMapper.createObjectNode();
                node.put("nr", item);
                node.put("reason", FailState.MISSING.readableFailString);
                failedCpr.add(node);
            });

            failedCprs.stream().forEach((item) -> {

                ObjectNode node = objectMapper.createObjectNode();
                node.put("nr", item.id);
                node.put("reason", item.fail.readableFailString);
                failedCpr.add(node);
            });


            cvrs.stream().forEach((item) -> {
                ObjectNode node = objectMapper.createObjectNode();
                node.put("nr", item);
                node.put("reason", FailState.MISSING.readableFailString);
                failedCvr.add(node);
            });

            failedCvrs.stream().forEach((item) -> {
                ObjectNode node = objectMapper.createObjectNode();
                node.put("nr", item.id);
                node.put("reason", item.fail.readableFailString);
                failedCvr.add(node);
            });

            invalidValues.set("cpr", failedCpr);
            invalidValues.set("cvr", failedCvr);

            returnValue.set("valid", validValues);
            returnValue.set("invalid", invalidValues);

            if (returnValue != null) {
                return objectMapper.writeValueAsString(returnValue);
            }
        }
        return null;
    }

    private boolean companyFromGreenland(AddressRecord addressRecord) {
        AddressMunicipalityRecord municipalityRecord = addressRecord.getMunicipality();
        if (municipalityRecord != null) {
            return municipalityRecord.getMunicipalityCode() >= 950;
        } else return "GL".equalsIgnoreCase(addressRecord.getCountryCode());
    }

    public HashSet<CompanyEntity> gerCompanyLookup(Session session, Collection<String> cvrNumbers) {
        CompanyQuery query = new CompanyQuery();
        for (String cvrNumber : cvrNumbers) {
            query.addGerNr(cvrNumber);
        }
        List<CompanyEntity> companyEntities = QueryManager.getAllEntities(session, query, CompanyEntity.class);
        return new HashSet<>(companyEntities);
    }

    protected void checkAndLogAccess(LoggerHelper loggerHelper) throws AccessDeniedException, AccessRequiredException {
        loggerHelper.logRequest();
        try {
            loggerHelper.getUser().checkHasSystemRole(CvrRolesDefinition.READ_CVR_ROLE);
            loggerHelper.getUser().checkHasSystemRole(CprRolesDefinition.READ_CPR_ROLE);
        } catch (AccessDeniedException e) {
            loggerHelper.info("Access denied: " + e.getMessage());
            throw (e);
        }
    }

    /**
     * Enumerations for indicating the reason for not accepting e-post
     */
    public enum FailState {

        UNDEFINED("Undefined"), MISSING("Missing"), NOTFROMGREENLAND("NotFromGreenland"), DEAD("Dead"), MINOR("Minor"), CEASED("Ceased");
        private final String readableFailString;

        FailState(String readableFailString) {
            this.readableFailString = readableFailString;
        }
    }


    private class FailResult {

        private String id = "";
        private FailState fail = FailState.UNDEFINED;

        public FailResult(String id, FailState fail) {
            this.id = id;
            this.fail = fail;
        }
    }
}
