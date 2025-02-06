package dk.magenta.datafordeler.statistik;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.util.UnorderedJsonListComparator;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.geo.data.GeoEntityManager;
import dk.magenta.datafordeler.geo.data.accessaddress.AccessAddressEntity;
import dk.magenta.datafordeler.geo.data.accessaddress.AccessAddressEntityManager;
import dk.magenta.datafordeler.geo.data.building.BuildingEntityManager;
import dk.magenta.datafordeler.geo.data.locality.GeoLocalityEntity;
import dk.magenta.datafordeler.geo.data.locality.LocalityEntityManager;
import dk.magenta.datafordeler.geo.data.municipality.GeoMunicipalityEntity;
import dk.magenta.datafordeler.geo.data.municipality.MunicipalityEntityManager;
import dk.magenta.datafordeler.geo.data.postcode.PostcodeEntity;
import dk.magenta.datafordeler.geo.data.postcode.PostcodeEntityManager;
import dk.magenta.datafordeler.geo.data.road.GeoRoadEntity;
import dk.magenta.datafordeler.geo.data.road.RoadEntityManager;
import dk.magenta.datafordeler.geo.data.unitaddress.UnitAddressEntity;
import dk.magenta.datafordeler.geo.data.unitaddress.UnitAddressEntityManager;
import dk.magenta.datafordeler.statistik.reportExecution.ReportAssignment;
import dk.magenta.datafordeler.statistik.services.StatisticsService;
import org.apache.commons.io.FileUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

@Component
public abstract class TestBase {

    @Autowired
    protected SessionManager sessionManager;

    @Autowired
    protected LocalityEntityManager localityEntityManager;

    @Autowired
    protected RoadEntityManager roadEntityManager;

    @Autowired
    protected BuildingEntityManager buildingEntityManager;

    @Autowired
    protected MunicipalityEntityManager municipalityEntityManager;

    @Autowired
    protected PostcodeEntityManager postcodeEntityManager;

    @Autowired
    protected AccessAddressEntityManager accessAddressEntityManager;

    @Autowired
    protected UnitAddressEntityManager unitAddressEntityManager;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected TestUtils testsUtils;

    protected void loadAllGeoAdress(SessionManager sessionManager) throws IOException {
        this.loadGeoData(sessionManager, localityEntityManager, "/locality.json");
        this.loadGeoData(sessionManager, roadEntityManager, "/road.json");
        this.loadGeoData(sessionManager, unitAddressEntityManager, "/unit.json");
        this.loadGeoData(sessionManager, municipalityEntityManager, "/municipality.json");
        this.loadGeoData(sessionManager, postcodeEntityManager, "/post.json");
        this.loadGeoData(sessionManager, buildingEntityManager, "/building.json");
        this.loadGeoData(sessionManager, accessAddressEntityManager, "/access.json");
    }

    private String oldPath = null;

    public void setPath() throws IOException {
        //Use this code block when temp directories need to be created
        if (this.oldPath == null) {
            Path path = Files.createTempDirectory("statistik");
            this.oldPath = StatisticsService.PATH_FILE;
            StatisticsService.PATH_FILE = String.valueOf(path);
        }
    }

    @AfterEach
    public void clearPath() {
        if (this.oldPath != null && !Objects.equals(StatisticsService.PATH_FILE, this.oldPath)) {
            this.deleteFiles(StatisticsService.PATH_FILE);
        }
        StatisticsService.PATH_FILE = oldPath;
    }

    private void deleteFiles(String path_file) {
        //This code can be places in @After
        File tempDir = null;
        try {
            tempDir = new File(path_file);
            boolean exists = tempDir.exists();
            Assertions.assertEquals(true, exists);
        } catch (Exception e) {
            // if any error occurs
            e.printStackTrace();
        } finally {
            File[] listOfFiles = tempDir.listFiles();
            if (listOfFiles.length > 0) {
                System.out.println("Number of files to delete: " + listOfFiles.length);
                for (File file : listOfFiles) {
                    file.deleteOnExit();
                    System.out.println("Deleted file: " + file.getName());
                }
            }
            try {
                FileUtils.deleteDirectory(new File(path_file));
                System.out.println("Deleted directory: " + path_file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @BeforeEach
    @AfterEach
    public void cleanup() {
        SessionFactory sessionFactory = sessionManager.getSessionFactory();
        try (Session session = sessionFactory.openSession()) {
            QueryManager.clearCaches();
            Class[] classes = new Class[]{
                    PersonEntity.class,
                    ReportAssignment.class,
                    GeoMunicipalityEntity.class,
                    GeoRoadEntity.class,
                    PostcodeEntity.class,
                    GeoLocalityEntity.class,
                    AccessAddressEntity.class,
                    UnitAddressEntity.class,
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

    /**
     * Compare two different JSON-arrays where the value of one field is ignored
     * This functionality can be used for validation of reports where the timezone during passing of input results in different dayswhen decoding a timestamp
     *
     * @param expected
     * @param actual
     * @param ignoreValue
     * @throws JsonProcessingException
     */
    public void compareJSONARRAYWithIgnoredValues(String expected, String actual, String ignoreValue) throws JsonProcessingException {
        ArrayNode expectedJsonArray = (ArrayNode) objectMapper.readTree(expected);
        ArrayNode actualJsonArray = (ArrayNode) objectMapper.readTree(actual);
        Assertions.assertEquals(expectedJsonArray.size(), actualJsonArray.size());
        for (int index = 0; index < expectedJsonArray.size(); index++) {
            ((ObjectNode) expectedJsonArray.get(index)).put(ignoreValue, "*");
            ((ObjectNode) actualJsonArray.get(index)).put(ignoreValue, "*");
        }
        Assertions.assertEquals(
                0, new UnorderedJsonListComparator().compare(expectedJsonArray, actualJsonArray),
                objectMapper.writeValueAsString(expectedJsonArray) + " != " + objectMapper.writeValueAsString(actualJsonArray)
        );
    }


    public ArrayNode csvToJson(String csv) {
        ArrayNode arrayNode = objectMapper.createArrayNode();
        String[] lines = csv.split("\n");
        String[] headers = lines[0].split(";");
        for (int i = 1; i < lines.length; i++) {
            ObjectNode objectNode = objectMapper.createObjectNode();
            String[] values = lines[i].split(";");
            for (int j = 0; j < values.length; j++) {
                objectNode.put(strip(headers[j]), strip(values[j]));
            }
            arrayNode.add(objectNode);
        }
        return arrayNode;
    }

    public String csvToJsonString(String csv) throws JsonProcessingException {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                this.csvToJson(csv)
        );
    }

    private static String strip(String subject) {
        return subject.replaceAll("^\"|\"$", "");
    }

}
