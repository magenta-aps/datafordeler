import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.geo.AdresseService;
import dk.magenta.datafordeler.geo.data.accessaddress.AccessAddressEntityManager;
import dk.magenta.datafordeler.geo.data.building.BuildingEntityManager;
import dk.magenta.datafordeler.geo.data.locality.LocalityEntityManager;
import dk.magenta.datafordeler.geo.data.municipality.MunicipalityEntityManager;
import dk.magenta.datafordeler.geo.data.postcode.PostcodeEntityManager;
import dk.magenta.datafordeler.geo.data.road.RoadEntityManager;
import dk.magenta.datafordeler.geo.data.unitaddress.UnitAddressEntityManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestService extends GeoTest {

    @Autowired
    private ObjectMapper objectMapper;


    @Before
    public void initialize() throws Exception {
        this.loadAll();
        this.loadCprAddress();
    }


    @Test
    public void testLocality() throws IOException {
        ResponseEntity<String> response = this.lookup("/geo/adresse/lokalitet?kommune=956");
        Assert.assertEquals(200, response.getStatusCode().value());
        ArrayNode localities = (ArrayNode) objectMapper.readTree(response.getBody());
        Assert.assertEquals(1, localities.size());
        ObjectNode locality = (ObjectNode) localities.get(0);
        Assert.assertEquals("Nuuk", locality.get("navn").asText());
        Assert.assertEquals("f0966470-f09f-474d-a820-e8a46ed6fcc7", locality.get("uuid").asText());
    }


    @Test
    public void testRoad() throws IOException {
        ResponseEntity<String> response = this.lookup("/geo/adresse/vej?lokalitet=f0966470-f09f-474d-a820-e8a46ed6fcc7");
        Assert.assertEquals(200, response.getStatusCode().value());
        ArrayNode roads = (ArrayNode) objectMapper.readTree(response.getBody());
        Assert.assertEquals(1, roads.size());
        ObjectNode road = (ObjectNode) roads.get(0);
        Assert.assertEquals(254, road.get("vejkode").intValue());
        Assert.assertEquals("Qarsaalik", road.get("navn").asText());
        Assert.assertEquals("Qarsaalik", road.get("andet_navn").asText());
        Assert.assertEquals(956, road.get("kommunekode").intValue());
        Assert.assertEquals("e1274f15-9e2b-4b6e-8b7d-c8078df65aa2", road.get("uuid").asText());
    }


    @Test
    public void testAccessAddress() throws IOException {
        ResponseEntity<String> response = this.lookup("/geo/adresse/hus?vej=e1274f15-9e2b-4b6e-8b7d-c8078df65aa2");
        Assert.assertEquals(200, response.getStatusCode().value());
        ArrayNode buildings = (ArrayNode) objectMapper.readTree(response.getBody());
        Assert.assertEquals(1, buildings.size());
        ObjectNode building = (ObjectNode) buildings.get(0);
        Assert.assertEquals("18", building.get("husnummer").asText());
        Assert.assertEquals("3197", building.get("b_nummer").asText());
        Assert.assertEquals("House of Testing!", building.get("b_kaldenavn").asText());
    }

    @Test
    public void testUnitAddress() throws IOException {
        ResponseEntity<String> response = this.lookup("/geo/adresse/adresse?b_nummer=B-3197B");
        Assert.assertEquals(400, response.getStatusCode().value());
        response = this.lookup("/geo/adresse/adresse?vej=e1274f15-9e2b-4b6e-8b7d-c8078df65aa2");
        Assert.assertEquals(200, response.getStatusCode().value());
        ArrayNode addresses = (ArrayNode) objectMapper.readTree(response.getBody());
        System.out.println(addresses);
        Assert.assertEquals(1, addresses.size());
        ObjectNode address = (ObjectNode) addresses.get(0);
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(address));
        Assert.assertEquals("18", address.get("husnummer").asText());
        Assert.assertEquals("House of Testing!", address.get("b_kaldenavn").asText());
        Assert.assertEquals("3197", address.get("b_nummer").asText());
        Assert.assertEquals("kld", address.get("etage").asText());
        Assert.assertEquals(1, address.get("anvendelse").intValue());
        response = this.lookup("/geo/adresse/adresse?vej=e1274f15-9e2b-4b6e-8b7d-c8078df65aa2&husnummer=18");
        Assert.assertEquals(200, response.getStatusCode().value());
        addresses = (ArrayNode) objectMapper.readTree(response.getBody());
        Assert.assertEquals(1, addresses.size());
        address = (ObjectNode) addresses.get(0);
        Assert.assertEquals("18", address.get("husnummer").asText());
        Assert.assertEquals("House of Testing!", address.get("b_kaldenavn").asText());
        Assert.assertEquals("3197", address.get("b_nummer").asText());
        Assert.assertEquals("kld", address.get("etage").asText());
        Assert.assertEquals(1, address.get("anvendelse").intValue());
    }

    @Test
    public void testUnitAddressDetails() throws IOException {
        ResponseEntity<String> response = this.lookup("/geo/adresse/adresseoplysninger?adresse=1b3ac64b-c28d-40b2-a106-16cee7c188b8");
        Assert.assertEquals(200, response.getStatusCode().value());
        ObjectNode address = (ObjectNode) objectMapper.readTree(response.getBody());
        Assert.assertEquals("1b3ac64b-c28d-40b2-a106-16cee7c188b8", address.get("uuid").asText());
        Assert.assertEquals("18", address.get("husnummer").asText());
        Assert.assertEquals("kld", address.get("etage").asText());
        Assert.assertEquals("3197", address.get("b_nummer").asText());
        Assert.assertEquals(254, address.get("vejkode").intValue());
        Assert.assertEquals("Qarsaalik", address.get("vejnavn").asText());
        Assert.assertEquals("f0966470-f09f-474d-a820-e8a46ed6fcc7", address.get("lokalitet").asText());
        Assert.assertEquals("Nuuk", address.get("lokalitetsnavn").asText());
        Assert.assertEquals(956, address.get("kommunekode").intValue());
        Assert.assertEquals(1, address.get("anvendelse").intValue());
        Assert.assertEquals("House of Testing!", address.get("b_kaldenavn").asText());
    }

    @Test
    public void testNumberComparator() {
        Comparator<String> c = AdresseService.fuzzyNumberComparator;
        ArrayList<String> subjects = new ArrayList<>(Arrays.asList("101", "1A", "2", "1000", "V002", "1B", "1", "V001"));
        subjects.sort(c);
        Assert.assertEquals(
                new ArrayList<>(Arrays.asList( "1", "1A", "1B", "2", "101", "1000", "V001", "V002")),
                subjects
        );
    }

}
