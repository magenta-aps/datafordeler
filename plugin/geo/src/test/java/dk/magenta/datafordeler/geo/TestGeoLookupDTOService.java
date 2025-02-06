package dk.magenta.datafordeler.geo;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.geo.data.municipality.GeoMunicipalityEntity;
import dk.magenta.datafordeler.geo.data.municipality.MunicipalityQuery;
import org.hibernate.Session;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.util.List;


@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestGeoLookupDTOService extends GeoTest {

    @Autowired
    private ObjectMapper objectMapper;


    @Before
    public void initialize() throws Exception {
        this.loadAll();
        this.loadCprAddress();
    }


    @Test
    public void testLookupService() throws InvalidClientInputException {

        GeoLookupService lookupService = new GeoLookupService(sessionManager);

        GeoLookupDTO geoLookupDTO = lookupService.doLookup(956, 254, "18", "B-3197", "kld", "tv", true);

        Assertions.assertEquals("Kommuneqarfik Sermersooq", geoLookupDTO.getMunicipalityName());
        Assertions.assertEquals("B-3197", geoLookupDTO.getbNumber());
        Assertions.assertTrue("Qarsaalik".equals(geoLookupDTO.getRoadName()) || "Qarsaalik_previous".equals(geoLookupDTO.getRoadName()));
        Assertions.assertEquals("0600", geoLookupDTO.getLocalityCode());
        Assertions.assertEquals("NUK", geoLookupDTO.getLocalityAbbrev());
        Assertions.assertEquals("Nuuk", geoLookupDTO.getLocalityName());
        Assertions.assertEquals(3900, geoLookupDTO.getPostalCode());
        Assertions.assertEquals("Nuuk", geoLookupDTO.getPostalDistrict());

        Assertions.assertEquals("Nuuk", lookupService.getPostalCodeDistrict(3900));
        Assertions.assertEquals("Santa Claus/ juulli inua", lookupService.getPostalCodeDistrict(2412));

        Assertions.assertEquals("{2e3776bf-05c2-433c-adb9-8a07df6b3e8f}", geoLookupDTO.getAccessAddressGlobalId());
        Assertions.assertEquals("{1b3ac64b-c28d-40b2-a106-16cee7c188b8}", geoLookupDTO.getUnitAddressGlobalId());
    }

    @Test
    public void testLookupInHardcodedList() throws IOException, InvalidClientInputException {

        GeoLookupService lookupService = new GeoLookupService(sessionManager);

        GeoLookupDTO geoLookupDTO = lookupService.doLookup(956, 254, "18", "B-3197");
        Assertions.assertEquals("Kommuneqarfik Sermersooq", geoLookupDTO.getMunicipalityName());
        Assertions.assertTrue("Qarsaalik".equals(geoLookupDTO.getRoadName()) || "Qarsaalik_previous".equals(geoLookupDTO.getRoadName()));
    }


    @Test
    public void testLookupServiceDk() throws IOException, InvalidClientInputException {

        Session session = sessionManager.getSessionFactory().openSession();


        MunicipalityQuery query = new MunicipalityQuery();
        query.addKommunekodeRestriction("1234");
        List<GeoMunicipalityEntity> localities = QueryManager.getAllEntities(session, query, GeoMunicipalityEntity.class);
        GeoLookupService lookupService = new GeoLookupService(sessionManager);

        GeoLookupDTO geoLookupDTO = lookupService.doLookup(730, 1, "18");

        Assertions.assertEquals("Randers", geoLookupDTO.getMunicipalityName());
        Assertions.assertEquals(null, geoLookupDTO.getbNumber());
        Assertions.assertEquals("Aage Beks Vej", geoLookupDTO.getRoadName());
        Assertions.assertEquals(8920, geoLookupDTO.getPostalCode());
        Assertions.assertEquals("Randers NV", geoLookupDTO.getPostalDistrict());

        geoLookupDTO = lookupService.doLookup(730, 4, "18");

        Assertions.assertEquals("Randers", geoLookupDTO.getMunicipalityName());
        Assertions.assertEquals(null, geoLookupDTO.getbNumber());
        Assertions.assertEquals("Aalborggade", geoLookupDTO.getRoadName());
        Assertions.assertEquals(8940, geoLookupDTO.getPostalCode());
        Assertions.assertEquals("Randers SV", geoLookupDTO.getPostalDistrict());
    }

}
