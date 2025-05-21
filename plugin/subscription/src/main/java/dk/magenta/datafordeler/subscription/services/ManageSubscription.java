package dk.magenta.datafordeler.subscription.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import dk.magenta.datafordeler.core.MonitorService;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.*;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import dk.magenta.datafordeler.subscription.data.subscriptionModel.*;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.PersistenceException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.*;


@RestController
@RequestMapping("/subscription/1/manager")
public class ManageSubscription {

    @Autowired
    SessionManager sessionManager;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private DafoUserManager dafoUserManager;

    @Autowired
    protected MonitorService monitorService;

    private final Logger log = LogManager.getLogger(ManageSubscription.class.getCanonicalName());


    private CprList getCprList(Session session, String listId) throws HttpNotFoundException {
        CprList cprListItem = null;
        if (listId != null && !listId.isEmpty()) {
            cprListItem = QueryManager.getItem(session, CprList.class, Collections.singletonMap(CprList.DB_FIELD_LIST_ID, listId));
            if (cprListItem == null) {
                throw new HttpNotFoundException("CprList could not be found");
            }
        }
        return cprListItem;
    }

    private CvrList getCvrList(Session session, String listId) throws HttpNotFoundException {
        CvrList cvrListItem = null;
        if (listId != null && !listId.isEmpty()) {
            cvrListItem = QueryManager.getItem(session, CvrList.class, Collections.singletonMap(CvrList.DB_FIELD_LIST_ID, listId));
            if (cvrListItem == null) {
                throw new HttpNotFoundException("CvrList could not be found");
            }
        }
        return cvrListItem;
    }

    private Subscriber getSubscriber(Session session, String subscriberId) throws HttpNotFoundException {
        Subscriber subscriber = QueryManager.getItem(session, Subscriber.class, Collections.singletonMap(Subscriber.DB_FIELD_SUBSCRIBER_ID, subscriberId));
        if (subscriber == null) {
            throw new HttpNotFoundException("Subscriber could not be found");
        }
        return subscriber;
    }

    private BusinessEventSubscription getBusinessEventSubscription(Session session, String subscriptionId) throws HttpNotFoundException {
        BusinessEventSubscription subscription = QueryManager.getItem(
                session,
                BusinessEventSubscription.class,
                Collections.singletonMap(BusinessEventSubscription.DB_FIELD_BUSINESS_EVENT_ID, subscriptionId)
        );
        if (subscription == null) {
            throw new HttpNotFoundException("BusinessEventSubscription could not be found");
        }
        return subscription;
    }

    private DataEventSubscription getDataEventSubscription(Session session, String subscriptionId) throws HttpNotFoundException {
        DataEventSubscription subscription = QueryManager.getItem(
                session,
                DataEventSubscription.class,
                Collections.singletonMap(DataEventSubscription.DB_FIELD_DATAEVENT_ID, subscriptionId)
        );
        if (subscription == null) {
            throw new HttpNotFoundException("DataEventSubscription could not be found");
        }
        return subscription;
    }

    private String getSubscriberId(HttpServletRequest request, DafoUserDetails user) {
        return Optional.ofNullable(request.getHeader("uxp-client")).orElse(user.getIdentity()).replaceAll("/", "_");
    }


