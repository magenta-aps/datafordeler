package dk.magenta.datafordeler.eboks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonRecordQuery;
import dk.magenta.datafordeler.cvr.CvrPlugin;
import dk.magenta.datafordeler.cvr.DirectLookup;
import dk.magenta.datafordeler.cvr.access.CvrAreaRestrictionDefinition;
import dk.magenta.datafordeler.cvr.access.CvrRolesDefinition;
import dk.magenta.datafordeler.cvr.query.CompanyRecordQuery;
import dk.magenta.datafordeler.cvr.records.*;
import dk.magenta.datafordeler.ger.data.company.CompanyEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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
    private CvrPlugin cvrPlugin;

    @Autowired
    protected MonitorService monitorService;

    @Autowired
    private DirectLookup directLookup;

    private Logger log = LogManager.getLogger(EboksRecieveLookupService.class.getCanonicalName());

    @Autowired
    private GerCompanyLookup gerCompanyLookup;

    @PostConstruct
    public void init() {
        this.monitorService.addAccessCheckPoint("/eboks/recipient");

    }

    @RequestMapping(method = RequestMethod.GET, path = "/{lookup}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String getSingle(@RequestParam(value="cpr") List<String> cprs, @RequestParam(value="cvr") List<String> cvrs, HttpServletRequest request)
            throws DataFordelerException, JsonProcessingException {


        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        this.checkAndLogAccess(loggerHelper);

        PersonRecordQuery personQuery = new PersonRecordQuery();
        personQuery.setPageSize(Integer.MAX_VALUE);


        if (cprs != null) {
            for (String cprNumber : cprs) {
                personQuery.addPersonnummer(cprNumber);
            }
        }
        if (personQuery.getPersonnumre().isEmpty()) {
            throw new InvalidClientInputException("Please specify at least one CPR number");
        }

        OffsetDateTime now = OffsetDateTime.now();
        personQuery.setRegistrationFromBefore(now);
        personQuery.setRegistrationToAfter(now);
        personQuery.setEffectFromBefore(now);
        personQuery.setEffectToAfter(now);


           try(Session session = sessionManager.getSessionFactory().openSession()) {

               personQuery.applyFilters(session);

               Stream<PersonEntity> personEntities = QueryManager.getAllEntitiesAsStream(session, personQuery, PersonEntity.class);
               ArrayNode cprList = objectMapper.createArrayNode();
               personEntities.forEach((k)->{
                   cprList.add(k.getPersonnummer());
                   cprs.remove(k.getPersonnummer());
               });

               ArrayNode cvrList = objectMapper.createArrayNode();


                //First find out if the company exists as a ger company
               if (!cvrs.isEmpty()) {
                   Collection<CompanyEntity> companyEntities = gerCompanyLookup.lookup(session, cvrs);
                   if (!companyEntities.isEmpty()) {
                       Iterator<CompanyEntity> companyEntityIterator = companyEntities.iterator();
                       while(companyEntityIterator.hasNext()) {
                           CompanyEntity companyEntity = companyEntityIterator.next();
                           String gerNo = Integer.toString(companyEntity.getGerNr());
                           cvrList.add(gerNo);
                           cvrs.remove(gerNo);
                       }
                   }
               }

               if (!cvrs.isEmpty()) {
                   CompanyRecordQuery query = new CompanyRecordQuery();
                   query.setCvrNumre(cvrs);
                   Stream<CompanyRecord> companyEntities = QueryManager.getAllEntitiesAsStream(session, query, CompanyRecord.class);

                   companyEntities.forEach((k)->{
                       cvrList.add(k.getCvrNumber());
                       cvrs.remove(k.getCvrNumber());
                   });
               }


               ObjectNode returnValue = objectMapper.createObjectNode();
               ObjectNode validValues = objectMapper.createObjectNode();
               validValues.set("cpr", cprList);
               validValues.set("cvr", cvrList);

               ObjectNode invalidValues = objectMapper.createObjectNode();

               ArrayNode failedCvr = objectMapper.createArrayNode();
               ArrayNode failedCpr = objectMapper.createArrayNode();

               cvrs.stream().forEach((k)->{
                   failedCvr.add(k);
               });

               cprs.stream().forEach((k)->{
                   failedCpr.add(k);
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

    protected Collection<CompanyRecord> getCompanies(Session session, Collection<String> cvrNumbers, DafoUserDetails user) throws DataFordelerException {
        CompanyRecordQuery query = new CompanyRecordQuery();
        query.setCvrNumre(cvrNumbers);
        this.applyAreaRestrictionsToQuery(query, user);
        return QueryManager.getAllEntities(session, query, CompanyRecord.class);
    }

    protected static final byte[] START_OBJECT = "{".getBytes();
    protected static final byte[] END_OBJECT = "}".getBytes();
    protected static final byte[] OBJECT_SEPARATOR = ",\n".getBytes();


    protected void checkAndLogAccess(LoggerHelper loggerHelper) throws AccessDeniedException, AccessRequiredException {
        try {
            loggerHelper.getUser().checkHasSystemRole(CvrRolesDefinition.READ_CVR_ROLE);
            loggerHelper.getUser().checkHasSystemRole(CprRolesDefinition.READ_CPR_ROLE);
        }
        catch (AccessDeniedException e) {
            loggerHelper.info("Access denied: " + e.getMessage());
            throw(e);
        }
    }

    private static Pattern nonDigits = Pattern.compile("[^\\d]");
    protected List<String> getCvrNumber(JsonNode node) {
        ArrayList<String> cvrNumbers = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode item : (ArrayNode) node) {
                cvrNumbers.addAll(this.getCvrNumber(item));
            }
        } else if (node.isTextual()) {
            cvrNumbers.add(nonDigits.matcher(node.asText()).replaceAll(""));
        } else if (node.isNumber()) {
            cvrNumbers.add(String.format("%08d", node.asInt()));
        }
        return cvrNumbers;
    }


    protected void applyAreaRestrictionsToQuery(CompanyRecordQuery query, DafoUserDetails user) throws InvalidClientInputException {
        Collection<AreaRestriction> restrictions = user.getAreaRestrictionsForRole(CvrRolesDefinition.READ_CVR_ROLE);
        AreaRestrictionDefinition areaRestrictionDefinition = this.cvrPlugin.getAreaRestrictionDefinition();
        AreaRestrictionType municipalityType = areaRestrictionDefinition.getAreaRestrictionTypeByName(CvrAreaRestrictionDefinition.RESTRICTIONTYPE_KOMMUNEKODER);
        for (AreaRestriction restriction : restrictions) {
            if (restriction.getType() == municipalityType) {
                query.addKommunekodeRestriction(restriction.getValue());
            }
        }
    }
}
