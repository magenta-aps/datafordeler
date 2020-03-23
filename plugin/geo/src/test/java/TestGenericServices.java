import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.Application;
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
        ResponseEntity<String> response = this.lookup("/geo/municipality/1/rest/search?fmt=dataonly&Kommunekode=956&navn=Kommuneqarfik Sermersooq");
        Assert.assertEquals(200, response.getStatusCode().value());
        ObjectNode responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        ObjectNode munipialicity = (ObjectNode) responseContent.get("results").get(0);
        Assert.assertEquals("33960e68-2f0a-4cb0-bb3d-02d9f0b21304", munipialicity.get("uuid").textValue());
        Assert.assertEquals("5cc15446cddb4633b6832847b6f5d66d", munipialicity.get("sumiffiik").textValue());
        Assert.assertEquals(956, munipialicity.get("kommunekode").asInt());
        Assert.assertNull(munipialicity.get("registreringFra"));
    }


    @Test
    public void testPostcodeService() throws IOException {
        ResponseEntity<String> response = this.lookup("/geo/postcode/1/rest/search?fmt=dataonly&kode=3910&navn=Kangerlussuaq");
        Assert.assertEquals(200, response.getStatusCode().value());
        ObjectNode responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        ObjectNode munipialicity = (ObjectNode) responseContent.get("results").get(0);
        Assert.assertEquals("c8d54254-0086-3554-a4ce-424e4ebfd072", munipialicity.get("uuid").textValue());
        Assert.assertNotNull(munipialicity.get("postdistrikt"));
        Assert.assertNull(munipialicity.get("registreringFra"));
    }

    @Test
    public void testLocalityService() throws IOException {
        ResponseEntity<String> response = this.lookup("/geo/locality/1/rest/search?fmt=dataonly&kode=0600");
        Assert.assertEquals(200, response.getStatusCode().value());
        ObjectNode responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        ObjectNode munipialicity = (ObjectNode) responseContent.get("results").get(0);
        Assert.assertEquals("f0966470-f09f-474d-a820-e8a46ed6fcc7", munipialicity.get("uuid").textValue());
        Assert.assertEquals("4F46D110-E6AD-46D1-B403-B12063152564", munipialicity.get("sumiffiik").textValue());
        Assert.assertEquals("0600", munipialicity.get("lokalitetskode").textValue());
        Assert.assertNull(munipialicity.get("registreringFra"));
    }

    @Test
    public void testRoadService() throws IOException {
        ResponseEntity<String> response = this.lookup("/geo/road/1/rest/search?fmt=dataonly&kode=0254");
        Assert.assertEquals(200, response.getStatusCode().value());
        ObjectNode responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        ObjectNode road = (ObjectNode) responseContent.get("results").get(0);
        Assert.assertEquals("e1274f15-9e2b-4b6e-8b7d-c8078df65aa2", road.get("uuid").textValue());
        Assert.assertEquals("{961FFC61-8B04-45F3-80FD-509B0676FEF6}", road.get("sumiffiik").textValue());
        Assert.assertEquals("Qarsaalik", road.get("navn").get(0).get("navn").textValue());
        Assert.assertNull(road.get("registreringFra"));
    }

    @Test
    public void testAccessAddressService() throws IOException {
        ResponseEntity<String> response = this.lookup("/geo/accessaddress/1/rest/search?fmt=dataonly&bnr=B-3197");
        Assert.assertEquals(200, response.getStatusCode().value());
        ObjectNode responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        ObjectNode road = (ObjectNode) responseContent.get("results").get(0);
        Assert.assertEquals("2e3776bf-05c2-433c-adb9-8a07df6b3e8f", road.get("uuid").textValue());
        Assert.assertEquals("{69231C66-F37A-4F78-80C1-E379BFEE165D}", road.get("sumiffiik").textValue());
        Assert.assertEquals("House of Testing!", road.get("blokNavn").get(0).textValue());
        Assert.assertNull(road.get("registreringFra"));

        response = this.lookup("/geo/accessaddress/1/rest/search?fmt=dataonly&vej=0254");
        Assert.assertEquals(200, response.getStatusCode().value());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        road = (ObjectNode) responseContent.get("results").get(0);
        Assert.assertEquals("2e3776bf-05c2-433c-adb9-8a07df6b3e8f", road.get("uuid").textValue());
        Assert.assertEquals("{69231C66-F37A-4F78-80C1-E379BFEE165D}", road.get("sumiffiik").textValue());
        Assert.assertEquals("House of Testing!", road.get("blokNavn").get(0).textValue());
        Assert.assertNull(road.get("registreringFra"));
    }
}