package dk.magenta.datafordeler.cvr;

import com.fasterxml.jackson.databind.JsonNode;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.cvr.entitymanager.CompanyEntityManager;
import dk.magenta.datafordeler.cvr.entitymanager.CompanyUnitEntityManager;
import dk.magenta.datafordeler.cvr.entitymanager.ParticipantEntityManager;
import dk.magenta.datafordeler.cvr.query.CompanyRecordQuery;
import dk.magenta.datafordeler.cvr.records.*;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ParseTest extends TestBase {

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
            Assertions.assertTrue(itemList.isArray());
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

        CompanyEntityManager entityManager = (CompanyEntityManager) this.registerManager.getEntityManager(CompanyRecord.schema);
        InputStream stream = this.registerManager.pullRawData(this.registerManager.getEventInterface(entityManager), entityManager, importMetadata);
        entityManager.parseData(stream, importMetadata);

        try (Session session = sessionManager.getSessionFactory().openSession()) {

            CompanyRecordQuery query = new CompanyRecordQuery();
            OffsetDateTime time = OffsetDateTime.now();
            query.setRegistrationAt(time);
            query.setEffectAt(time);
            query.applyFilters(session);

            List<CompanyRecord> companyList = QueryManager.getAllEntities(session, query, CompanyRecord.class);

            Assertions.assertEquals(4, companyList.size());
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
            Assertions.assertTrue(itemList.isArray());
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
        //Load units from unit.json
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            importMetadata.setSession(session);
            InputStream input = ParseTest.class.getResourceAsStream("/unit.json");
            JsonNode root = objectMapper.readTree(input);
            JsonNode itemList = root.get("hits").get("hits");
            Assertions.assertTrue(itemList.isArray());
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

        CompanyEntityManager entityManager = (CompanyEntityManager) this.registerManager.getEntityManager(CompanyRecord.schema);
        InputStream stream = this.registerManager.pullRawData(this.registerManager.getEventInterface(entityManager), entityManager, importMetadata);
        entityManager.parseData(stream, importMetadataCompany);

        try (Session session = sessionManager.getSessionFactory().openSession()) {

            CompanyRecordQuery query = new CompanyRecordQuery();
            OffsetDateTime time = OffsetDateTime.now();
            query.setRegistrationAt(time);
            query.setEffectAt(time);
            query.applyFilters(session);
            List<CompanyRecord> companyList = QueryManager.getAllEntities(session, query, CompanyRecord.class);
            Assertions.assertEquals(4, companyList.size());
        }

        //Load companies from GLBASETEST.json again to lalidate error-handling
        entityManager = (CompanyEntityManager) this.registerManager.getEntityManager(CompanyRecord.schema);
        stream = this.registerManager.pullRawData(this.registerManager.getEventInterface(entityManager), entityManager, importMetadata);
        entityManager.parseData(stream, importMetadataCompany);

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            CompanyRecordQuery query = new CompanyRecordQuery();
            OffsetDateTime time = OffsetDateTime.now();
            query.setRegistrationAt(time);
            query.setEffectAt(time);
            query.applyFilters(session);
            List<CompanyRecord> companyList = QueryManager.getAllEntities(session, query, CompanyRecord.class);
            Assertions.assertEquals(4, companyList.size());
        }


        try (Session session = sessionManager.getSessionFactory().openSession()) {
            CriteriaBuilder subscriptionBuilder = session.getCriteriaBuilder();

            // Read subscription to validate that missing companies gets subscribed
            CriteriaQuery<CompanySubscription> allCompanySubscription = subscriptionBuilder.createQuery(CompanySubscription.class);
            allCompanySubscription.from(CompanySubscription.class);

            List<Integer> subscribedCompanyList = session.createQuery(allCompanySubscription)
                    .getResultList().stream()
                    .map(CompanySubscription::getCvrNumber)
                    .sorted()
                    .collect(Collectors.toList());

            // Read companyunits to validate that their missing CVR'r is assigned to subscription
            CriteriaQuery<CompanyUnitMetadataRecord> unitQuery = subscriptionBuilder.createQuery(CompanyUnitMetadataRecord.class);
            Root<CompanyUnitMetadataRecord> unitRootEntry = unitQuery.from(CompanyUnitMetadataRecord.class);
            CriteriaQuery<CompanyUnitMetadataRecord> unitc = unitQuery.select(unitRootEntry);
            TypedQuery<CompanyUnitMetadataRecord> unit = session.createQuery(unitc);
            List<Integer> unitCompanyList = unit.getResultList().stream().map(s -> s.getNewestCvrRelation()).sorted().collect(Collectors.toList());

            Assertions.assertEquals(unitCompanyList, subscribedCompanyList);
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
            Assertions.assertTrue(itemList.isArray());
            Assertions.assertEquals(1, itemList.size());
            for (JsonNode item : itemList) {
                String type = item.get("_type").asText();
                ParticipantEntityManager entityManager = (ParticipantEntityManager) this.registerManager.getEntityManager(schemaMap.get(type));
                entityManager.parseData(item.get("_source").get("Vrdeltagerperson"), importMetadata, session);
            }
        } finally {
            transaction.rollback();
            session.close();
            QueryManager.clearCaches();
        }
    }

}
