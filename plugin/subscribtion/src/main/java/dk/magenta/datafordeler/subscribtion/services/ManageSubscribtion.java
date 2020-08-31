
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
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/subscribtion/v1/manager")
//@ResponseBody
/*@Slf4j
@RequiredArgsConstructor*/
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
        //this.monitorService.addAccessCheckPoint("/prisme/cpr/1/1234");
        //this.monitorService.addAccessCheckPoint("POST", "/prisme/cpr/1/", "{}");

        System.out.println("LLLLLLLLL");
    }




    @RequestMapping(method = RequestMethod.GET, path = "/{lookup}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String getSingle(@RequestParam(value = "cpr",required=false, defaultValue = "") List<String> cprs, @RequestParam(value = "cvr",required=false, defaultValue = "") List<String> cvrs, HttpServletRequest request) {


        return "WW";

    }




    /*@GetMapping
    public ResponseEntity<List<String>> sts() {

        return ResponseEntity.ok(new ArrayList());


        try(Session session = sessionManager.getSessionFactory().openSession()) {

            return ResponseEntity.ok(QueryManager.getAllItems(session, String.class));



        }

    }*/







    @GetMapping
    //@RequestMapping(method = RequestMethod.GET, path = "/{cprNummer}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Subscriber>> findAll() {


        //return ResponseEntity.ok(new ArrayList());

        try(Session session = sessionManager.getSessionFactory().openSession()) {

            List<Subscriber> ll = QueryManager.getAllItems(session, Subscriber.class);

            return ResponseEntity.ok(ll);



        }

    }
/*
    @PostMapping
    public ResponseEntity create(@Valid @RequestBody Subscriber subscriber) {

        return ResponseEntity.ok("");

        try(Session session = sessionManager.getSessionFactory().openSession()) {

            return ResponseEntity.ok(session.save(new Subscriber("testing")));
        }
    }*/

    @GetMapping("/{id}")
    public ResponseEntity<Subscriber> findById(@PathVariable Long userId) {


        try(Session session = sessionManager.getSessionFactory().openSession()) {

            Query q = session.createQuery(" from "+ Subscriber.class.getName() +" where userId = :userId", Subscriber.class);
            q.sets
        }

        /*Optional<Subscriber> stock = productService.findById(id);
        if (!stock.isPresent()) {
            log.error("Id " + id + " is not existed");
            ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(stock.get());*/
        return null;
    }

/*    @PutMapping("/{id}")
    public ResponseEntity<Subscriber> update(@PathVariable Long id, @Valid @RequestBody Subscriber product) {
        if (!productService.findById(id).isPresent()) {
            log.error("Id " + id + " is not existed");
            ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(productService.save(product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity delete(@PathVariable Long id) {
        if (!productService.findById(id).isPresent()) {
            log.error("Id " + id + " is not existed");
            ResponseEntity.badRequest().build();
        }

        productService.deleteById(id);

        return ResponseEntity.ok().build();
    }*/
}

















/*package dk.magenta.datafordeler.subscribtion.services;


import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.subscribtion.data.subscribtionModel.Subscriber;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;


@RestController
@RequestMapping("/subscribtion/v1/manager")
@ResponseBody

public class ManageSubscribtion {

    @Autowired
    private SessionManager sessionManager;

    //Logger log =new Logger();


    //private final ProductService productService;


    @PostConstruct
    public void init() {
        //this.monitorService.addAccessCheckPoint("/prisme/cpr/1/1234");
        //this.monitorService.addAccessCheckPoint("POST", "/prisme/cpr/1/", "{}");

        System.out.println("LLLLLLLLL");
    }








    @GetMapping
    public ResponseEntity<List<Subscriber>> sts() {


        try(Session session = sessionManager.getSessionFactory().openSession()) {

            return ResponseEntity.ok(QueryManager.getAllItems(session, Subscriber.class));



        }

    }







    //@GetMapping
    @RequestMapping(method = RequestMethod.GET, path = "/{cprNummer}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Subscriber>> findAll() {


        try(Session session = sessionManager.getSessionFactory().openSession()) {

            return ResponseEntity.ok(QueryManager.getAllItems(session, Subscriber.class));



        }

    }

    @PostMapping
    public ResponseEntity create(@Valid @RequestBody Subscriber product) {

        try(Session session = sessionManager.getSessionFactory().openSession()) {

            return ResponseEntity.ok(session.save(new Subscriber("testing")));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Subscriber> findById(@PathVariable Long id) {
        Optional<Subscriber> stock = productService.findById(id);
        if (!stock.isPresent()) {
            log.error("Id " + id + " is not existed");
            ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(stock.get());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Subscriber> update(@PathVariable Long id, @Valid @RequestBody Subscriber product) {
        if (!productService.findById(id).isPresent()) {
            log.error("Id " + id + " is not existed");
            ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(productService.save(product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity delete(@PathVariable Long id) {
        if (!productService.findById(id).isPresent()) {
            log.error("Id " + id + " is not existed");
            ResponseEntity.badRequest().build();
        }

        productService.deleteById(id);

        return ResponseEntity.ok().build();
    }
}*/
