import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.geo.data.accessaddress.AccessAddressEntity;
import dk.magenta.datafordeler.geo.data.building.BuildingEntity;
import dk.magenta.datafordeler.geo.data.locality.GeoLocalityEntity;
import dk.magenta.datafordeler.geo.data.municipality.GeoMunicipalityEntity;
import dk.magenta.datafordeler.geo.data.postcode.PostcodeEntity;
import dk.magenta.datafordeler.geo.data.road.GeoRoadEntity;
import dk.magenta.datafordeler.geo.data.unitaddress.UnitAddressEntity;
import org.hibernate.Session;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestParse extends GeoTest {

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private ObjectMapper objectMapper;


    private ResponseEntity<String> restSearch(ParameterMap parameters, String type) {
        return this.lookup("/geo/" + type + "/1/rest/search?" + parameters.asUrlParams());
    }

    private ResponseEntity<String> uuidSearch(String id, String type) {
        return this.lookup("/geo/" + type + "/1/rest/" + id);
    }

    @Before
    public void initialize() throws Exception {
        this.loadAll();
        this.loadCprAddress();
    }

    @Test
    public void testMunicipality() throws IOException {

        Session session = sessionManager.getSessionFactory().openSession();
        try {
            GeoMunicipalityEntity entity = QueryManager.getEntity(session, UUID.fromString("33960e68-2f0a-4cb0-bb3d-02d9f0b21304"), GeoMunicipalityEntity.class);
            Assert.assertNotNull(entity);
            Assert.assertEquals(956, entity.getCode());
            Assert.assertTrue(OffsetDateTime.parse("2018-07-19T11:11:05Z").isEqual(entity.getCreationDate()));
            Assert.assertEquals("GREENADMIN", entity.getCreator());
            Assert.assertEquals(1, entity.getShape().size());
            Assert.assertEquals("5cc15446cddb4633b6832847b6f5d66d", entity.getSumiffiikId());
            Assert.assertEquals(1, entity.getName().size());
            Assert.assertEquals("Kommuneqarfik Sermersooq", entity.getName().iterator().next().getName());
        } finally {
            session.close();
        }

        ResponseEntity<String> response = this.uuidSearch(GeoMunicipalityEntity.generateUUID(956).toString(), "municipality");
        Assert.assertEquals(200, response.getStatusCode().value());
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                objectMapper.readTree(response.getBody())
        ));
    }


    @Test
    public void testLocality() throws IOException {

        Session session = sessionManager.getSessionFactory().openSession();
        try {
            GeoLocalityEntity entity = QueryManager.getEntity(session, UUID.fromString("f0966470-f09f-474d-a820-e8a46ed6fcc7"), GeoLocalityEntity.class);
            Assert.assertNotNull(entity);
            Assert.assertEquals("0600", entity.getCode());
            Assert.assertTrue(OffsetDateTime.parse("2018-08-09T12:00:04Z").isEqual(entity.getCreationDate()));
            Assert.assertEquals("GREENADMIN", entity.getCreator());
            Assert.assertEquals(1, entity.getName().size());
            Assert.assertEquals("Nuuk", entity.getName().iterator().next().getName());
            Assert.assertTrue(OffsetDateTime.parse("2018-08-09T13:11:42Z").isEqual(entity.getName().iterator().next().getRegistrationFrom()));
            Assert.assertEquals(1, entity.getName().size());
            Assert.assertEquals("NUK", entity.getAbbreviation().iterator().next().getName());
            Assert.assertTrue(OffsetDateTime.parse("2018-08-09T13:11:42Z").isEqual(entity.getName().iterator().next().getRegistrationFrom()));
            Assert.assertEquals(1, entity.getName().size());
            Assert.assertTrue(OffsetDateTime.parse("2018-08-09T13:11:42Z").isEqual(entity.getName().iterator().next().getRegistrationFrom()));
            Assert.assertEquals(1, entity.getShape().size());
            Assert.assertEquals(1, entity.getMunicipality().size());
            Assert.assertEquals(Integer.valueOf(956), entity.getMunicipality().iterator().next().getCode());
            Assert.assertTrue(OffsetDateTime.parse("2018-08-09T13:11:42Z").isEqual(entity.getMunicipality().iterator().next().getRegistrationFrom()));
        } finally {
            session.close();
        }

        ResponseEntity<String> response = this.uuidSearch("F0966470-F09F-474D-A820-E8A46ED6FCC7", "locality");
        Assert.assertEquals(200, response.getStatusCode().value());
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                objectMapper.readTree(response.getBody())
        ));
    }


    @Test
    public void testRoad() throws IOException {

        Session session = sessionManager.getSessionFactory().openSession();
        try {
            GeoRoadEntity entity = QueryManager.getEntity(session, UUID.fromString("E1274F15-9E2B-4B6E-8B7D-C8078DF65AA2"), GeoRoadEntity.class);
            Assert.assertNotNull(entity);
            Assert.assertEquals(254, entity.getCode());
            Assert.assertTrue(OffsetDateTime.parse("2018-08-23T14:48:05Z").isEqual(entity.getCreationDate()));
            Assert.assertEquals("IRKS", entity.getCreator());
            Assert.assertEquals("{961FFC61-8B04-45F3-80FD-509B0676FEF6}", entity.getSumiffiikId());
            Assert.assertEquals(1, entity.getShape().size());
            Assert.assertEquals(1, entity.getName().size());
            Assert.assertEquals("Qarsaalik", entity.getName().iterator().next().getName());
            Assert.assertTrue(OffsetDateTime.parse("2018-08-23T14:48:05Z").isEqual(entity.getName().iterator().next().getRegistrationFrom()));
        } finally {
            session.close();
        }

        ResponseEntity<String> response = this.uuidSearch("E1274F15-9E2B-4B6E-8B7D-C8078DF65AA2", "road");
        Assert.assertEquals(200, response.getStatusCode().value());
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                objectMapper.readTree(response.getBody())
        ));

    }


    @Test
    public void testBuilding() throws IOException {

        Session session = sessionManager.getSessionFactory().openSession();
        try {
            BuildingEntity entity = QueryManager.getEntity(session, UUID.fromString("AF3550F5-2998-404D-B784-A70C4DEB2A18"), BuildingEntity.class);
            Assert.assertNotNull(entity);
            Assert.assertEquals(null, entity.getAnr());
            Assert.assertEquals("B-3197", entity.getBnr());
            Assert.assertEquals("{DC2CAE1B-1F98-44FF-AE8F-6A52556B13FD}", entity.getSumiffiikId());
            Assert.assertTrue(OffsetDateTime.parse("2017-09-29T16:12:36Z").isEqual(entity.getCreationDate()));
            Assert.assertEquals("thard_nukissiorfiit", entity.getCreator());
            Assert.assertEquals("0600", entity.getLocality().iterator().next().getCode());
            Assert.assertTrue(OffsetDateTime.parse("2018-08-29T10:31:16Z").isEqual(entity.getLocality().iterator().next().getRegistrationFrom()));
            Assert.assertEquals(1, entity.getShape().size());
        } finally {
            session.close();
        }

        ResponseEntity<String> response = this.uuidSearch("AF3550F5-2998-404D-B784-A70C4DEB2A18", "building");
        Assert.assertEquals(200, response.getStatusCode().value());
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                objectMapper.readTree(response.getBody())
        ));
    }


    @Test
    public void testAccessAddress() throws IOException {

        Session session = sessionManager.getSessionFactory().openSession();
        try {
            AccessAddressEntity entity = QueryManager.getEntity(session, UUID.fromString("2E3776BF-05C2-433C-ADB9-8A07DF6B3E8F"), AccessAddressEntity.class);
            Assert.assertNotNull(entity);
            Assert.assertEquals("B-3197", entity.getBnr());
            Assert.assertTrue(OffsetDateTime.parse("2018-08-23T14:48:05Z").isEqual(entity.getCreationDate()));
            Assert.assertEquals("IRKS", entity.getCreator());
            Assert.assertEquals("{69231C66-F37A-4F78-80C1-E379BFEE165D}", entity.getSumiffiikId());
            Assert.assertEquals(1, entity.getShape().size());
            Assert.assertEquals(1, entity.getSource().size());
            Assert.assertEquals(Integer.valueOf(1), entity.getSource().iterator().next().getSource());
            Assert.assertTrue(OffsetDateTime.parse("2018-08-29T10:42:07Z").isEqual(entity.getSource().iterator().next().getRegistrationFrom()));
            Assert.assertEquals(1, entity.getHouseNumber().size());
            Assert.assertEquals("18", entity.getHouseNumber().iterator().next().getNumber());
            Assert.assertTrue(OffsetDateTime.parse("2018-08-29T10:42:07Z").isEqual(entity.getHouseNumber().iterator().next().getRegistrationFrom()));
            Assert.assertEquals(1, entity.getBlockName().size());
            Assert.assertEquals("House of Testing!", entity.getBlockName().iterator().next().getName());
            Assert.assertTrue(OffsetDateTime.parse("2018-08-29T10:42:07Z").isEqual(entity.getBlockName().iterator().next().getRegistrationFrom()));
            Assert.assertEquals(1, entity.getLocality().size());
            Assert.assertEquals("0600", entity.getLocality().iterator().next().getCode());
            Assert.assertTrue(OffsetDateTime.parse("2018-08-29T10:42:07Z").isEqual(entity.getLocality().iterator().next().getRegistrationFrom()));
            Assert.assertEquals(1, entity.getStatus().size());
            Assert.assertEquals(Integer.valueOf(2), entity.getStatus().iterator().next().getStatus());
            Assert.assertTrue(OffsetDateTime.parse("2018-08-29T10:42:07Z").isEqual(entity.getStatus().iterator().next().getRegistrationFrom()));
            Assert.assertEquals(1, entity.getBuilding().size());
            Assert.assertEquals("af3550f5-2998-404d-b784-a70c4deb2a18", entity.getBuilding().iterator().next().getReference().getUuid().toString());
            Assert.assertEquals(1, entity.getPostcode().size());
            Assert.assertEquals("867af985-d281-3d38-99bd-c96549c8790d", entity.getPostcode().iterator().next().getReference().getUuid().toString());

            Assert.assertEquals(1, entity.getRoad().size());
            Assert.assertEquals(Integer.valueOf(956), entity.getRoad().iterator().next().getMunicipalityCode());
            Assert.assertEquals(Integer.valueOf(254), entity.getRoad().iterator().next().getRoadCode());
            Assert.assertTrue(OffsetDateTime.parse("2018-08-29T10:42:07Z").isEqual(entity.getRoad().iterator().next().getRegistrationFrom()));

        } finally {
            session.close();
        }

        ResponseEntity<String> response = this.uuidSearch("2E3776BF-05C2-433C-ADB9-8A07DF6B3E8F", "accessaddress");
        Assert.assertEquals(200, response.getStatusCode().value());
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                objectMapper.readTree(response.getBody())
        ));
    }


    @Test
    public void testUnitAddress() throws IOException {

        Session session = sessionManager.getSessionFactory().openSession();
        try {
            UnitAddressEntity entity = QueryManager.getEntity(session, UUID.fromString("1B3AC64B-C28D-40B2-A106-16CEE7C188B8"), UnitAddressEntity.class);
            Assert.assertNotNull(entity);
            Assert.assertTrue(OffsetDateTime.parse("2018-08-29T10:21:17Z").isEqual(entity.getCreationDate()));
            Assert.assertEquals("TELDWST", entity.getCreator());
            Assert.assertEquals("{EE4805F1-96AB-45A7-B045-ED4D3C28364C}", entity.getSumiffiikId());
            Assert.assertEquals(1, entity.getUsage().size());
            Assert.assertEquals(Integer.valueOf(1), entity.getUsage().iterator().next().getUsage());
            Assert.assertEquals(1, entity.getFloor().size());
            Assert.assertEquals("kld", entity.getFloor().iterator().next().getFloor());
            Assert.assertEquals(1, entity.getNumber().size());
            Assert.assertEquals("5678", entity.getNumber().iterator().next().getNumber());
            Assert.assertEquals(1, entity.getSource().size());
            Assert.assertEquals(Integer.valueOf(1), entity.getSource().iterator().next().getSource());
            Assert.assertEquals(1, entity.getStatus().size());
            Assert.assertEquals(Integer.valueOf(2), entity.getStatus().iterator().next().getStatus());
        } finally {
            session.close();
        }

        ResponseEntity<String> response = this.uuidSearch("1B3AC64B-C28D-40B2-A106-16CEE7C188B8", "unitaddress");
        Assert.assertEquals(200, response.getStatusCode().value());
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                objectMapper.readTree(response.getBody())
        ));
    }

    @Test
    public void testPostcode() throws IOException {

        Session session = sessionManager.getSessionFactory().openSession();
        try {
            PostcodeEntity entity = QueryManager.getEntity(session, PostcodeEntity.generateUUID(3900), PostcodeEntity.class);
            Assert.assertNotNull(entity);
            Assert.assertEquals(3900, entity.getCode());
            Assert.assertEquals("Nuuk", entity.getName().iterator().next().getName());
        } finally {
            session.close();
        }

        ResponseEntity<String> response = this.uuidSearch(PostcodeEntity.generateUUID(3900).toString(), "postcode");
        Assert.assertEquals(200, response.getStatusCode().value());
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                objectMapper.readTree(response.getBody())
        ));
    }

}
