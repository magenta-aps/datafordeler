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
import dk.magenta.datafordeler.core.fapi.Query;
import dk.magenta.datafordeler.core.plugin.AreaRestrictionDefinition;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.OutputStream;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/eboks/idLookup/1")
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
        this.monitorService.addAccessCheckPoint("/eboks/idLookup/1");

    }

    public static final String PARAM_UPDATED_SINCE = "updatedSince";
    public static final String PARAM_CVR_NUMBER = "cvrNumber";
    public static final String PARAM_RETURN_PARTICIPANT_DETAILS = "returnParticipantDetails";

    private boolean enableDirectLookup = true;

    public boolean isEnableDirectLookup() {
        return this.enableDirectLookup;
    }

    public void setEnableDirectLookup(boolean enableDirectLookup) {
        this.enableDirectLookup = enableDirectLookup;
    }


    private boolean enableGerLookup = true;

    public boolean isEnableGerLookup() {
        return this.enableGerLookup;
    }

    public void setEnableGerLookup(boolean enableGerLookup) {
        this.enableGerLookup = enableGerLookup;
    }

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

        HashSet<String> cvrNumbers = new HashSet<>();
        cvrNumbers.add(cvrNummer);

        Session session = sessionManager.getSessionFactory().openSession();

        try {
            ObjectNode formattedRecord = null;

            if (this.enableDirectLookup) {
                Collection<CompanyRecord> records = this.getCompanies(session, cvrNumbers, user);

            }

            if (this.enableGerLookup && formattedRecord == null) {
                Collection<CompanyEntity> companyEntities = gerCompanyLookup.lookup(session, cvrNumbers);

            }

            if (formattedRecord != null) {
                return objectMapper.writeValueAsString(formattedRecord);
            }
        } finally {
            session.close();
        }
        throw new HttpNotFoundException("No entity with CVR number " + cvrNummer + " was found");
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

    @RequestMapping(method = RequestMethod.POST, path = "/", produces = {MediaType.APPLICATION_JSON_VALUE})
    public StreamingResponseBody getBulk(HttpServletRequest request)
            throws AccessDeniedException, AccessRequiredException, InvalidTokenException, InvalidClientInputException, IOException, HttpNotFoundException, InvalidCertificateException {
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

        final OffsetDateTime updatedSince = requestObject.has(PARAM_UPDATED_SINCE) ? Query.parseDateTime(requestObject.get(PARAM_UPDATED_SINCE).asText()) : null;

        final List<String> cvrNumbers = (requestObject.has(PARAM_CVR_NUMBER)) ? this.getCvrNumber(requestObject.get(PARAM_CVR_NUMBER)) : null;

        boolean returnParticipantDetails = "1".equals(request.getParameter(PARAM_RETURN_PARTICIPANT_DETAILS));

        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.info(
                "Incoming REST request for PrismeCprService with " +
                        PARAM_UPDATED_SINCE + " = " + updatedSince + ", " +
                        PARAM_CVR_NUMBER + " = " + (cvrNumbers != null && cvrNumbers.size() > 10 ? (cvrNumbers.size() + " cpr numbers") : cvrNumbers) + " and " +
                        PARAM_RETURN_PARTICIPANT_DETAILS + " = " + returnParticipantDetails
        );
        this.checkAndLogAccess(loggerHelper, returnParticipantDetails);

        HashSet<String> cvr = new HashSet<>();

        if (cvrNumbers != null) {
            for (String cvrNumber : cvrNumbers) {
                try {
                    cvr.add(Integer.toString(Integer.parseInt(cvrNumber, 10)));
                } catch (NumberFormatException e) {
                }
            }
        }
        if (cvr.isEmpty()) {
            throw new InvalidClientInputException("Please specify at least one CVR number");
        }

        CompanyRecordQuery query = new CompanyRecordQuery();
        query.setCvrNumre(cvrNumbers);
        query.setRecordAfter(updatedSince);
        this.applyAreaRestrictionsToQuery(query, user);

        return new StreamingResponseBody() {
            @Override
            public void writeTo(OutputStream outputStream) throws IOException {
                Session session = sessionManager.getSessionFactory().openSession();
                List<CompanyRecord> records = QueryManager.getAllEntities(session, query, CompanyRecord.class);

                    outputStream.write(END_OBJECT);
                    outputStream.flush();

            }
        };
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


    private <T extends CvrBitemporalRecord> T getLastUpdated(Collection<T> records) {
        ArrayList<T> list = new ArrayList<>();
        for (T record : records) {
            if (record != null) {
                list.add(record);
            }
        }
        if (list.size() > 1) {
            list.sort(
                    Comparator.comparing(
                            CvrBitemporalRecord::getValidFrom, Comparator.nullsFirst(Comparator.naturalOrder())
                    ).thenComparing(
                            CvrBitemporalRecord::getLastUpdated, Comparator.nullsFirst(Comparator.naturalOrder())
                    )
            );
        }
        return list.isEmpty() ? null : list.get(list.size()-1);
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

    private static String emptyIfNull(String text) {
        if (text == null) return "";
        return text;
    }

}
