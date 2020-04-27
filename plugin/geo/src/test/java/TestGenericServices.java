import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.geo.data.accessaddress.AccessAddressEntity;
import dk.magenta.datafordeler.geo.data.accessaddress.AccessAddressHouseNumberRecord;
import dk.magenta.datafordeler.geo.data.accessaddress.AccessAddressRoadRecord;
import dk.magenta.datafordeler.geo.data.locality.GeoLocalityEntity;
import dk.magenta.datafordeler.geo.data.road.GeoRoadEntity;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
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
import java.util.HashSet;
import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestGenericServices extends GeoTest {

    @Autowired
    private ObjectMapper objectMapper;


    @Before
    public void initialize() throws Exception {
        this.loadAll();
        this.loadCprAddress();
    }


    @Test
    public void testMunicipalityService() throws IOException {
        ResponseEntity<String> response = this.lookup("/geo/municipality/1/rest/search?fmt=dataonly&kode=956");
        Assert.assertEquals(200, response.getStatusCode().value());
        ObjectNode responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        ObjectNode municipality = (ObjectNode) responseContent.get("results").get(0);
        Assert.assertEquals("33960e68-2f0a-4cb0-bb3d-02d9f0b21304", municipality.get("uuid").textValue());
        Assert.assertEquals("5cc15446cddb4633b6832847b6f5d66d", municipality.get("sumiffiik").textValue());
        Assert.assertEquals(956, municipality.get("kommunekode").asInt());
        Assert.assertNull(municipality.get("registreringFra"));

        response = this.lookup("/geo/municipality/1/rest/search?fmt=dataonly&navn=Kommuneqarfik Sermersooq");
        Assert.assertEquals(200, response.getStatusCode().value());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        ObjectNode munipialicity2 = (ObjectNode) responseContent.get("results").get(0);
        Assert.assertEquals(municipality, munipialicity2);
    }


    @Test
    public void testPostcodeService() throws IOException {
        ResponseEntity<String> response = this.lookup("/geo/postcode/1/rest/search?fmt=dataonly&kode=3910&navn=Kangerlussuaq");
        Assert.assertEquals(200, response.getStatusCode().value());
        ObjectNode responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        ObjectNode postcode = (ObjectNode) responseContent.get("results").get(0);
        System.out.println(postcode);
        Assert.assertEquals("c8d54254-0086-3554-a4ce-424e4ebfd072", postcode.get("uuid").textValue());
        Assert.assertNull(postcode.get("registreringFra"));

        response = this.lookup("/geo/postcode/1/rest/search?fmt=dataonly&kode=3910");
        Assert.assertEquals(200, response.getStatusCode().value());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        ObjectNode munipialicity1 = (ObjectNode) responseContent.get("results").get(0);
        Assert.assertEquals("c8d54254-0086-3554-a4ce-424e4ebfd072", munipialicity1.get("uuid").textValue());
        Assert.assertNotNull(munipialicity1.get("postdistrikt"));
        Assert.assertNull(munipialicity1.get("registreringFra"));

        response = this.lookup("/geo/postcode/1/rest/search?fmt=dataonly&navn=Kangerlussuaq");
        Assert.assertEquals(200, response.getStatusCode().value());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        ObjectNode munipialicity2 = (ObjectNode) responseContent.get("results").get(0);
        Assert.assertEquals(munipialicity1, munipialicity2);
    }

    @Test
    public void testLocalityService() throws IOException {
        ResponseEntity<String> response = this.lookup("/geo/locality/1/rest/search?fmt=dataonly&kode=0600");
        Assert.assertEquals(200, response.getStatusCode().value());
        ObjectNode responseContent = (ObjectNode) objectMapper.readTree(response.getBody());

        ObjectNode locality1 = (ObjectNode) responseContent.get("results").get(0);
        Assert.assertEquals("f0966470-f09f-474d-a820-e8a46ed6fcc7", locality1.get("uuid").textValue());
        Assert.assertEquals("4F46D110-E6AD-46D1-B403-B12063152564", locality1.get("sumiffiik").textValue());
        Assert.assertEquals("0600", locality1.get("lokalitetskode").textValue());
        Assert.assertNull(locality1.get("registreringFra"));

        response = this.lookup("/geo/locality/1/rest/search?fmt=dataonly&kommune=956");
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        ObjectNode locality2 = (ObjectNode) responseContent.get("results").get(0);
        Assert.assertEquals(locality1, locality2);


        /*response = this.lookup("/geo/locality/1/rest/search?fmt=dataonly&navn=Nuuk");
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        ObjectNode munipialicity2 = (ObjectNode) responseContent.get("results").get(0);
        Assert.assertEquals(munipialicity1, munipialicity2);*/
    }

    @Test
    public void testRoadService() throws IOException {
        ResponseEntity<String> response = this.lookup("/geo/road/1/rest/search?fmt=dataonly&kode=0254");
        Assert.assertEquals(200, response.getStatusCode().value());
        ObjectNode responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        ObjectNode road1 = (ObjectNode) responseContent.get("results").get(0);
        Assert.assertEquals("{961FFC61-8B04-45F3-80FD-509B0676FEF6}", road1.get("sumiffiik").textValue());
        Assert.assertEquals("Qarsaalik", road1.get("navn").get(0).get("navn").textValue());
        Assert.assertNull(road1.get("registreringFra"));

        response = this.lookup("/geo/road/1/rest/search?fmt=dataonly&navn=Qarsaalik");
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        ObjectNode road2 = (ObjectNode) responseContent.get("results").get(0);
        Assert.assertEquals(road1, road2);

        response = this.lookup("/geo/road/1/rest/search?fmt=dataonly&addresseringsNavn=Qarsaalik");
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        ObjectNode road3 = (ObjectNode) responseContent.get("results").get(0);
        Assert.assertEquals(road2, road3);

        response = this.lookup("/geo/road/1/rest/search?fmt=dataonly&kommunekode=956");
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        ObjectNode road4 = (ObjectNode) responseContent.get("results").get(0);
        Assert.assertEquals(road3, road4);

        response = this.lookup("/geo/road/1/rest/search?fmt=dataonly&lokalitetskode=0600");
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        ObjectNode road5 = (ObjectNode) responseContent.get("results").get(0);
        Assert.assertEquals(road4, road5);

    }

    @Test
    public void testAccessAddressService() throws IOException {
        ResponseEntity<String> response = this.lookup("/geo/accessaddress/1/rest/search?fmt=dataonly&bnr=B-3197");
        Assert.assertEquals(200, response.getStatusCode().value());
        ObjectNode responseContent = (ObjectNode) objectMapper.readTree(response.getBody());

        ObjectNode accessAdress1 = (ObjectNode) responseContent.get("results").get(0);
        Assert.assertEquals("2e3776bf-05c2-433c-adb9-8a07df6b3e8f", accessAdress1.get("uuid").textValue());
        Assert.assertEquals("{69231C66-F37A-4F78-80C1-E379BFEE165D}", accessAdress1.get("sumiffiik").textValue());
        Assert.assertEquals("House of Testing!", accessAdress1.get("blokNavn").get(0).textValue());
        Assert.assertNull(accessAdress1.get("registreringFra"));


        response = this.lookup("/geo/accessaddress/1/rest/search?fmt=dataonly&vej=0254");
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        ObjectNode accessAdress2 = (ObjectNode) responseContent.get("results").get(0);
        Assert.assertEquals(accessAdress1, accessAdress2);

        response = this.lookup("/geo/accessaddress/1/rest/search?fmt=dataonly&husNummer=18");
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        ObjectNode accessAdress3 = (ObjectNode) responseContent.get("results").get(0);
        Assert.assertEquals(accessAdress2, accessAdress3);

        response = this.lookup("/geo/accessaddress/1/rest/search?fmt=dataonly&kommuneKode=956");
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        ObjectNode accessAdress4 = (ObjectNode) responseContent.get("results").get(0);
        Assert.assertEquals(accessAdress3, accessAdress4);
    }

    @Test
    public void testGenericService() throws IOException {
        ResponseEntity<String> response = this.lookup("/geo/genericunitaddress/fullAddress?bnr=B-3197&husNummer=18&bloknavn=House of Testing!" +
                "&kommune_kode=956&lokalitet_kode=0600&post_kode=3900");
        Assert.assertEquals(200, response.getStatusCode().value());
        JsonNode responseContent = objectMapper.readTree(response.getBody());
        Assert.assertEquals(2, responseContent.get("results").size());

        response = this.lookup("/geo/genericunitaddress/fullAddress?bnr=B-3197");
        Assert.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assert.assertEquals(2, responseContent.get("results").size());

        response = this.lookup("/geo/genericunitaddress/fullAddress?bnr=B-3197&pageSize=1");
        Assert.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assert.assertEquals(1, responseContent.get("results").size());

        response = this.lookup("/geo/genericunitaddress/fullAddress?bnr=B-3197&pageSize=1&page=1");
        Assert.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assert.assertEquals(1, responseContent.get("results").size());

        response = this.lookup("/geo/genericunitaddress/fullAddress?bnr=B-3197&pageSize=1&page=2");
        Assert.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assert.assertEquals(1, responseContent.get("results").size());

        response = this.lookup("/geo/genericunitaddress/fullAddress?bnr=B-3197&pageSize=1&page=3");
        Assert.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assert.assertEquals(0, responseContent.get("results").size());


        response = this.lookup("/geo/genericunitaddress/fullAddress?bnr=B-31*");
        Assert.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assert.assertEquals(2, responseContent.get("results").size());

        response = this.lookup("/geo/genericunitaddress/fullAddress?bnr=B-32*");
        Assert.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assert.assertEquals(0, responseContent.get("results").size());

        response = this.lookup("/geo/genericunitaddress/fullAddress?husNummer=18");
        Assert.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assert.assertEquals(2, responseContent.get("results").size());
        System.out.println(responseContent);

        response = this.lookup("/geo/genericunitaddress/fullAddress?husNummer=17");
        Assert.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assert.assertEquals(0, responseContent.get("results").size());
        System.out.println(responseContent);

        response = this.lookup("/geo/genericunitaddress/fullAddress?kommune_kode=956");
        Assert.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assert.assertEquals(2, responseContent.get("results").size());
        System.out.println(responseContent);

        response = this.lookup("/geo/genericunitaddress/fullAddress?kommune_kode=955");
        Assert.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assert.assertEquals(0, responseContent.get("results").size());
        System.out.println(responseContent);

        response = this.lookup("/geo/genericunitaddress/fullAddress?kommune_kode.gt=955");
        Assert.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assert.assertEquals(2, responseContent.get("results").size());
        System.out.println(responseContent);

        response = this.lookup("/geo/genericunitaddress/fullAddress?kommune_kode.gt=957");
        Assert.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assert.assertEquals(0, responseContent.get("results").size());
        System.out.println(responseContent);

        response = this.lookup("/geo/genericunitaddress/fullAddress?kommune_kode.lt=955");
        Assert.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assert.assertEquals(0, responseContent.get("results").size());
        System.out.println(responseContent);

        response = this.lookup("/geo/genericunitaddress/fullAddress?kommune_kode.lt=957");
        Assert.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assert.assertEquals(2, responseContent.get("results").size());
        System.out.println(responseContent);

    }


    @Test
    public void testUnitAddressService() throws IOException {
        ResponseEntity<String> response = this.lookup("/geo/unitaddress/1/rest/search?fmt=dataonly&bnr=B-31*");
        System.out.println(response.getBody());
        Assert.assertEquals(200, response.getStatusCode().value());

        ObjectNode responseContent = (ObjectNode) objectMapper.readTree(response.getBody());

        Assert.assertEquals(2, responseContent.get("results").size());

        for (JsonNode j : responseContent.get("results")) {
            ObjectNode o = (ObjectNode) j;
            if (o.get("uuid").textValue().equals("1b3ac64b-c28d-40b2-a106-16cee7c188b8")) {
                Assert.assertEquals("2", o.get("dør").get(0).asText());
            } else if (o.get("uuid").textValue().equals("1b3ac64b-c28d-40b2-a106-16cee7c188b9")) {
                Assert.assertEquals("1", o.get("dør").get(0).asText());
            } else {
                Assert.fail();
            }
        }

    }
}
