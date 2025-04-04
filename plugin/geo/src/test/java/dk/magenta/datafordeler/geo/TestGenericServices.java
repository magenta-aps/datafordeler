package dk.magenta.datafordeler.geo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.Application;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;

@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestGenericServices extends GeoTest {

    @Autowired
    private ObjectMapper objectMapper;


    @BeforeEach
    public void initialize() throws Exception {
        this.loadAll();
        this.loadCprAddress();
    }


    @Test
    public void testMunicipalityService() throws IOException {
        ResponseEntity<String> response = this.lookup("/geo/municipality/1/rest/search?fmt=dataonly&kode=956");
        Assertions.assertEquals(200, response.getStatusCode().value());
        ObjectNode responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        ObjectNode municipality = (ObjectNode) responseContent.get("results").get(0);
        Assertions.assertEquals("33960e68-2f0a-4cb0-bb3d-02d9f0b21304", municipality.get("uuid").textValue());
        Assertions.assertEquals("5cc15446cddb4633b6832847b6f5d66d", municipality.get("sumiffiik").textValue());
        Assertions.assertEquals(956, municipality.get("kommunekode").asInt());
        Assertions.assertNull(municipality.get("registreringFra"));

        response = this.lookup("/geo/municipality/1/rest/search?fmt=dataonly&navn=Kommuneqarfik Sermersooq");
        Assertions.assertEquals(200, response.getStatusCode().value());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        ObjectNode munipialicity2 = (ObjectNode) responseContent.get("results").get(0);
        Assertions.assertEquals(municipality, munipialicity2);
    }

    @Test
    public void testPostcodeService() throws IOException {
        ResponseEntity<String> response = this.lookup("/geo/postcode/1/rest/search?fmt=dataonly&kode=3910&navn=Kangerlussuaq");
        Assertions.assertEquals(200, response.getStatusCode().value());
        ObjectNode responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        ObjectNode postcode = (ObjectNode) responseContent.get("results").get(0);
        System.out.println(postcode);
        Assertions.assertEquals("c8d54254-0086-3554-a4ce-424e4ebfd072", postcode.get("uuid").textValue());
        Assertions.assertNull(postcode.get("registreringFra"));

        response = this.lookup("/geo/postcode/1/rest/search?fmt=dataonly&kode=3910");
        Assertions.assertEquals(200, response.getStatusCode().value());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        ObjectNode munipialicity1 = (ObjectNode) responseContent.get("results").get(0);
        Assertions.assertEquals("c8d54254-0086-3554-a4ce-424e4ebfd072", munipialicity1.get("uuid").textValue());
        Assertions.assertNotNull(munipialicity1.get("postdistrikt"));
        Assertions.assertNull(munipialicity1.get("registreringFra"));

        response = this.lookup("/geo/postcode/1/rest/search?fmt=dataonly&navn=Kangerlussuaq");
        Assertions.assertEquals(200, response.getStatusCode().value());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        ObjectNode munipialicity2 = (ObjectNode) responseContent.get("results").get(0);
        Assertions.assertEquals(munipialicity1, munipialicity2);
    }

    @Test
    public void testLocalityService() throws IOException {
        ResponseEntity<String> response = this.lookup("/geo/locality/1/rest/search?fmt=dataonly&kode=0600");
        Assertions.assertEquals(200, response.getStatusCode().value());
        ObjectNode responseContent = (ObjectNode) objectMapper.readTree(response.getBody());

        ObjectNode locality1 = (ObjectNode) responseContent.get("results").get(0);
        Assertions.assertEquals("f0966470-f09f-474d-a820-e8a46ed6fcc7", locality1.get("uuid").textValue());
        Assertions.assertEquals("0600", locality1.get("lokalitetskode").textValue());
        Assertions.assertNull(locality1.get("registreringFra"));

        response = this.lookup("/geo/locality/1/rest/search?fmt=dataonly&kommunekode=956");
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        ObjectNode locality2 = (ObjectNode) responseContent.get("results").get(0);
        Assertions.assertEquals(locality1, locality2);


        /*response = this.lookup("/geo/locality/1/rest/search?fmt=dataonly&navn=Nuuk");
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        ObjectNode munipialicity2 = (ObjectNode) responseContent.get("results").get(0);
        Assertions.assertEquals(munipialicity1, munipialicity2);*/
    }

    //@Test TODO, we need to check why theesa has switched place
    public void testRoadService() throws IOException {
        ResponseEntity<String> response = this.lookup("/geo/road/1/rest/search?fmt=dataonly&kode=0254");
        Assertions.assertEquals(200, response.getStatusCode().value());
        ObjectNode responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        ObjectNode road1 = (ObjectNode) responseContent.get("results").get(0);
        Assertions.assertEquals("{961FFC61-8B04-45F3-80FD-509B0676FEF6}", road1.get("sumiffiik").textValue());
        Assertions.assertEquals("Qarsaalik", road1.get("navn").get(0).get("navn").textValue());
        Assertions.assertNull(road1.get("registreringFra"));

        response = this.lookup("/geo/road/1/rest/search?fmt=dataonly&navn=Qarsaalik");
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        ObjectNode road2 = (ObjectNode) responseContent.get("results").get(0);
        Assertions.assertEquals(road1, road2);

        response = this.lookup("/geo/road/1/rest/search?fmt=dataonly&addresseringsNavn=Qarsaalik");
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        ObjectNode road3 = (ObjectNode) responseContent.get("results").get(0);
        Assertions.assertEquals(road2, road3);

        response = this.lookup("/geo/road/1/rest/search?fmt=dataonly&kommunekode=956");
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        ObjectNode road4 = (ObjectNode) responseContent.get("results").get(0);
        Assertions.assertEquals(road3, road4);

        response = this.lookup("/geo/road/1/rest/search?fmt=dataonly&lokalitetskode=0600");
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        ObjectNode road5 = (ObjectNode) responseContent.get("results").get(0);
        Assertions.assertEquals(road4, road5);

    }

    @Test
    public void testAccessAddressService() throws IOException {
        ResponseEntity<String> response = this.lookup("/geo/accessaddress/1/rest/search?fmt=dataonly&bnr=B-3197");
        Assertions.assertEquals(200, response.getStatusCode().value());
        ObjectNode responseContent = (ObjectNode) objectMapper.readTree(response.getBody());

        ObjectNode accessAdress1 = (ObjectNode) responseContent.get("results").get(0);
        Assertions.assertEquals("2e3776bf-05c2-433c-adb9-8a07df6b3e8f", accessAdress1.get("uuid").textValue());
        Assertions.assertEquals("{69231C66-F37A-4F78-80C1-E379BFEE165D}", accessAdress1.get("sumiffiik").textValue());
        Assertions.assertEquals("House of Testing!", accessAdress1.get("blokNavn").get(0).textValue());
        Assertions.assertNull(accessAdress1.get("registreringFra"));


        response = this.lookup("/geo/accessaddress/1/rest/search?fmt=dataonly&vejKode=0254");
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        ObjectNode accessAdress2 = (ObjectNode) responseContent.get("results").get(0);
        Assertions.assertEquals(accessAdress1, accessAdress2);

        response = this.lookup("/geo/accessaddress/1/rest/search?fmt=dataonly&husNummer=18");
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        ObjectNode accessAdress3 = (ObjectNode) responseContent.get("results").get(0);
        Assertions.assertEquals(accessAdress2, accessAdress3);

        response = this.lookup("/geo/accessaddress/1/rest/search?fmt=dataonly&kommuneKode=956");
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        ObjectNode accessAdress4 = (ObjectNode) responseContent.get("results").get(0);
        Assertions.assertEquals(accessAdress3, accessAdress4);
    }

    @Test
    public void testGenericService() throws IOException {
        ResponseEntity<String> response;
        JsonNode responseContent;

        System.out.println("testGenericService");
        response = this.lookup("/geo/fullAddress/1/rest/search?bnr=B-3197&husNummer=18&bloknavn=House of Testing!" +
                "&kommune_kode=956&lokalitet_kode=0600&post_kode=3900");
        Assertions.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assertions.assertEquals(2, responseContent.get("results").size());

        response = this.lookup("/geo/fullAddress/1/rest/search?bnr=B-3197");
        Assertions.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assertions.assertEquals(2, responseContent.get("results").size());

        response = this.lookup("/geo/fullAddress/1/rest/search?bnr=B-3197&pageSize=1");
        Assertions.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assertions.assertEquals(1, responseContent.get("results").size());

        response = this.lookup("/geo/fullAddress/1/rest/search?bnr=B-3197&pageSize=1&page=1");
        Assertions.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assertions.assertEquals(1, responseContent.get("results").size());

        response = this.lookup("/geo/fullAddress/1/rest/search?bnr=B-3197&pageSize=1&page=2");
        Assertions.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assertions.assertEquals(1, responseContent.get("results").size());

        response = this.lookup("/geo/fullAddress/1/rest/search?bnr=B-3197&pageSize=1&page=3");
        Assertions.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assertions.assertEquals(0, responseContent.get("results").size());


        response = this.lookup("/geo/fullAddress/1/rest/search?bnr=B-31*");
        Assertions.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assertions.assertEquals(2, responseContent.get("results").size());

        response = this.lookup("/geo/fullAddress/1/rest/search?bnr=B-32*");
        Assertions.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assertions.assertEquals(0, responseContent.get("results").size());

        response = this.lookup("/geo/fullAddress/1/rest/search?husNummer=18");
        Assertions.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assertions.assertEquals(2, responseContent.get("results").size());

        response = this.lookup("/geo/fullAddress/1/rest/search?husNummer=17");
        Assertions.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assertions.assertEquals(0, responseContent.get("results").size());

        response = this.lookup("/geo/fullAddress/1/rest/search?kommune_kode=956");
        Assertions.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assertions.assertEquals(2, responseContent.get("results").size());

        response = this.lookup("/geo/fullAddress/1/rest/search?kommune_kode=955");
        Assertions.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assertions.assertEquals(0, responseContent.get("results").size());

        response = this.lookup("/geo/fullAddress/1/rest/search?kommune_kode.gt=955");
        Assertions.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assertions.assertEquals(2, responseContent.get("results").size());

        response = this.lookup("/geo/fullAddress/1/rest/search?kommune_kode.gt=957");
        Assertions.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assertions.assertEquals(0, responseContent.get("results").size());

        response = this.lookup("/geo/fullAddress/1/rest/search?kommune_kode.lt=955");
        Assertions.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assertions.assertEquals(0, responseContent.get("results").size());

        response = this.lookup("/geo/fullAddress/1/rest/search?kommune_kode.lt=957");
        Assertions.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assertions.assertEquals(2, responseContent.get("results").size());

        response = this.lookup("/geo/fullAddress/1/rest/search?kommune_navn=Kommuneqarfik Sermersooq");
        Assertions.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assertions.assertEquals(2, responseContent.get("results").size());

        response = this.lookup("/geo/fullAddress/1/rest/search?kommune_navn=Kommuneqa*");
        Assertions.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assertions.assertEquals(2, responseContent.get("results").size());

        response = this.lookup("/geo/fullAddress/1/rest/search?kommune_navn=Unknown");
        Assertions.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assertions.assertEquals(0, responseContent.get("results").size());

        response = this.lookup("/geo/fullAddress/1/rest/search?post_navn=Nuuk");
        Assertions.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assertions.assertEquals(2, responseContent.get("results").size());

        response = this.lookup("/geo/fullAddress/1/rest/search?post_navn=Nu*");
        Assertions.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assertions.assertEquals(2, responseContent.get("results").size());

        response = this.lookup("/geo/fullAddress/1/rest/search?post_navn=Unknown");
        Assertions.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assertions.assertEquals(0, responseContent.get("results").size());

        response = this.lookup("/geo/fullAddress/1/rest/search?lokalitet_navn=Nuuk");
        Assertions.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assertions.assertEquals(2, responseContent.get("results").size());

        response = this.lookup("/geo/fullAddress/1/rest/search?lokalitet_navn=Nu*");
        Assertions.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assertions.assertEquals(2, responseContent.get("results").size());

        response = this.lookup("/geo/fullAddress/1/rest/search?lokalitet_navn=Unknown");
        Assertions.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assertions.assertEquals(0, responseContent.get("results").size());

        response = this.lookup("/geo/fullAddress/1/rest/search?vej_kode=254");
        Assertions.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assertions.assertEquals(2, responseContent.get("results").size());

        response = this.lookup("/geo/fullAddress/1/rest/search?vej_kode.lt=253");
        Assertions.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assertions.assertEquals(0, responseContent.get("results").size());

        response = this.lookup("/geo/fullAddress/1/rest/search?vej_kode.gt=253");
        Assertions.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assertions.assertEquals(2, responseContent.get("results").size());

        response = this.lookup("/geo/fullAddress/1/rest/search?vej_navn=Unknown");
        Assertions.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assertions.assertEquals(0, responseContent.get("results").size());

        response = this.lookup("/geo/fullAddress/1/rest/search?vej_navn=Qarsaalik*");
        Assertions.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assertions.assertEquals(2, responseContent.get("results").size());

        /* The attribute doer has been removed from GAR
        response = this.lookup("/geo/fullAddress/1/rest/search?doer=1");
        Assertions.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assertions.assertEquals(1, responseContent.get("results").size());

        response = this.lookup("/geo/fullAddress/1/rest/search?doer=2");
        Assertions.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assertions.assertEquals(1, responseContent.get("results").size());

        response = this.lookup("/geo/fullAddress/1/rest/search?doer=3");
        Assertions.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assertions.assertEquals(0, responseContent.get("results").size());*/

        response = this.lookup("/geo/fullAddress/1/rest/search?etage=kld");
        Assertions.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assertions.assertEquals(2, responseContent.get("results").size());

        response = this.lookup("/geo/fullAddress/1/rest/search?etage=other");
        Assertions.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assertions.assertEquals(0, responseContent.get("results").size());

        response = this.lookup("/geo/fullAddress/1/rest/search?etage=kld");
        Assertions.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assertions.assertEquals(2, responseContent.get("results").size());
        Assertions.assertNull(responseContent.get("results").get(0).get("accessAddress_objectId"));
        Assertions.assertNull(responseContent.get("results").get(0).get("unitAddress_objectId"));

        response = this.lookup("/geo/fullAddress/1/rest/search?etage=kld&debug=1");
        Assertions.assertEquals(200, response.getStatusCode().value());
        responseContent = objectMapper.readTree(response.getBody());
        Assertions.assertEquals(2, responseContent.get("results").size());
        Assertions.assertNotNull(responseContent.get("results").get(0).get("accessAddress_objectId"));
        Assertions.assertNotNull(responseContent.get("results").get(0).get("unitAddress_objectId"));

    }


    @Test
    public void testUnitAddressService() throws IOException {
        ResponseEntity<String> response = this.lookup("/geo/unitaddress/1/rest/search?fmt=dataonly&bnr=B-31*");
        Assertions.assertEquals(200, response.getStatusCode().value());

        ObjectNode responseContent = (ObjectNode) objectMapper.readTree(response.getBody());

        Assertions.assertEquals(2, responseContent.get("results").size());

        for (JsonNode j : responseContent.get("results")) {
            ObjectNode o = (ObjectNode) j;
            if (o.get("uuid").textValue().equals("1b3ac64b-c28d-40b2-a106-16cee7c188b8")) {
                Assertions.assertEquals("5678", o.get("nummer").get(0).asText());
            } else if (o.get("uuid").textValue().equals("1b3ac64b-c28d-40b2-a106-16cee7c188b9")) {
                Assertions.assertEquals("5678", o.get("nummer").get(0).asText());
            } else {
                Assertions.fail();
            }
        }

    }
}
