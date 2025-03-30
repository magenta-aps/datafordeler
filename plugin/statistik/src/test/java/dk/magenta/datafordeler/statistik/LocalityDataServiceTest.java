package dk.magenta.datafordeler.statistik;


import com.fasterxml.jackson.core.JsonProcessingException;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.statistik.services.LocalityDataService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.mockito.Mockito.when;

@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LocalityDataServiceTest extends TestBase {

    @MockitoSpyBean
    private LocalityDataService localityDataService;

    @BeforeEach
    public void initialize() throws Exception {
        this.setPath();
        this.loadGeoLocalityData("Lokalitet_test.json");
    }

    @Test
    public void testService() throws JsonProcessingException {
        when(localityDataService.getWriteToLocalFile()).thenReturn(false);

        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        testUserDetails.giveAccess(StatistikRolesDefinition.EXECUTE_STATISTIK_ROLE);
        this.applyAccess(testUserDetails);

        MultiValueMap<String, Object> form = new LinkedMultiValueMap<String, Object>();
        form.add("file", new InputStreamResource(LocalityDataServiceTest.class.getResourceAsStream("/addressInput.csv")));

        ResponseEntity<String> response = restTemplate.exchange("/statistik/locality_data/", HttpMethod.GET, new HttpEntity(form, new HttpHeaders()), String.class);
        Assertions.assertEquals(200, response.getStatusCodeValue());
        Assertions.assertNotNull(response, "Response contains a body");
        /*String expected = "\"KomKod\";\"KomKortNavn\";\"KomNavn\";\"LokKode\";\"LokKortNavn\";\"LokNavn\";\"LokTypeKod\";\"LokTypeNavn\";\"LokStatusKod\";\"LokStatusNavn\";\"Void\"\n" +
                "\"960\";\"AK\";\"Avannaata\";\"1700\";\"QNQ\";\"Qaanaaq\";\"1\";\"By\";\"15\";\"Aktiv\";\n" +
                "\"960\";\"AK\";\"Avannaata\";\"1706\";\"MOR\";\"Moriusaq\";\"4\";\"Nedlagt bygd\";\"20\";\"Nedlagt\";";

        Assertions.assertEquals(
                testUtil.csvToJsonString(expected),
                testUtil.csvToJsonString(response.getBody().trim())
        );*/


    }
}
