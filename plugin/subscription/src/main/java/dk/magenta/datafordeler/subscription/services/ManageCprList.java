package dk.magenta.datafordeler.subscription.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.MonitorService;
import dk.magenta.datafordeler.core.database.setup.SessionManager;
import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.exception.InvalidCertificateException;
import dk.magenta.datafordeler.core.exception.InvalidTokenException;
import dk.magenta.datafordeler.core.fapi.Envelope;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.subscription.data.subscriptionModel.CprList;
import dk.magenta.datafordeler.subscription.data.subscriptionModel.SubscribedCprNumber;
import dk.magenta.datafordeler.subscription.data.subscriptionModel.Subscriber;
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
import javax.persistence.PersistenceException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Iterator;
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

    private final Logger log = LogManager.getLogger(ManageCprList.class.getCanonicalName());


    @PostConstruct
    public void init() {
    }


    /**
     * Create a cprList
     *
     * @param request
     * @param cprList
     * @return
     * @throws IOException
     * @throws AccessDeniedException
     * @throws InvalidTokenException
     * @throws InvalidCertificateException
     */
    @RequestMapping(method = RequestMethod.POST, path = "/subscriber/cprList/", headers = "Accept=application/json", consumes = MediaType.ALL_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity cprListCreate(HttpServletRequest request, @RequestParam(value = "cprList", required = false, defaultValue = "") String cprList) throws IOException, AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from " + Subscriber.class.getName() + " where subscriberId = :subscriberId", Subscriber.class);
            query.setParameter("subscriberId", Optional.ofNullable(request.getHeader("uxp-client")).orElse(user.getIdentity()).replaceAll("/", "_"));
            if (query.getResultList().size() == 0) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                Subscriber subscriber = (Subscriber) query.getResultList().get(0);
                CprList cprCreateList = new CprList(cprList, subscriber);
                session.save(cprCreateList);
                subscriber.addCprList(cprCreateList);

                transaction.commit();
                return ResponseEntity.ok(cprCreateList);
            }
        } catch (PersistenceException e) {
            String errorMessage = "cprList already exists";
            ObjectNode obj = objectMapper.createObjectNode();
            obj.put("errorMessage", errorMessage);
            log.warn(errorMessage, e);
            return new ResponseEntity(obj.toString(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            String errorMessage = "Failed creating list";
            ObjectNode obj = objectMapper.createObjectNode();
            obj.put("errorMessage", errorMessage);
            log.error(errorMessage, e);
            return new ResponseEntity(obj.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get a list of all cprList
     *
     * @return
     */
    @GetMapping("/subscriber/cprList")
    public ResponseEntity<List<CprList>> cprListfindAll(HttpServletRequest request) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        try (Session session = sessionManager.getSessionFactory().openSession()) {

            Query query = session.createQuery(" from " + Subscriber.class.getName() + " where subscriberId = :subscriberId", Subscriber.class);
            query.setParameter("subscriberId", Optional.ofNullable(request.getHeader("uxp-client")).orElse(user.getIdentity()).replaceAll("/", "_"));
            if (query.getResultList().size() == 0) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                Subscriber subscriber = (Subscriber) query.getResultList().get(0);
                return ResponseEntity.ok(subscriber.getCprLists().stream().collect(Collectors.toList()));
            }
        }
    }


    @DeleteMapping("/subscriber/cprList/cpr/{listId}")
    public ResponseEntity cprListCprDelete(HttpServletRequest request, @PathVariable("listId") String listId, @RequestParam(value = "cpr", required = false, defaultValue = "") List<String> cprs) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from " + CprList.class.getName() + " where listId = :listId ", CprList.class);
            query.setParameter("listId", listId);
            CprList foundList = (CprList) query.getResultList().get(0);
            if (!foundList.getSubscriber().getSubscriberId().equals(Optional.ofNullable(request.getHeader("uxp-client")).orElse(user.getIdentity()).replaceAll("/", "_"))) {
                String errorMessage = "No access to this list";
                ObjectNode obj = this.objectMapper.createObjectNode();
                obj.put("errorMessage", errorMessage);
                log.warn(errorMessage);
                return new ResponseEntity(obj.toString(), HttpStatus.FORBIDDEN);
            }
            List<SubscribedCprNumber> subscribedList = foundList.getCpr().stream().filter(item -> cprs.contains(item.getCprNumber())).collect(Collectors.toList());
            for (SubscribedCprNumber subscribed : subscribedList) {
                session.delete(subscribed);
                foundList.getCpr().remove(subscribed);
            }
            transaction.commit();
            String errorMessage = "Elements were removed";
            ObjectNode obj = objectMapper.createObjectNode();
            obj.put("message", errorMessage);
            return new ResponseEntity(objectMapper.writeValueAsString(obj), HttpStatus.OK);
        } catch (Exception e) {
            log.error("FAILED REMOVING ELEMENT", e);
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/subscriber/cprList/cpr/{listId}")
    public ResponseEntity cprListCprPut(HttpServletRequest request, @PathVariable("listId") String listId) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException, JsonProcessingException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query<CprList> query = session.createQuery(" from " + CprList.class.getName() + " where listId = :listId ", CprList.class);
            query.setParameter("listId", listId);
            List<CprList> lists = query.getResultList();
            if (lists.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            CprList foundList = lists.get(0);
            if (!foundList.getSubscriber().getSubscriberId().equals(Optional.ofNullable(request.getHeader("uxp-client")).orElse(user.getIdentity()).replaceAll("/", "_"))) {
                String errorMessage = "No access to this list";
                ObjectNode obj = this.objectMapper.createObjectNode();
                obj.put("errorMessage", errorMessage);
                log.warn(errorMessage);
                return new ResponseEntity(obj.toString(), HttpStatus.FORBIDDEN);
            }
            JsonNode requestBody = objectMapper.readTree(request.getInputStream());
            Iterator<JsonNode> cprBodyIterator = requestBody.get("cpr").iterator();
            while (cprBodyIterator.hasNext()) {
                JsonNode node = cprBodyIterator.next();
                foundList.addCprString(node.textValue());
            }
            transaction.commit();
            String errorMessage = "Elements were added";
            ObjectNode obj = objectMapper.createObjectNode();
            obj.put("message", errorMessage);
            return new ResponseEntity(objectMapper.writeValueAsString(obj), HttpStatus.OK);
        } catch (PersistenceException e) {
            String errorMessage = "Elements allready exists";
            ObjectNode obj = objectMapper.createObjectNode();
            obj.put("errorMessage", errorMessage);
            log.warn(errorMessage, e);
            return new ResponseEntity(objectMapper.writeValueAsString(obj), HttpStatus.CONFLICT);
        } catch (Exception e) {
            String errorMessage = "Failure";
            ObjectNode obj = objectMapper.createObjectNode();
            obj.put("errorMessage", errorMessage);
            log.error(errorMessage, e);
            return new ResponseEntity(objectMapper.writeValueAsString(obj), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Get a list of all CPR-numbers in a list
     *
     * @return
     */
    @GetMapping("/subscriber/cprList/cpr")
    public ResponseEntity<dk.magenta.datafordeler.core.fapi.Envelope> cprListCprfindAll(HttpServletRequest request, @RequestParam MultiValueMap<String, String> requestParams) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {

        String pageSize = requestParams.getFirst("pageSize");
        String page = requestParams.getFirst("page");
        String listId = requestParams.getFirst("listId");

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Query<CprList> query = session.createQuery(" from " + CprList.class.getName() + " where listId = :listId", CprList.class);
            try {
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
            } catch (NumberFormatException e) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
            query.setParameter("listId", listId);
            List<CprList> lists = query.getResultList();
            if (lists.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            CprList foundList = lists.get(0);
            if (!foundList.getSubscriber().getSubscriberId().equals(Optional.ofNullable(request.getHeader("uxp-client")).orElse(user.getIdentity()).replaceAll("/", "_"))) {
                String errorMessage = "No access to this list";
                ObjectNode obj = this.objectMapper.createObjectNode();
                obj.put("errorMessage", errorMessage);
                log.warn(errorMessage);
                return new ResponseEntity(obj.toString(), HttpStatus.FORBIDDEN);
            }

            Envelope envelope = new Envelope();
            envelope.setPageSize(query.getMaxResults());
            envelope.setPage(query.getFirstResult() + 1);
            envelope.setPath(request.getServletPath());
            envelope.setResponseTimestamp(OffsetDateTime.now());
            envelope.setResults(foundList.getCpr());

            return ResponseEntity.ok(envelope);
        } catch (Exception e) {
            log.error("FAILED REMOVING ELEMENT", e);
            return ResponseEntity.status(500).build();
        }
    }
}
