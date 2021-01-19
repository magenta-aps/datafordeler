package dk.magenta.datafordeler.subscription.services;

import com.fasterxml.jackson.databind.JsonNode;
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
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/subscription/1/manager")
public class ManageCvrList {

    @Autowired
    SessionManager sessionManager;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private DafoUserManager dafoUserManager;

    @Autowired
    protected MonitorService monitorService;

    private Logger log = LogManager.getLogger(ManageCvrList.class.getCanonicalName());


    @PostConstruct
    public void init() {
    }


    /**
     * Create a cprList
     * @param request
     * @param cvrList
     * @return
     * @throws IOException
     * @throws AccessDeniedException
     * @throws InvalidTokenException
     * @throws InvalidCertificateException
     */
    @RequestMapping(method = RequestMethod.POST, path = "/subscriber/cvrList/", headers="Accept=application/json", consumes = MediaType.ALL_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity cvrListCreate(HttpServletRequest request, @RequestParam(value = "cvrList",required=false, defaultValue = "") String cvrList) throws IOException, AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ Subscriber.class.getName() +" where subscriberId = :subscriberId", Subscriber.class);
            query.setParameter("subscriberId", Optional.ofNullable(request.getHeader("uxp-client")).orElse(user.getIdentity()).replaceAll("/","_"));
            if(query.getResultList().size()==0) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                Subscriber subscriber = (Subscriber) query.getResultList().get(0);
                CvrList cvrCreateList = new CvrList(cvrList, subscriber);
                session.save(cvrCreateList);
                subscriber.addCvrList(cvrCreateList);

                transaction.commit();
                return ResponseEntity.ok(cvrCreateList);
            }
        }  catch(PersistenceException e) {
            String errorMessage = "cvrList already exists";
            ObjectNode obj = objectMapper.createObjectNode();
            obj.put("errorMessage", errorMessage);
            log.warn(errorMessage, e);
            return new ResponseEntity(obj.toString(), HttpStatus.CONFLICT);
        }  catch(Exception e) {
            String errorMessage = "Failed creating list";
            ObjectNode obj = objectMapper.createObjectNode();
            obj.put("errorMessage", errorMessage);
            log.error(errorMessage, e);
            return new ResponseEntity(obj.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get a list of all cprList
     * @return
     */
    @GetMapping("/subscriber/cvrList")
    public ResponseEntity<List<CvrList>> cvrListfindAll(HttpServletRequest request) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        try(Session session = sessionManager.getSessionFactory().openSession()) {

            Query query = session.createQuery(" from "+ Subscriber.class.getName() +" where subscriberId = :subscriberId", Subscriber.class);
            query.setParameter("subscriberId", Optional.ofNullable(request.getHeader("uxp-client")).orElse(user.getIdentity()).replaceAll("/","_"));
            if(query.getResultList().size()==0) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                Subscriber subscriber = (Subscriber) query.getResultList().get(0);
                return ResponseEntity.ok(subscriber.getCvrLists().stream().collect(Collectors.toList()));
            }
        }
    }


    @DeleteMapping("/subscriber/cvrList/cvr/{listId}")
    public ResponseEntity cvrListCprDelete(HttpServletRequest request, @PathVariable("listId") String listId, @RequestParam(value = "cvr",required=false, defaultValue = "") List<String> cvrs) throws IOException, AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ CvrList.class.getName() +" where listId = :listId ", CvrList.class);
            query.setParameter("listId", listId);
            CvrList foundList = (CvrList)query.getResultList().get(0);
            if(!foundList.getSubscriber().getSubscriberId().equals(Optional.ofNullable(request.getHeader("uxp-client")).orElse(user.getIdentity()).replaceAll("/","_"))) {
                String errorMessage = "No access to this list";
                ObjectNode obj = this.objectMapper.createObjectNode();
                obj.put("errorMessage", errorMessage);
                log.warn(errorMessage);
                return new ResponseEntity(obj.toString(), HttpStatus.FORBIDDEN);
            }
            List<SubscribedCvrNumber> subscribedList = foundList.getCvr().stream().filter(item -> cvrs.contains(item.getCvrNumber())).collect(Collectors.toList());
            for(SubscribedCvrNumber subscribed : subscribedList) {
                session.delete(subscribed);
                foundList.getCvr().remove(subscribed);
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

    @PostMapping("/subscriber/cvrList/cvr/{listId}")
    public ResponseEntity cvrListCprPut(HttpServletRequest request, @PathVariable("listId") String listId) throws IOException, AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ CvrList.class.getName() +" where listId = :listId ", CvrList.class);
            query.setParameter("listId", listId);
            CvrList foundList = (CvrList)query.getResultList().get(0);
            if(!foundList.getSubscriber().getSubscriberId().equals(Optional.ofNullable(request.getHeader("uxp-client")).orElse(user.getIdentity()).replaceAll("/","_"))) {
                String errorMessage = "No access to this list";
                ObjectNode obj = this.objectMapper.createObjectNode();
                obj.put("errorMessage", errorMessage);
                log.warn(errorMessage);
                return new ResponseEntity(obj.toString(), HttpStatus.FORBIDDEN);
            }
            JsonNode requestBody = objectMapper.readTree(request.getInputStream());
            Iterator<JsonNode> cprBodyIterator = requestBody.get("cvr").iterator();
            while(cprBodyIterator.hasNext()) {
                JsonNode node = cprBodyIterator.next();
                foundList.addCvrString(node.textValue());
            }
            transaction.commit();
            String errorMessage = "Elements were added";
            ObjectNode obj = this.objectMapper.createObjectNode();
            obj.put("message", errorMessage);
            return new ResponseEntity(obj.toString(), HttpStatus.OK);
        } catch(PersistenceException e) {
            String errorMessage = "Elements allready exists";
            ObjectNode obj = this.objectMapper.createObjectNode();
            obj.put("errorMessage", errorMessage);
            log.warn(errorMessage, e);
            return new ResponseEntity(obj.toString(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            log.error("FAILED REMOVING ELEMENT", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Get a list of all CPR-numbers in a list
     * @return
     */
    @GetMapping("/subscriber/cvrList/cvr")
    public ResponseEntity<Envelope> cvrListCprfindAll(HttpServletRequest request, @RequestParam MultiValueMap<String, String> requestParams) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {

        String pageSize = requestParams.getFirst("pageSize");
        String page = requestParams.getFirst("page");
        String listId = requestParams.getFirst("listId");

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Query query = session.createQuery(" from "+ CvrList.class.getName() +" where listId = :listId", CvrList.class);
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
            CvrList foundList = (CvrList)query.getResultList().get(0);
            if(!foundList.getSubscriber().getSubscriberId().equals(Optional.ofNullable(request.getHeader("uxp-client")).orElse(user.getIdentity()).replaceAll("/","_"))) {
                String errorMessage = "No access to this list";
                ObjectNode obj = this.objectMapper.createObjectNode();
                obj.put("errorMessage", errorMessage);
                log.warn(errorMessage);
                return new ResponseEntity(obj.toString(), HttpStatus.FORBIDDEN);
            }

            Envelope envelope = new Envelope();
            envelope.setPageSize(query.getMaxResults());
            envelope.setPage(query.getFirstResult()+1);
            envelope.setPath(request.getServletPath());
            envelope.setResponseTimestamp(OffsetDateTime.now());
            envelope.setResults(foundList.getCvr());

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