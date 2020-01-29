package dk.magenta.datafordeler.prisme;

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
import dk.magenta.datafordeler.cvr.CollectiveCvrLookup;
import dk.magenta.datafordeler.cvr.CvrPlugin;
import dk.magenta.datafordeler.cvr.access.CvrAreaRestrictionDefinition;
import dk.magenta.datafordeler.cvr.access.CvrRolesDefinition;
import dk.magenta.datafordeler.cvr.query.CompanyRecordQuery;
import dk.magenta.datafordeler.cvr.records.*;
import dk.magenta.datafordeler.geo.GeoLookupService;
import dk.magenta.datafordeler.ger.data.company.CompanyEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Lookup companies by trying first in GER-register, if the company is not found try looking up in CVR-register amd after that look externally
 */
@RestController
@RequestMapping("/prisme/cvr/3")
public class CvrRecordCombinedService {

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
    private CollectiveCvrLookup collectiveLookup;


    @Autowired
    private CvrOutputWrapperPrisme cvrWrapper;

    private Logger log = LogManager.getLogger(CvrRecordCombinedService.class.getCanonicalName());

    @Autowired
    private GerCompanyLookup gerCompanyLookup;

    @PostConstruct
    public void init() {
        this.monitorService.addAccessCheckPoint("/prisme/cvr/3/1234");
        this.monitorService.addAccessCheckPoint("POST", "/prisme/cvr/3/", "{}");
    }

    public static final String PARAM_CVR_NUMBER = "cvrNumber";
    public static final String PARAM_RETURN_PARTICIPANT_DETAILS = "returnParticipantDetails";


    @RequestMapping(method = RequestMethod.GET, path = "/{cvrNummer}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String getSingle(@PathVariable("cvrNummer") String cvrNummer, HttpServletRequest request)
            throws DataFordelerException, JsonProcessingException {

        boolean returnParticipantDetails = "1".equals(request.getParameter(PARAM_RETURN_PARTICIPANT_DETAILS));

        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.info(
                "Incoming REST request for PrismeCvrService with cvrNummer " + cvrNummer + " and " +
                        PARAM_RETURN_PARTICIPANT_DETAILS + " = " + returnParticipantDetails
        );
        this.checkAndLogAccess(loggerHelper, returnParticipantDetails);

        ArrayList<String> cvrNumbers = new ArrayList<String>();
        cvrNumbers.add(cvrNummer);
        ObjectNode formattedRecord = getJSONFromCvrList(cvrNumbers, returnParticipantDetails, false);

        if (formattedRecord != null && formattedRecord.size()>0) {
            return objectMapper.writeValueAsString(formattedRecord);
        } else {
            throw new HttpNotFoundException("No entity with CVR number " + cvrNummer + " was found");
        }
    }

    /**
     * Get companies which has been loaded from CVR
     * @param session
     * @param cvrNumbers
     * @param user
     * @return
     * @throws DataFordelerException
     */
    protected Collection<CompanyRecord> getCompanies(Session session, Collection<String> cvrNumbers, DafoUserDetails user) throws DataFordelerException {
        CompanyRecordQuery query = new CompanyRecordQuery();
        query.setCvrNumre(cvrNumbers);
        this.applyAreaRestrictionsToQuery(query, user);
        return QueryManager.getAllEntities(session, query, CompanyRecord.class);
    }

    @RequestMapping(method = RequestMethod.POST, path = "/", produces = {MediaType.APPLICATION_JSON_VALUE})
    public StreamingResponseBody getBulk(HttpServletRequest request)
            throws DataFordelerException, IOException {
        JsonNode requestBody;
        try {
            requestBody = objectMapper.readTree(request.getInputStream());
        } catch (IOException e) {
            throw new InvalidClientInputException(e.getMessage());
        }
        if (!requestBody.isObject()) {
            throw new InvalidClientInputException("Input is not a JSON object");
        }

        ObjectNode requestObject = (ObjectNode) requestBody;
        final List<String> cvrNumbers = (requestObject.has(PARAM_CVR_NUMBER)) ? this.getCvrNumber(requestObject.get(PARAM_CVR_NUMBER)) : null;
        boolean returnParticipantDetails = "1".equals(request.getParameter(PARAM_RETURN_PARTICIPANT_DETAILS));
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.info(
                "Incoming REST request for PrismeCvrService with cvrNummer " + cvrNumbers + " and " +
                        PARAM_RETURN_PARTICIPANT_DETAILS + " = " + returnParticipantDetails
        );
        this.checkAndLogAccess(loggerHelper, returnParticipantDetails);

        ObjectNode formattedRecord = getJSONFromCvrList(cvrNumbers, returnParticipantDetails, true);

        return new StreamingResponseBody() {
            @Override
            public void writeTo(OutputStream outputStream) throws IOException {
                outputStream.write(objectMapper.writeValueAsString(formattedRecord).getBytes());
            }
        };
    }

    private ObjectNode getJSONFromCvrList(List<String> cvrNumbers, boolean returnParticipantDetails, boolean asList) throws DataFordelerException, JsonProcessingException {
        Session session = sessionManager.getSessionFactory().openSession();
        GeoLookupService service = new GeoLookupService(sessionManager);
        try {

            ObjectNode formattedRecord = objectMapper.createObjectNode();

            if (cvrNumbers!=null && !cvrNumbers.isEmpty()) {
                Collection<CompanyRecord> companyEntities = collectiveLookup.getCompanies(session, cvrNumbers);
                if (!companyEntities.isEmpty()) {
                    Iterator<CompanyRecord> companyEntityIterator = companyEntities.iterator();
                    while(companyEntityIterator.hasNext()) {
                        CompanyRecord companyRecord = companyEntityIterator.next();
                        String cvrNumber = Integer.toString(companyRecord.getCvrNumber());
                        if(asList) {
                            formattedRecord.set(cvrNumber, cvrWrapper.wrapRecord(companyRecord, service, returnParticipantDetails));
                        } else {
                            formattedRecord = cvrWrapper.wrapRecord(companyRecord, service, returnParticipantDetails);
                        }
                        cvrNumbers.remove(cvrNumber);
                    }
                }
            }

            if (cvrNumbers!=null && !cvrNumbers.isEmpty()) {
                Collection<CompanyEntity> companyEntities = gerCompanyLookup.lookup(session, cvrNumbers);
                if (!companyEntities.isEmpty()) {
                    Iterator<CompanyEntity> companyEntityIterator = companyEntities.iterator();
                    while(companyEntityIterator.hasNext()) {
                        CompanyEntity companyEntity = companyEntityIterator.next();
                        String gerNo = Integer.toString(companyEntity.getGerNr());
                        if(asList) {
                            formattedRecord.set(Integer.toString(companyEntity.getGerNr()), cvrWrapper.wrapGerCompany(companyEntity, service, returnParticipantDetails));
                        } else {
                            formattedRecord = cvrWrapper.wrapGerCompany(companyEntity, service, returnParticipantDetails);
                        }

                    }
                }
            }

            return formattedRecord;
        } finally {
            session.close();
        }
    }


    protected void checkAndLogAccess(LoggerHelper loggerHelper, boolean includeCpr) throws AccessDeniedException, AccessRequiredException {
        try {
            loggerHelper.getUser().checkHasSystemRole(CvrRolesDefinition.READ_CVR_ROLE);
            if (includeCpr) {
                loggerHelper.getUser().checkHasSystemRole(CprRolesDefinition.READ_CPR_ROLE);
            }
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
