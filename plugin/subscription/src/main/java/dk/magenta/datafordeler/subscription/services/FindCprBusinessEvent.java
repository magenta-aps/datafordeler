package dk.magenta.datafordeler.subscription.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.MonitorService;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.exception.AccessRequiredException;
import dk.magenta.datafordeler.core.exception.InvalidCertificateException;
import dk.magenta.datafordeler.core.exception.InvalidTokenException;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.Envelope;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.records.person.data.PersonEventDataRecord;
import dk.magenta.datafordeler.cvr.access.CvrRolesDefinition;
import dk.magenta.datafordeler.subscription.data.subscriptionModel.BusinessEventSubscription;
import dk.magenta.datafordeler.subscription.data.subscriptionModel.CprList;
import dk.magenta.datafordeler.subscription.data.subscriptionModel.SubscribedCprNumber;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@RestController
@RequestMapping("/subscription/1/findCprBusinessEvent")
public class FindCprBusinessEvent {

    @Value("${dafo.subscription.allowCallingOtherConsumersSubscriptions:false}")
    protected boolean allowCallingOtherConsumersSubscriptions = false;

    @Autowired
    SessionManager sessionManager;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private DafoUserManager dafoUserManager;

    @Autowired
    protected MonitorService monitorService;

    private final Logger log = LogManager.getLogger(FindCprBusinessEvent.class.getCanonicalName());


    @PostConstruct
    public void init() {

    }


    /**
     * Get a list of all subscribtions
     *
     * @return
     */
    @GetMapping("/fetchEvents")
    public ResponseEntity<Envelope> findAll(HttpServletRequest request, @RequestParam MultiValueMap<String, String> requestParams) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {

        String pageSize = requestParams.getFirst("pageSize");
        String page = requestParams.getFirst("page");
        String businessEventId = requestParams.getFirst("subscription");
        String timestampGTE = requestParams.getFirst("timestamp.GTE");
        String timestampLTE = requestParams.getFirst("timestamp.LTE");
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);

        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.urlInvokePersistablelogs("fetchEvents");

