package dk.magenta.datafordeler.subscription.services;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import dk.magenta.datafordeler.core.util.LoggerHelper;
import dk.magenta.datafordeler.subscription.data.subscriptionModel.CprList;
import dk.magenta.datafordeler.subscription.data.subscriptionModel.SubscribedCprNumber;
import dk.magenta.datafordeler.subscription.data.subscriptionModel.Subscriber;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.PersistenceException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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


    private String getSubscriberId(HttpServletRequest request) throws InvalidTokenException, AccessDeniedException, InvalidCertificateException {
        String subscriberId = Optional.ofNullable(
                request.getHeader("uxp-client")
        ).orElse(
                dafoUserManager.getUserFromRequest(request).getIdentity()
        ).replaceAll("/", "_");
        log.info("Got subscriber id from request: " + subscriberId);
        return subscriberId;
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
    @RequestMapping(method = RequestMethod.POST, path = {"/subscriber/cprList", "/subscriber/cprList/"}, headers = "Accept=application/json", consumes = MediaType.ALL_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity cprListCreate(HttpServletRequest request, @RequestParam(value = "cprList", required = false, defaultValue = "") String cprList) throws IOException, AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.info("Incoming subscription CREATE request with list "+cprList);
        String subscriberId = this.getSubscriberId(request);
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from " + Subscriber.class.getName() + " where subscriberId = :subscriberId", Subscriber.class);
            query.setParameter("subscriberId", subscriberId);
            if (query.getResultList().isEmpty()) {
                log.info("Did not find subscription with subscriber id " + subscriberId);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                Subscriber subscriber = (Subscriber) query.getResultList().get(0);
                CprList cprCreateList = new CprList(cprList, subscriber);
                session.persist(subscriber);
                session.persist(cprCreateList);
                subscriber.addCprList(cprCreateList);

                transaction.commit();
                return ResponseEntity.ok(cprCreateList);
            }
        } catch (ConstraintViolationException e) {
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
    @RequestMapping(method = RequestMethod.GET, path = {"/subscriber/cprList", "/subscriber/cprList/"})
    public ResponseEntity<List<CprList>> cprListfindAll(HttpServletRequest request) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        String subscriberId = this.getSubscriberId(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.info("Incoming subscription GET request for user "+user.getIdentity());
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Query query = session.createQuery(" from " + Subscriber.class.getName() + " where subscriberId = :subscriberId", Subscriber.class);
            query.setParameter("subscriberId", subscriberId);
            if (query.getResultList().isEmpty()) {
                log.info("Did not find subscription with subscriber id " + subscriberId);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                Subscriber subscriber = (Subscriber) query.getResultList().get(0);
                return ResponseEntity.ok(subscriber.getCprLists().stream().collect(Collectors.toList()));
            }
        }
    }


    @RequestMapping(method = RequestMethod.DELETE, path = {"/subscriber/cprList/cpr/{listId}", "/subscriber/cprList/cpr/{listId}/"})
    public ResponseEntity cprListCprDelete(HttpServletRequest request, @PathVariable("listId") String listId, @RequestParam(value = "cpr", required = false, defaultValue = "") List<String> cprs) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        String subscriberId = this.getSubscriberId(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.info("Incoming subscription DELETE request for list "+listId);
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from " + CprList.class.getName() + " where listId = :listId ", CprList.class);
            query.setParameter("listId", listId);
            CprList foundList = (CprList) query.getResultList().get(0);
            if (!foundList.getSubscriber().getSubscriberId().equals(subscriberId)) {
                String errorMessage = "No access to this list";
                ObjectNode obj = this.objectMapper.createObjectNode();
                obj.put("errorMessage", errorMessage);
                log.warn(errorMessage);
                return new ResponseEntity(obj.toString(), HttpStatus.FORBIDDEN);
            }
            List<SubscribedCprNumber> subscribedList = foundList.getCpr().stream().filter(item -> cprs.contains(item.getCprNumber())).collect(Collectors.toList());
            for (SubscribedCprNumber subscribed : subscribedList) {
                session.remove(subscribed);
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

    @RequestMapping(method = RequestMethod.POST, path = {"/subscriber/cprList/cpr/{listId}", "/subscriber/cprList/cpr/{listId}/"})
    public ResponseEntity cprListCprPut(HttpServletRequest request, @PathVariable("listId") String listId, @Valid @RequestBody String content) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException, JsonProcessingException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.info("Incoming subscription UPDATE request for list "+listId);
        Session session = sessionManager.getSessionFactory().openSession();
        try {
            loggerHelper.info("session open");
            Transaction transaction = session.beginTransaction();
            try {
                Query<CprList> query = session.createQuery(" from " + CprList.class.getName() + " where listId = :listId ", CprList.class);
                query.setParameter("listId", listId);
                List<CprList> lists = query.getResultList();
                if (lists.isEmpty()) {
                    loggerHelper.info("not found");
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }
                loggerHelper.info("found " + lists.size() + " cprs");
                CprList foundList = lists.get(0);
                String subscriberId = this.getSubscriberId(request);
                if (!foundList.getSubscriber().getSubscriberId().equals(subscriberId)) {
                    String errorMessage = "No access to this list";
                    ObjectNode obj = this.objectMapper.createObjectNode();
                    obj.put("errorMessage", errorMessage);
                    loggerHelper.warn(errorMessage);
                    loggerHelper.info("forbidden");
                    return new ResponseEntity(obj.toString(), HttpStatus.FORBIDDEN);
                }
                loggerHelper.info("has access");
                if (content == null || content.isEmpty()) {
                    String errorMessage = "No access to this list";
                    ObjectNode obj = this.objectMapper.createObjectNode();
                    obj.put("errorMessage", errorMessage);
                    return new ResponseEntity(obj.toString(), HttpStatus.BAD_REQUEST);
                }
                JsonNode requestBody = objectMapper.readTree(content);
                loggerHelper.info("request body: "+requestBody);
                Iterator<JsonNode> cprBodyIterator = requestBody.get("cpr").iterator();
                long i=0;
                while (cprBodyIterator.hasNext()) {
                    JsonNode node = cprBodyIterator.next();
                    foundList.addCprString(node.textValue());
                    loggerHelper.info("loop "+i);
                    i++;
                }
                loggerHelper.info("persisting");
                session.persist(foundList);
                for (SubscribedCprNumber n : foundList.getCpr()) {
                    session.persist(n);
                }
                loggerHelper.info("persisted");
                String errorMessage = "Elements were added";
                ObjectNode obj = objectMapper.createObjectNode();
                obj.put("message", errorMessage);
                String output = objectMapper.writeValueAsString(obj);
                loggerHelper.info("UPDATE complete " + listId);
                transaction.commit();
                loggerHelper.info("transaction committed");
                return new ResponseEntity(output, HttpStatus.OK);
            } catch (Exception e) {
                transaction.rollback();
                e.printStackTrace();
                throw e;
            }
        } catch (ConstraintViolationException e) {
            String errorMessage = "Elements already exists";
            ObjectNode obj = objectMapper.createObjectNode();
            obj.put("errorMessage", errorMessage);
            loggerHelper.warn(errorMessage, e);
            return new ResponseEntity(objectMapper.writeValueAsString(obj), HttpStatus.CONFLICT);
        } catch (Exception e) {
            String errorMessage = "Failure";
            ObjectNode obj = objectMapper.createObjectNode();
            obj.put("errorMessage", errorMessage);
            loggerHelper.error(errorMessage, e);
            return new ResponseEntity(objectMapper.writeValueAsString(obj), HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            log.info("closing session");
            session.close();
            log.info("session closed");
        }
    }


    /**
     * Get a list of all CPR-numbers in a list
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, path = {"/subscriber/cprList/cpr", "/subscriber/cprList/cpr/"})
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
            String subscriberId = this.getSubscriberId(request);
            query.setParameter("listId", listId);
            List<CprList> lists = query.getResultList();
            if (lists.isEmpty()) {
                log.info("Subscriber list with id "+listId+" not found");
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            CprList foundList = lists.get(0);
            if (!foundList.getSubscriber().getSubscriberId().equals(subscriberId)) {
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
