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
import dk.magenta.datafordeler.core.util.FinalWrapper;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import dk.magenta.datafordeler.cpr.CprAreaRestrictionDefinition;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonEntityManager;
import dk.magenta.datafordeler.cpr.data.person.PersonRecordQuery;
import dk.magenta.datafordeler.cpr.direct.CprDirectLookup;
import dk.magenta.datafordeler.geo.GeoLookupService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static dk.magenta.datafordeler.prisme.CprRecordCombinedService.PersonAttributeQuality.*;

@RestController
@RequestMapping("/prisme/cpr/combined/1")
public class CprRecordCombinedService {

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

    private Logger log = LogManager.getLogger(CprRecordCombinedService.class.getCanonicalName());

    @Autowired
    private PersonOutputWrapperPrisme personOutputWrapper;

    @Autowired
    private CprDirectLookup cprDirectLookup;

    @Autowired
    private PersonEntityManager entityManager;

    @PostConstruct
    public void init() {
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{cprNummer}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String getSingle(@PathVariable("cprNummer") String cprNummer, @RequestParam(value="forceDirect",required=false) String forceDirect, HttpServletRequest request)
            throws AccessDeniedException, AccessRequiredException, InvalidTokenException, InvalidClientInputException, JsonProcessingException, HttpNotFoundException, InvalidCertificateException {

        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.info(
                "Incoming REST request for PrismeCprService with cprNummer " + cprNummer
        );
        this.checkAndLogAccess(loggerHelper);
        loggerHelper.urlInvokePersistablelogs("CprRecordCombinedService");

        try (final Session session = sessionManager.getSessionFactory().openSession()) {
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
            if ("true".equals(forceDirect)) {
                PersonEntity personEntity = cprDirectLookup.getPerson(cprNummer);
                Object obj = personOutputWrapper.wrapRecordResult(personEntity, personQuery);
                return streamPersonOut(user, obj);
            }

            List<PersonEntity> personEntities = QueryManager.getAllEntities(session, personQuery, PersonEntity.class);
            if (personEntities.isEmpty()) {
                PersonEntity personEntity = cprDirectLookup.getPerson(cprNummer);
                if(personEntity==null) {
                    throw new HttpNotFoundException("No entity with CPR number " + cprNummer + " was found");
                }
                entityManager.createSubscription(Collections.singleton(cprNummer));
                Object obj = personOutputWrapper.wrapRecordResult(personEntity, personQuery);
                return streamPersonOut(user, obj);
            }
            PersonEntity personEntity = personEntities.get(0);//There is always one here

            PersonAttributeQuality personAttrQuality = this.acceptPersonEntity(personEntity);
            Object obj = personOutputWrapper.wrapRecordResult(personEntity, personQuery);
            loggerHelper.urlInvokePersistablelogs("CprRecordCombinedService done");
            switch (personAttrQuality) {
                case PersonInformationIsOk:
                    return objectMapper.writeValueAsString(obj);
                case needDirectLookup:
                    personEntity = cprDirectLookup.getPerson(cprNummer);
                    obj = personOutputWrapper.wrapRecordResult(personEntity, personQuery);
                    return streamPersonOut(user, obj);
                case needSubscribtionAndDirectLookup:
                    personEntity = cprDirectLookup.getPerson(cprNummer);
                    if(personEntity==null) {
                        throw new HttpNotFoundException("No entity with CPR number " + cprNummer + " was found");
                    }
                    entityManager.createSubscription(Collections.singleton(cprNummer));
                    obj = personOutputWrapper.wrapRecordResult(personEntity, personQuery);
                    return streamPersonOut(user, obj);
                default:
                    throw new HttpNotFoundException("No entity with CPR number " + cprNummer + " was found");
            }
        } catch(DataStreamException e) {
            log.error(e);
            throw new HttpNotFoundException("No entity with CPR number " + cprNummer + " was found");
        }
    }

    private String streamPersonOut(DafoUserDetails user, Object obj) throws HttpNotFoundException, JsonProcessingException {
        if (!hasAreaRestrictions(user)) {
            return objectMapper.writeValueAsString(obj);
        } else {
            throw new HttpNotFoundException("No entity with was found");
        }
    }

    private static final String PARAM_UPDATED_SINCE = "updatedSince";
    private static final String PARAM_CPR_NUMBER = "cprNumber";
    private static final byte[] START_OBJECT = "{".getBytes();
    private static final byte[] END_OBJECT = "}".getBytes();
    private static final byte[] OBJECT_SEPARATOR = ",\n".getBytes();

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

        final OffsetDateTime updatedSince = requestObject.has(PARAM_UPDATED_SINCE) ? Query.parseDateTime(requestObject.get(PARAM_UPDATED_SINCE).asText(), false) : null;

        final HashSet<String> cprNumbers = (requestObject.has(PARAM_CPR_NUMBER)) ? new HashSet<>(this.getCprNumber(requestObject.get(PARAM_CPR_NUMBER))) : null;


        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.info(
                "Incoming REST request for PrismeCprService with " +
                        PARAM_UPDATED_SINCE + " = " + updatedSince + " and " +
                        PARAM_CPR_NUMBER + " = " + cprNumbers
        );
        this.checkAndLogAccess(loggerHelper);

        PersonRecordQuery personQuery = new PersonRecordQuery();
        personQuery.setPageSize(Integer.MAX_VALUE);

        personQuery.setRecordAfter(updatedSince);

        if (cprNumbers == null || cprNumbers.isEmpty()) {
            throw new InvalidClientInputException("Please specify at least one CPR number");
        }
        for (String cprNumber : cprNumbers) {
            personQuery.addPersonnummer(cprNumber);
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
                CprRecordCombinedService.this.applyAreaRestrictionsToQuery(personQuery, user);

                Stream<PersonEntity> personEntities = QueryManager.getAllEntitiesAsStream(entitySession, personQuery, PersonEntity.class);

                final FinalWrapper<Boolean> first = new FinalWrapper<>(true);
                Consumer<PersonEntity> entityWriter = personEntity -> {
                    if(personEntity!=null && personEntity.getPersonnummer()!=null) {
                        try {
                            cprNumbers.remove(personEntity.getPersonnummer());
                            if (!first.getInner()) {
                                outputStream.flush();
                                outputStream.write(OBJECT_SEPARATOR);
                            } else {
                                first.setInner(false);
                            }

                            outputStream.write(("\"" + personEntity.getPersonnummer() + "\":").getBytes());
                            outputStream.write(
                                    objectMapper.writeValueAsString(
                                            personOutputWrapper.wrapRecordResult(personEntity, personQuery)
                                    ).getBytes(StandardCharsets.UTF_8)
                            );
                        } catch (IOException e) {
                            log.error("IOException", e.getStackTrace());
                        }
                        entitySession.evict(personEntity);
                    }
                };

                outputStream.write(START_OBJECT);
                personEntities.forEach(entityWriter);

                HashSet<String> found = new HashSet<>();
                if (!cprNumbers.isEmpty() && !hasAreaRestrictions(user)) {
                    List<String> remaining = new ArrayList<>(cprNumbers);
                    remaining.stream().map(pnr -> {
                        try {
                            PersonEntity personEntity = cprDirectLookup.getPerson(pnr);
                            if (personEntity != null) {
                                found.add(pnr);
                                return personEntity;
                            }
                        } catch (DataStreamException e) {
                            log.warn(e);
                        }
                        return null;
                    }).forEach(entityWriter);
                }

                outputStream.write(END_OBJECT);
                outputStream.flush();

                entityManager.createSubscription(found);

            } catch (InvalidClientInputException e) {
                e.printStackTrace();
            } finally {
                entitySession.close();
                lookupSession.close();
            }
        };
    }


    protected void checkAndLogAccess(LoggerHelper loggerHelper) throws AccessDeniedException {
        try {
            loggerHelper.getUser().checkHasSystemRole(CprRolesDefinition.READ_CPR_ROLE);
        } catch (AccessDeniedException e) {
            loggerHelper.info("Access denied: " + e.getMessage());
            throw (e);
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

    private static boolean hasAreaRestrictions(DafoUserDetails user) {
        return !user.getAreaRestrictionsForRole(CprRolesDefinition.READ_CPR_ROLE).isEmpty();
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

    // Some people have very little data on them, and we're better off looking them up directly
    private PersonAttributeQuality acceptPersonEntity(PersonEntity entity) {

        if (entity.getStatus() == null || entity.getStatus().isEmpty()) {
            //If the person has no status attached they need subscribtion, and direct lookup
            return needSubscribtionAndDirectLookup;
        } else if (entity.getAddress().isEmpty()) {

            if(entity.getStatus().iterator().next().getStatus() == 90) {
                //If the person is not dead, but has no address we need a subscribtion
                return needDirectLookup;
            } else {
                //If the person is not dead, but has no address we need a subscribtion
                return needSubscribtionAndDirectLookup;
            }
        } else {
            return PersonInformationIsOk;
        }
    }

    public enum PersonAttributeQuality {
        needSubscribtionAndDirectLookup,
        PersonInformationIsOk,
        needDirectLookup
    }
}
