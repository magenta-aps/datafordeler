package dk.magenta.datafordeler.statistik;


import com.fasterxml.jackson.core.JsonProcessingException;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.statistik.services.RoadDataService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.mockito.Mockito.when;

@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RoadDataServiceTest extends TestBase {

    @MockitoSpyBean
    private RoadDataService roadDataService;

    @BeforeEach
    public void initialize() throws Exception {
        this.setPath();
        this.loadGeoLocalityData("Lokalitet_test.json");
        this.loadGeoRoadData("Vejmidte_test.json");
        this.loadAccessLocalityData("Adgangsadresse_test.json");//HER
        this.loadPostalLocalityData("Postnummer_test.json");
    }

    @Test
    public void testDummy() {
    }

    @Test
    public void testService() throws JsonProcessingException {
        when(roadDataService.getWriteToLocalFile()).thenReturn(false);

        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        testUserDetails.giveAccess(StatistikRolesDefinition.EXECUTE_STATISTIK_ROLE);
        this.applyAccess(testUserDetails);

        ResponseEntity<String> response = restTemplate.exchange("/statistik/road_data/", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);
        Assertions.assertEquals(200, response.getStatusCodeValue());
        Assertions.assertNotNull(response, "Response contains a body");
        String expected = "\"KomKod\";\"LokKode\";\"VejKod\";\"VejNavn\";\"Bygde\";\"Postnr\";\"Void\"\n" +
                "\"960\";\"1700\";\"257\";\"Qaanaaq\";\"Qaanaaq\";\"3971\";;";

        Assertions.assertEquals(
                this.csvToJsonString(expected),
                this.csvToJsonString(response.getBody().trim())
        );
    }
}
