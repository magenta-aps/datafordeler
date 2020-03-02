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
import dk.magenta.datafordeler.core.fapi.Query;
import dk.magenta.datafordeler.core.plugin.AreaRestrictionDefinition;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import dk.magenta.datafordeler.cvr.CvrAreaRestrictionDefinition;
import dk.magenta.datafordeler.cvr.CvrPlugin;
import dk.magenta.datafordeler.cvr.CvrRolesDefinition;
import dk.magenta.datafordeler.cvr.data.company.CompanyEntity;
import dk.magenta.datafordeler.cvr.data.company.CompanyQuery;
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
import java.nio.charset.Charset;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@RestController
@RequestMapping("/prisme/cvr/0")
public class CvrService {

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

    private Logger log = LogManager.getLogger(CvrService.class.getCanonicalName());

    private CompanyOutputWrapperPrisme companyOutputWrapper = new CompanyOutputWrapperPrisme();

    @PostConstruct
    public void init() {
        this.monitorService.addAccessCheckPoint("/prisme/cvr/0/1234");
        this.monitorService.addAccessCheckPoint("POST", "/prisme/cvr/0/", "{}");
    }

    @RequestMapping(method = RequestMethod.GET, path="/{cvrNummer}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String getSingle(@PathVariable("cvrNummer") String cvrNummer, HttpServletRequest request)
            throws AccessDeniedException, AccessRequiredException, InvalidTokenException, InvalidClientInputException, JsonProcessingException, HttpNotFoundException {

        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.info(
                "Incoming REST request for PrismeCvrService with cvrNummer " + cvrNummer
        );
        this.checkAndLogAccess(loggerHelper);

        final Session session = sessionManager.getSessionFactory().openSession();
        try {
            LookupService lookupService = new LookupService(session);
            companyOutputWrapper.setLookupService(lookupService);

            CompanyQuery companyQuery = new CompanyQuery();
            companyQuery.setCvrNumre(cvrNummer);

            companyQuery.applyFilters(session);
            this.applyAreaRestrictionsToQuery(companyQuery, user);

            List<CompanyEntity> companyEntities = QueryManager.getAllEntities(session, companyQuery, CompanyEntity.class);

            if (!companyEntities.isEmpty()) {
                CompanyEntity companyEntity = companyEntities.get(0);
                return objectMapper.writeValueAsString(companyOutputWrapper.wrapResult(companyEntity, companyQuery));
            }
            throw new HttpNotFoundException("No entity with CVR number "+cvrNummer+" was found");
        } finally {
            session.close();
        }
    }


    private static final String PARAM_UPDATED_SINCE = "updatedSince";
    private static final String PARAM_CVR_NUMBER = "cvrNumber";
    private static final byte[] START_OBJECT = "{".getBytes();
    private static final byte[] END_OBJECT = "}".getBytes();
    private static final byte[] OBJECT_SEPARATOR = ",\n".getBytes();

    @RequestMapping(method = RequestMethod.POST, path = "/", produces = {MediaType.APPLICATION_JSON_VALUE})
    public StreamingResponseBody getBulk(HttpServletRequest request)
            throws AccessDeniedException, AccessRequiredException, InvalidTokenException, InvalidClientInputException, IOException, HttpNotFoundException {
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

        final List<String> cprNumbers = (requestObject.has(PARAM_CVR_NUMBER)) ? this.getCvrNumber(requestObject.get(PARAM_CVR_NUMBER)) : null;


        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.info(
                "Incoming REST request for PrismeCprService with " +
                        PARAM_UPDATED_SINCE + " = " + updatedSince + " and " +
                        PARAM_CVR_NUMBER + " = " + (cprNumbers != null && cprNumbers.size() > 10 ? (cprNumbers.size() + " cpr numbers") : cprNumbers)
        );
        this.checkAndLogAccess(loggerHelper);

        CompanyQuery companyQuery = new CompanyQuery();

        companyQuery.setRecordAfter(updatedSince);

        if (cprNumbers != null) {
            for (String cprNumber : cprNumbers) {
                companyQuery.addCvrNummer(cprNumber);
            }
        }
        if (companyQuery.getCvrNumre().isEmpty()) {
            throw new InvalidClientInputException("Please specify at least one CVR number");
        }

        return new StreamingResponseBody() {

            @Override
            public void writeTo(OutputStream outputStream) throws IOException {

                final Session lookupSession = sessionManager.getSessionFactory().openSession();
                LookupService lookupService = new LookupService(lookupSession);
                companyOutputWrapper.setLookupService(lookupService);


                final Session entitySession = sessionManager.getSessionFactory().openSession();
                try {
                    companyQuery.applyFilters(entitySession);
                    CvrService.this.applyAreaRestrictionsToQuery(companyQuery, user);

                    Stream<CompanyEntity> personEntities = QueryManager.getAllEntitiesAsStream(entitySession, companyQuery, CompanyEntity.class);
                    outputStream.write(START_OBJECT);
                    personEntities.forEach(new Consumer<CompanyEntity>() {
                        boolean first = true;

                        @Override
                        public void accept(CompanyEntity companyEntity) {
                            try {
                                if (!first) {
                                    outputStream.flush();
                                    outputStream.write(OBJECT_SEPARATOR);
                                } else {
                                    first = false;
                                }
                                outputStream.write(("\"" + companyEntity.getCvrNumber() + "\":").getBytes());
                                outputStream.write(
                                        objectMapper.writeValueAsString(
                                                companyOutputWrapper.wrapResult(companyEntity, companyQuery)
                                        ).getBytes(Charset.forName("UTF-8"))
                                );
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            entitySession.evict(companyEntity);
                        }
                    });
                    outputStream.write(END_OBJECT);
                    outputStream.flush();
                } catch (InvalidClientInputException e) {
                    e.printStackTrace();
                } finally {
                    entitySession.close();
                    lookupSession.close();
                }
            }
        };
    }

    protected void checkAndLogAccess(LoggerHelper loggerHelper) throws AccessDeniedException, AccessRequiredException {
        try {
            loggerHelper.getUser().checkHasSystemRole(CvrRolesDefinition.READ_CVR_ROLE);
        }
        catch (AccessDeniedException e) {
            loggerHelper.info("Access denied: " + e.getMessage());
            throw(e);
        }
    }

    protected void applyAreaRestrictionsToQuery(CompanyQuery query, DafoUserDetails user) throws InvalidClientInputException {
        Collection<AreaRestriction> restrictions = user.getAreaRestrictionsForRole(CvrRolesDefinition.READ_CVR_ROLE);
        AreaRestrictionDefinition areaRestrictionDefinition = this.cvrPlugin.getAreaRestrictionDefinition();
        AreaRestrictionType municipalityType = areaRestrictionDefinition.getAreaRestrictionTypeByName(CvrAreaRestrictionDefinition.RESTRICTIONTYPE_KOMMUNEKODER);
        for (AreaRestriction restriction : restrictions) {
            if (restriction.getType() == municipalityType) {
                query.addKommuneKode(restriction.getValue());
            }
        }
    }

    private static Pattern nonDigits = Pattern.compile("[^\\d]");
    private List<String> getCvrNumber(JsonNode node) {
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


}
