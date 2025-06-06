package dk.magenta.datafordeler.subscription.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.MonitorService;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.exception.ConflictException;
import dk.magenta.datafordeler.core.exception.InvalidCertificateException;
import dk.magenta.datafordeler.core.exception.InvalidTokenException;
import dk.magenta.datafordeler.core.fapi.Envelope;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import dk.magenta.datafordeler.subscription.data.subscriptionModel.CprList;
import dk.magenta.datafordeler.subscription.data.subscriptionModel.SubscribedCprNumber;
import dk.magenta.datafordeler.subscription.data.subscriptionModel.Subscriber;
import jakarta.servlet.http.HttpServletRequest;
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
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


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

    private String envelopMessage(String message) {
        ObjectNode obj = objectMapper.createObjectNode();
        obj.put("message", message);
        return obj.toString();
    }

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
    public ResponseEntity<String> cprListCreate(HttpServletRequest request, @RequestParam(value = "cprList", required = false, defaultValue = "") String cprList) throws IOException, AccessDeniedException, InvalidTokenException, InvalidCertificateException {
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
                return ResponseEntity.ok(objectMapper.writeValueAsString(cprCreateList));
            }
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(this.envelopMessage("cprList already exists"), HttpStatus.CONFLICT);
        } catch (Exception e) {
            String errorMessage = "Failed creating list";
            log.error(errorMessage, e);
            return new ResponseEntity<>(this.envelopMessage(errorMessage), HttpStatus.INTERNAL_SERVER_ERROR);
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
                Subscriber subscriber = (Subscriber) query.getResultList().getFirst();
                return ResponseEntity.ok(new ArrayList<>(subscriber.getCprLists()));
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
            Query<CprList> query = session.createQuery(" from " + CprList.class.getName() + " where listId = :listId ", CprList.class);
            query.setParameter("listId", listId);
            CprList foundList = query.getResultList().get(0);
            if (!foundList.getSubscriber().getSubscriberId().equals(subscriberId)) {
                return new ResponseEntity<>("Failed creating list", HttpStatus.FORBIDDEN);
            }
            List<SubscribedCprNumber> subscribedList = foundList.getCpr().stream().filter(item -> cprs.contains(item.getCprNumber())).toList();
            for (SubscribedCprNumber subscribed : subscribedList) {
                session.remove(subscribed);
                foundList.getCpr().remove(subscribed);
            }
            transaction.commit();
            return new ResponseEntity<>(this.envelopMessage("Elements were removed"), HttpStatus.OK);
        } catch (Exception e) {
            log.error("FAILED REMOVING ELEMENT", e);
            return ResponseEntity.status(500).build();
        }
    }

    @RequestMapping(method = RequestMethod.POST, path = {"/subscriber/cprList/cpr/{listId}", "/subscriber/cprList/cpr/{listId}/"})
    public ResponseEntity<String> cprListCprPut(HttpServletRequest request, @PathVariable("listId") String listId, @Valid @RequestBody String content) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException, JsonProcessingException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.info("Incoming subscription UPDATE request for list "+listId);
        if (content == null || content.isEmpty()) {
            log.info("Did not find json content in request");
            return new ResponseEntity<>(this.envelopMessage("No request body"), HttpStatus.BAD_REQUEST);
        }
        JsonNode requestBody;
        try {
            requestBody = objectMapper.readTree(content);
        } catch (JsonProcessingException e) {
            log.info("Did not understand json content in request");
            return new ResponseEntity<>(this.envelopMessage("Request body could not be parsed as json"), HttpStatus.BAD_REQUEST);
        }
        log.info("Content: "+content);
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            try {
                Transaction transaction = session.beginTransaction();
                try {
                    String subscriberId = this.getSubscriberId(request);
                    Query<CprList> query = session.createQuery(
                            " from " + CprList.class.getCanonicalName() + " list inner join list.subscriber as subscriber where list.listId = :listId and subscriber.subscriberId = :subscriberId",
                            CprList.class
                    );
                    query.setParameter("listId", listId);
                    query.setParameter("subscriberId", subscriberId);
                    List<CprList> lists = query.getResultList();
                    if (lists.isEmpty()) {
                        transaction.rollback();
                        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                    }
                    CprList foundList = lists.getFirst();
                    if (!foundList.getSubscriber().getSubscriberId().equals(subscriberId)) {
                        transaction.rollback();
                        return new ResponseEntity<>(this.envelopMessage("No access to this list"), HttpStatus.FORBIDDEN);
                    }
                    
                    for (JsonNode node : requestBody.get("cpr")) {
                        SubscribedCprNumber number = foundList.addCprString(node.textValue());
                        session.persist(number);
                    }
                    transaction.commit();
                    return ResponseEntity.ok(this.envelopMessage("Elements were added"));
                } catch (Exception e) {
                    transaction.rollback();
                    throw e;
                }
            } catch (ConstraintViolationException | ConflictException e) {
                String errorMessage = "Elements already exists";
                loggerHelper.warn(errorMessage);
                return new ResponseEntity<>(this.envelopMessage(errorMessage), HttpStatus.CONFLICT);
            } catch (Exception e) {
                String errorMessage = "Failure";
                loggerHelper.error(errorMessage, e);
                return new ResponseEntity<>(this.envelopMessage(errorMessage), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }


    /**
     * Get a list of all CPR-numbers in a list
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, path = {"/subscriber/cprList/cpr", "/subscriber/cprList/cpr/"})
    public ResponseEntity<Envelope> cprListCprfindAll(HttpServletRequest request, @RequestParam MultiValueMap<String, String> requestParams) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {

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
            CprList foundList = lists.getFirst();
            if (!foundList.getSubscriber().getSubscriberId().equals(subscriberId)) {
                log.warn("No access to this list");
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
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
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
