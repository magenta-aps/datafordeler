package dk.magenta.datafordeler.subscription.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.MonitorService;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.exception.InvalidCertificateException;
import dk.magenta.datafordeler.core.exception.InvalidTokenException;
import dk.magenta.datafordeler.core.fapi.Envelope;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.subscription.data.subscriptionModel.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.query.Query;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.persistence.PersistenceException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/subscription/1/manager")
public class ManageCprList {

    @Autowired
    SessionManager sessionManager;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private DafoUserManager dafoUserManager;

    @Autowired
    protected MonitorService monitorService;

    private Logger log = LogManager.getLogger(ManageCprList.class.getCanonicalName());


    @PostConstruct
    public void init() {
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
    @RequestMapping(method = RequestMethod.POST, path = "/subscriber/cprList/", headers="Accept=application/json", consumes = MediaType.ALL_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity cprListCreate(HttpServletRequest request, @RequestParam(value = "cprList",required=false, defaultValue = "") String cprList) throws IOException, AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ Subscriber.class.getName() +" where subscriberId = :subscriberId", Subscriber.class);
            query.setParameter("subscriberId", Optional.ofNullable(request.getHeader("uxp-client")).orElse(user.getIdentity()).replaceAll("/","_"));
            if(query.getResultList().size()==0) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                Subscriber subscriber = (Subscriber) query.getResultList().get(0);
                CprList cprCreateList = new CprList(cprList, subscriber);
                session.save(cprCreateList);
                subscriber.addCvrList(cprCreateList);

                transaction.commit();
                return ResponseEntity.ok(cprCreateList);
            }
        }  catch(ConstraintViolationException e) {
            String errorMessage = "Elements already exists";
            ObjectNode obj = objectMapper.createObjectNode();
            obj.put("errorMessage", errorMessage);
            log.error(errorMessage, e);
            return new ResponseEntity(obj.toString(), HttpStatus.CONFLICT);
        }
    }

    /**
     * Get a list of all cprList
     * @return
     */
    @GetMapping("/subscriber/cprList")
    public ResponseEntity<List<CprList>> cprListfindAll(HttpServletRequest request) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        try(Session session = sessionManager.getSessionFactory().openSession()) {

            Query query = session.createQuery(" from "+ Subscriber.class.getName() +" where subscriberId = :subscriberId", Subscriber.class);
            query.setParameter("subscriberId", Optional.ofNullable(request.getHeader("uxp-client")).orElse(user.getIdentity()).replaceAll("/","_"));
            if(query.getResultList().size()==0) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                Subscriber subscriber = (Subscriber) query.getResultList().get(0);
                return ResponseEntity.ok(subscriber.getCprLists().stream().collect(Collectors.toList()));
            }
        }
    }


    @DeleteMapping("/subscriber/cprList/cpr/{listId}")
    public ResponseEntity cprListCprDelete(HttpServletRequest request, @PathVariable("listId") String listId, @RequestParam(value = "cpr",required=false, defaultValue = "") List<String> cprs) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ CprList.class.getName() +" where listId = :listId ", CprList.class);
            query.setParameter("listId", listId);
            CprList foundList = (CprList)query.getResultList().get(0);
            if(!foundList.getSubscriber().getSubscriberId().equals(Optional.ofNullable(request.getHeader("uxp-client")).orElse(user.getIdentity()).replaceAll("/","_"))) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            List<SubscribedCprNumber> subscribedList = foundList.getCpr().stream().filter(item -> cprs.contains(item.getCprNumber())).collect(Collectors.toList());
            for(SubscribedCprNumber subscribed : subscribedList) {
                session.delete(subscribed);
                foundList.getCpr().remove(subscribed);
            }
            transaction.commit();
            String errorMessage = "Elements were removed";
            JSONObject obj = new JSONObject();
            obj.put("message", errorMessage);
            return new ResponseEntity(obj.toString(), HttpStatus.OK);
        } catch (Exception e) {
            log.error("FAILED REMOVING ELEMENT", e);
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/subscriber/cprList/cpr/{listId}")
    public ResponseEntity cprListCprPut(HttpServletRequest request, @PathVariable("listId") String listId, @RequestParam(value = "cpr",required=false, defaultValue = "") List<String> cprs) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ CprList.class.getName() +" where listId = :listId ", CprList.class);
            query.setParameter("listId", listId);
            CprList foundList = (CprList)query.getResultList().get(0);
            if(!foundList.getSubscriber().getSubscriberId().equals(Optional.ofNullable(request.getHeader("uxp-client")).orElse(user.getIdentity()).replaceAll("/","_"))) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            for(String cpr : cprs) {
                foundList.addCprString(cpr);
            }
            transaction.commit();
            String errorMessage = "Elements were added";
            JSONObject obj = new JSONObject();
            obj.put("message", errorMessage);
            return new ResponseEntity(obj.toString(), HttpStatus.OK);
        } catch(PersistenceException e) {
            String errorMessage = "Elements allready exists";
            JSONObject obj = new JSONObject();
            obj.put("errorMessage", errorMessage);
            log.error(errorMessage, e);
            return new ResponseEntity(obj.toString(), HttpStatus.NOT_ACCEPTABLE);
        } catch (Exception e) {
            log.error("FAILED REMOVING ELEMENT", e);
            return ResponseEntity.status(500).build();
        }
    }


    /**
     * Get a list of all CPR-numbers in a list
     * @return
     */
    @GetMapping("/subscriber/cprList/cpr")
    public ResponseEntity<dk.magenta.datafordeler.core.fapi.Envelope> cprListCprfindAll(HttpServletRequest request, @RequestParam MultiValueMap<String, String> requestParams) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {

        String pageSize = requestParams.getFirst("pageSize");
        String page = requestParams.getFirst("page");
        String listId = requestParams.getFirst("listId");

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Query query = session.createQuery(" from "+ CprList.class.getName() +" where  listId = :listId", CprList.class);
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
            query.setParameter("listId", listId);
            CprList foundList = (CprList)query.getResultList().get(0);
            if(!foundList.getSubscriber().getSubscriberId().equals(Optional.ofNullable(request.getHeader("uxp-client")).orElse(user.getIdentity()).replaceAll("/","_"))) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            Envelope envelope = new Envelope();
            envelope.setPageSize(query.getMaxResults());
            envelope.setPage(query.getFirstResult()+1);
            envelope.setPath(request.getServletPath());
            envelope.setResponseTimestamp(OffsetDateTime.now());
            envelope.setResults(foundList.getCpr());

            return ResponseEntity.ok(envelope);
        } catch (Exception e) {
            log.error("FAILED REMOVING ELEMENT", e);
            return ResponseEntity.status(500).build();
        }
    }


    private static void setHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Content-Type", "application/json; charset=utf-8");
        response.setStatus(200);
    }

}