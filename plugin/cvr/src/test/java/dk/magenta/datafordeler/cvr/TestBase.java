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
import org.h2.jdbc.JdbcSQLException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Table;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
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
        System.out.println("Cleaning up database:");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, tableNames.toArray(new String[tableNames.size()]));
        for (String tableName : tableNames) {
            System.out.println("count table "+tableName+": "+JdbcTestUtils.countRowsInTable(jdbcTemplate, tableName));
        }
        QueryManager.clearCaches();
        System.out.println("Database cleaned");
    }

}
