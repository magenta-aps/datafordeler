package dk.magenta.datafordeler.subscription.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonEntityManager;
import dk.magenta.datafordeler.cvr.CvrPlugin;
import dk.magenta.datafordeler.geo.data.accessaddress.AccessAddressEntity;
import dk.magenta.datafordeler.geo.data.locality.GeoLocalityEntity;
import dk.magenta.datafordeler.geo.data.municipality.GeoMunicipalityEntity;
import dk.magenta.datafordeler.geo.data.postcode.PostcodeEntity;
import dk.magenta.datafordeler.geo.data.road.GeoRoadEntity;
import dk.magenta.datafordeler.geo.data.unitaddress.UnitAddressEntity;
import dk.magenta.datafordeler.subscription.data.subscriptionModel.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public abstract class TestBase {
    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected SessionManager sessionManager;

    @Autowired
    protected ObjectMapper objectMapper;

    @SpyBean
    protected DafoUserManager dafoUserManager;

    @Autowired
    protected CvrPlugin plugin;

    @Autowired
    protected PersonEntityManager personEntityManager;

    @Before
    @After
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
                List<DatabaseEntry> eList = QueryManager.getAllItems(session, cls);
                for (DatabaseEntry e : eList) {
                    session.delete(e);
                }
            }
            transaction.commit();
            QueryManager.clearCaches();
        }
    }
}
