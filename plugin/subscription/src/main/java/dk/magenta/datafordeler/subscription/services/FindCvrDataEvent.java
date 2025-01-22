package dk.magenta.datafordeler.subscription.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.client.util.Value;
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
import dk.magenta.datafordeler.cvr.access.CvrRolesDefinition;
import dk.magenta.datafordeler.cvr.records.*;
import dk.magenta.datafordeler.subscription.data.subscriptionModel.CvrList;
import dk.magenta.datafordeler.subscription.data.subscriptionModel.DataEventSubscription;
import dk.magenta.datafordeler.subscription.data.subscriptionModel.SubscribedCvrNumber;
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

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@RestController
@RequestMapping("/subscription/1/findCvrDataEvent")
public class FindCvrDataEvent {

    @Value("${dafo.subscription.allowCallingOtherConsumersSubscriptions}")
    protected boolean allowCallingOtherConsumersSubscriptions = false;

    @Autowired
    SessionManager sessionManager;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private DafoUserManager dafoUserManager;

    @Autowired
    private RecordMetadataWrapper personRecordOutputWrapperStuff;

    @Autowired
    protected MonitorService monitorService;

    private final Logger log = LogManager.getLogger(FindCvrDataEvent.class.getCanonicalName());


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

            String hql = "SELECT max(event.timestamp) FROM " + CompanyDataEventRecord.class.getCanonicalName() + " event ";
            Query timestampQuery = session.createQuery(hql);
            OffsetDateTime newestEventTimestamp = (OffsetDateTime) timestampQuery.getResultList().get(0);