        try (Session session = sessionManager.getSessionFactory().openSession()) {

            this.checkAndLogAccess(loggerHelper);

            Query eventQuery = session.createQuery(" from " + BusinessEventSubscription.class.getName() + " where businessEventId = :businessEventId", BusinessEventSubscription.class);
            eventQuery.setParameter("businessEventId", businessEventId);
            if (eventQuery.getResultList().isEmpty()) {
                return this.getErrorMessage("Subscription not found", HttpStatus.NOT_FOUND);
            } else {
                BusinessEventSubscription subscription = (BusinessEventSubscription) eventQuery.getResultList().get(0);
                if (!allowCallingOtherConsumersSubscriptions && !subscription.getSubscriber().getSubscriberId().equals(Optional.ofNullable(request.getHeader("uxp-client")).orElse(user.getIdentity()).replaceAll("/", "_"))) {
                    return this.getErrorMessage("No access", HttpStatus.FORBIDDEN);
                }
                String hql = "SELECT max(event.timestamp) FROM " + PersonEventDataRecord.class.getCanonicalName() + " event ";
                Query timestampQuery = session.createQuery(hql);
                OffsetDateTime newestEventTimestamp = (OffsetDateTime) timestampQuery.getResultList().get(0);
                OffsetDateTime offsetTimestampGTE;
                if (timestampGTE == null) {
                    offsetTimestampGTE = OffsetDateTime.of(0, 1, 1, 1, 1, 1, 1, ZoneOffset.ofHours(0));
                } else {
                    try {
                        offsetTimestampGTE = BaseQuery.parseDateTime(timestampGTE);
                    } catch (DateTimeParseException e) {
                        return this.getErrorMessage("Cannot parse date " + timestampGTE, HttpStatus.BAD_REQUEST);
                    }
                }

                OffsetDateTime offsetTimestampLTE = null;
                if (timestampLTE != null) {
                    try {
                        offsetTimestampLTE = BaseQuery.parseDateTime(timestampLTE);
                    } catch (DateTimeParseException e) {
                        return this.getErrorMessage("Cannot parse date " + timestampLTE, HttpStatus.BAD_REQUEST);
                    }
                }

                String[] subscriptionKodeId = subscription.getKodeId().split("[.]");
                if (!"cpr".equals(subscriptionKodeId[0]) && !"dataevent".equals(subscriptionKodeId[1])) {
                    return this.getErrorMessage("No access", HttpStatus.FORBIDDEN);
                }

                String listId = subscription.getCprList().getListId();

                // This is manually joined and not as part of the std. query. The reason for this is that we need to join the data wrom subscription and data. This is not the purpose anywhere else
                String queryString = "SELECT DISTINCT person FROM " + CprList.class.getCanonicalName() + " list " +
                        " INNER JOIN " + SubscribedCprNumber.class.getCanonicalName() + " numbers ON (list = numbers.cprList) " +
                        " INNER JOIN " + PersonEntity.class.getCanonicalName() + " person ON (person.personnummer = numbers.cprNumber) " +
                        " INNER JOIN " + PersonEventDataRecord.class.getCanonicalName() + " dataeventDataRecord ON (person = dataeventDataRecord.entity) " +
                        " where (list.listId=:listId OR :listId IS NULL) AND" +
                        " (dataeventDataRecord.eventId=:eventId OR :eventId IS NULL) AND" +
                        " (dataeventDataRecord.timestamp IS NOT NULL) AND" +
                        " (dataeventDataRecord.timestamp >= :offsetTimestampGTE OR :offsetTimestampGTE IS NULL) AND" +
                        " (dataeventDataRecord.timestamp <= :offsetTimestampLTE OR :offsetTimestampLTE IS NULL)";

                Query query = session.createQuery(queryString);
                if (pageSize != null) {
                    query.setMaxResults(Integer.valueOf(pageSize));
                } else {
                    query.setMaxResults(10);
                }
                if (page != null) {
                    int pageIndex = (Integer.valueOf(page) - 1) * query.getMaxResults();
                    query.setFirstResult(pageIndex);
                } else {
                    query.setFirstResult(0);
                }
                if (query.getMaxResults() > 1000) {
                    return this.getErrorMessage("Pagesize is too large", HttpStatus.FORBIDDEN);
                }

                Stream<PersonEntity> personStream = query
                        .setParameter("offsetTimestampGTE", offsetTimestampGTE)
                        .setParameter("offsetTimestampLTE", offsetTimestampLTE)
                        .setParameter("listId", listId)
                        .setParameter("eventId", subscriptionKodeId[2])
                        .stream();

                Envelope envelope = new Envelope();

                List<Object> returnValues = personStream.map(f -> f.getPersonnummer()).collect(Collectors.toList());
                envelope.setResults(returnValues);
                envelope.setNewestResultTimestamp(newestEventTimestamp);
                envelope.setPage(query.getFirstResult());
                envelope.setPageSize(query.getMaxResults());
                loggerHelper.urlInvokePersistablelogs("fetchEvents done");
                return ResponseEntity.ok(envelope);
            }
        } catch (AccessRequiredException e) {
            String errorMessage = "No access to this information";
            ObjectNode obj = this.objectMapper.createObjectNode();
            obj.put("errorMessage", errorMessage);
            log.warn(errorMessage);
            return new ResponseEntity(obj.toString(), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            log.error("Failed pulling events from subscribtion", e);
        }
        return ResponseEntity.status(500).build();
    }

    private ResponseEntity getErrorMessage(String message, HttpStatus status) {
        String errorMessage = message;
        ObjectNode obj = this.objectMapper.createObjectNode();
        obj.put("errorMessage", message);
        log.warn(errorMessage);
        return new ResponseEntity(obj.toString(), status);
    }

    protected void checkAndLogAccess(LoggerHelper loggerHelper) throws AccessDeniedException, AccessRequiredException {
        loggerHelper.logRequest();
        try {
            loggerHelper.getUser().checkHasSystemRole(CprRolesDefinition.READ_CPR_ROLE);
            loggerHelper.getUser().checkHasSystemRole(CvrRolesDefinition.READ_CVR_ROLE);
        } catch (AccessDeniedException e) {
            loggerHelper.info("Access denied: " + e.getMessage());
            throw (e);
        }
    }
}
