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
import dk.magenta.datafordeler.subscribtion.data.subscribtionModel.CvrList;
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
@RequestMapping("/cvrlistplugin/v1/manager")
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
     * @param cprList
     * @return
     * @throws IOException
     * @throws AccessDeniedException
     * @throws InvalidTokenException
     * @throws InvalidCertificateException
     */
    @RequestMapping(method = RequestMethod.POST, path = "/subscriber/cvrList/create/", headers="Accept=application/json", consumes = MediaType.ALL_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity cvrListCreate(HttpServletRequest request, @RequestBody String cvrList) throws IOException, AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        CvrList cvrCreateList = new CvrList(cvrList, user.getIdentity());
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.save(cvrCreateList);
            transaction.commit();
            return ResponseEntity.ok(cvrCreateList);
        }
    }

    /**
     * Get a list of all cprList
     * @return
     */
    @GetMapping("/subscriber/cvrList/list")
    public ResponseEntity<List<CvrList>> cvrListfindAll(HttpServletRequest request) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Query query = session.createQuery(" from "+ CvrList.class.getName() +" where subscriberId = :subscriberId", CvrList.class);
            DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
            query.setParameter("subscriberId", user.getIdentity());
            return ResponseEntity.ok(query.getResultList());
        }
    }


    @RequestMapping(method = RequestMethod.POST, path = "/subscriber/cvrList/cvr/add/", headers="Accept=application/json", consumes = MediaType.ALL_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE})
    public void cvrListCprCreate(HttpServletRequest request, @RequestBody StringValuesDto cvrNo) throws IOException, AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ CvrList.class.getName() +" where subscriberId = :subscriberId and listId = :listId ", CvrList.class);
            query.setParameter("subscriberId", user.getIdentity());
            query.setParameter("listId", cvrNo.getKey());
            CvrList foundList = (CvrList)query.getResultList().get(0);


            for(String cvr : cvrNo.getValues()) {

                foundList.addCvrsString(cvr);
            }

            transaction.commit();
            //return (ResponseEntity) ResponseEntity.ok();
        }
    }

    @DeleteMapping("/subscriber/cvrList/cvr/remove/{listId}")
    public ResponseEntity cvrListCprDelete(HttpServletRequest request, @PathVariable("listId") String listId, @RequestParam(value = "cvr",required=false, defaultValue = "") List<String> cvrs) throws IOException, AccessDeniedException, InvalidTokenException, InvalidCertificateException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ CvrList.class.getName() +" where subscriberId = :subscriberId and listId = :listId ", CvrList.class);
            query.setParameter("subscriberId", user.getIdentity());
            query.setParameter("listId", listId);
            CvrList foundList = (CvrList)query.getResultList().get(0);
            foundList.getCvr().removeIf(item -> cvrs.contains(item.getCvrNumber()));
            transaction.commit();
            return ResponseEntity.ok(listId);
        } catch (Exception e) {
            log.error("FAILED REMOVING ELEMENT", e);
            return ResponseEntity.status(500).build();
        }
    }


    /**
     * Get a list of all CPR-numbers in a list
     * @return
     */
    @GetMapping("/subscriber/cvrList/cvr/list")
    public ResponseEntity<Envelope> cvrListCprfindAll(HttpServletRequest request, @RequestParam MultiValueMap<String, String> requestParams) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException {

        String pageSize = requestParams.getFirst("pageSize");
        String page = requestParams.getFirst("page");

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Query query = session.createQuery(" from "+ CvrList.class.getName() +" where subscriberId = :subscriberId and  listId = :listId", CvrList.class);
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
            query.setParameter("listId", "cvrTestList1");
            CvrList foundList = (CvrList)query.getResultList().get(0);

            Envelope envelope = new Envelope();
            envelope.setPageSize(query.getMaxResults());
            envelope.setPage(query.getFirstResult()+1);
            envelope.setPath(request.getServletPath());
            envelope.setResponseTimestamp(OffsetDateTime.now());
            envelope.setResults(foundList.getCvr());

            return ResponseEntity.ok(envelope);
        }
    }


    private static void setHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Content-Type", "application/json; charset=utf-8");
        response.setStatus(200);
    }

}