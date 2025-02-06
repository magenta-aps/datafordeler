package dk.magenta.datafordeler.gladdrreg;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.Engine;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.io.Event;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.util.InputStreamReader;
import dk.magenta.datafordeler.gladdrreg.data.GladdrregEntityManager;
import dk.magenta.datafordeler.gladdrreg.data.address.AddressEntity;
import dk.magenta.datafordeler.gladdrreg.data.address.AddressEntityManager;
import dk.magenta.datafordeler.gladdrreg.data.bnumber.BNumberEntity;
import dk.magenta.datafordeler.gladdrreg.data.bnumber.BNumberEntityManager;
import dk.magenta.datafordeler.gladdrreg.data.district.DistrictEntity;
import dk.magenta.datafordeler.gladdrreg.data.district.DistrictEntityManager;
import dk.magenta.datafordeler.gladdrreg.data.locality.LocalityEntity;
import dk.magenta.datafordeler.gladdrreg.data.locality.LocalityEntityManager;
import dk.magenta.datafordeler.gladdrreg.data.municipality.MunicipalityEntity;
import dk.magenta.datafordeler.gladdrreg.data.municipality.MunicipalityEntityManager;
import dk.magenta.datafordeler.gladdrreg.data.postalcode.PostalCodeEntity;
import dk.magenta.datafordeler.gladdrreg.data.postalcode.PostalCodeEntityManager;
import dk.magenta.datafordeler.gladdrreg.data.road.RoadEntity;
import dk.magenta.datafordeler.gladdrreg.data.road.RoadEntityManager;
import dk.magenta.datafordeler.gladdrreg.data.state.StateEntity;
import dk.magenta.datafordeler.gladdrreg.data.state.StateEntityManager;
import org.hibernate.Session;
import org.junit.jupiter.api.Assertions;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;


@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Ignore
// Unittests in gladreg is failing, it if now disabled since plugin-gladreg is going to be replaced by plugin-geo
public class QueryTest {

    @Autowired
    private Engine engine;

    @Autowired
    private GladdrregPlugin plugin;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SessionManager sessionManager;

    @MockitoSpyBean
    private AddressEntityManager addressEntityManager;

    @MockitoSpyBean
    private BNumberEntityManager bNumberEntityManager;

    @MockitoSpyBean
    private DistrictEntityManager districtEntityManager;

    @MockitoSpyBean
    private LocalityEntityManager localityEntityManager;

    @MockitoSpyBean
    private MunicipalityEntityManager municipalityEntityManager;

    @MockitoSpyBean
    private PostalCodeEntityManager postalCodeEntityManager;

    @MockitoSpyBean
    private RoadEntityManager roadEntityManager;

    @MockitoSpyBean
    private StateEntityManager stateEntityManager;

    private String wrapInEvent(String data, String schema) {
        String wrapper = "{\n" +
                "  \"beskedVersion\":\"1.0\",\n" +
                "  \"eventID\":\"msgid\",\n" +
                "  \"beskedData\":{\n" +
                "    \"Objektdata\":{\n" +
                "      \"dataskema\":\"%{skema}\",\n" +
                "      \"objektdata\":\"%{data}\"\n" +
                "    }\n" +
                "  }\n" +
                "}\n" +
                "\n";
        return wrapper.replace("%{data}", data).replace("%{skema}", schema);
    }

    private void load(GladdrregEntityManager entityManager, String resourceName, String schema) throws IOException, DataFordelerException {
        Mockito.doReturn(0).when(entityManager).sendReceipt(null);
        String testData = InputStreamReader.readInputStream(QueryTest.class.getResourceAsStream(resourceName));
        Event event = new Event();
        event.setSchema(schema);
        event.setData(testData);
        ImportMetadata importMetadata = new ImportMetadata();
        this.engine.handleEvent(event, this.plugin.getRegisterManager().getPlugin(), importMetadata);
    }

    public void loadAddress() throws Exception {
        this.load(addressEntityManager, "/address.json", AddressEntity.schema);
    }

    public void loadBnumber() throws Exception {
        this.load(bNumberEntityManager, "/bnumber.json", BNumberEntity.schema);
    }

    public void loadDistrict() throws Exception {
        this.load(districtEntityManager, "/district.json", DistrictEntity.schema);
    }

