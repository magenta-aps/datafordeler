package dk.magenta.datafordeler.prisme;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.util.InputStreamReader;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cvr.CvrPlugin;
import dk.magenta.datafordeler.cvr.records.CompanyRecord;
import dk.magenta.datafordeler.geo.data.GeoEntityManager;
import dk.magenta.datafordeler.geo.data.accessaddress.AccessAddressEntityManager;
import dk.magenta.datafordeler.geo.data.building.BuildingEntityManager;
import dk.magenta.datafordeler.geo.data.locality.LocalityEntityManager;
import dk.magenta.datafordeler.geo.data.municipality.MunicipalityEntityManager;
import dk.magenta.datafordeler.geo.data.postcode.PostcodeEntityManager;
import dk.magenta.datafordeler.geo.data.road.RoadEntityManager;
import dk.magenta.datafordeler.geo.data.unitaddress.UnitAddressEntityManager;
import dk.magenta.datafordeler.ger.GerPlugin;
import dk.magenta.datafordeler.ger.data.company.CompanyEntity;
import dk.magenta.datafordeler.ger.data.responsible.ResponsibleEntity;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public abstract class TestBase {


    @Autowired
    private LocalityEntityManager localityEntityManager;

    @Autowired
    private RoadEntityManager roadEntityManager;

    @Autowired
    private BuildingEntityManager buildingEntityManager;

    @Autowired
    private MunicipalityEntityManager municipalityEntityManager;

    @Autowired
    private PostcodeEntityManager postcodeEntityManager;

    @Autowired
    private AccessAddressEntityManager accessAddressEntityManager;

    @Autowired
    private UnitAddressEntityManager unitAddressEntityManager;


    protected void loadAllGeoAdress(SessionManager sessionManager) throws IOException {
        this.loadGeoData(sessionManager, localityEntityManager, "/locality.json");
        this.loadGeoData(sessionManager, roadEntityManager, "/road.json");
        this.loadGeoData(sessionManager, unitAddressEntityManager, "/unit.json");
        this.loadGeoData(sessionManager, municipalityEntityManager, "/municipality.json");
        this.loadGeoData(sessionManager, postcodeEntityManager, "/post.json");
        this.loadGeoData(sessionManager, buildingEntityManager, "/building.json");
        this.loadGeoData(sessionManager, accessAddressEntityManager, "/access.json");
    }


    protected void cleanup(SessionManager sessionManager, Class[] classes) {
        Session session = sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        try {
            for (Class cls : classes) {
                List<DatabaseEntry> eList = QueryManager.getAllItems(session, cls);
                for (DatabaseEntry e : eList) {
                    session.delete(e);
                }
            }
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            session.close();
        }
    }

    protected void loadGeoData(SessionManager sessionManager, GeoEntityManager entityManager, String resourceName) throws IOException {
        InputStream data = TestBase.class.getResourceAsStream(resourceName);
        Session session = sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        ImportMetadata importMetadata = new ImportMetadata();
        try {
            importMetadata.setTransactionInProgress(true);
            importMetadata.setSession(session);
            entityManager.parseData(data, importMetadata);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        } finally {
            importMetadata.setTransactionInProgress(false);
            session.close();
            data.close();
        }
    }

    protected void loadCompany(CvrPlugin cvrPlugin, SessionManager sessionManager, ObjectMapper objectMapper) throws IOException, DataFordelerException {
        InputStream testData = CvrCombinedTest.class.getResourceAsStream("/company_in.json");
        Session session = sessionManager.getSessionFactory().openSession();
        try {
            dk.magenta.datafordeler.cvr.entitymanager.CompanyEntityManager companyEntityManager = (dk.magenta.datafordeler.cvr.entitymanager.CompanyEntityManager) cvrPlugin.getRegisterManager().getEntityManager(CompanyRecord.schema);
            JsonNode root = objectMapper.readTree(testData);
            testData.close();
            JsonNode itemList = root.get("hits").get("hits");
            Assert.assertTrue(itemList.isArray());
            ImportMetadata importMetadata = new ImportMetadata();
            importMetadata.setSession(session);

            for (JsonNode item : itemList) {
                String source = objectMapper.writeValueAsString(item.get("_source").get("Vrvirksomhed"));
                ByteArrayInputStream bais = new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8));
                companyEntityManager.parseData(bais, importMetadata);
                bais.close();
            }
        } finally {
            session.close();
        }
    }

    protected void loadManyCompanies(CvrPlugin cvrPlugin, SessionManager sessionManager, int count) throws Exception {
        this.loadManyCompanies(cvrPlugin, sessionManager, count, 0);
    }

    protected void loadManyCompanies(CvrPlugin cvrPlugin, SessionManager sessionManager, int count, int start) throws Exception {
        dk.magenta.datafordeler.cvr.entitymanager.CompanyEntityManager companyEntityManager = (dk.magenta.datafordeler.cvr.entitymanager.CompanyEntityManager) cvrPlugin.getRegisterManager().getEntityManager(CompanyRecord.schema);
        Session session = sessionManager.getSessionFactory().openSession();
        try {
            ImportMetadata importMetadata = new ImportMetadata();
            importMetadata.setSession(session);
            String testData = InputStreamReader.readInputStream(CvrCombinedTest.class.getResourceAsStream("/company_in.json"));
            for (int i = start; i < count + start; i++) {
                String altered = testData.replaceAll("25052943", "1" + String.format("%07d", i)).replaceAll("\n", "");
                ByteArrayInputStream bais = new ByteArrayInputStream(altered.getBytes(StandardCharsets.UTF_8));
                companyEntityManager.parseData(bais, importMetadata);
                bais.close();
            }
        } finally {
            session.close();
        }
    }

    protected void loadGerCompany(GerPlugin gerPlugin, SessionManager sessionManager) throws IOException, DataFordelerException {
        InputStream testData = CvrCombinedTest.class.getResourceAsStream("/GER.test.xlsx");
        Session session = sessionManager.getSessionFactory().openSession();
        try {
            dk.magenta.datafordeler.ger.data.company.CompanyEntityManager companyEntityManager = (dk.magenta.datafordeler.ger.data.company.CompanyEntityManager) gerPlugin.getRegisterManager().getEntityManager(CompanyEntity.schema);
            ImportMetadata importMetadata = new ImportMetadata();
            importMetadata.setSession(session);
            companyEntityManager.parseData(testData, importMetadata);
        } finally {
            session.close();
            testData.close();
        }
    }

    protected void loadGerParticipant(GerPlugin gerPlugin, SessionManager sessionManager) throws IOException, DataFordelerException {
        InputStream testData = CvrCombinedTest.class.getResourceAsStream("/GER.test.xlsx");
        Session session = sessionManager.getSessionFactory().openSession();
        try {
            dk.magenta.datafordeler.ger.data.responsible.ResponsibleEntityManager responsibleEntityManager = (dk.magenta.datafordeler.ger.data.responsible.ResponsibleEntityManager) gerPlugin.getRegisterManager().getEntityManager(ResponsibleEntity.schema);
            ImportMetadata importMetadata = new ImportMetadata();
            importMetadata.setSession(session);
            responsibleEntityManager.parseData(testData, importMetadata);
        } finally {
            session.close();
            testData.close();
        }
    }

    protected void cleanupGeoData(SessionManager sessionManager) {
        //TODO: might be needed for some cleanup between tests

    }

    protected void cleanupPersonData(SessionManager sessionManager) {
        this.cleanup(sessionManager, new Class[]{
                PersonEntity.class,
        });
        QueryManager.clearCaches();
    }

    protected void cleanupCompanyData(SessionManager sessionManager) {
        this.cleanup(sessionManager, new Class[]{
                dk.magenta.datafordeler.cvr.records.CompanyRecord.class,
                dk.magenta.datafordeler.cvr.records.CompanyUnitRecord.class,
                dk.magenta.datafordeler.cvr.records.ParticipantRecord.class,
                dk.magenta.datafordeler.ger.data.company.CompanyEntity.class,
                dk.magenta.datafordeler.ger.data.unit.UnitEntity.class,
                dk.magenta.datafordeler.ger.data.responsible.ResponsibleEntity.class
        });
        QueryManager.clearCaches();
    }

}
