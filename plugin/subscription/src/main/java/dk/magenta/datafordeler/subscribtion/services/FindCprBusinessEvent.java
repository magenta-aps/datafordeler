package dk.magenta.datafordeler.subscribtion.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.MonitorService;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.exception.InvalidCertificateException;
import dk.magenta.datafordeler.core.exception.InvalidTokenException;
import dk.magenta.datafordeler.core.fapi.Envelope;
import dk.magenta.datafordeler.core.fapi.ResultSet;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonRecordQuery;
import dk.magenta.datafordeler.subscribtion.data.subscribtionModel.BusinessEventSubscription;
import dk.magenta.datafordeler.subscribtion.data.subscribtionModel.SubscribedCprNumber;
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
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/subscriptionplugin/v1/findCprBusinessEvent")
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
        String businessEventId = requestParams.getFirst("subscribtion");
        String timestamp = requestParams.getFirst("timestamp");
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);

        try(Session session = sessionManager.getSessionFactory().openSession()) {

            Query eventQuery = session.createQuery(" from "+ BusinessEventSubscription.class.getName() +" where businessEventId = :businessEventId", BusinessEventSubscription.class);
            eventQuery.setParameter("businessEventId", businessEventId);
            if(eventQuery.getResultList().size()==0) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                BusinessEventSubscription subscribtion = (BusinessEventSubscription) eventQuery.getResultList().get(0);
                PersonRecordQuery query = new PersonRecordQuery();
                List<SubscribedCprNumber> theList = subscribtion.getCprList().getCpr();
                List<String> pnrFilterList = theList.stream().map(x -> x.getCprNumber()).collect(Collectors.toList());
                query.setEvent(subscribtion.getKodeId());
                if(timestamp!=null) {
                    query.setEventTimeAfter(timestamp);
                }
                query.setPersonnumre(pnrFilterList);//TODO: consider joining this on DB-level
                query.setPageSize(pageSize);
                query.setPage(page);
                List<ResultSet<PersonEntity>> entities = QueryManager.getAllEntitySets(session, query, PersonEntity.class);
                Envelope envelope = new Envelope();
                List<String> pnrList = entities.stream().map(x -> x.getPrimaryEntity().getPersonnummer()).collect(Collectors.toList());
                envelope.setResults(pnrList);
                return ResponseEntity.ok(envelope);
            }
        } catch(Exception e) {
            log.error("Failed pulling events from subscribtion", e);
        }
        return ResponseEntity.status(500).build();
    }



    private static void setHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Content-Type", "application/json; charset=utf-8");
        response.setStatus(200);
    }

}