    public void loadLocality() throws Exception {
        this.load(localityEntityManager, "/locality.json", LocalityEntity.schema);
    }

    public void loadMunicipality() throws Exception {
        this.load(municipalityEntityManager, "/municipality.json", MunicipalityEntity.schema);
    }

    public void loadPostalcode() throws Exception {
        this.load(postalCodeEntityManager, "/postalcode.json", PostalCodeEntity.schema);
    }

    public void loadRoad() throws Exception {
        this.load(roadEntityManager, "/road.json", RoadEntity.schema);
    }

    public void loadState() throws Exception {
        this.load(stateEntityManager, "/state.json", StateEntity.schema);
    }


    private ResponseEntity<String> restSearch(ParameterMap parameters, String type) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        HttpEntity<String> httpEntity = new HttpEntity<String>("", headers);
        return this.restTemplate.exchange("/gladdrreg/" + type + "/1/rest/search?" + parameters.asUrlParams(), HttpMethod.GET, httpEntity, String.class);
    }


    @Test
    public void testAddressQuery() throws Exception {
        loadAddress();

        Session session = sessionManager.getSessionFactory().openSession();
        session.close();

        String type = "address";

        ParameterMap searchParameters = new ParameterMap();
        searchParameters.add("husnummer", "5");
        ResponseEntity<String> response;

        response = restSearch(searchParameters, type);
        Assertions.assertEquals(200, response.getStatusCode().value());
        JsonNode jsonBody = objectMapper.readTree(response.getBody());
        JsonNode results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals("6921fbb1-ddd7-4c7c-bb98-bbf63ace6a3a", results.get(0).get("UUID").asText());


        searchParameters = new ParameterMap();
        searchParameters.add("number", "5");
        response = restSearch(searchParameters, type);
        Assertions.assertEquals(400, response.getStatusCode().value());
    }

    @Test
    public void testAddressRecordTime() throws Exception {
        OffsetDateTime now = OffsetDateTime.now();
        loadAddress();
        String type = "address";

        ParameterMap searchParameters = new ParameterMap();
        searchParameters.add("registreringFra", now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        searchParameters.add("opdateretEfter", now.plusSeconds(5).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        ResponseEntity<String> response = restSearch(searchParameters, type);
        Assertions.assertEquals(200, response.getStatusCode().value());
        JsonNode jsonBody = objectMapper.readTree(response.getBody());
        JsonNode results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(0, results.size());


        searchParameters = new ParameterMap();
        searchParameters.add("registreringFra", now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        searchParameters.add("opdateretEfter", now.minusDays(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        response = restSearch(searchParameters, type);
        Assertions.assertEquals(200, response.getStatusCode().value());
        jsonBody = objectMapper.readTree(response.getBody());
        results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(1, results.size());
    }


    @Test
    public void testBNumberQuery() throws Exception {
        loadBnumber();
        String type = "bnumber";

        ParameterMap searchParameters = new ParameterMap();
        searchParameters.add("bnummer", "293");
        ResponseEntity<String> response;


        response = restSearch(searchParameters, type);
        Assertions.assertEquals(200, response.getStatusCode().value());
        JsonNode jsonBody = objectMapper.readTree(response.getBody());
        JsonNode results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals("53191b3a-ba25-44d0-8381-4d1b86d4c38d", results.get(0).get("UUID").asText());

        searchParameters = new ParameterMap();
        searchParameters.add("kaldenavn", "testhus");
        response = restSearch(searchParameters, type);
        Assertions.assertEquals(200, response.getStatusCode().value());
        jsonBody = objectMapper.readTree(response.getBody());
        results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals("53191b3a-ba25-44d0-8381-4d1b86d4c38d", results.get(0).get("UUID").asText());

        searchParameters = new ParameterMap();
        searchParameters.add("kaldenavn", "test*");
        response = restSearch(searchParameters, type);
        Assertions.assertEquals(200, response.getStatusCode().value());
        jsonBody = objectMapper.readTree(response.getBody());
        results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals("53191b3a-ba25-44d0-8381-4d1b86d4c38d", results.get(0).get("UUID").asText());

        searchParameters = new ParameterMap();
        searchParameters.add("kaldenavn", "*hus");
        response = restSearch(searchParameters, type);
        Assertions.assertEquals(200, response.getStatusCode().value());
        jsonBody = objectMapper.readTree(response.getBody());
        results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals("53191b3a-ba25-44d0-8381-4d1b86d4c38d", results.get(0).get("UUID").asText());


        searchParameters = new ParameterMap();
        searchParameters.add("code", "293");
        response = restSearch(searchParameters, type);
        Assertions.assertEquals(400, response.getStatusCode().value());
    }


    @Test
    public void testBNumberRecordTime() throws Exception {
        OffsetDateTime now = OffsetDateTime.now();
        loadBnumber();
        String type = "bnumber";

        ParameterMap searchParameters = new ParameterMap();
        searchParameters.add("registreringFra", now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        searchParameters.add("opdateretEfter", now.plusSeconds(5).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        ResponseEntity<String> response = restSearch(searchParameters, type);
        Assertions.assertEquals(200, response.getStatusCode().value());
        JsonNode jsonBody = objectMapper.readTree(response.getBody());
        JsonNode results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(0, results.size());

        searchParameters = new ParameterMap();
        searchParameters.add("registreringFra", now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        searchParameters.add("opdateretEfter", now.minusDays(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        response = restSearch(searchParameters, type);
        Assertions.assertEquals(200, response.getStatusCode().value());
        jsonBody = objectMapper.readTree(response.getBody());
        results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(1, results.size());
    }


    @Test
    public void testDistrictQuery() throws Exception {
        loadDistrict();
        String type = "district";

        ParameterMap searchParameters = new ParameterMap();
        searchParameters.add("navn", "Aasiaat");
        ResponseEntity<String> response;

        response = restSearch(searchParameters, type);
        Assertions.assertEquals(200, response.getStatusCode().value());
        JsonNode jsonBody = objectMapper.readTree(response.getBody());
        JsonNode results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals("92b6e52f-fb29-4249-bceb-4a8c410c3d65", results.get(0).get("UUID").asText());


        searchParameters = new ParameterMap();
        searchParameters.add("navn", "Aas*");
        response = restSearch(searchParameters, type);
        Assertions.assertEquals(200, response.getStatusCode().value());
        jsonBody = objectMapper.readTree(response.getBody());
        results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals("92b6e52f-fb29-4249-bceb-4a8c410c3d65", results.get(0).get("UUID").asText());

        searchParameters = new ParameterMap();
        searchParameters.add("forkortelse", "AAS");
        response = restSearch(searchParameters, type);
        Assertions.assertEquals(200, response.getStatusCode().value());
        jsonBody = objectMapper.readTree(response.getBody());
        results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals("92b6e52f-fb29-4249-bceb-4a8c410c3d65", results.get(0).get("UUID").asText());


        searchParameters = new ParameterMap();
        searchParameters.add("name", "Aasiaat");
        response = restSearch(searchParameters, type);
        Assertions.assertEquals(400, response.getStatusCode().value());
    }

    @Test
    public void testDistrictRecordTime() throws Exception {
        OffsetDateTime now = OffsetDateTime.now();
        loadDistrict();
        String type = "district";

        ParameterMap searchParameters = new ParameterMap();
        searchParameters.add("registreringFra", now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        searchParameters.add("opdateretEfter", now.plusSeconds(5).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        ResponseEntity<String> response = restSearch(searchParameters, type);
        Assertions.assertEquals(200, response.getStatusCode().value());
        JsonNode jsonBody = objectMapper.readTree(response.getBody());
        JsonNode results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(0, results.size());

        searchParameters = new ParameterMap();
        searchParameters.add("registreringFra", now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        searchParameters.add("opdateretEfter", now.minusDays(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        response = restSearch(searchParameters, type);
        Assertions.assertEquals(200, response.getStatusCode().value());
        jsonBody = objectMapper.readTree(response.getBody());
        results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(1, results.size());
    }


    @Test
    public void testLocalityQuery() throws Exception {
        loadLocality();
        String type = "locality";

        ParameterMap searchParameters = new ParameterMap();
        searchParameters.add("lokalitetskode", "500");
        ResponseEntity<String> response;

        response = restSearch(searchParameters, type);
        Assertions.assertEquals(200, response.getStatusCode().value());
        JsonNode jsonBody = objectMapper.readTree(response.getBody());
        JsonNode results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals("4d9cd2a0-89f1-4acc-a259-4fd139006d87", results.get(0).get("UUID").asText());

        searchParameters = new ParameterMap();
        searchParameters.add("navn", "Paamiut");
        response = restSearch(searchParameters, type);
        Assertions.assertEquals(200, response.getStatusCode().value());
        jsonBody = objectMapper.readTree(response.getBody());
        results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals("4d9cd2a0-89f1-4acc-a259-4fd139006d87", results.get(0).get("UUID").asText());

        searchParameters = new ParameterMap();
        searchParameters.add("navn", "Paa*");
        response = restSearch(searchParameters, type);
        Assertions.assertEquals(200, response.getStatusCode().value());
        jsonBody = objectMapper.readTree(response.getBody());
        results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals("4d9cd2a0-89f1-4acc-a259-4fd139006d87", results.get(0).get("UUID").asText());

        searchParameters = new ParameterMap();
        searchParameters.add("forkortelse", "PAA");
        response = restSearch(searchParameters, type);
        Assertions.assertEquals(200, response.getStatusCode().value());
        jsonBody = objectMapper.readTree(response.getBody());
        results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals("4d9cd2a0-89f1-4acc-a259-4fd139006d87", results.get(0).get("UUID").asText());

        searchParameters = new ParameterMap();
        searchParameters.add("type", "1");
        response = restSearch(searchParameters, type);
        Assertions.assertEquals(200, response.getStatusCode().value());
        jsonBody = objectMapper.readTree(response.getBody());
        results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals("4d9cd2a0-89f1-4acc-a259-4fd139006d87", results.get(0).get("UUID").asText());

        searchParameters = new ParameterMap();
        searchParameters.add("tilstand", "15");
        response = restSearch(searchParameters, type);
        Assertions.assertEquals(200, response.getStatusCode().value());
        jsonBody = objectMapper.readTree(response.getBody());
        results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals("4d9cd2a0-89f1-4acc-a259-4fd139006d87", results.get(0).get("UUID").asText());


        searchParameters = new ParameterMap();
        searchParameters.add("name", "Paamiut");
        response = restSearch(searchParameters, type);
        Assertions.assertEquals(400, response.getStatusCode().value());
    }

    @Test
    public void testLocalityRecordTime() throws Exception {
        OffsetDateTime now = OffsetDateTime.now();
        loadLocality();
        String type = "locality";

        ParameterMap searchParameters = new ParameterMap();
        searchParameters.add("registreringFra", now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        searchParameters.add("opdateretEfter", now.plusSeconds(5).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        ResponseEntity<String> response = restSearch(searchParameters, type);
        Assertions.assertEquals(200, response.getStatusCode().value());
        JsonNode jsonBody = objectMapper.readTree(response.getBody());
        JsonNode results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(0, results.size());

        searchParameters = new ParameterMap();
        searchParameters.add("registreringFra", now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        searchParameters.add("opdateretEfter", now.minusDays(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        response = restSearch(searchParameters, type);
        Assertions.assertEquals(200, response.getStatusCode().value());
        jsonBody = objectMapper.readTree(response.getBody());
        results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(1, results.size());
    }


    @Test
    public void testMunicipalityQuery() throws Exception {
        loadMunicipality();
        String type = "municipality";

        ParameterMap searchParameters = new ParameterMap();
        searchParameters.add("navn", "Kommune Kujalleq");
        ResponseEntity<String> response;

        response = restSearch(searchParameters, type);
        Assertions.assertEquals(200, response.getStatusCode().value());
        JsonNode jsonBody = objectMapper.readTree(response.getBody());
        JsonNode results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals("f792151b-f509-4173-aa5d-2f237cca1784", results.get(0).get("UUID").asText());

        searchParameters = new ParameterMap();
        searchParameters.add("navn", "*Kujalleq");

        response = restSearch(searchParameters, type);
        Assertions.assertEquals(200, response.getStatusCode().value());
        jsonBody = objectMapper.readTree(response.getBody());
        results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals("f792151b-f509-4173-aa5d-2f237cca1784", results.get(0).get("UUID").asText());


        searchParameters = new ParameterMap();
        searchParameters.add("kommunekode", "955");

        response = restSearch(searchParameters, type);
        Assertions.assertEquals(200, response.getStatusCode().value());
        jsonBody = objectMapper.readTree(response.getBody());
        results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals("f792151b-f509-4173-aa5d-2f237cca1784", results.get(0).get("UUID").asText());


        searchParameters = new ParameterMap();
        searchParameters.add("name", "Kommune Kujalleq");
        response = restSearch(searchParameters, type);
        Assertions.assertEquals(400, response.getStatusCode().value());
    }


    @Test
    public void testMunicipalityRecordTime() throws Exception {
        OffsetDateTime now = OffsetDateTime.now();
        loadMunicipality();
        String type = "municipality";

        ParameterMap searchParameters = new ParameterMap();
        searchParameters.add("registreringFra", now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        searchParameters.add("opdateretEfter", now.plusSeconds(5).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        ResponseEntity<String> response = restSearch(searchParameters, type);
        Assertions.assertEquals(200, response.getStatusCode().value());
        JsonNode jsonBody = objectMapper.readTree(response.getBody());
        JsonNode results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(0, results.size());

        searchParameters = new ParameterMap();
        searchParameters.add("registreringFra", now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        searchParameters.add("opdateretEfter", now.minusDays(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        response = restSearch(searchParameters, type);
        Assertions.assertEquals(200, response.getStatusCode().value());
        jsonBody = objectMapper.readTree(response.getBody());
        results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(1, results.size());
    }


    @Test
    public void testPostalCodeQuery() throws Exception {
        loadPostalcode();
        String type = "postalcode";

        ParameterMap searchParameters = new ParameterMap();
        searchParameters.add("navn", "Mestersvig");
        ResponseEntity<String> response;

        response = restSearch(searchParameters, type);
        Assertions.assertEquals(200, response.getStatusCode().value());
        JsonNode jsonBody = objectMapper.readTree(response.getBody());
        JsonNode results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals("a0c543c9-5588-42ef-9497-ab765fa3f8c0", results.get(0).get("UUID").asText());

        searchParameters = new ParameterMap();
        searchParameters.add("navn", "Mester*");

        response = restSearch(searchParameters, type);
        Assertions.assertEquals(200, response.getStatusCode().value());
        jsonBody = objectMapper.readTree(response.getBody());
        results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals("a0c543c9-5588-42ef-9497-ab765fa3f8c0", results.get(0).get("UUID").asText());


        searchParameters = new ParameterMap();
        searchParameters.add("postnummer", "3982");

        response = restSearch(searchParameters, type);
        Assertions.assertEquals(200, response.getStatusCode().value());
        jsonBody = objectMapper.readTree(response.getBody());
        results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals("a0c543c9-5588-42ef-9497-ab765fa3f8c0", results.get(0).get("UUID").asText());


        searchParameters = new ParameterMap();
        searchParameters.add("code", "3982");
        response = restSearch(searchParameters, type);
        Assertions.assertEquals(400, response.getStatusCode().value());
    }


    @Test
    public void testPostalCodeRecordTime() throws Exception {
        OffsetDateTime now = OffsetDateTime.now();
        loadPostalcode();
        String type = "postalcode";

        ParameterMap searchParameters = new ParameterMap();
        searchParameters.add("registreringFra", now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        searchParameters.add("opdateretEfter", now.plusSeconds(5).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        ResponseEntity<String> response = restSearch(searchParameters, type);
        Assertions.assertEquals(200, response.getStatusCode().value());
        JsonNode jsonBody = objectMapper.readTree(response.getBody());
        JsonNode results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(0, results.size());

        searchParameters = new ParameterMap();
        searchParameters.add("registreringFra", now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        searchParameters.add("opdateretEfter", now.minusDays(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        response = restSearch(searchParameters, type);
        Assertions.assertEquals(200, response.getStatusCode().value());
        jsonBody = objectMapper.readTree(response.getBody());
        results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(1, results.size());
    }


    @Test
    public void testRoadQuery() throws Exception {
        loadRoad();
        String type = "road";

        ParameterMap searchParameters = new ParameterMap();
        searchParameters.add("navn", "Aadarujuup Aqquserna");
        ResponseEntity<String> response;

        response = restSearch(searchParameters, type);
        Assertions.assertEquals(200, response.getStatusCode().value());
        JsonNode jsonBody = objectMapper.readTree(response.getBody());
        JsonNode results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals("e4dc6c09-baae-40b1-8696-57771b2f7a81", results.get(0).get("UUID").asText());


        searchParameters = new ParameterMap();
        searchParameters.add("navn", "Aadarujuup*");
        response = restSearch(searchParameters, type);
        Assertions.assertEquals(200, response.getStatusCode().value());
        jsonBody = objectMapper.readTree(response.getBody());
        results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals("e4dc6c09-baae-40b1-8696-57771b2f7a81", results.get(0).get("UUID").asText());


        searchParameters = new ParameterMap();
        searchParameters.add("kortNavn", "Aadarujuup Aqq.");
        response = restSearch(searchParameters, type);
        Assertions.assertEquals(200, response.getStatusCode().value());
        jsonBody = objectMapper.readTree(response.getBody());
        results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals("e4dc6c09-baae-40b1-8696-57771b2f7a81", results.get(0).get("UUID").asText());


        searchParameters = new ParameterMap();
        searchParameters.add("name", "Aadarujuup Aqquserna");
        response = restSearch(searchParameters, type);
        Assertions.assertEquals(400, response.getStatusCode().value());
    }

    @Test
    public void testRoadRecordTime() throws Exception {
        OffsetDateTime now = OffsetDateTime.now();
        loadRoad();
        String type = "road";

        ParameterMap searchParameters = new ParameterMap();
        searchParameters.add("registreringFra", now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        searchParameters.add("opdateretEfter", now.plusSeconds(5).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        ResponseEntity<String> response = restSearch(searchParameters, type);
        Assertions.assertEquals(200, response.getStatusCode().value());
        JsonNode jsonBody = objectMapper.readTree(response.getBody());
        JsonNode results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(0, results.size());

        searchParameters = new ParameterMap();
        searchParameters.add("registreringFra", now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        searchParameters.add("opdateretEfter", now.minusDays(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        response = restSearch(searchParameters, type);
        Assertions.assertEquals(200, response.getStatusCode().value());
        jsonBody = objectMapper.readTree(response.getBody());
        results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(1, results.size());
    }


    @Test
    public void testStateQuery() throws Exception {
        loadState();
        String type = "state";

        ParameterMap searchParameters = new ParameterMap();
        searchParameters.add("navn", "Eftersyn");
        ResponseEntity<String> response;

        response = restSearch(searchParameters, type);
        Assertions.assertEquals(200, response.getStatusCode().value());
        JsonNode jsonBody = objectMapper.readTree(response.getBody());
        JsonNode results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals("2e4b5cdc-5c85-4951-b41e-50138792daee", results.get(0).get("UUID").asText());


        searchParameters = new ParameterMap();
        searchParameters.add("navn", "Efter*");
        response = restSearch(searchParameters, type);
        Assertions.assertEquals(200, response.getStatusCode().value());
        jsonBody = objectMapper.readTree(response.getBody());
        results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals("2e4b5cdc-5c85-4951-b41e-50138792daee", results.get(0).get("UUID").asText());


        searchParameters = new ParameterMap();
        searchParameters.add("kode", "2");
        response = restSearch(searchParameters, type);
        Assertions.assertEquals(200, response.getStatusCode().value());
        jsonBody = objectMapper.readTree(response.getBody());
        results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals("2e4b5cdc-5c85-4951-b41e-50138792daee", results.get(0).get("UUID").asText());


        searchParameters = new ParameterMap();
        searchParameters.add("name", "Eftersyn");
        response = restSearch(searchParameters, type);
        Assertions.assertEquals(400, response.getStatusCode().value());

    }


    @Test
    public void testStateRecordTime() throws Exception {
        OffsetDateTime now = OffsetDateTime.now();
        loadState();
        String type = "state";

        ParameterMap searchParameters = new ParameterMap();
        searchParameters.add("registreringFra", now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        searchParameters.add("opdateretEfter", now.plusSeconds(5).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        ResponseEntity<String> response = restSearch(searchParameters, type);
        Assertions.assertEquals(200, response.getStatusCode().value());
        JsonNode jsonBody = objectMapper.readTree(response.getBody());
        JsonNode results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(0, results.size());

        searchParameters = new ParameterMap();
        searchParameters.add("registreringFra", now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        searchParameters.add("opdateretEfter", now.minusDays(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        response = restSearch(searchParameters, type);
        Assertions.assertEquals(200, response.getStatusCode().value());
        jsonBody = objectMapper.readTree(response.getBody());
        results = jsonBody.get("results");
        Assertions.assertTrue(results.isArray());
        Assertions.assertEquals(1, results.size());
    }

}
