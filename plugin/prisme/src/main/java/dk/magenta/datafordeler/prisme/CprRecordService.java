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
import dk.magenta.datafordeler.cpr.CprAreaRestrictionDefinition;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonRecordQuery;
import dk.magenta.datafordeler.geo.GeoLookupService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/prisme/cpr/2")
public class CprRecordService {

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

    private Logger log = LogManager.getLogger(CprRecordService.class.getCanonicalName());

    @Autowired
    private PersonOutputWrapperPrisme personOutputWrapper;

    @PostConstruct
    public void init() {
        this.monitorService.addAccessCheckPoint("/prisme/cpr/2/1234");
        this.monitorService.addAccessCheckPoint("POST", "/prisme/cpr/2/", "{}");
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{cprNummer}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String getSingle(@PathVariable("cprNummer") String cprNummer, HttpServletRequest request)
            throws AccessDeniedException, AccessRequiredException, InvalidTokenException, InvalidClientInputException, JsonProcessingException, HttpNotFoundException, InvalidCertificateException {

        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.info(
                "Incoming REST request for PrismeCprService with cprNummer " + cprNummer
        );
        this.checkAndLogAccess(loggerHelper);
        loggerHelper.urlInvokePersistablelogs("CprRecordService");

        final Session session = sessionManager.getSessionFactory().openSession();
        try {
            GeoLookupService lookupService = new GeoLookupService(sessionManager);
            personOutputWrapper.setLookupService(lookupService);

            PersonRecordQuery personQuery = new PersonRecordQuery();
            personQuery.setPersonnummer(cprNummer);

            OffsetDateTime now = OffsetDateTime.now();
            personQuery.setRegistrationFromBefore(now);
            personQuery.setRegistrationToAfter(now);
            personQuery.setEffectFromBefore(now);
            personQuery.setEffectToAfter(now);

            personQuery.applyFilters(session);
            this.applyAreaRestrictionsToQuery(personQuery, user);

            List<PersonEntity> personEntities = QueryManager.getAllEntities(session, personQuery, PersonEntity.class);

            if (!personEntities.isEmpty()) {
                PersonEntity personEntity = personEntities.get(0);
                loggerHelper.urlResponsePersistablelogs(HttpStatus.OK.value(), "CprRecordService done");
                return objectMapper.writeValueAsString(personOutputWrapper.wrapRecordResult(personEntity, personQuery));
            }
            loggerHelper.urlResponsePersistablelogs(HttpStatus.NOT_FOUND.value(), "CprRecordService done");
            throw new HttpNotFoundException("No entity with CPR number " + cprNummer + " was found");
        } finally {
            session.close();
        }
    }

    private static final String PARAM_UPDATED_SINCE = "updatedSince";
    private static final String PARAM_CPR_NUMBER = "cprNumber";
    private static final byte[] START_OBJECT = "{".getBytes();
    private static final byte[] END_OBJECT = "}".getBytes();
    private static final byte[] OBJECT_SEPARATOR = ",\n".getBytes();

    @RequestMapping(method = RequestMethod.POST, path = "/", produces = {MediaType.APPLICATION_JSON_VALUE})
    public StreamingResponseBody getBulk(HttpServletRequest request)
            throws AccessDeniedException, AccessRequiredException, InvalidTokenException, InvalidClientInputException, QueryBuildException, IOException, HttpNotFoundException, InvalidCertificateException {
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

        final OffsetDateTime updatedSince = requestObject.has(PARAM_UPDATED_SINCE) ? Query.parseDateTime(requestObject.get(PARAM_UPDATED_SINCE).asText(), false) : null;

        final List<String> cprNumbers = (requestObject.has(PARAM_CPR_NUMBER)) ? this.getCprNumber(requestObject.get(PARAM_CPR_NUMBER)) : null;

        if(cprNumbers.size()>100) {
            throw new QueryBuildException("Maximum 100 numbers is allowed");
        }

        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.info(
                "Incoming REST request for PrismeCprService with " +
                        PARAM_UPDATED_SINCE + " = " + updatedSince + " and " +
                        PARAM_CPR_NUMBER + " = " + (cprNumbers != null && cprNumbers.size() > 10 ? (cprNumbers.size() + " cpr numbers") : cprNumbers)
        );
        this.checkAndLogAccess(loggerHelper);
        loggerHelper.urlInvokePersistablelogs("CprRecordService");

        PersonRecordQuery personQuery = new PersonRecordQuery();
        personQuery.setPageSize(Integer.MAX_VALUE);

        personQuery.setRecordAfter(updatedSince);

        if (cprNumbers != null) {
            for (String cprNumber : cprNumbers) {
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

        return outputStream -> {

            final Session lookupSession = sessionManager.getSessionFactory().openSession();
            GeoLookupService lookupService = new GeoLookupService(sessionManager);
            personOutputWrapper.setLookupService(lookupService);

            final Session entitySession = sessionManager.getSessionFactory().openSession();
            try {

                personQuery.applyFilters(entitySession);
                CprRecordService.this.applyAreaRestrictionsToQuery(personQuery, user);

                Stream<PersonEntity> personEntities = QueryManager.getAllEntitiesAsStream(entitySession, personQuery, PersonEntity.class);
                outputStream.write(START_OBJECT);
                personEntities.forEach(new Consumer<PersonEntity>() {
                    boolean first = true;

                    @Override
                    public void accept(PersonEntity personEntity) {
                        try {
                            if (!first) {
                                outputStream.flush();
                                outputStream.write(OBJECT_SEPARATOR);
                            } else {
                                first = false;
                            }
                            outputStream.write(("\"" + personEntity.getPersonnummer() + "\":").getBytes());
                            outputStream.write(
                                    objectMapper.writeValueAsString(
                                            personOutputWrapper.wrapRecordResult(personEntity, personQuery)
                                    ).getBytes(Charset.forName("UTF-8"))
                            );
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        entitySession.evict(personEntity);
                    }
                });
                outputStream.write(END_OBJECT);
                outputStream.flush();
            } catch (InvalidClientInputException e) {
                e.printStackTrace();
            } finally {
                loggerHelper.urlResponsePersistablelogs("CprRecordService done");
                entitySession.close();
                lookupSession.close();
            }
        };
    }


    protected void checkAndLogAccess(LoggerHelper loggerHelper) throws AccessDeniedException, AccessRequiredException {
        try {
            loggerHelper.getUser().checkHasSystemRole(CprRolesDefinition.READ_CPR_ROLE);
        }
        catch (AccessDeniedException e) {
            loggerHelper.info("Access denied: " + e.getMessage());
            throw(e);
        }
    }

    protected void applyAreaRestrictionsToQuery(PersonRecordQuery query, DafoUserDetails user) throws InvalidClientInputException {
        Collection<AreaRestriction> restrictions = user.getAreaRestrictionsForRole(CprRolesDefinition.READ_CPR_ROLE);
        AreaRestrictionDefinition areaRestrictionDefinition = this.cprPlugin.getAreaRestrictionDefinition();
        AreaRestrictionType municipalityType = areaRestrictionDefinition.getAreaRestrictionTypeByName(CprAreaRestrictionDefinition.RESTRICTIONTYPE_KOMMUNEKODER);
        for (AreaRestriction restriction : restrictions) {
            if (restriction.getType() == municipalityType) {
                query.addKommunekode(restriction.getValue());
            }
        }
    }

    private static Pattern nonDigits = Pattern.compile("[^\\d]");
    private List<String> getCprNumber(JsonNode node) {
        ArrayList<String> cprNumbers = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode item : (ArrayNode) node) {
                cprNumbers.addAll(this.getCprNumber(item));
            }
        } else if (node.isTextual()) {
            cprNumbers.add(nonDigits.matcher(node.asText()).replaceAll(""));
        } else if (node.isNumber()) {
            cprNumbers.add(String.format("%010d", node.asInt()));
        }
        return cprNumbers;
    }


}
