package dk.magenta.datafordeler.subscribtion.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.MonitorService;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;

import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.exception.AccessRequiredException;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.cpr.CprAccessChecker;
import dk.magenta.datafordeler.cpr.records.road.RoadRecordQuery;
import dk.magenta.datafordeler.subscribtion.data.subscribtionModel.Subscriber;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping("/subscribtionplugin/v1/manager")
public class ManageSubscribtion {

    @Autowired
    SessionManager sessionManager;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private DafoUserManager dafoUserManager;

    @Autowired
    protected MonitorService monitorService;

    private Logger log = LogManager.getLogger(ManageSubscribtion.class.getCanonicalName());


    @PostConstruct
    public void init() {
    }


    /**
     * Get a list of all subscribtions
     * @return
     */
    @GetMapping("/subscriber/list")
    //@RequestMapping(method = RequestMethod.GET, path = "/{cprNummer}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Subscriber>> findAll() {
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            List<Subscriber> subscribtionList = QueryManager.getAllItems(session, Subscriber.class);
            return ResponseEntity.ok(subscribtionList);
        }
    }

    @RequestMapping(method = RequestMethod.POST, path = "/subscriber/create/", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity create(HttpServletRequest request, @Valid @RequestBody String subscriberContent) throws IOException {

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Subscriber subscriber = objectMapper.readValue(subscriberContent, Subscriber.class);
            session.save(subscriber);
            transaction.commit();
            return ResponseEntity.ok(subscriber);
        }
    }

    @GetMapping("/subscriber/{subscriberId}")
    public ResponseEntity<Subscriber> createBySubscriberId(@PathVariable("subscriberId") String subscriberId) {
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Query query = session.createQuery(" from "+ Subscriber.class.getName() +" where subscriberId = :subscriberId", Subscriber.class);
            query.setParameter("subscriberId", subscriberId);
            Subscriber subscriber = (Subscriber)query.getResultList().get(0);
            return ResponseEntity.ok(subscriber);
        }
    }

/*
    @PutMapping("/{id}")
    public ResponseEntity<Subscriber> update(@PathVariable Long id, @Valid @RequestBody Subscriber product) {
        if (!productService.findById(id).isPresent()) {
            log.error("Id " + id + " is not existed");
            ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(productService.save(product));
    }

/*    @DeleteMapping("/{id}")
    public ResponseEntity delete(@PathVariable Long id) {
        if (!productService.findById(id).isPresent()) {
            log.error("Id " + id + " is not existed");
            ResponseEntity.badRequest().build();
        }

        productService.deleteById(id);

        return ResponseEntity.ok().build();
    }*/



    public int getVersion() {
        return 1;
    }


    public String getServiceName() {
        return "subscribtionservices";
    }


    protected Class<Subscriber> getEntityClass() {
        return Subscriber.class;
    }


    public Plugin getPlugin() {
        return null;
    }


    protected void checkAccess(DafoUserDetails dafoUserDetails) throws AccessDeniedException, AccessRequiredException {
        CprAccessChecker.checkAccess(dafoUserDetails);
    }


    protected RoadRecordQuery getEmptyQuery() {
        return new RoadRecordQuery();
    }
}