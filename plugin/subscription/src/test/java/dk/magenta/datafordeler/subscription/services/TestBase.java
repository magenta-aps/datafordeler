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
            Class[] classes = new Class[]{
                    SubscribedCprNumber.class,
                    SubscribedCvrNumber.class,
                    CprList.class,
                    CvrList.class,
                    BusinessEventSubscription.class,
                    DataEventSubscription.class,
                    Subscriber.class,
            };
            Transaction transaction = session.beginTransaction();
            for (Class cls : classes) {
                /*List<DatabaseEntry> eList = QueryManager.getAllItems(session, cls);
                for (DatabaseEntry e : eList) {
                    session.remove(e);
                }*/
                session.createQuery("delete from " + cls.getName()).executeUpdate();
            }
            transaction.commit();
            QueryManager.clearCaches();
        }
    }
}
