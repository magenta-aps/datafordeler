package dk.magenta.datafordeler.subscription.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.MonitorService;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.*;
import dk.magenta.datafordeler.core.fapi.Envelope;
import dk.magenta.datafordeler.core.fapi.ResultSet;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonRecordQuery;
import dk.magenta.datafordeler.cpr.records.person.data.PersonEventDataRecord;
import dk.magenta.datafordeler.cvr.access.CvrRolesDefinition;
import dk.magenta.datafordeler.subscription.data.subscriptionModel.BusinessEventSubscription;
import dk.magenta.datafordeler.subscription.data.subscriptionModel.CprList;
import dk.magenta.datafordeler.subscription.data.subscriptionModel.SubscribedCprNumber;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/subscription/1/findCprBusinessEvent")
public class FindCprBusinessEvent {

    @Autowired
    SessionManager sessionManager;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private DafoUserManager dafoUserManager;

    @Autowired
    protected MonitorService monitorService;

    private Logger log = LogManager.getLogger(FindCprBusinessEvent.class.getCanonicalName());


    @PostConstruct
    public void init() {

    }


    /**
     * Get a list of all subscribtions
     * @return
     */
    @GetMapping("/fetchEvents")
    public ResponseEntity<Envelope> findAll(HttpServletRequest request, @RequestParam MultiValueMap<String, String> requestParams) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {

        String pageSize = requestParams.getFirst("pageSize");
        String page = requestParams.getFirst("page");
        String businessEventId = requestParams.getFirst("subscription");
        String timestampGTE = requestParams.getFirst("timestamp.GTE");
        String timestampLTE = requestParams.getFirst("timestamp.LTE");
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);

        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.urlInvokePersistablelogs("fetchEvents");

        try(Session session = sessionManager.getSessionFactory().openSession()) {

            this.checkAndLogAccess(loggerHelper);

            Query eventQuery = session.createQuery(" from "+ BusinessEventSubscription.class.getName() +" where businessEventId = :businessEventId", BusinessEventSubscription.class);
            eventQuery.setParameter("businessEventId", businessEventId);
            if(eventQuery.getResultList().isEmpty()) {
                String errorMessage = "Subscription not found";
                ObjectNode obj = this.objectMapper.createObjectNode();
                obj.put("errorMessage", errorMessage);
                log.warn(errorMessage);
                return new ResponseEntity(obj.toString(), HttpStatus.NOT_FOUND);
            } else {
                BusinessEventSubscription subscribtion = (BusinessEventSubscription) eventQuery.getResultList().get(0);
                if(!subscribtion.getSubscriber().getSubscriberId().equals(Optional.ofNullable(request.getHeader("uxp-client")).orElse(user.getIdentity()).replaceAll("/","_"))) {
                    String errorMessage = "No access";
                    ObjectNode obj = this.objectMapper.createObjectNode();
                    obj.put("errorMessage", errorMessage);
                    log.warn(errorMessage);
                    return new ResponseEntity(obj.toString(), HttpStatus.FORBIDDEN);
                }
                String hql = "SELECT max(event.timestamp) FROM "+ PersonEventDataRecord.class.getCanonicalName()+" event ";
                Query timestampQuery = session.createQuery(hql);
                OffsetDateTime newestEventTimestamp = (OffsetDateTime)timestampQuery.getResultList().get(0);

                PersonRecordQuery query = new PersonRecordQuery();
                CprList cprList = subscribtion.getCprList();
                if(cprList!=null) {
                    Collection<SubscribedCprNumber> theList = cprList.getCpr();
                    List<String> pnrFilterList = theList.stream().map(x -> x.getCprNumber()).collect(Collectors.toList());
                    query.setPersonnumre(pnrFilterList);
                }
                //TODO: dette skal oprettes med opsplitning i forskellige attributter med betydning
                String[] subscribtionKodeId = subscribtion.getKodeId().split("[.]");
                if(!"cpr".equals(subscribtionKodeId[0]) && !"businessevent".equals(subscribtionKodeId[1])) {
                    String errorMessage = "No access";
                    ObjectNode obj = this.objectMapper.createObjectNode();
                    obj.put("errorMessage", errorMessage);
                    log.warn(errorMessage);
                    return new ResponseEntity(obj.toString(), HttpStatus.FORBIDDEN);
                }

                query.setEvent(subscribtionKodeId[2]);
                if(timestampGTE!=null) {
                    query.setEventTimeAfter(timestampGTE);
                }
                if(timestampLTE!=null) {
                    query.setEventTimeBefore(timestampLTE);
                }
                query.setPageSize(pageSize);
                if(query.getPageSize()>1000) {
                    String errorMessage = "No access";
                    ObjectNode obj = this.objectMapper.createObjectNode();
                    obj.put("errorMessage", errorMessage);
                    log.warn(errorMessage);
                    return new ResponseEntity(obj.toString(), HttpStatus.FORBIDDEN);
                }
                query.setPage(page);
                List<ResultSet<PersonEntity>> entities = QueryManager.getAllEntitySets(session, query, PersonEntity.class);
                Envelope envelope = new Envelope();
                List<String> pnrList = entities.stream().map(x -> x.getPrimaryEntity().getPersonnummer()).collect(Collectors.toList());
                envelope.setResults(pnrList);
                envelope.setNewestResultTimestamp(newestEventTimestamp);
                loggerHelper.urlInvokePersistablelogs("fetchEvents done");
                return ResponseEntity.ok(envelope);
            }
        } catch (AccessRequiredException e) {
            String errorMessage = "No access to this information";
            ObjectNode obj = this.objectMapper.createObjectNode();
            obj.put("errorMessage", errorMessage);
            log.warn(errorMessage);
            return new ResponseEntity(obj.toString(), HttpStatus.FORBIDDEN);
        } catch(Exception e) {
            log.error("Failed pulling events from subscribtion", e);
        }
        return ResponseEntity.status(500).build();
    }

    protected void checkAndLogAccess(LoggerHelper loggerHelper) throws AccessDeniedException, AccessRequiredException {
        try {
            loggerHelper.getUser().checkHasSystemRole(CprRolesDefinition.READ_CPR_ROLE);
            loggerHelper.getUser().checkHasSystemRole(CvrRolesDefinition.READ_CVR_ROLE);
        }
        catch (AccessDeniedException e) {
            loggerHelper.info("Access denied: " + e.getMessage());
            throw(e);
        }
    }
}