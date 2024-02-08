import dk.magenta.datafordeler.core.database.setup.SessionManager;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.geo.data.GeoEntityManager;
import dk.magenta.datafordeler.geo.data.accessaddress.AccessAddressEntityManager;
import dk.magenta.datafordeler.geo.data.building.BuildingEntityManager;
import dk.magenta.datafordeler.geo.data.locality.LocalityEntityManager;
import dk.magenta.datafordeler.geo.data.municipality.MunicipalityEntityManager;
import dk.magenta.datafordeler.geo.data.postcode.PostcodeEntityManager;
import dk.magenta.datafordeler.geo.data.road.RoadEntityManager;
import dk.magenta.datafordeler.geo.data.unitaddress.UnitAddressEntityManager;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public abstract class GeoTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private dk.magenta.datafordeler.cpr.data.road.RoadEntityManager cprRoadEntityManager;

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

    protected void loadAll() throws IOException {
        this.load(localityEntityManager, "/locality.json");
        this.load(roadEntityManager, "/road.json");//We have failures is data from GAR, If there is more than one roadentity, we use the new one find finding a roadname
        this.load(unitAddressEntityManager, "/unit.json");
        this.load(municipalityEntityManager, "/municipality.json");
        this.load(postcodeEntityManager, "/post.json");
        this.load(buildingEntityManager, "/building.json");
        this.load(accessAddressEntityManager, "/access.json");
    }

    public void loadCprAddress() throws Exception {
        InputStream testData = GeoTest.class.getResourceAsStream("/roaddata.txt");
        Session session = sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        ImportMetadata importMetadata = new ImportMetadata();
        importMetadata.setSession(session);
        importMetadata.setTransactionInProgress(true);
        cprRoadEntityManager.parseData(testData, importMetadata);
        transaction.commit();
        session.close();
    }


    @Autowired
    protected SessionManager sessionManager;

    protected ResponseEntity<String> lookup(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        HttpEntity<String> httpEntity = new HttpEntity<String>("", headers);
        return this.restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
    }

    //protected void load(GeoEntityManager entityManager, String filename) throws IOException {
    //    FileInputStream data = new FileInputStream(new File(filename));
    protected void load(GeoEntityManager entityManager, String resourceName) throws IOException {
        InputStream data = GeoTest.class.getResourceAsStream(resourceName);
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

}
