package dk.magenta.datafordeler.cvr;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.Engine;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.cvr.records.CompanyRecord;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Table;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.After;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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


    @After
    public void cleanup() {
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            for (CompanyRecord companyRecord : QueryManager.getAllEntities(session, CompanyRecord.class)) {
                Transaction transaction = session.beginTransaction();
                companyRecord.delete(session);
                transaction.commit();
            }
        }
    }

}
