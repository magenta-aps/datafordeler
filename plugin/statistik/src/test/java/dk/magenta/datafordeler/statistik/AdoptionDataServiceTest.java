package dk.magenta.datafordeler.statistik;

import com.fasterxml.jackson.core.JsonProcessingException;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.statistik.services.AdoptionDataService;
import org.junit.jupiter.api.AfterEach;
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
public class AdoptionDataServiceTest extends TestBase {

    @MockitoSpyBean
    private AdoptionDataService adoptionDataService;

    @BeforeEach
    public void initialize() throws Exception {
        this.setPath();
        this.loadPersonData("adoptedpersons.txt");
        this.loadAllGeoAdress(sessionManager);
    }

    @AfterEach
    public void cleanup() {
        this.deleteAll();
    }

    @Test
    public void testService() throws JsonProcessingException {
        when(adoptionDataService.getTimeintervallimit()).thenReturn(false);
        when(adoptionDataService.getWriteToLocalFile()).thenReturn(false);

        ResponseEntity<String> response = restTemplate.exchange("/statistik/adoption_data/", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);
        Assertions.assertEquals(403, response.getStatusCodeValue());

        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        testUserDetails.giveAccess(StatistikRolesDefinition.EXECUTE_STATISTIK_ROLE);
        this.applyAccess(testUserDetails);

        response = restTemplate.exchange("/statistik/adoption_data/?registrationAfter=2000-01-01", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);
        Assertions.assertEquals(200, response.getStatusCodeValue());

        Assertions.assertNotNull(response, "Response contains a body");
        String expected = "\"CODE\";\"Pnr\";\"FoedAar\";\"M_Pnr\";\"F_Pnr\";\"AM_mynkod\";\"AF_mynkod\";\"FoedMynKod\";\"StatKod\";\"ProdDto\";\"ProdFilDto\";\"AdoptionDto\";\"KomKod\";\"LokNavn\";\"LokKortNavn\";\"LokKode\";\"VejKod\";\"HusNr\";\"Etage\";\"Bnr\"\n" +
                "\"PRE\";\"1111111111\";\"2004\";\"2222222228\";\"2222222229\";\"99\";\"1202\";\"9507\";\"5100\";\"04-12-2019\";;\"04-12-2019\";\"956\";\"Nuuk\";\"NUK\";\"0600\";\"0254\";\"0018\";\"1\";\"1234\"\n" +
                "\"POST\";\"1111111111\";\"2004\";\"2222222226\";\"2222222227\";\"1350\";\"1350\";\"9507\";\"5100\";\"04-12-2019\";;\"04-12-2019\";\"956\";\"Nuuk\";\"NUK\";\"0600\";\"0254\";\"0018\";\"1\";\"1234\"\n" +
                "\"PRE\";\"1111111112\";\"2004\";\"2222222222\";\"2222222225\";\"99\";\"1202\";\"9507\";\"5100\";\"18-12-2019\";;\"18-12-2019\";\"956\";\"Nuuk\";\"NUK\";\"0600\";\"0254\";\"0018\";\"1\";\"1234\"\n" +
                "\"POST\";\"1111111112\";\"2004\";\"2222222222\";\"2222222223\";\"99\";\"1316\";\"9507\";\"5100\";\"18-12-2019\";;\"18-12-2019\";\"956\";\"Nuuk\";\"NUK\";\"0600\";\"0254\";\"0018\";\"1\";\"1234\"\n" +
                "";

        Assertions.assertEquals(
                objectMapper.readTree(this.csvToJsonString(expected)),
                objectMapper.readTree(this.csvToJsonString(response.getBody().trim()))
        );
    }
}
