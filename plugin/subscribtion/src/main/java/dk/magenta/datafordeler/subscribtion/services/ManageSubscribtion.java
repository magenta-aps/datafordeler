package dk.magenta.datafordeler.subscribtion.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.MonitorService;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;

import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.subscribtion.data.subscribtionModel.Subscriber;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
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
    @GetMapping("/list")
    //@RequestMapping(method = RequestMethod.GET, path = "/{cprNummer}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Subscriber>> findAll() {

        try(Session session = sessionManager.getSessionFactory().openSession()) {

            List<Subscriber> subscribtionList = QueryManager.getAllItems(session, Subscriber.class);
            return ResponseEntity.ok(subscribtionList);
        }

    }





    //@PostMapping("/subscriber/create")
    @RequestMapping(method = RequestMethod.POST, path = "/subscriber/create/", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity create(HttpServletRequest request/*, @Valid @RequestBody Subscriber subscriber*/) {

        System.out.println("CREATE CALLED");

        return null;
    }

    @GetMapping("/subscriber/{subscriberId}")
    public ResponseEntity<Subscriber> findById(@PathVariable("subscriberId") String subscriberId) {


        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Query query = session.createQuery(" from "+ Subscriber.class.getName() +" where subscriberId = :subscriberId", Subscriber.class);
            query.setParameter("subscriberId", subscriberId);

            List<Subscriber> subscriptions = QueryManager.getAllItems(session, Subscriber.class);

            Subscriber s = (Subscriber)query.getResultList().get(0);
            return ResponseEntity.ok(s);
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
}