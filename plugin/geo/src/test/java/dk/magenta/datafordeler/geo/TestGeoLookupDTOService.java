package dk.magenta.datafordeler.geo;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.geo.data.municipality.GeoMunicipalityEntity;
import dk.magenta.datafordeler.geo.data.municipality.MunicipalityQuery;
import org.hibernate.Session;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
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

        Assert.assertEquals("Kommuneqarfik Sermersooq", geoLookupDTO.getMunicipalityName());
        Assert.assertEquals("B-3197", geoLookupDTO.getbNumber());
        Assert.assertTrue("Qarsaalik".equals(geoLookupDTO.getRoadName()) || "Qarsaalik_previous".equals(geoLookupDTO.getRoadName()));
        Assert.assertEquals("0600", geoLookupDTO.getLocalityCode());
        Assert.assertEquals("NUK", geoLookupDTO.getLocalityAbbrev());
        Assert.assertEquals("Nuuk", geoLookupDTO.getLocalityName());
        Assert.assertEquals(3900, geoLookupDTO.getPostalCode());
        Assert.assertEquals("Nuuk", geoLookupDTO.getPostalDistrict());

        Assert.assertEquals("Nuuk", lookupService.getPostalCodeDistrict(3900));
        Assert.assertEquals("Santa Claus/ juulli inua", lookupService.getPostalCodeDistrict(2412));

        Assert.assertEquals("{2e3776bf-05c2-433c-adb9-8a07df6b3e8f}", geoLookupDTO.getAccessAddressGlobalId());
        Assert.assertEquals("{1b3ac64b-c28d-40b2-a106-16cee7c188b8}", geoLookupDTO.getUnitAddressGlobalId());
    }

    @Test
    public void testLookupInHardcodedList() throws IOException, InvalidClientInputException {

        GeoLookupService lookupService = new GeoLookupService(sessionManager);

        GeoLookupDTO geoLookupDTO = lookupService.doLookup(956, 254, "18", "B-3197");
        Assert.assertEquals("Kommuneqarfik Sermersooq", geoLookupDTO.getMunicipalityName());
        Assert.assertTrue("Qarsaalik".equals(geoLookupDTO.getRoadName()) || "Qarsaalik_previous".equals(geoLookupDTO.getRoadName()));
    }


    @Test
    public void testLookupServiceDk() throws IOException, InvalidClientInputException {

        Session session = sessionManager.getSessionFactory().openSession();


        MunicipalityQuery query = new MunicipalityQuery();
        query.addKommunekodeRestriction("1234");
        List<GeoMunicipalityEntity> localities = QueryManager.getAllEntities(session, query, GeoMunicipalityEntity.class);
        GeoLookupService lookupService = new GeoLookupService(sessionManager);

        GeoLookupDTO geoLookupDTO = lookupService.doLookup(730, 1, "18");

        Assert.assertEquals("Randers", geoLookupDTO.getMunicipalityName());
        Assert.assertEquals(null, geoLookupDTO.getbNumber());
        Assert.assertEquals("Aage Beks Vej", geoLookupDTO.getRoadName());
        Assert.assertEquals(8920, geoLookupDTO.getPostalCode());
        Assert.assertEquals("Randers NV", geoLookupDTO.getPostalDistrict());

        geoLookupDTO = lookupService.doLookup(730, 4, "18");

        Assert.assertEquals("Randers", geoLookupDTO.getMunicipalityName());
        Assert.assertEquals(null, geoLookupDTO.getbNumber());
        Assert.assertEquals("Aalborggade", geoLookupDTO.getRoadName());
        Assert.assertEquals(8940, geoLookupDTO.getPostalCode());
        Assert.assertEquals("Randers SV", geoLookupDTO.getPostalDistrict());
    }

}
