package dk.magenta.datafordeler.subscribtion.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.MonitorService;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.exception.InvalidCertificateException;
import dk.magenta.datafordeler.core.exception.InvalidTokenException;
import dk.magenta.datafordeler.core.fapi.Envelope;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.subscribtion.data.subscribtionModel.PnumberList;
import dk.magenta.datafordeler.subscribtion.data.subscribtionModel.StringValuesDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;


@RestController
@RequestMapping("/pnolistplugin/v1/manager")
public class ManagePnoList {

    @Autowired
    SessionManager sessionManager;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private DafoUserManager dafoUserManager;

    @Autowired
    protected MonitorService monitorService;

    private Logger log = LogManager.getLogger(ManagePnoList.class.getCanonicalName());


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
    @RequestMapping(method = RequestMethod.POST, path = "/subscriber/pnoList/create/", headers="Accept=application/json", consumes = MediaType.ALL_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity cprListCreate(HttpServletRequest request, @RequestBody String cprList) throws IOException, AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        PnumberList cprCreateList = new PnumberList(cprList, user.getIdentity());
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
    @GetMapping("/subscriber/pnoList/list")
    public ResponseEntity<List<PnumberList>> cprListfindAll(HttpServletRequest request) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Query query = session.createQuery(" from "+ PnumberList.class.getName() +" where subscriberId = :subscriberId", PnumberList.class);
            DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
            query.setParameter("subscriberId", user.getIdentity());
            return ResponseEntity.ok(query.getResultList());
        }
    }


    @RequestMapping(method = RequestMethod.POST, path = "/subscriber/pnoList/cpr/add/", headers="Accept=application/json", consumes = MediaType.ALL_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE})
    public void cprListCprCreate(HttpServletRequest request, @RequestBody StringValuesDto cprNo) throws IOException, AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ PnumberList.class.getName() +" where subscriberId = :subscriberId and listId = :listId ", PnumberList.class);
            query.setParameter("subscriberId", user.getIdentity());
            query.setParameter("listId", cprNo.getKey());
            PnumberList foundList = (PnumberList)query.getResultList().get(0);


            for(String cpr : cprNo.getValues()) {

                foundList.addPNumbersString(cpr);
            }

            transaction.commit();
            //return (ResponseEntity) ResponseEntity.ok();
        }
    }

    @DeleteMapping("/subscriber/pnoList/pno/remove/{listId}")
    public ResponseEntity cprListCprDelete(HttpServletRequest request, @PathVariable("listId") String listId, @RequestParam(value = "cpr",required=false, defaultValue = "") List<String> cprs) throws IOException, AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ PnumberList.class.getName() +" where subscriberId = :subscriberId and listId = :listId ", PnumberList.class);
            query.setParameter("subscriberId", user.getIdentity());
            query.setParameter("listId", listId);
            PnumberList foundList = (PnumberList)query.getResultList().get(0);
            foundList.getPNumbers().removeIf(item -> cprs.contains(item.getPNumber()));
            transaction.commit();
            return (ResponseEntity) ResponseEntity.ok();
        }
    }


    /**
     * Get a list of all CPR-numbers in a list
     * @return
     */
    @GetMapping("/subscriber/pnoList/pno/list")
    public ResponseEntity<Envelope> pnoListCprfindAll(HttpServletRequest request, @RequestParam MultiValueMap<String, String> requestParams) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {

        String pageSize = requestParams.getFirst("pageSize");
        String page = requestParams.getFirst("page");

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Query query = session.createQuery(" from "+ PnumberList.class.getName() +" where subscriberId = :subscriberId and  listId = :listId", PnumberList.class);
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
            PnumberList foundList = (PnumberList)query.getResultList().get(0);

            Envelope envelope = new Envelope();
            envelope.setPageSize(query.getMaxResults());
            envelope.setPage(query.getFirstResult()+1);
            envelope.setPath(request.getServletPath());
            envelope.setResponseTimestamp(OffsetDateTime.now());
            envelope.setResults(foundList.getPNumbers());

            return ResponseEntity.ok(envelope);
        }
    }


    private static void setHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Content-Type", "application/json; charset=utf-8");
        response.setStatus(200);
    }

}