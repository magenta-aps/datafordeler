package dk.magenta.datafordeler.cvr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.cvr.entitymanager.CompanyEntityManager;
import dk.magenta.datafordeler.cvr.entitymanager.CompanyUnitEntityManager;
import dk.magenta.datafordeler.cvr.entitymanager.CvrEntityManager;
import dk.magenta.datafordeler.cvr.entitymanager.ParticipantEntityManager;
import dk.magenta.datafordeler.cvr.query.CompanyRecordQuery;
import dk.magenta.datafordeler.cvr.records.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class ParseTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private CvrPlugin plugin;

    @Autowired
    private CvrRegisterManager registerManager;

    private CvrEntityManager entityManager;

    private static final HashMap<String, String> schemaMap = new HashMap<>();

    static {
        schemaMap.put("_doc", CompanyRecord.schema);
        schemaMap.put("produktionsenhed", CompanyUnitRecord.schema);
        schemaMap.put("deltager", ParticipantRecord.schema);
    }

    @Test
    public void testParseCompanyFile() throws DataFordelerException, IOException {
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        try {
            importMetadata.setSession(session);
            InputStream input = ParseTest.class.getResourceAsStream("/company_in.json");
            JsonNode root = objectMapper.readTree(input);
            JsonNode itemList = root.get("hits").get("hits");
            Assert.assertTrue(itemList.isArray());
            for (JsonNode item : itemList) {
                String type = item.get("_type").asText();
                CompanyEntityManager entityManager = (CompanyEntityManager) plugin.getRegisterManager().getEntityManager(schemaMap.get(type));
                entityManager.parseData(item.get("_source").get("Vrvirksomhed"), importMetadata, session);
            }
        } finally {
            transaction.rollback();
            session.close();
            QueryManager.clearCaches();
        }
    }

    @Test
    public void testParseCompanyDemoFile() throws DataFordelerException, URISyntaxException {
        ImportMetadata importMetadata = new ImportMetadata();

        URL testData = ParseTest.class.getResource("/GLBASETEST.json");
        String testDataPath = testData.toURI().toString();
        registerManager.setCvrDemoCompanyFile(testDataPath);

        entityManager = (CvrEntityManager) this.registerManager.getEntityManagers().get(0);
        InputStream stream = this.registerManager.pullRawData(this.registerManager.getEventInterface(entityManager), entityManager, importMetadata);
        entityManager.parseData(stream, importMetadata);

        try (Session session = sessionManager.getSessionFactory().openSession()) {

            CompanyRecordQuery query = new CompanyRecordQuery();
            OffsetDateTime time = OffsetDateTime.now();
            query.setRegistrationFromBefore(time);
            query.setRegistrationToAfter(time);
            query.setEffectToAfter(time);
            query.setEffectFromBefore(time);
            query.applyFilters(session);

            List<CompanyRecord> companyList = QueryManager.getAllEntities(session, query, CompanyRecord.class);

            Assert.assertEquals(4, companyList.size());
        }
    }


    @Test
    public void testParseUnitFile() throws IOException, DataFordelerException {
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        try {
            importMetadata.setSession(session);
            InputStream input = ParseTest.class.getResourceAsStream("/unit.json");
            JsonNode root = objectMapper.readTree(input);
            JsonNode itemList = root.get("hits").get("hits");
            Assert.assertTrue(itemList.isArray());
            for (JsonNode item : itemList) {
                String type = item.get("_type").asText();
                CompanyUnitEntityManager entityManager = (CompanyUnitEntityManager) plugin.getRegisterManager().getEntityManager(schemaMap.get(type));
                entityManager.parseData(item.get("_source").get("VrproduktionsEnhed"), importMetadata, session);
            }
        } finally {
            transaction.rollback();
            session.close();
            QueryManager.clearCaches();
        }
    }

    /**
     * Validate that when parsing and creating data for companies and units. Then a subscription for missing companies is created
     *
     * @throws IOException
     * @throws DataFordelerException
     * @throws URISyntaxException
     */
    @Test
    public void testUnitsWithNoMatchingCVR() throws IOException, DataFordelerException, URISyntaxException {
        ImportMetadata importMetadata = new ImportMetadata();
        Transaction transaction = null;
        //Load units from unit.json
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            importMetadata.setSession(session);
            InputStream input = ParseTest.class.getResourceAsStream("/unit.json");
            JsonNode root = objectMapper.readTree(input);
            JsonNode itemList = root.get("hits").get("hits");
            Assert.assertTrue(itemList.isArray());
            for (JsonNode item : itemList) {
                String type = item.get("_type").asText();
                CompanyUnitEntityManager entityManager = (CompanyUnitEntityManager) plugin.getRegisterManager().getEntityManager(schemaMap.get(type));
                entityManager.parseData(item.get("_source").get("VrproduktionsEnhed"), importMetadata, session);
            }
            transaction.commit();
        }

        //Load companies from GLBASETEST.json
        ImportMetadata importMetadataCompany = new ImportMetadata();
        URL testData = ParseTest.class.getResource("/GLBASETEST.json");
        String testDataPath = testData.toURI().toString();
        registerManager.setCvrDemoCompanyFile(testDataPath);

        entityManager = (CvrEntityManager) this.registerManager.getEntityManagers().get(0);
        InputStream stream = this.registerManager.pullRawData(this.registerManager.getEventInterface(entityManager), entityManager, importMetadata);
        entityManager.parseData(stream, importMetadataCompany);

        try (Session session = sessionManager.getSessionFactory().openSession()) {

            CompanyRecordQuery query = new CompanyRecordQuery();
            OffsetDateTime time = OffsetDateTime.now();
            query.setRegistrationFromBefore(time);
            query.setRegistrationToAfter(time);
            query.setEffectToAfter(time);
            query.setEffectFromBefore(time);
            query.applyFilters(session);
            List<CompanyRecord> companyList = QueryManager.getAllEntities(session, query, CompanyRecord.class);
            Assert.assertEquals(4, companyList.size());
        }

        //Load companies from GLBASETEST.json again to lalidate error-handling
        entityManager = (CvrEntityManager) this.registerManager.getEntityManagers().get(0);
        stream = this.registerManager.pullRawData(this.registerManager.getEventInterface(entityManager), entityManager, importMetadata);
        entityManager.parseData(stream, importMetadataCompany);

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            CompanyRecordQuery query = new CompanyRecordQuery();
            OffsetDateTime time = OffsetDateTime.now();
            query.setRegistrationFromBefore(time);
            query.setRegistrationToAfter(time);
            query.setEffectToAfter(time);
            query.setEffectFromBefore(time);
            query.applyFilters(session);
            List<CompanyRecord> companyList = QueryManager.getAllEntities(session, query, CompanyRecord.class);
            Assert.assertEquals(4, companyList.size());
        }


        try (Session session = sessionManager.getSessionFactory().openSession()) {
            CriteriaBuilder subscriptionBuilder = session.getCriteriaBuilder();

            // Read subscription to validate that missing companies gets subscribed
            CriteriaQuery<CompanySubscription> allCompanySubscription = subscriptionBuilder.createQuery(CompanySubscription.class);
            allCompanySubscription.from(CompanySubscription.class);
            List<Integer> subscribedCompanyList = session.createQuery(allCompanySubscription).getResultList().stream().map(s -> s.getCvrNumber()).sorted().collect(Collectors.toList());

            // Read companyunits to validate that their missing CVR'r is assigned to subscription
            CriteriaQuery<CompanyUnitMetadataRecord> unitQuery = subscriptionBuilder.createQuery(CompanyUnitMetadataRecord.class);
            Root<CompanyUnitMetadataRecord> unitRootEntry = unitQuery.from(CompanyUnitMetadataRecord.class);
            CriteriaQuery<CompanyUnitMetadataRecord> unitc = unitQuery.select(unitRootEntry);
            TypedQuery<CompanyUnitMetadataRecord> unit = session.createQuery(unitc);
            List<Integer> unitCompanyList = unit.getResultList().stream().map(s -> s.getNewestCvrRelation()).sorted().collect(Collectors.toList());

            Assert.assertEquals(unitCompanyList, subscribedCompanyList);
        }
    }

    @Test
    public void testParseParticipantFile() throws IOException, DataFordelerException {
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        try {
            importMetadata.setSession(session);
            InputStream input = ParseTest.class.getResourceAsStream("/person.json");
            JsonNode root = objectMapper.readTree(input);
            JsonNode itemList = root.get("hits").get("hits");
            Assert.assertTrue(itemList.isArray());
            Assert.assertEquals(1, itemList.size());
            for (JsonNode item : itemList) {
                String type = item.get("_type").asText();
                ParticipantEntityManager entityManager = (ParticipantEntityManager) plugin.getRegisterManager().getEntityManager(schemaMap.get(type));
                entityManager.parseData(item.get("_source").get("Vrdeltagerperson"), importMetadata, session);
            }
        } finally {
            transaction.rollback();
            session.close();
            QueryManager.clearCaches();
        }
    }

}
