package dk.magenta.datafordeler.subscription.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.client.util.Value;
import dk.magenta.datafordeler.core.MonitorService;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.exception.AccessRequiredException;
import dk.magenta.datafordeler.core.exception.InvalidCertificateException;
import dk.magenta.datafordeler.core.exception.InvalidTokenException;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.Envelope;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.BitemporalityComparator;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.CprNontemporalRecord;
import dk.magenta.datafordeler.cpr.records.person.CprBitemporalPersonRecord;
import dk.magenta.datafordeler.cpr.records.person.data.*;
import dk.magenta.datafordeler.cvr.access.CvrRolesDefinition;
import dk.magenta.datafordeler.subscription.data.subscriptionModel.CprList;
import dk.magenta.datafordeler.subscription.data.subscriptionModel.DataEventSubscription;
import dk.magenta.datafordeler.subscription.data.subscriptionModel.SubscribedCprNumber;
import dk.magenta.datafordeler.subscription.queries.GeneralQuery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.naturalOrder;


@RestController
@RequestMapping("/subscription/1/findCprDataEvent")
public class FindCprDataEvent {

    @Value("${dafo.subscription.allowCallingOtherConsumersSubscriptions}")
    protected boolean allowCallingOtherConsumersSubscriptions = false;

    @Autowired
    SessionManager sessionManager;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private DafoUserManager dafoUserManager;

    @Autowired
    private RecordMetadataWrapper personRecordOutputWrapper;


    @Autowired
    protected MonitorService monitorService;

    private final Logger log = LogManager.getLogger(FindCprDataEvent.class.getCanonicalName());


    @PostConstruct
    public void init() {

    }


    /**
     * Get a list of all subscriptions
     *
     * @return
     */
    @GetMapping("/fetchEvents")
    public ResponseEntity<Envelope> findAll(HttpServletRequest request, @RequestParam MultiValueMap<String, String> requestParams) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {

        String pageSize = requestParams.getFirst("pageSize");
        String page = requestParams.getFirst("page");
        String dataEventId = requestParams.getFirst("subscription");
        String timestampGTE = requestParams.getFirst("timestamp.GTE");
        String timestampLTE = requestParams.getFirst("timestamp.LTE");
        Boolean includeMeta = Boolean.parseBoolean(requestParams.getFirst("includeMeta"));
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);

        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.urlInvokePersistablelogs("fetchEvents");

