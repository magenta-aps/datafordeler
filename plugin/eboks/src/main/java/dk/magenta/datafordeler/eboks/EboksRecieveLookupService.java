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
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonRecordQuery;
import dk.magenta.datafordeler.cpr.records.person.data.BirthTimeDataRecord;
import dk.magenta.datafordeler.cvr.access.CvrRolesDefinition;
import dk.magenta.datafordeler.cvr.query.CompanyRecordQuery;
import dk.magenta.datafordeler.cvr.records.AddressRecord;
import dk.magenta.datafordeler.cvr.records.CompanyRecord;
import dk.magenta.datafordeler.eboks.utils.FilterUtilities;
import dk.magenta.datafordeler.ger.data.company.CompanyEntity;
import dk.magenta.datafordeler.ger.data.company.CompanyQuery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

/**
 * Webservice for finding out if a cpr- or cvr- number is allowed to recieve e-post
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

    private Logger log = LogManager.getLogger(EboksRecieveLookupService.class.getCanonicalName());


    @PostConstruct
    public void init() {
        this.monitorService.addAccessCheckPoint("/eboks/recipient/lookup?cpr=1111&cvr=1111");

    }

    @RequestMapping(method = RequestMethod.GET, path = "/{lookup}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String getSingle(@RequestParam(value = "cpr",required=false, defaultValue = "") List<String> cprs, @RequestParam(value = "cvr",required=false, defaultValue = "") List<String> cvrs, HttpServletRequest request)
            throws DataFordelerException, JsonProcessingException {

        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        this.checkAndLogAccess(loggerHelper);

        PersonRecordQuery personQuery = new PersonRecordQuery();
        personQuery.setPageSize(Integer.MAX_VALUE);

        try (Session session = sessionManager.getSessionFactory().openSession()) {

            ArrayList<FailResult> failedCprs = new ArrayList<FailResult>();
            ArrayList<FailResult> failedCvrs = new ArrayList<FailResult>();
            ArrayNode validCprList = objectMapper.createArrayNode();

            if (cprs != null && !cprs.isEmpty()) {
                for (String cprNumber : cprs) {
                    personQuery.addPersonnummer(cprNumber);
                }

                OffsetDateTime now = OffsetDateTime.now();
                personQuery.setRegistrationFromBefore(now);
                personQuery.setRegistrationToAfter(now);
                personQuery.setEffectFromBefore(now);
                personQuery.setEffectToAfter(now);

                personQuery.applyFilters(session);
                Stream<PersonEntity> personEntities = QueryManager.getAllEntitiesAsStream(session, personQuery, PersonEntity.class);

                personEntities.forEach((k) -> {
                    BirthTimeDataRecord birthtime = FilterUtilities.findNewestUnclosedCpr(k.getBirthTime());
                    LocalDateTime fifteenYearsAgo = LocalDateTime.now().minusYears(15);
                    if (fifteenYearsAgo.isBefore(birthtime.getBirthDatetime())) {
                        failedCprs.add(new FailResult(k.getPersonnummer(), FailStrate.MINOR));
                    } else if (FilterUtilities.findNewestUnclosedCpr(k.getStatus()).getStatus() == 90) {
                        failedCprs.add(new FailResult(k.getPersonnummer(), FailStrate.DEAD));
                    } else if (k.getAddress().size()==0 || FilterUtilities.findNewestUnclosedCpr(k.getAddress()).getMunicipalityCode() < 950) {
                        failedCprs.add(new FailResult(k.getPersonnummer(), FailStrate.NOTFROMGREENLAND));
                    } else {
                        validCprList.add(k.getPersonnummer());
                    }
                    cprs.remove(k.getPersonnummer());
                });
            }


            ArrayNode cvrList = objectMapper.createArrayNode();


            //First find out if the company exists as a ger company
            if (cvrs != null &&!cvrs.isEmpty()) {
                Collection<CompanyEntity> companyEntities = gerCompanyLookup(session, cvrs);
                if (!companyEntities.isEmpty()) {
                    companyEntities.forEach((k) -> {
                        String gerNo = Integer.toString(k.getGerNr());
                        if (k.getMunicipalityCode() >= 950) {
                            cvrList.add(gerNo);
                        } else {
                            failedCvrs.add(new FailResult(gerNo, FailStrate.NOTFROMGREENLAND));
                        }
                        cvrs.remove(gerNo);
                    });
                }
            }

            if (!cvrs.isEmpty()) {
                CompanyRecordQuery query = new CompanyRecordQuery();
                query.setCvrNumre(cvrs);
                Stream<CompanyRecord> companyEntities = QueryManager.getAllEntitiesAsStream(session, query, CompanyRecord.class);

                companyEntities.forEach((k) -> {
                    String cvrNumber = Integer.toString(k.getCvrNumber());

                    AddressRecord adress = FilterUtilities.findNewestUnclosedCvr(k.getLocationAddress());
                    if(adress==null) {
                        adress = FilterUtilities.findNewestUnclosedCvr(k.getPostalAddress());
                    }
                    if (adress.getMunicipality().getMunicipalityCode() >= 950) {
                        cvrList.add(cvrNumber);
                    } else {
                        failedCvrs.add(new FailResult(cvrNumber, FailStrate.NOTFROMGREENLAND));
                    }
                    cvrs.remove(cvrNumber);
                });
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
                node.put("reason", FailStrate.MISSING.readableFailString);
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
                node.put("reason", FailStrate.MISSING.readableFailString);
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


    public HashSet<CompanyEntity> gerCompanyLookup(Session session, Collection<String> cvrNumbers) {
        CompanyQuery query = new CompanyQuery();
        for (String cvrNumber : cvrNumbers) {
            query.addGerNr(cvrNumber);
        }
        List<CompanyEntity> companyEntities = QueryManager.getAllEntities(session, query, CompanyEntity.class);
        return new HashSet<>(companyEntities);
    }

    protected void checkAndLogAccess(LoggerHelper loggerHelper) throws AccessDeniedException, AccessRequiredException {
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
    public enum FailStrate {

        UNDEFINED("Undefined"), MISSING("Missing"), NOTFROMGREENLAND("NotFromGreenland"), DEAD("Dead"), MINOR("Minor");
        private String readableFailString;

        FailStrate(String readableFailString) {
            this.readableFailString = readableFailString;
        }
    }


    private class FailResult {

        private String id = "";
        private FailStrate fail = FailStrate.UNDEFINED;

        public FailResult(String id, FailStrate fail) {
            this.id = id;
            this.fail = fail;
        }
    }
}
