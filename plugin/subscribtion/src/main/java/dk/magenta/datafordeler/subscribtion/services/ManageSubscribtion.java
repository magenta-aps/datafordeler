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
@RequestMapping("/subscribtionplugin/v1/manager")
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
    public ResponseEntity<List<BusinessEventSubscribtion>> businessEventSubscribtionfindAll(HttpServletRequest request) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Query query = session.createQuery(" from "+ Subscriber.class.getName() +" where subscriberId = :subscriberId", Subscriber.class);
            DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
            query.setParameter("subscriberId", user.getIdentity());
            if(query.getResultList().size()==0) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                Subscriber subscriber = (Subscriber) query.getResultList().get(0);
                Iterator<BusinessEventSubscribtion> subscribtions = subscriber.getBusinessEventSubscribtion().iterator();
                List <BusinessEventSubscribtion> list = new ArrayList <BusinessEventSubscribtion>();
                while(subscribtions.hasNext()) {
                    list.add(subscribtions.next());
                }

                return ResponseEntity.ok(list);
            }
        }
    }

    @RequestMapping(method = RequestMethod.POST, path = "/subscriber/businessEventSubscribtion/create/", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity businessEventSubscribtioncreateSubscriber(HttpServletRequest request,
                                                                    @RequestParam(value = "businessEventId",required=false, defaultValue = "") String businessEventId,
                                                                    @RequestParam(value = "kodeId",required=false, defaultValue = "") String kodeId) throws IOException, AccessDeniedException, InvalidTokenException, InvalidCertificateException {

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ Subscriber.class.getName() +" where subscriberId = :subscriberId", Subscriber.class);

            DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
            query.setParameter("subscriberId", user.getIdentity());

            Subscriber subscriber = (Subscriber) query.getResultList().get(0);
            BusinessEventSubscribtion subscribtion = new BusinessEventSubscribtion(businessEventId, kodeId);
            subscriber.addBusinessEventSubscribtion(subscribtion);

            session.update(subscriber);
            transaction.commit();
            return ResponseEntity.ok(subscriber);
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
    public ResponseEntity<BusinessEventSubscribtion> businessEventSubscribtiondeleteBySubscriberId(HttpServletRequest request, @PathVariable("subscribtionId") String subscribtionId) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ BusinessEventSubscribtion.class.getName() +" where businessEventId = :businessEventId", BusinessEventSubscribtion.class);
            query.setParameter("businessEventId", subscribtionId);
            if(query.getResultList().size()==0) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                BusinessEventSubscribtion subscribtion = (BusinessEventSubscribtion) query.getResultList().get(0);
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
    public ResponseEntity<List<DataEventSubscribtion>> dataEventSubscribtionfindAll(HttpServletRequest request) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Query query = session.createQuery(" from "+ Subscriber.class.getName() +" where subscriberId = :subscriberId", Subscriber.class);
            DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
            query.setParameter("subscriberId", user.getIdentity());
            if(query.getResultList().size()==0) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                Subscriber subscriber = (Subscriber) query.getResultList().get(0);
                Iterator<DataEventSubscribtion> subscribtions = subscriber.getDataEventSubscribtion().iterator();
                List <DataEventSubscribtion> list = new ArrayList <DataEventSubscribtion>();
                while(subscribtions.hasNext()) {
                    list.add(subscribtions.next());
                }
                return ResponseEntity.ok(list);
            }
        }
    }



    @RequestMapping(method = RequestMethod.POST, path = "/subscriber/dataEventSubscribtion/create/", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity dataEventSubscribtioncreateSubscriber(HttpServletRequest request,
                                                                @RequestParam(value = "businessEventId",required=false, defaultValue = "") String businessEventId) throws IOException, AccessDeniedException, InvalidTokenException, InvalidCertificateException {

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ Subscriber.class.getName() +" where subscriberId = :subscriberId", Subscriber.class);

            DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
            query.setParameter("subscriberId", user.getIdentity());

            Subscriber subscriber = (Subscriber) query.getResultList().get(0);
            DataEventSubscribtion subscribtion = new DataEventSubscribtion(businessEventId, "");
            subscriber.addDataEventSubscribtion(subscribtion);

            transaction.commit();
            return ResponseEntity.ok(subscriber);
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
    public ResponseEntity<DataEventSubscribtion> dataEventSubscribtiondeleteBySubscriberId(HttpServletRequest request, @PathVariable("subscribtionId") String subscribtionId) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ DataEventSubscribtion.class.getName() +" where dataEventId = :dataEventId", DataEventSubscribtion.class);
            query.setParameter("dataEventId", subscribtionId);
            if(query.getResultList().size()==0) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                DataEventSubscribtion subscribtion = (DataEventSubscribtion) query.getResultList().get(0);
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