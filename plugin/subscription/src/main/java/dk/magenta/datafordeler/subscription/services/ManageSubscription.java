package dk.magenta.datafordeler.subscription.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.MonitorService;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.exception.InvalidCertificateException;
import dk.magenta.datafordeler.core.exception.InvalidTokenException;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import dk.magenta.datafordeler.subscription.data.subscriptionModel.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.persistence.PersistenceException;
import javax.servlet.http.HttpServletRequest;
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

    private Logger log = LogManager.getLogger(ManageSubscription.class.getCanonicalName());


    @PostConstruct
    public void init() {
    }


    /**
     * Get a list of all subscriptions
     * @return
     */
    @GetMapping("/subscriber")
    public ResponseEntity<List<Subscriber>> findAll(HttpServletRequest request) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            List<Subscriber> subscriptionList = QueryManager.getAllItems(session, Subscriber.class);
            return ResponseEntity.ok(subscriptionList);
        }
    }

    /**
     * Special case for creation of other users
     * @param request
     * @param subscriberContent
     * @return
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.POST, path = "/subscriber/create/", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity createSubscriber(HttpServletRequest request, @Valid @RequestBody String subscriberContent) throws IOException {

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Subscriber subscriber = objectMapper.readValue(subscriberContent, Subscriber.class);
            session.save(subscriber);
            transaction.commit();
            return ResponseEntity.ok(subscriber);
        } catch(PersistenceException e) {
            String errorMessage = "Elements already exists";
            ObjectNode obj = objectMapper.createObjectNode();
            obj.put("errorMessage", errorMessage);
            log.error(errorMessage, e);
            return new ResponseEntity(obj.toString(), HttpStatus.CONFLICT);
        }  catch(Exception e) {
            String errorMessage = "Failed creating subscriber";
            ObjectNode obj = objectMapper.createObjectNode();
            obj.put("errorMessage", errorMessage);
            log.error(errorMessage, e);
            return new ResponseEntity(obj.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @RequestMapping(method = RequestMethod.POST, path = "/subscriber/", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity createMySubscriber(HttpServletRequest request) throws IOException, AccessDeniedException, InvalidTokenException, InvalidCertificateException {

        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.urlInvokePersistablelogs("subscriber");
        Transaction transaction = null;
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Subscriber subscriber = new Subscriber(Optional.ofNullable(request.getHeader("uxp-client")).orElse(user.getIdentity()).replaceAll("/","_"));
            session.save(subscriber);
            transaction.commit();
            loggerHelper.urlInvokePersistablelogs("subscriber done");
            return ResponseEntity.ok(subscriber);
        } catch(PersistenceException e) {
            String errorMessage = "Failed creating subscriber";
            ObjectNode obj = objectMapper.createObjectNode();
            obj.put("errorMessage", errorMessage);
            log.error(errorMessage, e);
            return new ResponseEntity<>(obj.toString(), HttpStatus.CONFLICT);
        }  catch(Exception e) {
            String errorMessage = "Failed creating subscriber";
            ObjectNode obj = objectMapper.createObjectNode();
            obj.put("errorMessage", errorMessage);
            log.error(errorMessage, e);
            return new ResponseEntity(obj.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/subscriber/{subscriberId}")
    public ResponseEntity getBySubscriberId(@PathVariable("subscriberId") String subscriberId) {
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Query query = session.createQuery(" from "+ Subscriber.class.getName() +" where subscriberId = :subscriberId", Subscriber.class);
            query.setParameter("subscriberId", subscriberId);
            List<Subscriber> subscribers = query.getResultList();
            if(subscribers.size()==0) {
                String errorMessage = "Could not find subscriber subscriber";
                ObjectNode obj = objectMapper.createObjectNode();
                obj.put("errorMessage", errorMessage);
                return new ResponseEntity<>(obj.toString(), HttpStatus.NOT_FOUND);
            } else {
                Subscriber subscriber = subscribers.get(0);
                return ResponseEntity.ok(subscriber);
            }
        }
    }

    @DeleteMapping("/subscriber/")
    public ResponseEntity<Subscriber> deleteBySubscriberId(HttpServletRequest request) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.urlInvokePersistablelogs("subscriber");
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Query query = session.createQuery(" from "+ Subscriber.class.getName() +" where subscriberId = :subscriberId", Subscriber.class);

            query.setParameter("subscriberId", Optional.ofNullable(request.getHeader("uxp-client")).orElse(user.getIdentity()).replaceAll("/","_"));
            List<Subscriber> subscribers = query.getResultList();
            if(subscribers.isEmpty()) {
                String errorMessage = "Subscription could not be found";
                ObjectNode obj = objectMapper.createObjectNode();
                obj.put("errorMessage", errorMessage);
                log.error(errorMessage, errorMessage);
                return new ResponseEntity(obj.toString(), HttpStatus.NOT_FOUND);
            } else {
                Transaction transaction = session.beginTransaction();
                Subscriber subscriber = subscribers.get(0);
                session.delete(subscriber);
                transaction.commit();
                loggerHelper.urlInvokePersistablelogs("subscriber done");
                return ResponseEntity.ok(subscriber);
            }
        }
    }



    /**
     * Get a list of all businessEventSubscribtions
     * @return
     */
    @GetMapping("/subscriber/subscription/businesseventSubscription")
    public ResponseEntity<List<BusinessEventSubscription>> businessEventSubscribtionfindAll(HttpServletRequest request) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Query query = session.createQuery(" from "+ Subscriber.class.getName() +" where subscriberId = :subscriberId", Subscriber.class);
            DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
            query.setParameter("subscriberId", Optional.ofNullable(request.getHeader("uxp-client")).orElse(user.getIdentity()).replaceAll("/","_"));
            LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
            loggerHelper.urlInvokePersistablelogs("businesseventSubscription");

            Subscriber subscriber = (Subscriber) query.getResultList().get(0);
            Iterator<BusinessEventSubscription> subscriptions = subscriber.getBusinessEventSubscription().iterator();
            List <BusinessEventSubscription> list = new ArrayList <BusinessEventSubscription>();
            while(subscriptions.hasNext()) {
                list.add(subscriptions.next());
            }
            loggerHelper.urlInvokePersistablelogs("businesseventSubscription done");
            return ResponseEntity.ok(list);
        }
    }

    @RequestMapping(method = RequestMethod.POST, path = "/subscriber/subscription/businesseventSubscription/", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity businessEventSubscribtionCreate(HttpServletRequest request,
                                                          @RequestParam(value = "businessEventId",required=false, defaultValue = "") String businessEventId,
                                                          @RequestParam(value = "kodeId",required=false, defaultValue = "") String kodeId,
                                                          @RequestParam(value = "cprList",required=false, defaultValue = "") String cprList) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            CprList cprListItem = null;
            if(!"".equals(cprList)) {
                Query cprListQuery = session.createQuery(" from "+ CprList.class.getName() +" where listId = :listId", CprList.class);
                cprListQuery.setParameter("listId", cprList);

                if(cprListQuery.getResultList().isEmpty()) {
                    String errorMessage = "Subscription could not be found";
                    ObjectNode obj = objectMapper.createObjectNode();
                    obj.put("errorMessage", errorMessage);
                    log.error(errorMessage, errorMessage);
                    return new ResponseEntity(obj.toString(), HttpStatus.NOT_FOUND);
                } else {
                    cprListItem = (CprList) cprListQuery.getResultList().get(0);
                }
            }
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ Subscriber.class.getName() +" where subscriberId = :subscriberId", Subscriber.class);

            DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
            LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
            loggerHelper.urlInvokePersistablelogs("businesseventSubscription");
            query.setParameter("subscriberId", Optional.ofNullable(request.getHeader("uxp-client")).orElse(user.getIdentity()).replaceAll("/","_"));

            Subscriber subscriber = (Subscriber) query.getResultList().get(0);
            BusinessEventSubscription subscription = new BusinessEventSubscription(businessEventId, kodeId);
            subscription.setCprList(cprListItem);
            subscription.setSubscriber(subscriber);
            subscriber.addBusinessEventSubscription(subscription);

            session.save(subscriber);
            transaction.commit();
            loggerHelper.urlInvokePersistablelogs("businesseventSubscription done");
            return ResponseEntity.ok(subscription);
        }  catch(PersistenceException e) {
            String errorMessage = "Subscription already exists";
            ObjectNode obj = objectMapper.createObjectNode();
            obj.put("errorMessage", errorMessage);
            log.error(errorMessage, e);
            return new ResponseEntity(obj.toString(), HttpStatus.CONFLICT);
        }  catch(Exception e) {
            String errorMessage = "Failed adding element";
            ObjectNode obj = objectMapper.createObjectNode();
            obj.put("errorMessage", errorMessage);
            log.error(errorMessage, e);
            return new ResponseEntity(obj.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/subscriber/subscription/businesseventSubscription/{businessEventId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity businessEventSubscribtionUpdate(HttpServletRequest request, @PathVariable("businessEventId") String businessEventId,
                                                          @RequestParam(value = "kodeId",required=false, defaultValue = "") String kodeId,
                                                          @RequestParam(value = "cprList",required=false, defaultValue = "") String cprList) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Query subscriptionQuery = session.createQuery(" from "+ BusinessEventSubscription.class.getName() +" where businessEventId = :businessEventId", BusinessEventSubscription.class);
            subscriptionQuery.setParameter("businessEventId", businessEventId);
            if(subscriptionQuery.getResultList().isEmpty()) {
                String errorMessage = "Subscription could not be found";
                ObjectNode obj = objectMapper.createObjectNode();
                obj.put("errorMessage", errorMessage);
                log.error(errorMessage, errorMessage);
                return new ResponseEntity(obj.toString(), HttpStatus.NOT_FOUND);
            } else {
                DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
                LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
                loggerHelper.urlInvokePersistablelogs("businesseventSubscription");
                Transaction transaction = session.beginTransaction();
                BusinessEventSubscription subscription = (BusinessEventSubscription) subscriptionQuery.getResultList().get(0);

                if(!subscription.getSubscriber().getSubscriberId().equals(Optional.ofNullable(request.getHeader("uxp-client")).orElse(user.getIdentity()).replaceAll("/","_"))) {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
                if(!"".equals(kodeId)) {
                    subscription.setKodeId(kodeId);
                }
                CprList cprListItem = null;
                if(!"".equals(cprList)) {
                    Query cprListQuery = session.createQuery(" from "+ CprList.class.getName() +" where listId = :listId", CprList.class);
                    cprListQuery.setParameter("listId", cprList);

                    if(cprListQuery.getResultList().isEmpty()) {
                        String errorMessage = "CprList are unknown";
                        ObjectNode obj = objectMapper.createObjectNode();
                        obj.put("errorMessage", errorMessage);
                        log.error(errorMessage, errorMessage);
                        return new ResponseEntity(obj.toString(), HttpStatus.NOT_FOUND);
                    } else {
                        cprListItem = (CprList) cprListQuery.getResultList().get(0);
                        subscription.setCprList(cprListItem);
                    }
                }
                session.update(subscription);
                transaction.commit();
                loggerHelper.urlInvokePersistablelogs("businesseventSubscription done");
                return ResponseEntity.ok(subscription);
            }
        }
    }

    @GetMapping("/subscriber/subscription/businesseventSubscription/{subscriptionId}")
    public ResponseEntity<BusinessEventSubscription> businessEventSubscribtiongetBySubscriberId(HttpServletRequest request, @PathVariable("subscriptionId") String subscriptionId) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Query query = session.createQuery(" from "+ Subscriber.class.getName() +" where subscriptionId = :subscriptionId", Subscriber.class);

            DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
            LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
            loggerHelper.urlInvokePersistablelogs("businesseventSubscription");
            query.setParameter("subscriptionId", subscriptionId);
            if(query.getResultList().size()==0) {
                String errorMessage = "Subscription could not be found";
                ObjectNode obj = objectMapper.createObjectNode();
                obj.put("errorMessage", errorMessage);
                log.error(errorMessage, errorMessage);
                return new ResponseEntity(obj.toString(), HttpStatus.NOT_FOUND);
            } else {
                BusinessEventSubscription subscriber = (BusinessEventSubscription) query.getResultList().get(0);
                loggerHelper.urlInvokePersistablelogs("businesseventSubscription done");
                return ResponseEntity.ok(subscriber);
            }
        }
    }

    @DeleteMapping("/subscriber/subscription/businesseventSubscription/{subscriptionId}")
    public ResponseEntity<BusinessEventSubscription> businessEventSubscribtiondeleteBySubscriberId(HttpServletRequest request, @PathVariable("subscriptionId") String subscriptionId) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ BusinessEventSubscription.class.getName() +" where businessEventId = :businessEventId", BusinessEventSubscription.class);
            query.setParameter("businessEventId", subscriptionId);
            if(query.getResultList().isEmpty()) {
                String errorMessage = "Subscription could not be found";
                ObjectNode obj = objectMapper.createObjectNode();
                obj.put("errorMessage", errorMessage);
                log.error(errorMessage, errorMessage);
                return new ResponseEntity(obj.toString(), HttpStatus.NOT_FOUND);
            } else {
                BusinessEventSubscription subscription = (BusinessEventSubscription) query.getResultList().get(0);
                DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
                LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
                loggerHelper.urlInvokePersistablelogs("dataeventSubscription");
                if(subscription.getSubscriber().getSubscriberId().equals(Optional.ofNullable(request.getHeader("uxp-client")).orElse(user.getIdentity()).replaceAll("/","_"))) {
                    subscription.getSubscriber().removeBusinessEventSubscription(subscription);
                    session.delete(subscription);
                    transaction.commit();
                    loggerHelper.urlInvokePersistablelogs("dataeventSubscription done");
                    return ResponseEntity.ok(subscription);
                } else {
                    transaction.rollback();
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            }
        } catch (Exception e) {
            log.error("Failed acessing webservice: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get a list of all dataEventSubscribtions
     * @return
     */
    @GetMapping("/subscriber/subscription/dataeventSubscription")
    public ResponseEntity<List<DataEventSubscription>> dataEventSubscribtionfindAll(HttpServletRequest request) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Query query = session.createQuery(" from "+ Subscriber.class.getName() +" where subscriberId = :subscriberId", Subscriber.class);
            DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
            LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
            loggerHelper.urlInvokePersistablelogs("dataeventSubscription");
            query.setParameter("subscriberId", Optional.ofNullable(request.getHeader("uxp-client")).orElse(user.getIdentity()).replaceAll("/","_"));
            Subscriber subscriber = (Subscriber) query.getResultList().get(0);
            List <DataEventSubscription> list = new ArrayList <DataEventSubscription>(subscriber.getDataEventSubscription());
            loggerHelper.urlInvokePersistablelogs("dataeventSubscription done");
            return ResponseEntity.ok(list);
        }
    }



    @RequestMapping(method = RequestMethod.POST, path = "/subscriber/subscription/dataeventSubscription/", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity dataEventSubscribtionCreate(HttpServletRequest request,
                                                      @RequestParam(value = "dataEventId",required=false, defaultValue = "") String dataEventId,
                                                      @RequestParam(value = "kodeId",required=false, defaultValue = "") String kodeId,
                                                      @RequestParam(value = "cprList",required=false, defaultValue = "") String cprList,
                                                      @RequestParam(value = "cvrList",required=false, defaultValue = "") String cvrList) throws IOException, AccessDeniedException, InvalidTokenException, InvalidCertificateException {

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
            LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
            loggerHelper.urlInvokePersistablelogs("dataeventSubscription");
            CprList cprListItem = null;
            if(!cprList.isEmpty()) {
                Query cprListQuery = session.createQuery(" from "+ CprList.class.getName() +" where listId = :listId", CprList.class);
                cprListQuery.setParameter("listId", cprList);

                if(cprListQuery.getResultList().isEmpty()) {
                    String errorMessage = "Subscription could not be found";
                    ObjectNode obj = objectMapper.createObjectNode();
                    obj.put("errorMessage", errorMessage);
                    log.error(errorMessage, errorMessage);
                    return new ResponseEntity(obj.toString(), HttpStatus.NOT_FOUND);
                } else {
                    cprListItem = (CprList) cprListQuery.getResultList().get(0);
                }
            }
            CvrList cvrListItem = null;
            if(!cvrList.isEmpty()) {
                Query cvrListQuery = session.createQuery(" from "+ CvrList.class.getName() +" where listId = :listId", CvrList.class);
                cvrListQuery.setParameter("listId", cvrList);

                if(cvrListQuery.getResultList().isEmpty()) {
                    String errorMessage = "CvrList are unknown";
                    ObjectNode obj = objectMapper.createObjectNode();
                    obj.put("errorMessage", errorMessage);
                    log.error(errorMessage, errorMessage);
                    return new ResponseEntity(obj.toString(), HttpStatus.NOT_FOUND);
                } else {
                    cvrListItem = (CvrList) cvrListQuery.getResultList().get(0);
                }
            }
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ Subscriber.class.getName() +" where subscriberId = :subscriberId", Subscriber.class);

            query.setParameter("subscriberId", Optional.ofNullable(request.getHeader("uxp-client")).orElse(user.getIdentity()).replaceAll("/","_"));

            Subscriber subscriber = (Subscriber) query.getResultList().get(0);
            DataEventSubscription subscription = new DataEventSubscription(dataEventId, kodeId);
            subscription.setCprList(cprListItem);
            subscription.setCvrList(cvrListItem);
            subscription.setSubscriber(subscriber);
            subscriber.addDataEventSubscription(subscription);

            session.update(subscriber);
            transaction.commit();
            loggerHelper.urlInvokePersistablelogs("dataeventSubscription done");
            return ResponseEntity.ok(subscription);
        }  catch(PersistenceException e) {
            String errorMessage = "Subscription already exists";
            ObjectNode obj = objectMapper.createObjectNode();
            obj.put("errorMessage", errorMessage);
            log.error(errorMessage, e);
            return new ResponseEntity(obj.toString(), HttpStatus.CONFLICT);
        }  catch(Exception e) {
            String errorMessage = "Failed adding element";
            ObjectNode obj = objectMapper.createObjectNode();
            obj.put("errorMessage", errorMessage);
            log.error(errorMessage, e);
            return new ResponseEntity(obj.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/subscriber/subscription/dataeventSubscription/{dataEventId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity dataEventSubscribtionUpdate(HttpServletRequest request, @PathVariable("dataEventId") String dataEventId,
                                                      @RequestParam(value = "kodeId",required=false, defaultValue = "") String kodeId,
                                                      @RequestParam(value = "cprList",required=false, defaultValue = "") String cprList,
                                                      @RequestParam(value = "cvrList",required=false, defaultValue = "") String cvrList) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            Query subscribtionQuery = session.createQuery(" from "+ DataEventSubscription.class.getName() +" where dataEventId = :dataEventId", DataEventSubscription.class);
            subscribtionQuery.setParameter("dataEventId", dataEventId);
            if(subscribtionQuery.getResultList().isEmpty()) {
                String errorMessage = "Subscription could not be found";
                ObjectNode obj = objectMapper.createObjectNode();
                obj.put("errorMessage", errorMessage);
                log.error(errorMessage, errorMessage);
                return new ResponseEntity(obj.toString(), HttpStatus.NOT_FOUND);
            } else {
                DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
                LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
                loggerHelper.urlInvokePersistablelogs("dataeventSubscription");
                DataEventSubscription subscribtion = (DataEventSubscription) subscribtionQuery.getResultList().get(0);
                if(!subscribtion.getSubscriber().getSubscriberId().equals(Optional.ofNullable(request.getHeader("uxp-client")).orElse(user.getIdentity()).replaceAll("/","_"))) {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
                if(!"".equals(kodeId)) {
                    subscribtion.setKodeId(kodeId);
                }
                CprList cprListItem = null;
                if(!"".equals(cprList)) {
                    Query cprListQuery = session.createQuery(" from "+ CprList.class.getName() +" where listId = :listId", CprList.class);
                    cprListQuery.setParameter("listId", cprList);

                    if(cprListQuery.getResultList().isEmpty()) {
                        String errorMessage = "CprList are unknown";
                        ObjectNode obj = objectMapper.createObjectNode();
                        obj.put("errorMessage", errorMessage);
                        log.error(errorMessage, errorMessage);
                        return new ResponseEntity(obj.toString(), HttpStatus.NOT_FOUND);
                    } else {
                        cprListItem = (CprList) cprListQuery.getResultList().get(0);
                        subscribtion.setCprList(cprListItem);
                    }
                }
                CvrList cvrListItem = null;
                if(!"".equals(cvrList)) {
                    Query cvrListQuery = session.createQuery(" from "+ CvrList.class.getName() +" where listId = :listId", CvrList.class);
                    cvrListQuery.setParameter("listId", cvrList);

                    if(cvrListQuery.getResultList().isEmpty()) {
                        String errorMessage = "CvrList are unknown";
                        ObjectNode obj = objectMapper.createObjectNode();
                        obj.put("errorMessage", errorMessage);
                        log.error(errorMessage, errorMessage);
                        return new ResponseEntity(obj.toString(), HttpStatus.NOT_FOUND);
                    } else {
                        cvrListItem = (CvrList) cvrListQuery.getResultList().get(0);
                        subscribtion.setCvrList(cvrListItem);
                    }
                }
                session.update(subscribtion);
                transaction.commit();
                loggerHelper.urlInvokePersistablelogs("dataeventSubscription done");
                return ResponseEntity.ok(subscribtion);
            }
        }
    }

    @GetMapping("/subscriber/subscription/dataeventSubscription/{subscribtionId}")
    public ResponseEntity<DataEventSubscription> dataEventSubscribtiongetBySubscriberId(HttpServletRequest request, @PathVariable("subscribtionId") String subscribtionId) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Query query = session.createQuery(" from "+ DataEventSubscription.class.getName() +" where subscribtionId = :subscribtionId", Subscriber.class);

            DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
            LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
            loggerHelper.urlInvokePersistablelogs("dataeventSubscription");
            query.setParameter("subscribtionId", subscribtionId);
            if(query.getResultList().size()==0) {
                String errorMessage = "Subscription could not be found";
                ObjectNode obj = objectMapper.createObjectNode();
                obj.put("errorMessage", errorMessage);
                log.error(errorMessage, errorMessage);
                return new ResponseEntity(obj.toString(), HttpStatus.NOT_FOUND);
            } else {
                DataEventSubscription subscriber = (DataEventSubscription) query.getResultList().get(0);
                loggerHelper.urlInvokePersistablelogs("dataeventSubscription done");
                return ResponseEntity.ok(subscriber);
            }
        }
    }

    @DeleteMapping("/subscriber/subscription/dataeventSubscription/{dataEventId}")
    public ResponseEntity<DataEventSubscription> dataEventSubscribtiondeleteBySubscriberId(HttpServletRequest request, @PathVariable("dataEventId") String dataEventId) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {

        try(Session session = sessionManager.getSessionFactory().openSession()) {

            Query query = session.createQuery(" from "+ DataEventSubscription.class.getName() +" where dataEventId = :dataEventId", DataEventSubscription.class);
            query.setParameter("dataEventId", dataEventId);
            if(query.getResultList().size()==0) {
                String errorMessage = "Subscription could not be found";
                ObjectNode obj = objectMapper.createObjectNode();
                obj.put("errorMessage", errorMessage);
                log.error(errorMessage, errorMessage);
                return new ResponseEntity(obj.toString(), HttpStatus.NOT_FOUND);
            } else {

                DataEventSubscription subscribtion = (DataEventSubscription) query.getResultList().get(0);
                Transaction transaction = session.beginTransaction();
                DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
                LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
                loggerHelper.urlInvokePersistablelogs("dataeventSubscription");
                if(subscribtion.getSubscriber().getSubscriberId().equals(Optional.ofNullable(request.getHeader("uxp-client")).orElse(user.getIdentity()).replaceAll("/","_"))) {
                    subscribtion.getSubscriber().removeDataEventSubscription(subscribtion);
                    session.delete(subscribtion);
                    transaction.commit();
                    loggerHelper.urlInvokePersistablelogs("dataeventSubscription done");
                    return ResponseEntity.ok(subscribtion);
                } else {
                    transaction.rollback();
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            }
        } catch (Exception e) {
            log.error("Failed acessing webservice: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}