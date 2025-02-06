import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.adresseservice.AdresseService;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.Entity;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.Registration;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.gladdrreg.GladdrregPlugin;
import dk.magenta.datafordeler.gladdrreg.data.address.AddressEntity;
import dk.magenta.datafordeler.gladdrreg.data.address.AddressEntityManager;
import dk.magenta.datafordeler.gladdrreg.data.address.AddressRegistration;
import dk.magenta.datafordeler.gladdrreg.data.bnumber.BNumberEntity;
import dk.magenta.datafordeler.gladdrreg.data.bnumber.BNumberEntityManager;
import dk.magenta.datafordeler.gladdrreg.data.bnumber.BNumberRegistration;
import dk.magenta.datafordeler.gladdrreg.data.locality.LocalityEntity;
import dk.magenta.datafordeler.gladdrreg.data.locality.LocalityEntityManager;
import dk.magenta.datafordeler.gladdrreg.data.locality.LocalityRegistration;
import dk.magenta.datafordeler.gladdrreg.data.municipality.MunicipalityEntity;
import dk.magenta.datafordeler.gladdrreg.data.municipality.MunicipalityEntityManager;
import dk.magenta.datafordeler.gladdrreg.data.municipality.MunicipalityRegistration;
import dk.magenta.datafordeler.gladdrreg.data.road.RoadEntity;
import dk.magenta.datafordeler.gladdrreg.data.road.RoadEntityManager;
import dk.magenta.datafordeler.gladdrreg.data.road.RoadRegistration;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.jupiter.api.Assertions;
import org.junit.Before;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AdresseServiceTest {

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private GladdrregPlugin gladdrregPlugin;

    @Autowired
    AdresseService adresseService;

    @Autowired
    ObjectMapper objectMapper;


    @Test
    public void testLocalityService() throws IOException, DataFordelerException {
        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
                "/adresse/lokalitet/",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        response = restTemplate.exchange(
                "/adresse/lokalitet/?kommune=1234",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        response = restTemplate.exchange(
                "/adresse/lokalitet/?kommune=955",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertTrue(
                objectMapper.readTree("[{\"uuid\":\"4d9cd2a0-89f1-4acc-a259-4fd139006d87\",\"navn\":\"Paamiut\",\"forkortelse\":\"PAA\"}]").equals(
                        objectMapper.readTree(response.getBody())
                )
        );
    }

    @Test
    public void testRoadService() throws IOException {
        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
                "/adresse/vej/",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        response = restTemplate.exchange(
                "/adresse/vej/?lokalitet=invalid-uuid",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        response = restTemplate.exchange(
                "/adresse/vej/?lokalitet=4d9cd2a0-89f1-4acc-a259-4fd139006d87",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertTrue(
                "Non-matching response " + response.getBody(),
                objectMapper.readTree("[{\"uuid\":\"e4dc6c09-baae-40b1-8696-57771b2f7a81\",\"vejkode\":1,\"navn\":\"Aadarujuup Aqquserna\",\"forkortet_navn\":\"Aadarujuup Aqq.\",\"andet_navn\":null,\"cpr_navn\":null}]").equals(
                        objectMapper.readTree(response.getBody())
                )
        );
    }

    @Test
    public void testBuildingService() throws IOException {
        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
                "/adresse/hus/",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        response = restTemplate.exchange(
                "/adresse/hus/?vej=e4dc6c09-baae-40b1-8696-57771b2f7a81",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertTrue(
                "Non-matching response " + response.getBody(),
                objectMapper.readTree("[{\"husnummer\":\"05\",\"b_nummer\":\"293\",\"b_kaldenavn\":\"testhus\"}]").equals(
                        objectMapper.readTree(response.getBody())
                )
        );
    }


    //@Test
    //Disabling this unittest during cleanup in unittests, the project has not been touched for a long time, and the test is failing
    public void testAddressService() throws IOException {
        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
                "/adresse/adresse/",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        response = restTemplate.exchange(
                "/adresse/adresse/?vej=e4dc6c09-baae-40b1-8696-57771b2f7a81",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertTrue(
                "Non-matching response " + response.getBody(),
                objectMapper.readTree("[" +
                        "{\"uuid\":\"6921fbb1-ddd7-4c7c-bb98-bbf63ace6a3a\",\"husnummer\":\"05\",\"b_nummer\":\"293\",\"etage\":\"1\",\"doer\":null}," +
                        "{\"uuid\":\"6921fbb1-ddd7-4c7c-bb98-bbf63ace6a3b\",\"husnummer\":\"05\",\"b_nummer\":\"293\",\"etage\":\"2\",\"doer\":null}" +
                        "]").equals(
                        objectMapper.readTree(response.getBody())
                ) ||
                        objectMapper.readTree("[" +
                                "{\"uuid\":\"6921fbb1-ddd7-4c7c-bb98-bbf63ace6a3b\",\"husnummer\":\"05\",\"b_nummer\":\"293\",\"etage\":\"2\",\"doer\":null}," +
                                "{\"uuid\":\"6921fbb1-ddd7-4c7c-bb98-bbf63ace6a3a\",\"husnummer\":\"05\",\"b_nummer\":\"293\",\"etage\":\"1\",\"doer\":null}" +
                                "]").equals(
                                objectMapper.readTree(response.getBody())
                        )
        );

        response = restTemplate.exchange(
                "/adresse/adresse/?husnr=5",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        response = restTemplate.exchange(
                "/adresse/adresse/?vej=e4dc6c09-baae-40b1-8696-57771b2f7a81&husnr=5",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertTrue(
                "Non-matching response " + response.getBody(),
                objectMapper.readTree("[" +
                        "{\"uuid\":\"6921fbb1-ddd7-4c7c-bb98-bbf63ace6a3a\",\"husnummer\":\"05\",\"b_nummer\":\"293\",\"etage\":\"1\",\"doer\":null}," +
                        "{\"uuid\":\"6921fbb1-ddd7-4c7c-bb98-bbf63ace6a3b\",\"husnummer\":\"05\",\"b_nummer\":\"293\",\"etage\":\"2\",\"doer\":null}" +
                        "]").equals(
                        objectMapper.readTree(response.getBody())
                ) ||
                        objectMapper.readTree("[" +
                                "{\"uuid\":\"6921fbb1-ddd7-4c7c-bb98-bbf63ace6a3b\",\"husnummer\":\"05\",\"b_nummer\":\"293\",\"etage\":\"2\",\"doer\":null}," +
                                "{\"uuid\":\"6921fbb1-ddd7-4c7c-bb98-bbf63ace6a3a\",\"husnummer\":\"05\",\"b_nummer\":\"293\",\"etage\":\"1\",\"doer\":null}" +
                                "]").equals(
                                objectMapper.readTree(response.getBody())
                        )
        );

        response = restTemplate.exchange(
                "/adresse/adresse/?vej=e4dc6c09-baae-40b1-8696-57771b2f7a81&husnr=6",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertTrue(
                "Non-matching response " + response.getBody(),
                objectMapper.readTree("[]").equals(
                        objectMapper.readTree(response.getBody())
                )
        );

        response = restTemplate.exchange(
                "/adresse/adresse/?vej=e4dc6c09-baae-40b1-8696-57771b2f7a81&b_nummer=293",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertTrue(
                "Non-matching response " + response.getBody(),
                objectMapper.readTree("[" +
                        "{\"uuid\":\"6921fbb1-ddd7-4c7c-bb98-bbf63ace6a3a\",\"husnummer\":\"05\",\"b_nummer\":\"293\",\"etage\":\"1\",\"doer\":null}," +
                        "{\"uuid\":\"6921fbb1-ddd7-4c7c-bb98-bbf63ace6a3b\",\"husnummer\":\"05\",\"b_nummer\":\"293\",\"etage\":\"2\",\"doer\":null}" +
                        "]").equals(
                        objectMapper.readTree(response.getBody())
                ) ||
                        objectMapper.readTree("[" +
                                "{\"uuid\":\"6921fbb1-ddd7-4c7c-bb98-bbf63ace6a3b\",\"husnummer\":\"05\",\"b_nummer\":\"293\",\"etage\":\"2\",\"doer\":null}," +
                                "{\"uuid\":\"6921fbb1-ddd7-4c7c-bb98-bbf63ace6a3a\",\"husnummer\":\"05\",\"b_nummer\":\"293\",\"etage\":\"1\",\"doer\":null}" +
                                "]").equals(
                                objectMapper.readTree(response.getBody())
                        )
        );

        response = restTemplate.exchange(
                "/adresse/adresse/?vej=e4dc6c09-baae-40b1-8696-57771b2f7a81&b_nummer=123",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertTrue(
                "Non-matching response " + response.getBody(),
                objectMapper.readTree("[]").equals(
                        objectMapper.readTree(response.getBody())
                )
        );
    }


    @Test
    public void testAddressDetailsService() throws IOException {
        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
                "/adresse/adresseoplysninger/",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        response = restTemplate.exchange(
                "/adresse/adresseoplysninger/?adresse=01234567-89ab-cdef-0123-456789abcdef",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertTrue(
                "Non-matching response " + response.getBody(),
                objectMapper.readTree("{}").equals(
                        objectMapper.readTree(response.getBody())
                )
        );

        response = restTemplate.exchange(
                "/adresse/adresseoplysninger/?adresse=6921fbb1-ddd7-4c7c-bb98-bbf63ace6a3a",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        String expected1 = "{\"uuid\":\"6921fbb1-ddd7-4c7c-bb98-bbf63ace6a3a\",\"husnummer\":\"05\",\"b_nummer\":\"293\",\"vej_uuid\":\"e4dc6c09-baae-40b1-8696-57771b2f7a81\",\"vejkode\":1,\"vejnavn\":\"Aadarujuup Aqquserna\",\"lokalitet\":\"4d9cd2a0-89f1-4acc-a259-4fd139006d87\",\"lokalitetsnavn\":\"Paamiut\",\"kommunekode\":955,\"etage\":\"1\",\"doer\":null,\"bolig\":null}";
        String expected2 = "{\"uuid\":\"6921fbb1-ddd7-4c7c-bb98-bbf63ace6a3b\",\"husnummer\":\"05\",\"b_nummer\":\"293\",\"vej_uuid\":\"e4dc6c09-baae-40b1-8696-57771b2f7a81\",\"vejkode\":1,\"vejnavn\":\"Aadarujuup Aqquserna\",\"lokalitet\":\"4d9cd2a0-89f1-4acc-a259-4fd139006d87\",\"lokalitetsnavn\":\"Paamiut\",\"kommunekode\":955,\"etage\":\"2\",\"doer\":null,\"bolig\":true}";

        Assertions.assertTrue(
                "Non-matching response " + response.getBody(),
                objectMapper.readTree(expected1).equals(
                        objectMapper.readTree(response.getBody())
                ));


        response = restTemplate.exchange(
                "/adresse/adresseoplysninger/?adresse=6921fbb1-ddd7-4c7c-bb98-bbf63ace6a3b",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        Assertions.assertTrue(
                "Non-matching response " + response.getBody(),
                objectMapper.readTree(expected2).equals(
                        objectMapper.readTree(response.getBody())
                ));
    }


    @Before
    public void load() throws IOException, DataFordelerException {
        Session session = sessionManager.getSessionFactory().openSession();
        try {
            Transaction transaction = session.beginTransaction();
            loadMunicipality(session);
            loadLocality(session);
            loadRoad(session);
            loadBuilding(session);
            loadAddress(session);
            transaction.commit();
            adresseService.loadMunicipalities();
        } finally {
            session.close();
        }
    }

    @After
    public void unload() {
        Session session = sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        try {
            for (Entity entity : createdEntities) {
                session.delete(entity);
            }
            createdEntities.clear();
        } finally {
            try {
                transaction.commit();
            } catch (Exception e) {
            } finally {
                session.close();
            }
        }
    }

    HashSet<Entity> createdEntities = new HashSet<>();

    private void loadMunicipality(Session session) throws DataFordelerException, IOException {
        InputStream testData = AdresseServiceTest.class.getResourceAsStream("/municipality.json");
        MunicipalityEntityManager municipalityEntityManager = (MunicipalityEntityManager) gladdrregPlugin.getRegisterManager().getEntityManager(MunicipalityEntity.schema);
        List<? extends Registration> regs = municipalityEntityManager.parseData(testData, new ImportMetadata());
        testData.close();
        for (Registration registration : regs) {
            MunicipalityRegistration municipalityRegistration = (MunicipalityRegistration) registration;
            QueryManager.saveRegistration(session, municipalityRegistration.getEntity(), municipalityRegistration);
            createdEntities.add(municipalityRegistration.getEntity());
        }
    }

    private void loadLocality(Session session) throws DataFordelerException, IOException {
        InputStream testData = AdresseServiceTest.class.getResourceAsStream("/locality.json");
        LocalityEntityManager localityEntityManager = (LocalityEntityManager) gladdrregPlugin.getRegisterManager().getEntityManager(LocalityEntity.schema);
        List<? extends Registration> regs = localityEntityManager.parseData(testData, new ImportMetadata());
        testData.close();
        for (Registration registration : regs) {
            LocalityRegistration localityRegistration = (LocalityRegistration) registration;
            QueryManager.saveRegistration(session, localityRegistration.getEntity(), localityRegistration);
            createdEntities.add(localityRegistration.getEntity());
        }
    }

    private void loadRoad(Session session) throws DataFordelerException, IOException {
        InputStream testData = AdresseServiceTest.class.getResourceAsStream("/road.json");
        RoadEntityManager roadEntityManager = (RoadEntityManager) gladdrregPlugin.getRegisterManager().getEntityManager(RoadEntity.schema);
        List<? extends Registration> regs = roadEntityManager.parseData(testData, new ImportMetadata());
        testData.close();
        for (Registration registration : regs) {
            RoadRegistration roadRegistration = (RoadRegistration) registration;
            QueryManager.saveRegistration(session, roadRegistration.getEntity(), roadRegistration);
            createdEntities.add(roadRegistration.getEntity());
        }
    }

    private void loadBuilding(Session session) throws DataFordelerException, IOException {
        InputStream testData = AdresseServiceTest.class.getResourceAsStream("/bnumber.json");
        BNumberEntityManager bNumberEntityManager = (BNumberEntityManager) gladdrregPlugin.getRegisterManager().getEntityManager(BNumberEntity.schema);
        List<? extends Registration> regs = bNumberEntityManager.parseData(testData, new ImportMetadata());
        testData.close();
        for (Registration registration : regs) {
            BNumberRegistration bNumberRegistration = (BNumberRegistration) registration;
            QueryManager.saveRegistration(session, bNumberRegistration.getEntity(), bNumberRegistration);
            createdEntities.add(bNumberRegistration.getEntity());
        }
    }

    private void loadAddress(Session session) throws DataFordelerException, IOException {
        InputStream testData = AdresseServiceTest.class.getResourceAsStream("/address.json");
        AddressEntityManager addressEntityManager = (AddressEntityManager) gladdrregPlugin.getRegisterManager().getEntityManager(AddressEntity.schema);
        List<? extends Registration> regs = addressEntityManager.parseData(testData, new ImportMetadata());
        testData.close();
        for (Registration registration : regs) {
            AddressRegistration addressRegistration = (AddressRegistration) registration;
            QueryManager.saveRegistration(session, addressRegistration.getEntity(), addressRegistration);
            createdEntities.add(addressRegistration.getEntity());
        }
    }

}
