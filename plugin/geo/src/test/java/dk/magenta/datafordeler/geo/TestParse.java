package dk.magenta.datafordeler.geo;

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
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.UUID;


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
            Assertions.assertNotNull(entity);
            Assertions.assertEquals(956, entity.getCode());
            Assertions.assertTrue(OffsetDateTime.parse("2018-07-19T11:11:05Z").isEqual(entity.getCreationDate()));
            Assertions.assertEquals("GREENADMIN", entity.getCreator());
            Assertions.assertEquals(1, entity.getShape().size());
            Assertions.assertEquals("5cc15446cddb4633b6832847b6f5d66d", entity.getSumiffiikId());
            Assertions.assertEquals(1, entity.getName().size());
            Assertions.assertEquals("Kommuneqarfik Sermersooq", entity.getName().iterator().next().getName());
        } finally {
            session.close();
        }

        ResponseEntity<String> response = this.uuidSearch(GeoMunicipalityEntity.generateUUID(956).toString(), "municipality");
        Assertions.assertEquals(200, response.getStatusCode().value());
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                objectMapper.readTree(response.getBody())
        ));
    }


    @Test
    public void testLocality() throws IOException {

        Session session = sessionManager.getSessionFactory().openSession();
        try {
            GeoLocalityEntity entity = QueryManager.getEntity(session, UUID.fromString("f0966470-f09f-474d-a820-e8a46ed6fcc7"), GeoLocalityEntity.class);
            Assertions.assertNotNull(entity);
            Assertions.assertEquals("0600", entity.getCode());
            Assertions.assertTrue(OffsetDateTime.parse("2018-08-09T12:00:04Z").isEqual(entity.getCreationDate()));
            Assertions.assertEquals("GREENADMIN", entity.getCreator());
            Assertions.assertEquals(1, entity.getName().size());
            Assertions.assertEquals("Nuuk", entity.getName().iterator().next().getName());
            Assertions.assertTrue(OffsetDateTime.parse("2018-08-09T13:11:42Z").isEqual(entity.getName().iterator().next().getRegistrationFrom()));
            Assertions.assertEquals(1, entity.getName().size());
            Assertions.assertEquals("NUK", entity.getAbbreviation().iterator().next().getName());
            Assertions.assertTrue(OffsetDateTime.parse("2018-08-09T13:11:42Z").isEqual(entity.getName().iterator().next().getRegistrationFrom()));
            Assertions.assertEquals(1, entity.getName().size());
            Assertions.assertTrue(OffsetDateTime.parse("2018-08-09T13:11:42Z").isEqual(entity.getName().iterator().next().getRegistrationFrom()));
            Assertions.assertEquals(1, entity.getShape().size());
            Assertions.assertEquals(1, entity.getMunicipality().size());
            Assertions.assertEquals(Integer.valueOf(956), entity.getMunicipality().iterator().next().getCode());
            Assertions.assertTrue(OffsetDateTime.parse("2018-08-09T13:11:42Z").isEqual(entity.getMunicipality().iterator().next().getRegistrationFrom()));
        } finally {
            session.close();
        }

        ResponseEntity<String> response = this.uuidSearch("F0966470-F09F-474D-A820-E8A46ED6FCC7", "locality");
        Assertions.assertEquals(200, response.getStatusCode().value());
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                objectMapper.readTree(response.getBody())
        ));
    }


    @Test
    public void testRoad() throws IOException {

        Session session = sessionManager.getSessionFactory().openSession();
        try {
            GeoRoadEntity entity = QueryManager.getEntity(session, UUID.fromString("E1274F15-9E2B-4B6E-8B7D-C8078DF65AA2"), GeoRoadEntity.class);
            Assertions.assertNotNull(entity);
            Assertions.assertEquals(254, entity.getCode());
            Assertions.assertTrue(OffsetDateTime.parse("2018-08-23T14:48:05Z").isEqual(entity.getCreationDate()));
            Assertions.assertEquals("IRKS", entity.getCreator());
            Assertions.assertEquals("{961FFC61-8B04-45F3-80FD-509B0676FEF6}", entity.getSumiffiikId());
            Assertions.assertEquals(1, entity.getShape().size());
            Assertions.assertEquals(1, entity.getName().size());
            Assertions.assertEquals("Qarsaalik", entity.getName().iterator().next().getName());
            Assertions.assertTrue(OffsetDateTime.parse("2018-08-23T14:48:05Z").isEqual(entity.getName().iterator().next().getRegistrationFrom()));
        } finally {
            session.close();
        }

        ResponseEntity<String> response = this.uuidSearch("E1274F15-9E2B-4B6E-8B7D-C8078DF65AA2", "road");
        Assertions.assertEquals(200, response.getStatusCode().value());
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                objectMapper.readTree(response.getBody())
        ));

    }


    @Test
    public void testBuilding() throws IOException {

        Session session = sessionManager.getSessionFactory().openSession();
        try {
            BuildingEntity entity = QueryManager.getEntity(session, UUID.fromString("AF3550F5-2998-404D-B784-A70C4DEB2A18"), BuildingEntity.class);
            Assertions.assertNotNull(entity);
            Assertions.assertEquals(null, entity.getAnr());
            Assertions.assertEquals("B-3197", entity.getBnr());
            Assertions.assertEquals("{DC2CAE1B-1F98-44FF-AE8F-6A52556B13FD}", entity.getSumiffiikId());
            Assertions.assertTrue(OffsetDateTime.parse("2017-09-29T16:12:36Z").isEqual(entity.getCreationDate()));
            Assertions.assertEquals("thard_nukissiorfiit", entity.getCreator());
            Assertions.assertEquals("0600", entity.getLocality().iterator().next().getCode());
            Assertions.assertTrue(OffsetDateTime.parse("2018-08-29T10:31:16Z").isEqual(entity.getLocality().iterator().next().getRegistrationFrom()));
            Assertions.assertEquals(1, entity.getShape().size());
        } finally {
            session.close();
        }

        ResponseEntity<String> response = this.uuidSearch("AF3550F5-2998-404D-B784-A70C4DEB2A18", "building");
        Assertions.assertEquals(200, response.getStatusCode().value());
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                objectMapper.readTree(response.getBody())
        ));
    }


    @Test
    public void testAccessAddress() throws IOException {

        Session session = sessionManager.getSessionFactory().openSession();
        try {
            AccessAddressEntity entity = QueryManager.getEntity(session, UUID.fromString("2E3776BF-05C2-433C-ADB9-8A07DF6B3E8F"), AccessAddressEntity.class);
            Assertions.assertNotNull(entity);
            Assertions.assertEquals("B-3197", entity.getBnr());
            Assertions.assertTrue(OffsetDateTime.parse("2018-08-23T14:48:05Z").isEqual(entity.getCreationDate()));
            Assertions.assertEquals("IRKS", entity.getCreator());
            Assertions.assertEquals("{69231C66-F37A-4F78-80C1-E379BFEE165D}", entity.getSumiffiikId());
            Assertions.assertEquals(1, entity.getShape().size());
            Assertions.assertEquals(1, entity.getSource().size());
            Assertions.assertEquals(Integer.valueOf(1), entity.getSource().iterator().next().getSource());
            Assertions.assertTrue(OffsetDateTime.parse("2018-08-29T10:42:07Z").isEqual(entity.getSource().iterator().next().getRegistrationFrom()));
            Assertions.assertEquals(1, entity.getHouseNumber().size());
            Assertions.assertEquals("18", entity.getHouseNumber().iterator().next().getNumber());
            Assertions.assertTrue(OffsetDateTime.parse("2018-08-29T10:42:07Z").isEqual(entity.getHouseNumber().iterator().next().getRegistrationFrom()));
            Assertions.assertEquals(1, entity.getBlockName().size());
            Assertions.assertEquals("House of Testing!", entity.getBlockName().iterator().next().getName());
            Assertions.assertTrue(OffsetDateTime.parse("2018-08-29T10:42:07Z").isEqual(entity.getBlockName().iterator().next().getRegistrationFrom()));
            Assertions.assertEquals(1, entity.getLocality().size());
            Assertions.assertEquals("0600", entity.getLocality().iterator().next().getCode());
            Assertions.assertTrue(OffsetDateTime.parse("2018-08-29T10:42:07Z").isEqual(entity.getLocality().iterator().next().getRegistrationFrom()));
            Assertions.assertEquals(1, entity.getStatus().size());
            Assertions.assertEquals(Integer.valueOf(2), entity.getStatus().iterator().next().getStatus());
            Assertions.assertTrue(OffsetDateTime.parse("2018-08-29T10:42:07Z").isEqual(entity.getStatus().iterator().next().getRegistrationFrom()));
            Assertions.assertEquals(1, entity.getBuilding().size());
            Assertions.assertEquals("af3550f5-2998-404d-b784-a70c4deb2a18", entity.getBuilding().iterator().next().getReference().getUuid().toString());
            Assertions.assertEquals(1, entity.getPostcode().size());
            Assertions.assertEquals("867af985-d281-3d38-99bd-c96549c8790d", entity.getPostcode().iterator().next().getReference().getUuid().toString());

            Assertions.assertEquals(1, entity.getRoad().size());
            Assertions.assertEquals(Integer.valueOf(956), entity.getRoad().iterator().next().getMunicipalityCode());
            Assertions.assertEquals(Integer.valueOf(254), entity.getRoad().iterator().next().getRoadCode());
            Assertions.assertTrue(OffsetDateTime.parse("2018-08-29T10:42:07Z").isEqual(entity.getRoad().iterator().next().getRegistrationFrom()));

        } finally {
            session.close();
        }

        ResponseEntity<String> response = this.uuidSearch("2E3776BF-05C2-433C-ADB9-8A07DF6B3E8F", "accessaddress");
        Assertions.assertEquals(200, response.getStatusCode().value());
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                objectMapper.readTree(response.getBody())
        ));
    }


    @Test
    public void testUnitAddress() throws IOException {

        Session session = sessionManager.getSessionFactory().openSession();
        try {
            UnitAddressEntity entity = QueryManager.getEntity(session, UUID.fromString("1B3AC64B-C28D-40B2-A106-16CEE7C188B8"), UnitAddressEntity.class);
            Assertions.assertNotNull(entity);
            Assertions.assertTrue(OffsetDateTime.parse("2018-08-29T10:21:17Z").isEqual(entity.getCreationDate()));
            Assertions.assertEquals("TELDWST", entity.getCreator());
            Assertions.assertEquals("{EE4805F1-96AB-45A7-B045-ED4D3C28364C}", entity.getSumiffiikId());
            Assertions.assertEquals(1, entity.getUsage().size());
            Assertions.assertEquals(Integer.valueOf(1), entity.getUsage().iterator().next().getUsage());
            Assertions.assertEquals(1, entity.getFloor().size());
            Assertions.assertEquals("kld", entity.getFloor().iterator().next().getFloor());
            Assertions.assertEquals(1, entity.getNumber().size());
            Assertions.assertEquals("5678", entity.getNumber().iterator().next().getNumber());
            Assertions.assertEquals(1, entity.getSource().size());
            Assertions.assertEquals(Integer.valueOf(1), entity.getSource().iterator().next().getSource());
            Assertions.assertEquals(1, entity.getStatus().size());
            Assertions.assertEquals(Integer.valueOf(2), entity.getStatus().iterator().next().getStatus());
        } finally {
            session.close();
        }

        ResponseEntity<String> response = this.uuidSearch("1B3AC64B-C28D-40B2-A106-16CEE7C188B8", "unitaddress");
        Assertions.assertEquals(200, response.getStatusCode().value());
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                objectMapper.readTree(response.getBody())
        ));
    }

    @Test
    public void testPostcode() throws IOException {

        Session session = sessionManager.getSessionFactory().openSession();
        try {
            PostcodeEntity entity = QueryManager.getEntity(session, PostcodeEntity.generateUUID(3900), PostcodeEntity.class);
            Assertions.assertNotNull(entity);
            Assertions.assertEquals(3900, entity.getCode());
            Assertions.assertEquals("Nuuk", entity.getName().iterator().next().getName());
        } finally {
            session.close();
        }

        ResponseEntity<String> response = this.uuidSearch(PostcodeEntity.generateUUID(3900).toString(), "postcode");
        Assertions.assertEquals(200, response.getStatusCode().value());
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                objectMapper.readTree(response.getBody())
        ));
    }

}
