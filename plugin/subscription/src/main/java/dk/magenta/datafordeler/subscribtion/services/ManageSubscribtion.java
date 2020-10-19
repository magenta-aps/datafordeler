package dk.magenta.datafordeler.subscribtion.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.MonitorService;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.exception.InvalidCertificateException;
import dk.magenta.datafordeler.core.exception.InvalidTokenException;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.subscribtion.data.subscribtionModel.*;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.*;


@RestController
@RequestMapping("/subscriptionplugin/v1/manager")
public class ManageSubscribtion {

    @Autowired
    SessionManager sessionManager;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private DafoUserManager dafoUserManager;

    @Autowired
    protected MonitorService monitorService;

    private Logger log = LogManager.getLogger(ManageSubscribtion.class.getCanonicalName());


    @PostConstruct
    public void init() {
    }


    /**
     * Get a list of all subscribtions
     * @return
     */
    @GetMapping("/subscriber/list")
    //@RequestMapping(method = RequestMethod.GET, path = "/{cprNummer}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Subscriber>> findAll(HttpServletRequest request) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            List<Subscriber> subscribtionList = QueryManager.getAllItems(session, Subscriber.class);
            return ResponseEntity.ok(subscribtionList);
        }
    }

    @RequestMapping(method = RequestMethod.POST, path = "/subscriber/create/", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity createSubscriber(HttpServletRequest request, @Valid @RequestBody String subscriberContent) throws IOException {

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Subscriber subscriber = objectMapper.readValue(subscriberContent, Subscriber.class);
            session.save(subscriber);
            transaction.commit();
            return ResponseEntity.ok(subscriber);
        }
    }


    @RequestMapping(method = RequestMethod.POST, path = "/subscriber/createMy/", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity createMySubscriber(HttpServletRequest request) throws IOException, AccessDeniedException, InvalidTokenException, InvalidCertificateException {

        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Subscriber subscriber = new Subscriber(user.getIdentity());
            session.save(subscriber);
            transaction.commit();
            return ResponseEntity.ok(subscriber);
        }
    }

    @GetMapping("/subscriber/{subscriberId}")
    public ResponseEntity<Subscriber> getBySubscriberId(@PathVariable("subscriberId") String subscriberId) {
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Query query = session.createQuery(" from "+ Subscriber.class.getName() +" where subscriberId = :subscriberId", Subscriber.class);
            query.setParameter("subscriberId", subscriberId);
            if(query.getResultList().size()==0) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                Subscriber subscriber = (Subscriber) query.getResultList().get(0);
                return ResponseEntity.ok(subscriber);
            }
        }
    }

    @DeleteMapping("/subscriber/delete/{subscriberId}")
    public ResponseEntity<Subscriber> deleteBySubscriberId(@PathVariable("subscriberId") String subscriberId) {
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ Subscriber.class.getName() +" where subscriberId = :subscriberId", Subscriber.class);
            query.setParameter("subscriberId", subscriberId);
            if(query.getResultList().size()==0) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                Subscriber subscriber = (Subscriber) query.getResultList().get(0);
                session.delete(subscriber);
                transaction.commit();
                return ResponseEntity.ok(subscriber);
            }
        }
    }



    /**
     * Get a list of all businessEventSubscribtions
     * @return
     */
    @GetMapping("/subscriber/businessEventSubscribtion/list")
    public ResponseEntity<List<BusinessEventSubscription>> businessEventSubscribtionfindAll(HttpServletRequest request) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Query query = session.createQuery(" from "+ Subscriber.class.getName() +" where subscriberId = :subscriberId", Subscriber.class);
            DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
            query.setParameter("subscriberId", user.getIdentity());
            if(query.getResultList().size()==0) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                Subscriber subscriber = (Subscriber) query.getResultList().get(0);
                Iterator<BusinessEventSubscription> subscribtions = subscriber.getBusinessEventSubscription().iterator();
                List <BusinessEventSubscription> list = new ArrayList <BusinessEventSubscription>();
                while(subscribtions.hasNext()) {
                    list.add(subscribtions.next());
                }

                return ResponseEntity.ok(list);
            }
        }
    }

    @RequestMapping(method = RequestMethod.POST, path = "/subscriber/businessEventSubscribtion/", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity businessEventSubscribtionCreate(HttpServletRequest request,
                                                                    @RequestParam(value = "businessEventId",required=false, defaultValue = "") String businessEventId,
                                                                    @RequestParam(value = "kodeId",required=false, defaultValue = "") String kodeId,
                                                                    @RequestParam(value = "cprList",required=false, defaultValue = "") String cprList) throws IOException, AccessDeniedException, InvalidTokenException, InvalidCertificateException {

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            CprList cprListItem = null;
            if(!"".equals(cprList)) {
                Query cprListQuery = session.createQuery(" from "+ CprList.class.getName() +" where listId = :listId", CprList.class);
                cprListQuery.setParameter("listId", cprList);

                if(cprListQuery.getResultList().isEmpty()) {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                } else {
                    cprListItem = (CprList) cprListQuery.getResultList().get(0);
                }
            }

            Query query = session.createQuery(" from "+ Subscriber.class.getName() +" where subscriberId = :subscriberId", Subscriber.class);

            DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
            query.setParameter("subscriberId", user.getIdentity());

            Subscriber subscriber = (Subscriber) query.getResultList().get(0);
            BusinessEventSubscription subscribtion = new BusinessEventSubscription(businessEventId, kodeId);
            subscribtion.setCprList(cprListItem);
            subscriber.addBusinessEventSubscribtion(subscribtion);

            session.update(subscriber);
            transaction.commit();
            return ResponseEntity.ok(subscriber);
        }
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/subscriber/businessEventSubscribtion/", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity businessEventSubscribtionUpdate(HttpServletRequest request,
                                                                    @RequestParam(value = "businessEventId",required=false, defaultValue = "") String businessEventId,
                                                                    @RequestParam(value = "kodeId",required=false, defaultValue = "") String kodeId,
                                                                    @RequestParam(value = "cprList",required=false, defaultValue = "") String cprList) throws IOException, AccessDeniedException, InvalidTokenException, InvalidCertificateException {

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            Query subscribtionQuery = session.createQuery(" from "+ BusinessEventSubscription.class.getName() +" where businessEventId = :businessEventId", BusinessEventSubscription.class);
            subscribtionQuery.setParameter("businessEventId", businessEventId);
            if(subscribtionQuery.getResultList().isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                BusinessEventSubscription subscribtion = (BusinessEventSubscription) subscribtionQuery.getResultList().get(0);
                subscribtion.setKodeId(kodeId);
                CprList cprListItem = null;
                if(!"".equals(cprList)) {
                    Query cprListQuery = session.createQuery(" from "+ CprList.class.getName() +" where listId = :listId", CprList.class);
                    cprListQuery.setParameter("listId", cprList);

                    if(cprListQuery.getResultList().isEmpty()) {
                        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                    } else {
                        cprListItem = (CprList) cprListQuery.getResultList().get(0);
                        subscribtion.setCprList(cprListItem);
                    }
                }
                session.update(subscribtion);
                transaction.commit();
                return ResponseEntity.ok(subscribtion);
            }
        }
    }

    @GetMapping("/subscriber/businessEventSubscribtion/{subscriberId}")
    public ResponseEntity<Subscriber> businessEventSubscribtiongetBySubscriberId(HttpServletRequest request) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Query query = session.createQuery(" from "+ Subscriber.class.getName() +" where subscriberId = :subscriberId", Subscriber.class);

            DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
            query.setParameter("subscriberId", user.getIdentity());
            if(query.getResultList().size()==0) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                Subscriber subscriber = (Subscriber) query.getResultList().get(0);
                return ResponseEntity.ok(subscriber);
            }
        }
    }

    @DeleteMapping("/subscriber/businessEventSubscribtion/delete/{subscribtionId}")
    public ResponseEntity<BusinessEventSubscription> businessEventSubscribtiondeleteBySubscriberId(HttpServletRequest request, @PathVariable("subscribtionId") String subscribtionId) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ BusinessEventSubscription.class.getName() +" where businessEventId = :businessEventId", BusinessEventSubscription.class);
            query.setParameter("businessEventId", subscribtionId);
            if(query.getResultList().size()==0) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                BusinessEventSubscription subscribtion = (BusinessEventSubscription) query.getResultList().get(0);
                //TODO: validate ownership
                session.delete(subscribtion);
                transaction.commit();
                return ResponseEntity.ok(subscribtion);
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
    @GetMapping("/subscriber/dataEventSubscribtion/list")
    public ResponseEntity<List<DataEventSubscription>> dataEventSubscribtionfindAll(HttpServletRequest request) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Query query = session.createQuery(" from "+ Subscriber.class.getName() +" where subscriberId = :subscriberId", Subscriber.class);
            DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
            query.setParameter("subscriberId", user.getIdentity());
            if(query.getResultList().size()==0) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                Subscriber subscriber = (Subscriber) query.getResultList().get(0);
                Iterator<DataEventSubscription> subscribtions = subscriber.getDataEventSubscription().iterator();
                List <DataEventSubscription> list = new ArrayList <DataEventSubscription>();
                while(subscribtions.hasNext()) {
                    list.add(subscribtions.next());
                }
                return ResponseEntity.ok(list);
            }
        }
    }



    @RequestMapping(method = RequestMethod.POST, path = "/subscriber/dataEventSubscribtion/", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity dataEventSubscribtionCreate(HttpServletRequest request,
                                                                @RequestParam(value = "dataEventId",required=false, defaultValue = "") String dataEventId,
                                                                @RequestParam(value = "kodeId",required=false, defaultValue = "") String kodeId,
                                                                @RequestParam(value = "cprList",required=false, defaultValue = "") String cprList,
                                                                @RequestParam(value = "cvrList",required=false, defaultValue = "") String cvrList) throws IOException, AccessDeniedException, InvalidTokenException, InvalidCertificateException {

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            CprList cprListItem = null;
            if(!"".equals(cprList)) {
                Query cprListQuery = session.createQuery(" from "+ CprList.class.getName() +" where listId = :listId", CprList.class);
                cprListQuery.setParameter("listId", cprList);

                if(cprListQuery.getResultList().isEmpty()) {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                } else {
                    cprListItem = (CprList) cprListQuery.getResultList().get(0);
                }
            }
            CvrList cvrListItem = null;
            if(!"".equals(cvrList)) {
                Query cvrListQuery = session.createQuery(" from "+ CvrList.class.getName() +" where listId = :listId", CvrList.class);
                cvrListQuery.setParameter("listId", cvrList);

                if(cvrListQuery.getResultList().isEmpty()) {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                } else {
                    cvrListItem = (CvrList) cvrListQuery.getResultList().get(0);
                }
            }

            Query query = session.createQuery(" from "+ Subscriber.class.getName() +" where subscriberId = :subscriberId", Subscriber.class);

            DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
            query.setParameter("subscriberId", user.getIdentity());

            Subscriber subscriber = (Subscriber) query.getResultList().get(0);
            DataEventSubscription subscribtion = new DataEventSubscription(dataEventId, kodeId);
            subscribtion.setCprList(cprListItem);
            subscribtion.setCvrList(cvrListItem);
            subscriber.addDataEventSubscribtion(subscribtion);

            session.update(subscriber);
            transaction.commit();
            return ResponseEntity.ok(subscriber);
        }
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/subscriber/dataEventSubscribtion/", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity dataEventSubscribtionUpdate(HttpServletRequest request,
                                                      @RequestParam(value = "dataEventId",required=false, defaultValue = "") String dataEventId,
                                                      @RequestParam(value = "kodeId",required=false, defaultValue = "") String kodeId,
                                                      @RequestParam(value = "cprList",required=false, defaultValue = "") String cprList,
                                                      @RequestParam(value = "cvrList",required=false, defaultValue = "") String cvrList) throws IOException, AccessDeniedException, InvalidTokenException, InvalidCertificateException {

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            Query subscribtionQuery = session.createQuery(" from "+ DataEventSubscription.class.getName() +" where dataEventId = :dataEventId", DataEventSubscription.class);
            subscribtionQuery.setParameter("dataEventId", dataEventId);
            if(subscribtionQuery.getResultList().isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                DataEventSubscription subscribtion = (DataEventSubscription) subscribtionQuery.getResultList().get(0);
                subscribtion.setKodeId(kodeId);
                CprList cprListItem = null;
                if(!"".equals(cprList)) {
                    Query cprListQuery = session.createQuery(" from "+ CprList.class.getName() +" where listId = :listId", CprList.class);
                    cprListQuery.setParameter("listId", cprList);

                    if(cprListQuery.getResultList().isEmpty()) {
                        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
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
                        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                    } else {
                        cvrListItem = (CvrList) cvrListQuery.getResultList().get(0);
                        subscribtion.setCvrList(cvrListItem);
                    }
                }
                session.update(subscribtion);
                transaction.commit();
                return ResponseEntity.ok(subscribtion);
            }
        }
    }

    @GetMapping("/subscriber/dataEventSubscribtion/{subscribtionId}")
    public ResponseEntity<Subscriber> dataEventSubscribtiongetBySubscriberId(HttpServletRequest request) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Query query = session.createQuery(" from "+ Subscriber.class.getName() +" where subscribtionId = :subscribtionId", Subscriber.class);
            //query.setParameter("subscriberId", subscriberId);

            DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
            query.setParameter("subscribtionId", user.getIdentity());
            if(query.getResultList().size()==0) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                Subscriber subscriber = (Subscriber) query.getResultList().get(0);
                return ResponseEntity.ok(subscriber);
            }
        }
    }

    @DeleteMapping("/subscriber/dataEventSubscribtion/delete/{subscribtionId}")
    public ResponseEntity<DataEventSubscription> dataEventSubscribtiondeleteBySubscriberId(HttpServletRequest request, @PathVariable("subscribtionId") String subscribtionId) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ DataEventSubscription.class.getName() +" where dataEventId = :dataEventId", DataEventSubscription.class);
            query.setParameter("dataEventId", subscribtionId);
            if(query.getResultList().size()==0) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                DataEventSubscription subscribtion = (DataEventSubscription) query.getResultList().get(0);
                //TODO: validate ownership
                session.delete(subscribtion);
                transaction.commit();
                return ResponseEntity.ok(subscribtion);
            }
        } catch (Exception e) {
            log.error("Failed acessing webservice: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    private static void setHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Content-Type", "application/json; charset=utf-8");
        response.setStatus(200);
    }

}