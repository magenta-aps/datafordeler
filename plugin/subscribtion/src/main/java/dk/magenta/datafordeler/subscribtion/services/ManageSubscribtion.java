package dk.magenta.datafordeler.subscribtion.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.MonitorService;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.fapi.Envelope;
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
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.time.OffsetDateTime;
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
    public ResponseEntity businessEventSubscribtioncreateSubscriber(HttpServletRequest request, @Valid @RequestBody String subscriberContent) throws IOException, AccessDeniedException, InvalidTokenException, InvalidCertificateException {

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ Subscriber.class.getName() +" where subscriberId = :subscriberId", Subscriber.class);

            DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
            query.setParameter("subscriberId", user.getIdentity());

            Subscriber subscriber = (Subscriber) query.getResultList().get(0);
            BusinessEventSubscribtion subscribtion = new BusinessEventSubscribtion(subscriberContent);
            subscriber.addBusinessEventSubscribtion(subscribtion);

            session.update(subscriber);
            transaction.commit();
            return ResponseEntity.ok(subscriber);
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
    public ResponseEntity dataEventSubscribtioncreateSubscriber(HttpServletRequest request, @Valid @RequestBody String subscriberContent) throws IOException, AccessDeniedException, InvalidTokenException, InvalidCertificateException {

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ Subscriber.class.getName() +" where subscriberId = :subscriberId", Subscriber.class);

            DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
            query.setParameter("subscriberId", user.getIdentity());

            Subscriber subscriber = (Subscriber) query.getResultList().get(0);
            DataEventSubscribtion subscribtion = new DataEventSubscribtion(subscriberContent);
            subscriber.addDataEventSubscribtion(subscribtion);

            transaction.commit();
            return ResponseEntity.ok(subscriber);
        }
    }


    @GetMapping("/subscriber/businessEventSubscribtion/{subscriberId}")
    public ResponseEntity<Subscriber> businessEventSubscribtiongetBySubscriberId(@PathVariable("subscriberId") String subscriberId, HttpServletRequest request) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Query query = session.createQuery(" from "+ Subscriber.class.getName() +" where subscriberId = :subscriberId", Subscriber.class);
            //query.setParameter("subscriberId", subscriberId);

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

    @DeleteMapping("/subscriber/businessEventSubscribtion/delete/{subscriberId}")
    public ResponseEntity<Subscriber> businessEventSubscribtiondeleteBySubscriberId(@PathVariable("subscriberId") String subscriberId) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ Subscriber.class.getName() +" where subscriberId = :subscriberId", Subscriber.class);


            //DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
           // query.setParameter("subscriberId", user.getIdentity());
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
     * Create a cprList
     * @param request
     * @param cprList
     * @return
     * @throws IOException
     * @throws AccessDeniedException
     * @throws InvalidTokenException
     * @throws InvalidCertificateException
     */
    @RequestMapping(method = RequestMethod.POST, path = "/subscriber/cprList/create/", headers="Accept=application/json", consumes = MediaType.ALL_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity cprListCreate(HttpServletRequest request, @RequestBody String cprList) throws IOException, AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        CprList cprCreateList = new CprList(cprList, user.getIdentity());
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.save(cprCreateList);
            transaction.commit();
            return ResponseEntity.ok(cprCreateList);
        }
    }

    /**
     * Get a list of all cprList
     * @return
     */
    @GetMapping("/subscriber/cprList/list")
    public ResponseEntity<List<CprList>> cprListfindAll(HttpServletRequest request) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Query query = session.createQuery(" from "+ CprList.class.getName() +" where subscriberId = :subscriberId", CprList.class);
            DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
            query.setParameter("subscriberId", user.getIdentity());
            return ResponseEntity.ok(query.getResultList());
        }
    }


    @RequestMapping(method = RequestMethod.POST, path = "/subscriber/cprList/cpr/add/", headers="Accept=application/json", consumes = MediaType.ALL_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE})
    public void cprListCprCreate(HttpServletRequest request, @RequestBody StringValuesDto cprNo) throws IOException, AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ CprList.class.getName() +" where subscriberId = :subscriberId and listId = :listId ", CprList.class);
            query.setParameter("subscriberId", user.getIdentity());
            query.setParameter("listId", cprNo.getKey());
            CprList foundList = (CprList)query.getResultList().get(0);
            foundList.addCprs(cprNo.getValues());
            transaction.commit();
            //return (ResponseEntity) ResponseEntity.ok();
        }
    }

    /**
     * Get a list of all CPR-numbers in a list
     * @return
     */
    @GetMapping("/subscriber/cprList/cpr/list")
    public ResponseEntity<dk.magenta.datafordeler.core.fapi.Envelope> cprListCprfindAll(HttpServletRequest request, @RequestParam MultiValueMap<String, String> requestParams) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {

        String pageSize = requestParams.getFirst("pageSize");
        String page = requestParams.getFirst("page");

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Query query = session.createQuery(" from "+ CprList.class.getName() +" where subscriberId = :subscriberId and  listId = :listId", CprList.class);
            if(pageSize != null) {
                query.setMaxResults(Integer.valueOf(pageSize));
            } else {
                query.setMaxResults(10);
            }
            if(page != null) {
                int pageIndex = (Integer.valueOf(page)-1)*query.getMaxResults();
                query.setFirstResult(pageIndex);
            } else {
                query.setFirstResult(0);
            }

            DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
            query.setParameter("subscriberId", user.getIdentity());
            query.setParameter("listId", "cprTestList1");
            CprList foundList = (CprList)query.getResultList().get(0);

            Envelope envelope = new Envelope();
            envelope.setPageSize(query.getMaxResults());
            envelope.setPage(query.getFirstResult()+1);
            envelope.setPath(request.getServletPath());
            envelope.setResponseTimestamp(OffsetDateTime.now());
            envelope.setResults(foundList.getCpr());

            return ResponseEntity.ok(envelope);
        }
    }


    private static void setHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Content-Type", "application/json; charset=utf-8");
        response.setStatus(200);
    }

}