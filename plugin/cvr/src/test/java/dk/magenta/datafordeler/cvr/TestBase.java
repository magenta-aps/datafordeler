package dk.magenta.datafordeler.cvr;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.Engine;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.util.Debugging;
import dk.magenta.datafordeler.cvr.records.*;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Table;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.TransientObjectException;
import org.hibernate.query.Query;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import dk.magenta.datafordeler.core.database.InterruptedPull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static dk.magenta.datafordeler.core.util.Debugging.dumpHibernateSession;

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

    @PersistenceContext
    private EntityManager entityManager;

    private List<String> tableNames;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        tableNames = entityManager.getMetamodel().getEntities().stream()
                .filter(e -> e.getJavaType().getAnnotation(Table.class) != null)
                .map(e -> e.getJavaType().getAnnotation(Table.class).name())
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
    }

    @AfterEach
    public void cleanup() {
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            for (CompanyRecord companyRecord : QueryManager.getAllEntities(session, CompanyRecord.class)) {
                Transaction transaction = session.beginTransaction();
                companyRecord.delete(session);
                transaction.commit();
            }
            for (CompanyUnitRecord companyUnitRecord : QueryManager.getAllEntities(session, CompanyUnitRecord.class)) {
                Transaction transaction = session.beginTransaction();

                System.out.println("secnamerecords "+companyUnitRecord.getId());
                ArrayList<SecNameRecord> db_secNameRecords = new ArrayList<>();
                Query<SecNameRecord> q = session.createQuery("from "+ SecNameRecord.class.getCanonicalName()+" x where x.companyUnitRecord=:record", SecNameRecord.class);
                q.setParameter("record", companyUnitRecord);
                for (SecNameRecord r : q.getResultList()) {
                    System.out.println("Exists in DB:      " + r.getId()+" "+r.getName()+" "+r.getBitemporality());
                    System.out.println("Exists in hash:    " + r.getId()+" "+ db_secNameRecords.contains(r));
                    db_secNameRecords.add(r);
                }

                ArrayList<SecNameRecord> traverse_secNameRecords = new ArrayList<>();
                companyUnitRecord.traverse(null, new Consumer<CvrRecord>() {
                    @Override
                    public void accept(CvrRecord cvrRecord) {
                        if (cvrRecord instanceof SecNameRecord) {
                            SecNameRecord r = (SecNameRecord) cvrRecord;
                            traverse_secNameRecords.add(r);
                            System.out.println("Found by traverse: "+ r.getId()+" "+r.getName()+" "+r.getBitemporality());
                        }
                    }
                });
                if (db_secNameRecords.size() != traverse_secNameRecords.size()) {
                    System.out.println(db_secNameRecords.size() + " != " + traverse_secNameRecords.size());
                }


                companyUnitRecord.delete(session);
                transaction.commit();
            }
            for (ParticipantRecord participantRecord : QueryManager.getAllEntities(session, ParticipantRecord.class)) {
                Transaction transaction = session.beginTransaction();
                participantRecord.delete(session);
                transaction.commit();
            }
            Transaction transaction = session.beginTransaction();
            List<InterruptedPull> interruptedPulls = QueryManager.getAllItems(session, InterruptedPull.class);
            for (InterruptedPull interruptedPull : interruptedPulls) {
                session.remove(interruptedPull);
            }
            transaction.commit();
        }
    }

}
