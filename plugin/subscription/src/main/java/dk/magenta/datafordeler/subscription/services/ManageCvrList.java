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
import dk.magenta.datafordeler.subscription.data.subscriptionModel.CvrList;
import dk.magenta.datafordeler.subscription.data.subscriptionModel.SubscribedCvrNumber;
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

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
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

    private final Logger log = LogManager.getLogger(ManageCvrList.class.getCanonicalName());

    private String envelopMessage(String message) {
        ObjectNode obj = objectMapper.createObjectNode();
        obj.put("message", message);
        return obj.toString();
    }

    /**
     * Create a cvrList
     *
     * @param request
     * @param cvrList
     * @return
     * @throws IOException
     * @throws AccessDeniedException
     * @throws InvalidTokenException
     * @throws InvalidCertificateException
     */
    @RequestMapping(method = RequestMethod.POST, path = {"/subscriber/cvrList", "/subscriber/cvrList/"}, headers = "Accept=application/json", consumes = MediaType.ALL_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> cvrListCreate(HttpServletRequest request, @RequestParam(value = "cvrList", required = false, defaultValue = "") String cvrList) throws IOException, AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query<Subscriber> query = session.createQuery(" from " + Subscriber.class.getName() + " where subscriberId = :subscriberId", Subscriber.class);
            query.setParameter("subscriberId", Optional.ofNullable(request.getHeader("uxp-client")).orElse(user.getIdentity()).replaceAll("/", "_"));
            List<Subscriber> subscribers = query.getResultList();
            if (subscribers.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                Subscriber subscriber = subscribers.getFirst();
                CvrList cvrCreateList = new CvrList(cvrList, subscriber);
                session.persist(cvrCreateList);
                subscriber.addCvrList(cvrCreateList);
                transaction.commit();
                return ResponseEntity.ok(objectMapper.writeValueAsString(cvrCreateList));
            }
        } catch (ConstraintViolationException e) {
            String errorMessage = "cvrList already exists";
            log.warn(errorMessage, e);
            return new ResponseEntity<>(this.envelopMessage(errorMessage), HttpStatus.CONFLICT);
        } catch (Exception e) {
            String errorMessage = "Failed creating list";
            log.error(errorMessage, e);
            return new ResponseEntity<>(this.envelopMessage(errorMessage), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get a list of all cvrList
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, path = {"/subscriber/cvrList", "/subscriber/cvrList/"})
    public ResponseEntity<List<CvrList>> cvrListfindAll(HttpServletRequest request) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        try (Session session = sessionManager.getSessionFactory().openSession()) {

            Query<Subscriber> query = session.createQuery(" from " + Subscriber.class.getName() + " where subscriberId = :subscriberId", Subscriber.class);
            query.setParameter("subscriberId", Optional.ofNullable(request.getHeader("uxp-client")).orElse(user.getIdentity()).replaceAll("/", "_"));
            List<Subscriber> subscribers = query.getResultList();
            if (subscribers.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                Subscriber subscriber = subscribers.getFirst();
                return ResponseEntity.ok(new ArrayList<>(subscriber.getCvrLists()));
            }
        }
    }


    @RequestMapping(method = RequestMethod.DELETE, path = {"/subscriber/cvrList/cvr/{listId}", "/subscriber/cvrList/cvr/{listId}/"})
    public ResponseEntity<String> cvrListCvrDelete(HttpServletRequest request, @PathVariable("listId") String listId, @RequestParam(value = "cvr", required = false, defaultValue = "") List<String> cvrs) throws IOException, AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query<CvrList> query = session.createQuery(" from " + CvrList.class.getName() + " where listId = :listId ", CvrList.class);
            query.setParameter("listId", listId);
            List<CvrList> lists = query.getResultList();
            if (lists.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            CvrList foundList = lists.getFirst();
            if (!foundList.getSubscriber().getSubscriberId().equals(Optional.ofNullable(request.getHeader("uxp-client")).orElse(user.getIdentity()).replaceAll("/", "_"))) {
                String errorMessage = "No access to this list";
                log.warn(errorMessage);
                return new ResponseEntity<>(this.envelopMessage(errorMessage), HttpStatus.FORBIDDEN);
            }
            List<SubscribedCvrNumber> subscribedList = foundList.getCvr().stream().filter(item -> cvrs.contains(item.getCvrNumber())).collect(Collectors.toList());
            for (SubscribedCvrNumber subscribed : subscribedList) {
                session.remove(subscribed);
                foundList.getCvr().remove(subscribed);
            }
            transaction.commit();
            String errorMessage = "Elements were removed";
            ObjectNode obj = objectMapper.createObjectNode();
            obj.put("message", errorMessage);
            return new ResponseEntity<>(objectMapper.writeValueAsString(obj), HttpStatus.OK);

        } catch (Exception e) {
            log.error("FAILED REMOVING ELEMENT", e);
            return ResponseEntity.status(500).build();
        }
    }

    @RequestMapping(method = RequestMethod.POST, path = {"/subscriber/cvrList/cvr/{listId}", "/subscriber/cvrList/cvr/{listId}/"})
    public ResponseEntity<String> cvrListCvrPut(HttpServletRequest request, @PathVariable("listId") String listId) throws IOException, AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query<CvrList> query = session.createQuery(" from " + CvrList.class.getName() + " where listId = :listId ", CvrList.class);
            query.setParameter("listId", listId);
            List<CvrList> lists = query.getResultList();
            if (lists.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            CvrList foundList = lists.getFirst();
            if (!foundList.getSubscriber().getSubscriberId().equals(Optional.ofNullable(request.getHeader("uxp-client")).orElse(user.getIdentity()).replaceAll("/", "_"))) {
                String errorMessage = "No access to this list";
                ObjectNode obj = this.objectMapper.createObjectNode();
                obj.put("errorMessage", errorMessage);
                log.warn(errorMessage);
                return new ResponseEntity<>(obj.toString(), HttpStatus.FORBIDDEN);
            }
            JsonNode requestBody = objectMapper.readTree(request.getInputStream());
            for (JsonNode node : requestBody.get("cvr")) {
                foundList.addCvrString(node.textValue());
            }
            transaction.commit();
            String errorMessage = "Elements were added";
            ObjectNode obj = this.objectMapper.createObjectNode();
            obj.put("message", errorMessage);
            return new ResponseEntity<>(objectMapper.writeValueAsString(obj), HttpStatus.OK);
        } catch (ConstraintViolationException e) {
            String errorMessage = "Elements allready exists";
            log.warn(errorMessage, e);
            return new ResponseEntity<>(this.envelopMessage(errorMessage), HttpStatus.CONFLICT);
        } catch (Exception e) {
            log.error("FAILED REMOVING ELEMENT", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Get a list of all CVR-numbers in a list
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, path = {"/subscriber/cvrList/cvr", "/subscriber/cvrList/cvr/"})
    public ResponseEntity<Envelope> cvrListCvrfindAll(HttpServletRequest request, @RequestParam MultiValueMap<String, String> requestParams) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {

        String pageSize = requestParams.getFirst("pageSize");
        String page = requestParams.getFirst("page");
        String listId = requestParams.getFirst("listId");

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Query<CvrList> query = session.createQuery(" from " + CvrList.class.getName() + " where listId = :listId", CvrList.class);
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
            List<CvrList> lists = query.getResultList();
            if (lists.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            CvrList foundList = lists.getFirst();
            if (!foundList.getSubscriber().getSubscriberId().equals(Optional.ofNullable(request.getHeader("uxp-client")).orElse(user.getIdentity()).replaceAll("/", "_"))) {
                log.warn("No access to this list");
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            Envelope envelope = new Envelope();
            envelope.setPageSize(query.getMaxResults());
            envelope.setPage(query.getFirstResult() + 1);
            envelope.setPath(request.getServletPath());
            envelope.setResponseTimestamp(OffsetDateTime.now());
            envelope.setResults(foundList.getCvr());

            return ResponseEntity.ok(envelope);
        } catch (Exception e) {
            log.error("FAILED REMOVING ELEMENT", e);
            return ResponseEntity.status(500).build();
        }
    }

}
