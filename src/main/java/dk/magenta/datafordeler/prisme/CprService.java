package dk.magenta.datafordeler.prisme;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.arearestriction.AreaRestriction;
import dk.magenta.datafordeler.core.arearestriction.AreaRestrictionType;
import dk.magenta.datafordeler.core.database.*;
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
import dk.magenta.datafordeler.cpr.data.person.PersonQuery;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

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
@RequestMapping("/prisme/cpr/1")
public class CprService {

    @Autowired
    SessionManager sessionManager;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private DafoUserManager dafoUserManager;

    @Autowired
    private CprPlugin cprPlugin;

    private Logger log = LoggerFactory.getLogger(CprService.class);

    private PersonOutputWrapperPrisme personOutputWrapper = new PersonOutputWrapperPrisme();

    @RequestMapping(method = RequestMethod.GET, path = "/{cprNummer}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String getSingle(@PathVariable("cprNummer") String cprNummer, HttpServletRequest request)
            throws AccessDeniedException, AccessRequiredException, InvalidTokenException, InvalidClientInputException, JsonProcessingException, HttpNotFoundException {

        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.info(
                "Incoming REST request for PrismeCprService with cprNummer " + cprNummer
        );
        this.checkAndLogAccess(loggerHelper);

        final Session session = sessionManager.getSessionFactory().openSession();
        try {
            LookupService lookupService = new LookupService(session);
            personOutputWrapper.setLookupService(lookupService);

            PersonQuery personQuery = new PersonQuery();
            personQuery.setPersonnummer(cprNummer);

            OffsetDateTime now = OffsetDateTime.now();
            personQuery.setRegistrationFrom(now);
            personQuery.setRegistrationTo(now);
            personQuery.setEffectFrom(now);
            personQuery.setEffectTo(now);

            personQuery.applyFilters(session);
            this.applyAreaRestrictionsToQuery(personQuery, user);

            List<PersonEntity> personEntities = QueryManager.getAllEntities(session, personQuery, PersonEntity.class);

            if (!personEntities.isEmpty()) {
                PersonEntity personEntity = personEntities.get(0);
                return objectMapper.writeValueAsString(personOutputWrapper.wrapResult(personEntity));
            }
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

        final List<String> cprNumbers = (requestObject.has(PARAM_CPR_NUMBER)) ? this.getCprNumber(requestObject.get(PARAM_CPR_NUMBER)) : null;


        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.info(
                "Incoming REST request for PrismeCprService with " +
                        PARAM_UPDATED_SINCE + " = " + updatedSince + " and " +
                        PARAM_CPR_NUMBER + " = " + (cprNumbers != null && cprNumbers.size() > 10 ? (cprNumbers.size() + " cpr numbers") : cprNumbers)
        );
        this.checkAndLogAccess(loggerHelper);

        return new StreamingResponseBody() {

            @Override
            public void writeTo(OutputStream outputStream) throws IOException {

                final Session lookupSession = sessionManager.getSessionFactory().openSession();
                LookupService lookupService = new LookupService(lookupSession);
                personOutputWrapper.setLookupService(lookupService);

                final Session entitySession = sessionManager.getSessionFactory().openSession();
                try {

                    PersonQuery personQuery = new PersonQuery();
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
                    personQuery.setRegistrationFrom(now);
                    personQuery.setRegistrationTo(now);
                    personQuery.setEffectFrom(now);
                    personQuery.setEffectTo(now);

                    personQuery.applyFilters(entitySession);
                    CprService.this.applyAreaRestrictionsToQuery(personQuery, user);

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
                                                personOutputWrapper.wrapResult(personEntity)
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
                    entitySession.close();
                    lookupSession.close();
                }
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

    protected void applyAreaRestrictionsToQuery(PersonQuery query, DafoUserDetails user) throws InvalidClientInputException {
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
