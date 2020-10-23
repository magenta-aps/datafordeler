package dk.magenta.datafordeler.subscribtion.services;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import dk.magenta.datafordeler.cvr.access.CvrRolesDefinition;
import dk.magenta.datafordeler.subscribtion.data.subscribtionModel.CprList;
import dk.magenta.datafordeler.subscribtion.data.subscribtionModel.DataEventSubscription;
import dk.magenta.datafordeler.subscribtion.data.subscribtionModel.SubscribedCprNumber;
import dk.magenta.datafordeler.subscribtion.queries.GeneralQuery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/subscriptionplugin/v1/findCprDataEvent")
public class FindCprDataEvent {

    @Autowired
    SessionManager sessionManager;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private DafoUserManager dafoUserManager;

    @Autowired
    protected MonitorService monitorService;

    private Logger log = LogManager.getLogger(FindCprDataEvent.class.getCanonicalName());


    @PostConstruct
    public void init() throws DataFordelerException {

    }


    /**
     * Get a list of all subscribtions
     * @return
     */
    @GetMapping("/fetchEvents")
    public ResponseEntity<Envelope> findAll(HttpServletRequest request, @RequestParam MultiValueMap<String, String> requestParams) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {

        String pageSize = requestParams.getFirst("pageSize");
        String page = requestParams.getFirst("page");
        String dataEventId = requestParams.getFirst("subscribtion");
        String timestampGTE = requestParams.getFirst("timestamp.GTE");
        String timestampLTE = requestParams.getFirst("timestamp.LTE");
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);

        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);

        try(Session session = sessionManager.getSessionFactory().openSession()) {

            this.checkAndLogAccess(loggerHelper);

            Query eventQuery = session.createQuery(" from "+ DataEventSubscription.class.getName() +" where dataEventId = :dataEventId", DataEventSubscription.class);
            eventQuery.setParameter("dataEventId", dataEventId);
            if(eventQuery.getResultList().size()==0) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                DataEventSubscription subscribtion = (DataEventSubscription) eventQuery.getResultList().get(0);
                if(!subscribtion.getSubscriber().getSubscriberId().equals(user.getIdentity())) {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
                OffsetDateTime offsetTimestampGTE;
                if(timestampGTE==null) {
                    offsetTimestampGTE = OffsetDateTime.of(0,1,1,1,1,1,1, ZoneOffset.ofHours(0));
                } else {
                    offsetTimestampGTE = dk.magenta.datafordeler.core.fapi.Query.parseDateTime(timestampGTE);
                }

                OffsetDateTime offsetTimestampLTE=null;
                if(timestampLTE!=null) {
                    offsetTimestampLTE = dk.magenta.datafordeler.core.fapi.Query.parseDateTime(timestampLTE);
                }

                String[] subscribtionKodeId = subscribtion.getKodeId().split("[.]");
                if(!"cpr".equals(subscribtionKodeId[0]) && !"dataevent".equals(subscribtionKodeId[1])) {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }

                PersonRecordQuery query = GeneralQuery.getPersonQuery(subscribtionKodeId[2], offsetTimestampGTE, offsetTimestampLTE);
                CprList cprList = subscribtion.getCprList();
                if(cprList!=null) {
                    List<SubscribedCprNumber> theList = cprList.getCpr();
                    List<String> pnrFilterList = theList.stream().map(x -> x.getCprNumber()).collect(Collectors.toList());
                    query.setPersonnumre(pnrFilterList);//TODO: consider joining this on DB-level
                }

                query.setPageSize(pageSize);
                if(query.getPageSize()>1000) {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
                query.setPage(page);
                List<ResultSet<PersonEntity>> entities = QueryManager.getAllEntitySets(session, query, PersonEntity.class);
                Envelope envelope = new Envelope();
                List<String> pnrList = entities.stream().map(x -> x.getPrimaryEntity().getPersonnummer()).collect(Collectors.toList());
                envelope.setResults(pnrList);
                return ResponseEntity.ok(envelope);
            }
        } catch (AccessRequiredException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
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



    private static void setHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Content-Type", "application/json; charset=utf-8");
        response.setStatus(200);
    }

}