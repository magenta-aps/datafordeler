package dk.magenta.datafordeler.combined;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.MonitorService;
import dk.magenta.datafordeler.core.arearestriction.AreaRestriction;
import dk.magenta.datafordeler.core.arearestriction.AreaRestrictionType;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.*;
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
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

@RestController
@RequestMapping("/combined/personLookup/1")
public class CprRecordCombinedPersonLookupService {

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DafoUserManager dafoUserManager;

    @Autowired
    private CprPlugin cprPlugin;

    @Autowired
    protected MonitorService monitorService;

    private Logger log = LogManager.getLogger(CprRecordCombinedPersonLookupService.class.getCanonicalName());

    @Autowired
    private PersonOutputWrapper personOutputWrapper;

    @Autowired
    private CprDirectLookup cprDirectLookup;

    @Autowired
    private PersonEntityManager entityManager;

    @PostConstruct
    public void init() {
    }

    @RequestMapping(method = RequestMethod.GET, path = "/cpr/{cprNummer}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String getSingle(@PathVariable("cprNummer") String cprNummer, HttpServletRequest request, @RequestParam MultiValueMap<String, String> requestParams)
            throws AccessDeniedException, InvalidTokenException, InvalidClientInputException, HttpNotFoundException, InvalidCertificateException {

        String forceDirect = requestParams.getFirst("forceDirect");
        String allowDirect = requestParams.getFirst("allowDirect");

        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.info(
                "Incoming REST request for PrismeCprService with cprNummer " + cprNummer
        );
        this.checkAndLogAccess(loggerHelper);
        loggerHelper.urlInvokePersistablelogs("CprRecordService");

        try (final Session session = sessionManager.getSessionFactory().openSession()) {
            GeoLookupService lookupService = new GeoLookupService(sessionManager);
            personOutputWrapper.setLookupService(lookupService);

            PersonRecordQuery personQuery = new PersonRecordQuery();
            personQuery.setPersonnummer(cprNummer);

            OffsetDateTime now = OffsetDateTime.now();
            personQuery.setRegistrationAt(now);
            personQuery.setEffectAt(now);

            personQuery.applyFilters(session);
            this.applyAreaRestrictionsToQuery(personQuery, user);
            if ("true".equals(forceDirect)) {
                PersonEntity personEntity = cprDirectLookup.getPerson(cprNummer);
                Object obj = personOutputWrapper.wrapRecordResult(personEntity, personQuery);
                return obj.toString();
            }

            List<PersonEntity> personEntities = QueryManager.getAllEntities(session, personQuery, PersonEntity.class);
            PersonEntity personEntity = null;
            if (!personEntities.isEmpty()) {
                personEntity = personEntities.get(0);
            }
            if (personEntity == null && "true".equals(allowDirect)) {
                personEntity = cprDirectLookup.getPerson(cprNummer);
                entityManager.createSubscription(Collections.singleton(cprNummer));
            }

            if(personEntity==null) {
                throw new HttpNotFoundException("No entity with CPR number " + cprNummer + " was found");
            }

            Object obj = personOutputWrapper.wrapRecordResult(personEntity, personQuery);
            return obj.toString();
        } catch(DataStreamException e) {
            log.error(e);
            throw new HttpNotFoundException("No entity with CPR number " + cprNummer + " was found");
        }
    }

    @GetMapping("/cpr")
    public StreamingResponseBody findAll(HttpServletRequest request, @RequestParam MultiValueMap<String, String> requestParams) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException, InvalidDataInputException, QueryBuildException {

        List<String> cprs = requestParams.get("cpr");
        String allowDirect = requestParams.getFirst("allowDirect");

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

        return outputStream -> {

            try (Session session = sessionManager.getSessionFactory().openSession()) {


                GeoLookupService lookupService = new GeoLookupService(sessionManager);
                personOutputWrapper.setLookupService(lookupService);

                personQuery.applyFilters(session);
                this.applyAreaRestrictionsToQuery(personQuery, user);

                Stream<PersonEntity> personEntities = QueryManager.getAllEntitiesAsStream(session, personQuery, PersonEntity.class);

                final FinalWrapper<Boolean> first = new FinalWrapper<>(true);
                Consumer<PersonEntity> entityWriter = personEntity -> {
                    if (personEntity != null && personEntity.getPersonnummer() != null) {
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
                        session.evict(personEntity);
                    }
                };

                outputStream.write(START_OBJECT);
                personEntities.forEach(entityWriter);

                HashSet<String> found = new HashSet<>();
                if (!cprNumbers.isEmpty() && !hasAreaRestrictions(user) && "true".equals(allowDirect)) {
                    List<String> remaining = new ArrayList<>(cprNumbers);
                    remaining.stream().map(cprNummer -> {
                        try {
                            PersonEntity personEntity = cprDirectLookup.getPerson(cprNummer);
                            entityManager.createSubscription(Collections.singleton(cprNummer));
                            if (personEntity != null) {
                                found.add(cprNummer);
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
                log.warn("InvalidClientInputException");
            }
        };
    }

    private static final byte[] START_OBJECT = "{".getBytes();
    private static final byte[] END_OBJECT = "}".getBytes();
    private static final byte[] OBJECT_SEPARATOR = ",\n".getBytes();

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
                query.addKommunekodeRestriction(restriction.getValue());
            }
        }
    }

    private static boolean hasAreaRestrictions(DafoUserDetails user) {
        return !user.getAreaRestrictionsForRole(CprRolesDefinition.READ_CPR_ROLE).isEmpty();
    }
}