    /**
     * Get a list of all subscriptions
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, path = {"/subscriber", "/subscriber/"})
    public ResponseEntity<String> findAll(HttpServletRequest request) {
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            List<Subscriber> subscriptionList = QueryManager.getAllItems(session, Subscriber.class);
            ArrayNode arrayNode = objectMapper.createArrayNode();
            System.out.println("----------------------");
            System.out.println(objectMapper.writeValueAsString(subscriptionList));
            for (Subscriber s : subscriptionList) {
                System.out.println(objectMapper.writeValueAsString(s));
                System.out.println(s.getCvrLists());
                arrayNode.add(objectMapper.valueToTree(s));
                System.out.println(objectMapper.writeValueAsString(s));
                System.out.println(objectMapper.writeValueAsString(objectMapper.valueToTree(s)));
            }
            System.out.println(objectMapper.writeValueAsString(arrayNode));
            return ResponseEntity.ok(objectMapper.writeValueAsString(arrayNode));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Special case for creation of other users
     *
     * @param request
     * @param subscriberContent
     * @return
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.POST, path = {"/subscriber/create", "/subscriber/create/"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> createSubscriber(HttpServletRequest request, @Valid @RequestBody String subscriberContent) throws IOException, ConflictException {
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Subscriber subscriber = objectMapper.readValue(subscriberContent, Subscriber.class);
            session.persist(subscriber);
            transaction.commit();
            return ResponseEntity.ok(objectMapper.writeValueAsString(subscriber));
        } catch (PersistenceException e) {
            throw new ConflictException("Elements already exists");
        }
    }


    @RequestMapping(method = RequestMethod.POST, path = {"/subscriber", "/subscriber/"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Subscriber> createMySubscriber(HttpServletRequest request) throws IOException, AccessDeniedException, InvalidTokenException, InvalidCertificateException, ConflictException {

        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.urlInvokePersistablelogs("subscriber");
        String subscriberId = this.getSubscriberId(request, user);
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                Subscriber subscriber = new Subscriber(subscriberId);
                session.persist(subscriber);
                transaction.commit();
                loggerHelper.urlInvokePersistablelogs("subscriber done");
                return ResponseEntity.ok(subscriber);
            } catch (PersistenceException e) {
                transaction.rollback();
                throw new ConflictException("Failed creating subscriber");
            } catch (Exception e) {
                transaction.rollback();
                throw e;
            }
        }
    }

    @RequestMapping(method = RequestMethod.GET, path = {"/subscriber/{subscriberId}", "/subscriber/{subscriberId}/"})
    public ResponseEntity<Subscriber> getBySubscriberId(@PathVariable("subscriberId") String subscriberId) throws HttpNotFoundException {
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Subscriber subscriber = this.getSubscriber(session, subscriberId);
            return ResponseEntity.ok(subscriber);
        }
    }

    @RequestMapping(method = RequestMethod.DELETE, path = {"/subscriber", "/subscriber/"})
    public ResponseEntity<Subscriber> deleteBySubscriberId(HttpServletRequest request) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException, HttpNotFoundException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.urlInvokePersistablelogs("subscriber");
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            String subscriberId = this.getSubscriberId(request, user);
            Subscriber subscriber = this.getSubscriber(session, subscriberId);
            Transaction transaction = session.beginTransaction();
            session.remove(subscriber);
            transaction.commit();
            loggerHelper.urlInvokePersistablelogs("subscriber done");
            return ResponseEntity.ok(subscriber);
        }
    }


    /**
     * Get a list of all businessEventSubscriptions
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, path = {"/subscriber/subscription/businesseventSubscription", "/subscriber/subscription/businesseventSubscription/"})
    public ResponseEntity<List<BusinessEventSubscription>> businessEventSubscriptionfindAll(HttpServletRequest request) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException, HttpNotFoundException {
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
            String subscriberId = this.getSubscriberId(request, user);
            Subscriber subscriber = this.getSubscriber(session, subscriberId);
            LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
            loggerHelper.urlInvokePersistablelogs("businesseventSubscription");
            Iterator<BusinessEventSubscription> subscriptions = subscriber.getBusinessEventSubscription().iterator();
            List<BusinessEventSubscription> list = new ArrayList<BusinessEventSubscription>();
            while (subscriptions.hasNext()) {
                list.add(subscriptions.next());
            }
            loggerHelper.urlInvokePersistablelogs("businesseventSubscription done");
            return ResponseEntity.ok(list);
        }
    }


    @RequestMapping(method = RequestMethod.POST, path = {"/subscriber/subscription/businesseventSubscription", "/subscriber/subscription/businesseventSubscription/"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity businessEventSubscriptionCreate(HttpServletRequest request,
                                                          @RequestParam(value = "businessEventId", required = false, defaultValue = "") String businessEventId,
                                                          @RequestParam(value = "kodeId", required = false, defaultValue = "") String kodeId,
                                                          @RequestParam(value = "cprList", required = false, defaultValue = "") String cprList) throws HttpNotFoundException, InvalidTokenException, AccessDeniedException, InvalidCertificateException, ConflictException {

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            CprList cprListItem = null;
            if (!"".equals(cprList)) {
                cprListItem = this.getCprList(session, cprList);
            }
            Transaction transaction = session.beginTransaction();

            DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
            LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
            loggerHelper.urlInvokePersistablelogs("businesseventSubscription");

            String subscriberId = this.getSubscriberId(request, user);
            Subscriber subscriber = this.getSubscriber(session, subscriberId);

            BusinessEventSubscription subscription = new BusinessEventSubscription(businessEventId, kodeId, subscriber);
            subscription.setCprList(cprListItem);
            subscriber.addBusinessEventSubscription(subscription);

            session.persist(subscriber);
            transaction.commit();
            loggerHelper.urlInvokePersistablelogs("businesseventSubscription done");
            return ResponseEntity.ok(subscription);
        } catch (PersistenceException e) {
            throw new ConflictException("Subscription already exists", e);
        }
    }

    @RequestMapping(method = RequestMethod.PUT, path = {"/subscriber/subscription/businesseventSubscription/{businessEventId}", "/subscriber/subscription/businesseventSubscription/{businessEventId}/"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<BusinessEventSubscription> businessEventSubscriptionUpdate(HttpServletRequest request, @PathVariable("businessEventId") String businessEventId,
                                                          @RequestParam(value = "kodeId", required = false, defaultValue = "") String kodeId,
                                                          @RequestParam(value = "cprList", required = false, defaultValue = "") String cprList) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException, HttpNotFoundException {

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            BusinessEventSubscription subscription = this.getBusinessEventSubscription(session, businessEventId);

            DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
            LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
            loggerHelper.urlInvokePersistablelogs("businesseventSubscription");
            Transaction transaction = session.beginTransaction();
            String subscriberId = this.getSubscriberId(request, user);
            if (!subscription.getSubscriber().getSubscriberId().equals(subscriberId)) {
                throw new AccessDeniedException("No access to this subscription");
            }
            if (!"".equals(kodeId)) {
                subscription.setKodeId(kodeId);
            }
            CprList cprListItem = null;
            if (!"".equals(cprList)) {
                cprListItem = this.getCprList(session, cprList);
                subscription.setCprList(cprListItem);
            }
            session.persist(subscription);
            transaction.commit();
            loggerHelper.urlInvokePersistablelogs("businesseventSubscription done");
            return ResponseEntity.ok(subscription);
        }
    }

    @RequestMapping(method = RequestMethod.GET, path = {"/subscriber/subscription/businesseventSubscription/{subscriptionId}", "/subscriber/subscription/businesseventSubscription/{subscriptionId}/"})
    public ResponseEntity<BusinessEventSubscription> businessEventSubscriptiongetBySubscriberId(HttpServletRequest request, @PathVariable("subscriptionId") String subscriptionId) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException, HttpNotFoundException {
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            BusinessEventSubscription subscription = this.getBusinessEventSubscription(session, subscriptionId);
            DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
            LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
            loggerHelper.urlInvokePersistablelogs("businesseventSubscription");
            loggerHelper.urlInvokePersistablelogs("businesseventSubscription done");
            return ResponseEntity.ok(subscription);
        }
    }

    @RequestMapping(method = RequestMethod.DELETE, path = {"/subscriber/subscription/businesseventSubscription/{subscriptionId}", "/subscriber/subscription/businesseventSubscription/{subscriptionId}/"})
    public ResponseEntity<BusinessEventSubscription> businessEventSubscriptiondeleteBySubscriberId(HttpServletRequest request, @PathVariable("subscriptionId") String subscriptionId) throws HttpNotFoundException, AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            BusinessEventSubscription subscription = this.getBusinessEventSubscription(session, subscriptionId);

            DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
            LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
            loggerHelper.urlInvokePersistablelogs("dataeventSubscription");
            String subscriberId = this.getSubscriberId(request, user);
            if (subscription.getSubscriber().getSubscriberId().equals(subscriberId)) {
                subscription.getSubscriber().removeBusinessEventSubscription(subscription);
                session.remove(subscription);
                transaction.commit();
                loggerHelper.urlInvokePersistablelogs("dataeventSubscription done");
                return ResponseEntity.ok(subscription);
            } else {
                transaction.rollback();
                throw new AccessDeniedException("No access to this subscription");
            }
        }
    }

    /**
     * Get a list of all dataEventSubscriptions
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, path = {"/subscriber/subscription/dataeventSubscription", "/subscriber/subscription/dataeventSubscription/"})
    public ResponseEntity<List<DataEventSubscription>> dataEventSubscriptionfindAll(HttpServletRequest request) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException, HttpNotFoundException {
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
            LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
            loggerHelper.urlInvokePersistablelogs("dataeventSubscription");
            String subscriberId = this.getSubscriberId(request, user);
            Subscriber subscriber = this.getSubscriber(session, subscriberId);
            loggerHelper.urlInvokePersistablelogs("dataeventSubscription done");
            return ResponseEntity.ok(new ArrayList<>(subscriber.getDataEventSubscription()));
        }
    }


    @RequestMapping(method = RequestMethod.POST, path = {"/subscriber/subscription/dataeventSubscription", "/subscriber/subscription/dataeventSubscription/"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<DataEventSubscription> dataEventSubscriptionCreate(HttpServletRequest request,
                                                      @RequestParam(value = "dataEventId", required = false, defaultValue = "") String dataEventId,
                                                      @RequestParam(value = "kodeId", required = false, defaultValue = "") String kodeId,
                                                      @RequestParam(value = "cprList", required = false, defaultValue = "") String cprList,
                                                      @RequestParam(value = "cvrList", required = false, defaultValue = "") String cvrList) throws HttpNotFoundException, InvalidTokenException, AccessDeniedException, InvalidCertificateException, ConflictException {

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
            LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
            loggerHelper.urlInvokePersistablelogs("dataeventSubscription");
            CprList cprListItem = null;
            if (!cprList.isEmpty()) {
                cprListItem = this.getCprList(session, cprList);
            }
            CvrList cvrListItem = null;
            if (!cvrList.isEmpty()) {
                cvrListItem = this.getCvrList(session, cvrList);
            }
            Transaction transaction = session.beginTransaction();
            String subscriberId = this.getSubscriberId(request, user);
            Subscriber subscriber = this.getSubscriber(session, subscriberId);

            DataEventSubscription subscription = new DataEventSubscription(dataEventId, kodeId, subscriber);
            subscription.setCprList(cprListItem);
            subscription.setCvrList(cvrListItem);
            subscriber.addDataEventSubscription(subscription);
            session.persist(subscriber);
            transaction.commit();
            loggerHelper.urlInvokePersistablelogs("dataeventSubscription done");
            return ResponseEntity.ok(subscription);

        } catch (PersistenceException e) {
            throw new ConflictException("Subscription already exists", e);
        }
    }

    @RequestMapping(method = RequestMethod.PUT, path = {"/subscriber/subscription/dataeventSubscription/{dataEventId}", "/subscriber/subscription/dataeventSubscription/{dataEventId}/"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<DataEventSubscription> dataEventSubscriptionUpdate(HttpServletRequest request, @PathVariable("dataEventId") String dataEventId,
                                                      @RequestParam(value = "kodeId", required = false, defaultValue = "") String kodeId,
                                                      @RequestParam(value = "cprList", required = false, defaultValue = "") String cprList,
                                                      @RequestParam(value = "cvrList", required = false, defaultValue = "") String cvrList) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException, HttpNotFoundException {

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            DataEventSubscription subscription = this.getDataEventSubscription(session, dataEventId);
            DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
            LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
            loggerHelper.urlInvokePersistablelogs("dataeventSubscription");
            String subscriberId = this.getSubscriberId(request, user);
            if (!subscription.getSubscriber().getSubscriberId().equals(subscriberId)) {
                throw new AccessDeniedException("No access to this subscription");
            }
            if (!"".equals(kodeId)) {
                subscription.setKodeId(kodeId);
            }
            if (!"".equals(cprList)) {
                CprList cprListItem = this.getCprList(session, cprList);
                subscription.setCprList(cprListItem);
            }
            if (!"".equals(cvrList)) {
                CvrList cvrListItem = this.getCvrList(session, cvrList);
                subscription.setCvrList(cvrListItem);
            }
            session.persist(subscription);
            transaction.commit();
            loggerHelper.urlInvokePersistablelogs("dataeventSubscription done");
            return ResponseEntity.ok(subscription);
        }
    }

    @RequestMapping(method = RequestMethod.GET, path = {"/subscriber/subscription/dataeventSubscription/{subscriptionId}", "/subscriber/subscription/dataeventSubscription/{subscriptionId}/"})
    public ResponseEntity<DataEventSubscription> dataEventSubscriptiongetBySubscriberId(HttpServletRequest request, @PathVariable("dataEventId") String dataEventId) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException, HttpNotFoundException {
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            DataEventSubscription subscription = this.getDataEventSubscription(session, dataEventId);
            DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
            LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
            loggerHelper.urlInvokePersistablelogs("dataeventSubscription done");
            return ResponseEntity.ok(subscription);
        }
    }

    @RequestMapping(path = {"/subscriber/subscription/dataeventSubscription/{dataEventId}", "/subscriber/subscription/dataeventSubscription/{dataEventId}/"})
    public ResponseEntity<DataEventSubscription> dataEventSubscriptionDeleteBySubscriberId(HttpServletRequest request, @PathVariable("dataEventId") String dataEventId) throws HttpNotFoundException, AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            DataEventSubscription subscription = this.getDataEventSubscription(session, dataEventId);
            Transaction transaction = session.beginTransaction();
            DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
            LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
            loggerHelper.urlInvokePersistablelogs("dataeventSubscription");
            String subscriberId = this.getSubscriberId(request, user);
            if (subscription.getSubscriber().getSubscriberId().equals(subscriberId)) {
                subscription.getSubscriber().removeDataEventSubscription(subscription);
                session.remove(subscription);
                transaction.commit();
                loggerHelper.urlInvokePersistablelogs("dataeventSubscription done");
                return ResponseEntity.ok(subscription);
            } else {
                transaction.rollback();
                throw new AccessDeniedException("No access to this subscription");
            }
        }
    }

}
