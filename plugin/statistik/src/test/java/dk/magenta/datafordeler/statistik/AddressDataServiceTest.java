package dk.magenta.datafordeler.statistik;


import com.fasterxml.jackson.core.JsonProcessingException;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.util.InputStreamReader;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.statistik.services.AddressDataService;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;


@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AddressDataServiceTest extends TestBase {

    @Autowired
    protected AddressDataService addressDataService;

    @BeforeEach
    public void initialize() throws Exception {
        this.setPath();
        testsUtils.loadPersonData("bornperson.txt");
        this.loadAllGeoAdress(sessionManager);
        addressDataService.setUseTimeintervallimit(false);
    }

    @Test
    public void testService() throws JsonProcessingException {
        addressDataService.setWriteToLocalFile(false);

        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        testsUtils.applyAccess(testUserDetails);

        ResponseEntity<String> response = restTemplate.exchange("/statistik/address_data/?registrationAfter=2000-01-01", HttpMethod.GET, new HttpEntity("", new HttpHeaders()), String.class);
        Assertions.assertEquals(InputStreamReader.readInputStream(AddressDataService.class.getResourceAsStream("/addressServiceForm.html")), response.getBody());

        MultiValueMap<String, Object> form = new LinkedMultiValueMap<String, Object>();
        form.add("file", new InputStreamResource(AddressDataServiceTest.class.getResourceAsStream("/addressInput.csv")));

        response = restTemplate.exchange("/statistik/address_data/?registrationAfter=2000-01-01", HttpMethod.POST, new HttpEntity(form, new HttpHeaders()), String.class);
        String expected = "\"Pnr\";\"Fornavn\";\"Mellemnavn\";\"Efternavn\";\"Bnr\";\"VejNavn\";\"HusNr\";\"Etage\";\"SideDoer\";\"Postnr\";\"PostDistrikt\"\n" +
                "\"0101001234\";\"Tester Testmember\";;\"Testersen\";\"1234\";\"Qarsaalik\";\"18\";\"1\";\"tv\";\"3900\";\"Nuuk\";\n";
        Assertions.assertEquals(
                this.csvToJsonString(expected),
                this.csvToJsonString(response.getBody().trim())
        );

    }
}
