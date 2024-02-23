package dk.magenta.datafordeler.cvr;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.Engine;
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

    @After
    public synchronized void cleanup() {
        SessionFactory sessionFactory = sessionManager.getSessionFactory();
        try (Session session = sessionFactory.openSession()) {
            QueryManager.clearCaches();

            // Tøm tabeller efter hver test
            // Undersøg gerne om der findes bedre metoder som også faktisk virker
            Transaction transaction = session.beginTransaction();

            //sessio.createQuery("delete from "+CompanyRecord.class.getCanonicalName()).executeUpdate();
            for (AttributeValueRecord entity : QueryManager.getAllEntities(session, AttributeValueRecord.class, false)) {
                session.delete(entity);
            }


            for (CompanyRecord entity : QueryManager.getAllEntities(session, CompanyRecord.class)) {
                session.delete(entity);
            }
            for (CompanyUnitRecord entity : QueryManager.getAllEntities(session, CompanyUnitRecord.class)) {
                session.delete(entity);
            }
            for (ParticipantRecord entity : QueryManager.getAllEntities(session, ParticipantRecord.class)) {
                session.delete(entity);
            }
            for (LastUpdated entity : QueryManager.getAllEntities(session, LastUpdated.class, false)) {
                session.delete(entity);
            }
            transaction.commit();
        }
    }
}