        try (Session session = sessionManager.getSessionFactory().openSession()) {

            this.checkAndLogAccess(loggerHelper);

            String hql = "SELECT max(event.timestamp) FROM " + PersonDataEventDataRecord.class.getCanonicalName() + " event ";
            Query timestampQuery = session.createQuery(hql);
            OffsetDateTime newestEventTimestamp = (OffsetDateTime) timestampQuery.getResultList().get(0);

            Query eventQuery = session.createQuery(" from " + DataEventSubscription.class.getName() + " where dataEventId = :dataEventId", DataEventSubscription.class);
            eventQuery.setParameter("dataEventId", dataEventId);
            if (eventQuery.getResultList().size() == 0) {
                return this.getErrorMessage("Subscription not found", HttpStatus.NOT_FOUND);
            } else {
                DataEventSubscription subscription = (DataEventSubscription) eventQuery.getResultList().get(0);
                if (!allowCallingOtherConsumersSubscriptions && !subscription.getSubscriber().getSubscriberId().equals(Optional.ofNullable(request.getHeader("uxp-client")).orElse(user.getIdentity()).replaceAll("/", "_"))) {
                    return this.getErrorMessage("No access", HttpStatus.FORBIDDEN);
                }
                OffsetDateTime offsetTimestampGTE;
                OffsetDateTime offsetTimestampLTE = null;
                if (timestampGTE == null) {
                    offsetTimestampGTE = OffsetDateTime.of(0, 1, 1, 1, 1, 1, 1, ZoneOffset.ofHours(0));
                } else {
                    try {
                        offsetTimestampGTE = BaseQuery.parseDateTime(timestampGTE);
                    } catch (DateTimeParseException e) {
                        return this.getErrorMessage("Cannot parse date " + timestampGTE, HttpStatus.BAD_REQUEST);
                    }
                }
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
                        " INNER JOIN " + SubscribedCprNumber.class.getCanonicalName() + " numbers ON (list.id = numbers.cprList) " +
                        " INNER JOIN " + PersonEntity.class.getCanonicalName() + " person ON (person.personnummer = numbers.cprNumber) " +
                        " INNER JOIN " + PersonDataEventDataRecord.class.getCanonicalName() + " dataeventDataRecord ON (person.id = dataeventDataRecord.entity) " +
                        " where (list.listId=:listId OR :listId IS NULL) AND" +
                        " (dataeventDataRecord.field=:fieldEntity OR :fieldEntity IS NULL) AND" +
                        " (dataeventDataRecord.timestamp IS NOT NULL) AND" +
                        " (dataeventDataRecord.timestamp >= : offsetTimestampGTE OR :offsetTimestampGTE IS NULL) AND" +
                        " (dataeventDataRecord.timestamp <= : offsetTimestampLTE OR :offsetTimestampLTE IS NULL)";

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
                String fieldType = null;
                if (!"anything".equals(subscriptionKodeId[2])) {
                    fieldType = subscriptionKodeId[2];
                }

                Stream<PersonEntity> personStream = query
                        .setParameter("offsetTimestampGTE", offsetTimestampGTE)
                        .setParameter("offsetTimestampLTE", offsetTimestampLTE)
                        .setParameter("listId", listId)
                        .setParameter("fieldEntity", fieldType)
                        .stream();

                Envelope envelope = new Envelope();
                List<Object> returnValues = null;

                if (!includeMeta) {

                    returnValues = personStream.map(f -> f.getPersonnummer()).collect(Collectors.toList());
                    envelope.setResults(returnValues);
                } else {
                    List otherList = new ArrayList<ObjectNode>();
                    List<PersonEntity> entities = personStream.collect(Collectors.toList());

                    for (PersonEntity entity : entities) {
                        CprBitemporalPersonRecord oldValues = null;
                        CprBitemporalPersonRecord newValues = getActualValueRecord(subscriptionKodeId[2], entity);
                        PersonDataEventDataRecord eventRecord = entity.getDataEvent(subscriptionKodeId[2]);
                        if (eventRecord != null) {
                            if (eventRecord.getOldItem() != null) {
                                String queryPreviousItem = GeneralQuery.getQueryPersonValueObjectFromIdInEvent(subscriptionKodeId[2]);
                                oldValues = (CprBitemporalPersonRecord) session.createQuery(queryPreviousItem).setParameter("id", eventRecord.getOldItem().longValue()).getResultList().get(0);
                            }
                        }

                        if (subscriptionKodeId.length >= 5) {
                            if (subscriptionKodeId[3].equals("before")) {
                                if (this.validateIt(subscriptionKodeId[2], subscriptionKodeId[4], oldValues)) {
                                    ObjectNode node = personRecordOutputWrapper.fillContainer(entity.getPersonnummer(), subscriptionKodeId[2], oldValues, newValues);
                                    otherList.add(node);
                                }
                            } else if (subscriptionKodeId[3].equals("after")) {
                                if (this.validateIt(subscriptionKodeId[2], subscriptionKodeId[4], newValues)) {
                                    ObjectNode node = personRecordOutputWrapper.fillContainer(entity.getPersonnummer(), subscriptionKodeId[2], oldValues, newValues);
                                    otherList.add(node);
                                }
                            }
                        } else {
                            ObjectNode node = personRecordOutputWrapper.fillContainer(entity.getPersonnummer(), subscriptionKodeId[2], oldValues, newValues);
                            otherList.add(node);
                        }
                    }
                    envelope.setResults(otherList);
                }

                envelope.setNewestResultTimestamp(newestEventTimestamp);
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
            log.error("Failed pulling events from subscription", e);
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
        try {
            loggerHelper.getUser().checkHasSystemRole(CprRolesDefinition.READ_CPR_ROLE);
            loggerHelper.getUser().checkHasSystemRole(CvrRolesDefinition.READ_CVR_ROLE);
        } catch (AccessDeniedException e) {
            loggerHelper.info("Access denied: " + e.getMessage());
            throw (e);
        }
    }

    private CprBitemporalPersonRecord getActualValueRecord(String fieldname, PersonEntity personEntity) {

        switch (fieldname) {
            case NameDataRecord.TABLE_NAME:
                return personEntity.getName().current().get(0);
            case AddressDataRecord.TABLE_NAME:
                return personEntity.getAddress().current().get(0);
            case AddressConameDataRecord.TABLE_NAME:
                return personEntity.getConame().current().get(0);
            case AddressNameDataRecord.TABLE_NAME:
                return personEntity.getAddressName().current().get(0);
            case CitizenshipDataRecord.TABLE_NAME:
                return personEntity.getCitizenship().current().get(0);
            case CivilStatusDataRecord.TABLE_NAME:
                return personEntity.getCivilstatus().current().get(0);
            default:
                return null;
        }
    }

    private boolean validateIt(String fieldname, String logic, CprBitemporalPersonRecord personEntity) {

        if (AddressDataRecord.TABLE_NAME.equals(fieldname)) {
            String[] splitLogic = logic.split("=");
            if (personEntity != null && "kommunekode".equals(splitLogic[0])) {
                return ((AddressDataRecord) personEntity).getMunicipalityCode() == Integer.parseInt(splitLogic[1]);
            }
        }
        return false;
    }

    private static final Comparator bitemporalComparator = Comparator.comparing(FindCprDataEvent::getBitemporality, BitemporalityComparator.ALL)
            .thenComparing(CprNontemporalRecord::getOriginDate, Comparator.nullsLast(naturalOrder()))
            .thenComparing(CprNontemporalRecord::getDafoUpdated)
            .thenComparing(DatabaseEntry::getId);


    public static CprBitemporality getBitemporality(CprBitemporalRecord record) {
        return record.getBitemporality();
    }

    /**
     * Find the newest unclosed record from the list of records
     * Records with a missing OriginDate is also removed since they are considered invalid
     *
     * @param records
     * @param <R>
     * @return
     */
    public static <R extends CprBitemporalRecord> R findNewestUnclosedOnRegistartionAndEffect(Collection<R> records) {
        return (R) records.stream().filter(r -> r.getBitemporality().registrationTo == null &&
                r.getBitemporality().effectTo == null).max(bitemporalComparator).orElse(null);
    }
}
