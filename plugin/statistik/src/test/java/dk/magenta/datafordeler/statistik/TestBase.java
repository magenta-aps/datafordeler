package dk.magenta.datafordeler.statistik;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.io.ImportInputStream;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.LabeledSequenceInputStream;
import dk.magenta.datafordeler.core.util.UnorderedJsonListComparator;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonEntityManager;
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
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.mockito.Mockito.when;

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

    @MockitoSpyBean
    protected DafoUserManager dafoUserManager;

    @Autowired
    private PersonEntityManager personEntityManager;

    @Autowired
    private dk.magenta.datafordeler.cpr.data.road.RoadEntityManager cprRoadEntityManager;


    public void applyAccess(TestUserDetails testUserDetails) {
        when(dafoUserManager.getFallbackUser()).thenReturn(testUserDetails);
    }

    protected void loadAllGeoAdress() throws IOException {
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
                    session.remove(e);
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



    public void loadPersonData(File source) throws Exception {
        loadPersonData(new FileInputStream(source));
    }

    public void loadPersonData(String resource) throws Exception {
        loadPersonData(
                new ImportInputStream(
                        new LabeledSequenceInputStream(
                                Collections.singletonList(
                                        Pair.of(
                                                resource,
                                                TestBase.class.getResourceAsStream("/" + resource)
                                        )
                                )
                        )
                )
        );
    }


    public void loadRoadData(String resource) throws Exception {
        loadRoadData(
                new ImportInputStream(
                        new LabeledSequenceInputStream(
                                Collections.singletonList(
                                        Pair.of(
                                                resource,
                                                TestBase.class.getResourceAsStream("/" + resource)
                                        )
                                )
                        )
                )
        );
    }


    public void loadRoadData(InputStream testData) throws Exception {
        ImportMetadata importMetadata = new ImportMetadata();
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            importMetadata.setSession(session);
            cprRoadEntityManager.parseData(testData, importMetadata);
        }
        testData.close();
    }

    public void loadGeoRoadData(String resource) throws DataFordelerException {
        loadGeoRoadData(
                new ImportInputStream(
                        new LabeledSequenceInputStream(
                                Collections.singletonList(
                                        Pair.of(
                                                resource,
                                                TestBase.class.getResourceAsStream("/" + resource)
                                        )
                                )
                        )
                )
        );
    }


    public void loadGeoRoadData(InputStream testData) throws DataFordelerException {
        ImportMetadata importMetadata = new ImportMetadata();
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            importMetadata.setSession(session);
            Transaction transaction = session.beginTransaction();
            try {
                importMetadata.setTransactionInProgress(true);
                roadEntityManager.parseData(testData, importMetadata);
                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
                throw e;
            }
        }
    }

    public void loadGeoLocalityData(String resource) throws DataFordelerException {
        loadGeoLocalityData(
                new ImportInputStream(
                        new LabeledSequenceInputStream(
                                Collections.singletonList(
                                        Pair.of(
                                                resource,
                                                TestBase.class.getResourceAsStream("/" + resource)
                                        )
                                )
                        )
                )
        );
    }


    public void loadGeoLocalityData(InputStream testData) throws DataFordelerException {
        ImportMetadata importMetadata = new ImportMetadata();
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            importMetadata.setSession(session);
            Transaction transaction = session.beginTransaction();
            try {
                importMetadata.setTransactionInProgress(true);
                localityEntityManager.parseData(testData, importMetadata);
                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
                throw e;
            }
        }
    }


    public void loadAccessLocalityData(String resource) throws DataFordelerException {
        loadAccessLocalityData(
                new ImportInputStream(
                        new LabeledSequenceInputStream(
                                Collections.singletonList(
                                        Pair.of(
                                                resource,
                                                TestBase.class.getResourceAsStream("/" + resource)
                                        )
                                )
                        )
                )
        );
    }


    public void loadAccessLocalityData(InputStream testData) throws DataFordelerException {
        ImportMetadata importMetadata = new ImportMetadata();
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            importMetadata.setSession(session);
            Transaction transaction = session.beginTransaction();
            try {
                importMetadata.setTransactionInProgress(true);
                accessAddressEntityManager.parseData(testData, importMetadata);
                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
                throw e;
            }
        }
    }


    public void loadPostalLocalityData(String resource) throws DataFordelerException {
        loadPostalLocalityData(
                new ImportInputStream(
                        new LabeledSequenceInputStream(
                                Collections.singletonList(
                                        Pair.of(
                                                resource,
                                                TestBase.class.getResourceAsStream("/" + resource)
                                        )
                                )
                        )
                )
        );
    }


    public void loadPostalLocalityData(InputStream testData) throws DataFordelerException {
        ImportMetadata importMetadata = new ImportMetadata();
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            importMetadata.setSession(session);
            Transaction transaction = session.beginTransaction();
            try {
                importMetadata.setTransactionInProgress(true);
                postcodeEntityManager.parseData(testData, importMetadata);
                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
                throw e;
            }
        }
    }


    public void loadPersonData(InputStream testData) throws Exception {
        ImportMetadata importMetadata = new ImportMetadata();
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            importMetadata.setSession(session);
            Transaction transaction = session.beginTransaction();
            importMetadata.setTransactionInProgress(true);
            try {
                personEntityManager.parseData(testData, importMetadata);
                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
                throw e;
            }
        }
    }

    public <E extends DatabaseEntry> void deleteAll(Class<E> eClass) {
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                Collection<E> entities = QueryManager.getAllEntities(session, eClass);
                for (E entity : entities) {
                    session.remove(entity);
                }
                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
                throw e;
            }
        }
    }

    public void deleteAll() {
        this.deleteAll(PersonEntity.class);
    }


}