            Query eventQuery = session.createQuery(" from " + DataEventSubscription.class.getName() + " where dataEventId = :dataEventId", DataEventSubscription.class);
            eventQuery.setParameter("dataEventId", dataEventId);
            if (eventQuery.getResultList().size() == 0) {
                return this.getErrorMessage("Subscription not found", HttpStatus.NOT_FOUND);
            } else {
                DataEventSubscription subscription = (DataEventSubscription) eventQuery.getResultList().get(0);
                String subscriberId = subscription.getSubscriber().getSubscriberId();
                String clientId = Optional.ofNullable(request.getHeader("uxp-client")).orElse(user.getIdentity()).replaceAll("/", "_");
                if (!allowCallingOtherConsumersSubscriptions && !Objects.equals(subscriberId, clientId)) {
                    return this.getErrorMessage("No access", HttpStatus.FORBIDDEN);
                }
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
                if (!"cvr".equals(subscriptionKodeId[0]) && !"dataevent".equals(subscriptionKodeId[1])) {
                    return this.getErrorMessage("No access", HttpStatus.FORBIDDEN);
                }

                CvrList cvrList = subscription.getCvrList();
                if (cvrList == null) {
                    return this.getErrorMessage("No cvrlist for subscription", HttpStatus.NOT_FOUND);
                }

                String listId = cvrList.getListId();

                // This is manually joined and not as part of the std. query. The reason for this is that we need to join the data wrom subscription and data. This is not the purpose anywhere else
                String queryString = "SELECT DISTINCT company FROM " + CvrList.class.getCanonicalName() + " list " +
                        " INNER JOIN " + SubscribedCvrNumber.class.getCanonicalName() + " numbers ON (list.id = numbers.cvrList) " +
                        " INNER JOIN " + CompanyRecord.class.getCanonicalName() + " company ON (company.cvrNumber = numbers.cvrNumber) " +
                        " INNER JOIN " + CompanyDataEventRecord.class.getCanonicalName() + " dataeventDataRecord ON (company.id = dataeventDataRecord.companyRecord) " +
                        " where" +
                        " (list.listId=:listId OR :listId IS NULL) AND" +
                        " (dataeventDataRecord.field=:fieldEntity OR :fieldEntity IS NULL) AND" +
                        " (dataeventDataRecord.timestamp IS NOT NULL) AND" +
                        " (dataeventDataRecord.timestamp >= : offsetTimestampGTE OR :offsetTimestampGTE IS NULL) AND" +
                        " (dataeventDataRecord.timestamp <= : offsetTimestampLTE OR :offsetTimestampLTE IS NULL)";

                Query<CompanyRecord> query = session.createQuery(queryString, CompanyRecord.class);
                if (pageSize != null) {
                    query.setMaxResults(Integer.parseInt(pageSize));
                } else {
                    query.setMaxResults(10);
                }
                if (page != null) {
                    int pageIndex = (Integer.parseInt(page) - 1) * query.getMaxResults();
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

                Stream<CompanyRecord> personStream = query
                        .setParameter("offsetTimestampGTE", offsetTimestampGTE)
                        .setParameter("offsetTimestampLTE", offsetTimestampLTE)
                        .setParameter("listId", listId)
                        .setParameter("fieldEntity", fieldType)
                        .stream();

                Envelope envelope = new Envelope();

                if (!includeMeta) {
                    envelope.setResults(personStream.map(CompanyRecord::getCvrNumber).collect(Collectors.toList()));
                } else {
                    List<ObjectNode> otherList = new ArrayList<>();
                    List<CompanyRecord> entities = personStream.collect(Collectors.toList());

                    for (CompanyRecord entity : entities) {
                        CvrBitemporalDataRecord oldValues = null;
                        CvrBitemporalDataRecord newValues = getActualValueRecord(subscriptionKodeId[2], entity);
                        Set<CompanyDataEventRecord> events = entity.getDataevent();
                        if (events.size() > 0) {
                            CompanyDataEventRecord eventRecord = events.iterator().next();
                            if (eventRecord.getOldItem() != null) {
                                String queryPreviousItem = GeneralQuery.getQueryCompanyValueObjectFromIdInEvent(subscriptionKodeId[2]);
                                if (eventRecord.getOldItem() != null) {
                                    oldValues = (CvrBitemporalDataMetaRecord) session.createQuery(queryPreviousItem).setParameter("id", eventRecord.getOldItem()).getResultList().get(0);
                                }
                            }
                        }

                        if (subscriptionKodeId.length >= 5) {
                            if (subscriptionKodeId[3].equals("before")) {
                                if (this.validateIt(subscriptionKodeId[2], subscriptionKodeId[4], oldValues)) {
                                    ObjectNode node = personRecordOutputWrapperStuff.fillContainer(entity.getCvrNumberString(), subscriptionKodeId[2], oldValues, newValues);
                                    otherList.add(node);
                                }
                            } else if (subscriptionKodeId[3].equals("after")) {
                                if (this.validateIt(subscriptionKodeId[2], subscriptionKodeId[4], newValues)) {
                                    ObjectNode node = personRecordOutputWrapperStuff.fillContainer(entity.getCvrNumberString(), subscriptionKodeId[2], oldValues, newValues);
                                    otherList.add(node);
                                }
                            }
                        } else {
                            ObjectNode node = personRecordOutputWrapperStuff.fillContainer(entity.getCvrNumberString(), subscriptionKodeId[2], oldValues, newValues);
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
        ObjectNode obj = this.objectMapper.createObjectNode();
        obj.put("errorMessage", message);
        log.warn(message);
        return new ResponseEntity(obj.toString(), status);
    }


    private boolean validateIt(String fieldname, String logic, CvrBitemporalDataRecord companyEntity) {

        if (AddressRecord.TABLE_NAME.equals(fieldname)) {
            String[] splitLogic = logic.split("=");
            if ("kommunekode".equals(splitLogic[0])) {
                return ((AddressRecord) companyEntity).getMunicipality().getMunicipalityCode() == Integer.parseInt(splitLogic[1]);
            }
        }
        return false;
    }


    private CvrBitemporalDataRecord getActualValueRecord(String fieldname, CompanyRecord companyEntity) {

        switch (fieldname) {
            case BaseNameRecord.TABLE_NAME:
                return companyEntity.getNames().stream().reduce((first, second) -> second).orElse(null);
            case AddressRecord.TABLE_NAME:
                return companyEntity.getPostalAddress().stream().reduce((first, second) -> second).orElse(null);
            case StatusRecord.TABLE_NAME:
                return companyEntity.getStatus().stream().reduce((first, second) -> second).orElse(null);
            default:
                return null;
        }
    }

    protected void checkAndLogAccess(LoggerHelper loggerHelper) throws AccessDeniedException, AccessRequiredException {
        try {
            loggerHelper.getUser().checkHasSystemRole(CprRolesDefinition.READ_CPR_ROLE);
            loggerHelper.getUser().checkHasSystemRole(CvrRolesDefinition.READ_CVR_ROLE);
        } catch (AccessDeniedException e) {
            loggerHelper.info("Access denied: " + e.getMessage());
            throw(e);
        }
    }
}
