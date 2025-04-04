package dk.magenta.datafordeler.subscription.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.cpr.data.person.PersonEntityManager;
import dk.magenta.datafordeler.cvr.CvrPlugin;
import dk.magenta.datafordeler.subscription.data.subscriptionModel.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public abstract class TestBase {
    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected SessionManager sessionManager;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoSpyBean
    protected DafoUserManager dafoUserManager;

    @Autowired
    protected CvrPlugin plugin;

    @Autowired
    protected PersonEntityManager personEntityManager;

    @BeforeEach
    @AfterEach
    public void cleanup() {
        SessionFactory sessionFactory = sessionManager.getSessionFactory();

        try (Session session = sessionFactory.openSession()) {
            QueryManager.clearCaches();
            List<Class<? extends DatabaseEntry>> classes = Arrays.asList(
                    CprList.class,
                    CvrList.class,
                    BusinessEventSubscription.class,
                    DataEventSubscription.class,
                    SubscribedCprNumber.class,
                    SubscribedCvrNumber.class,
                    Subscriber.class
                    );
            Transaction transaction = session.beginTransaction();
            for (Class cls : classes) {
                List<DatabaseEntry> eList = QueryManager.getAllItems(session, cls);
                for (DatabaseEntry e : eList) {
                    session.remove(e);
                }
            }
            transaction.commit();
            QueryManager.clearCaches();
        }

//        Session session = sessionFactory.getCurrentSession();
//        QueryManager.clearCaches();
//        List<Class<? extends DatabaseEntry>> classes = Arrays.asList(
//                BusinessEventSubscription.class,
//                DataEventSubscription.class,
//                CprList.class,
//                CvrList.class,
//                SubscribedCprNumber.class,
//                SubscribedCvrNumber.class,
//                Subscriber.class
//        );
//        Transaction transaction = session.beginTransaction();
//        for (Class cls : classes) {
//            System.out.println("Querying...");
//            List<DatabaseEntry> eList = QueryManager.getAllItems(session, cls);
//            System.out.println("Removing " + eList.size() + " entries from table "+cls.getSimpleName());
//            for (DatabaseEntry e : eList) {
//                session.remove(e);
//            }
//            System.out.println("Removed " + eList.size() + " entries from table "+cls.getSimpleName());
////                session.createQuery("delete from " + cls.getName()).executeUpdate();
//        }
//        transaction.commit();
//        QueryManager.clearCaches();

    }
}
