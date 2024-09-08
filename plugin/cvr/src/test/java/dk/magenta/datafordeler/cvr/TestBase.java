package dk.magenta.datafordeler.cvr;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.Engine;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.LastUpdated;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonSubscription;
import dk.magenta.datafordeler.cpr.records.road.data.RoadEntity;
import dk.magenta.datafordeler.cvr.records.AttributeValueRecord;
import dk.magenta.datafordeler.cvr.records.CompanyRecord;
import dk.magenta.datafordeler.cvr.records.CompanyUnitRecord;
import dk.magenta.datafordeler.cvr.records.ParticipantRecord;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.After;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public abstract class TestBase {

    @Autowired
    protected SessionManager sessionManager;

    @Autowired
    protected CvrRegisterManager registerManager;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected CvrPlugin plugin;

    @Autowired
    protected Engine engine;

    /*@After
    public void cleanup() {
        SessionFactory sessionFactory = sessionManager.getSessionFactory();
        try (Session session = sessionFactory.openSession()) {
            QueryManager.clearCaches();

            // Tøm tabeller efter hver test
            // Undersøg gerne om der findes bedre metoder som også faktisk virker
            Class[] classes = new Class[] {
                    CompanyRecord.class,
                    CompanyUnitRecord.class,
                    ParticipantRecord.class,
                    LastUpdated.class,
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
    }*/
}
