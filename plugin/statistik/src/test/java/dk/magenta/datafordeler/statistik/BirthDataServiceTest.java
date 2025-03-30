package dk.magenta.datafordeler.statistik;

import com.fasterxml.jackson.core.JsonProcessingException;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.util.InputStreamReader;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.statistik.services.BirthDataService;
import dk.magenta.datafordeler.statistik.services.StatisticsService;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.mockito.Mockito.when;

@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BirthDataServiceTest extends TestBase {

    @MockitoSpyBean
    private BirthDataService birthDataService;

    @BeforeEach
    public void initialize() throws Exception {
        this.setPath();
        this.loadPersonData("bornperson.txt");
        this.loadAllGeoAdress(sessionManager);
    }

    @AfterEach
    public void cleanup() {
        this.deleteAll();
    }

    @Test
    public void testService() throws JsonProcessingException {
        when(birthDataService.getTimeintervallimit()).thenReturn(false);
        when(birthDataService.getWriteToLocalFile()).thenReturn(false);

        ResponseEntity<String> response = restTemplate.exchange("/statistik/birth_data/", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);
        Assertions.assertEquals(403, response.getStatusCodeValue());

        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        testUserDetails.giveAccess(StatistikRolesDefinition.EXECUTE_STATISTIK_ROLE);
        this.applyAccess(testUserDetails);

        response = restTemplate.exchange("/statistik/birth_data/?registrationAfter=2000-01-01&afterDate=1999-01-01", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);
        Assertions.assertEquals(200, response.getStatusCodeValue());

        Assertions.assertNotNull(response, "Response contains a body");
        String expected = "\"B_Pnr\";\"B_FoedAar\";\"B_PnrGaeld\";\"B_FoedMynKod\";\"B_FoedMynTxt\";\"B_FoedMynKodTxt\";\"B_StatKod\";\"B_ProdDto\";\"B_ProdFilDto\";\"M_Pnr\";\"M_FoedMynKod\";\"M_FoedMynTxt\";\"M_FoedMynKodTxt\";\"M_StatKod\";\"M_KomKod\";\"M_LokNavn\";\"M_LokKode\";\"M_VejKod\";\"M_HusNr\";\"M_Etage\";\"M_SideDoer\";\"M_Bnr\";\"F_Pnr\";\"F_FoedMynKod\";\"F_FoedMynTxt\";\"F_FoedMynKodTxt\";\"F_StatKod\";\"F_KomKod\";\"F_LokNavn\";\"F_LokKode\";\"F_VejKod\";\"F_HusNr\";\"F_Etage\";\"F_SideDoer\";\"F_Bnr\"\n" +
                "\"0101001234\";\"2000\";;\"9516\";\"\";\"0\";;\"13-01-2000\";\"\";\"2903641234\";\"6666\";\"\";;\"5100\";\"956\";\"Nuuk\";\"0600\";\"0254\";\"0018\";\"1\";\"tv\";\"1234\";\"0101641234\";\"8888\";\"\";;\"5100\";\"956\";\"Nuuk\";\"0600\";\"0254\";\"0018\";\"1\";\"tv\";\"1234\"";
        Assertions.assertEquals(
                this.csvToJsonString(expected),
                this.csvToJsonString(response.getBody().trim())
        );

        response = restTemplate.exchange("/statistik/birth_data/?registrationAfter=2000-01-15", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);
        Assertions.assertEquals(204, response.getStatusCodeValue());
    }

    @Test
    public void testFileOutput() throws IOException {
        when(birthDataService.getTimeintervallimit()).thenReturn(false);
        when(birthDataService.getWriteToLocalFile()).thenReturn(true);

        ResponseEntity<String> response = restTemplate.exchange("/statistik/birth_data/?registrationAfter=2000-01-01", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);
        Assertions.assertEquals(403, response.getStatusCodeValue());

        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        testUserDetails.giveAccess(StatistikRolesDefinition.EXECUTE_STATISTIK_ROLE);
        this.applyAccess(testUserDetails);

        response = restTemplate.exchange("/statistik/birth_data/?registrationAfter=2000-01-01", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);
        Assertions.assertEquals(200, response.getStatusCodeValue());

        String[] birthFiles = new File(StatisticsService.PATH_FILE).list((dir, name) -> name.startsWith(StatisticsService.ServiceName.BIRTH.getIdentifier()));
        Assertions.assertEquals(1, birthFiles.length);

        FileInputStream fileInputStream = new FileInputStream(StatisticsService.PATH_FILE + File.separator + birthFiles[0]);
        String contents = InputStreamReader.readInputStream(
                fileInputStream, "UTF-8"
        );
        fileInputStream.close();

        String expected = "\"B_Pnr\";\"B_FoedAar\";\"B_PnrGaeld\";\"B_FoedMynKod\";\"B_FoedMynTxt\";\"B_FoedMynKodTxt\";\"B_StatKod\";\"B_ProdDto\";\"B_ProdFilDto\";\"M_Pnr\";\"M_FoedMynKod\";\"M_FoedMynTxt\";\"M_FoedMynKodTxt\";\"M_StatKod\";\"M_KomKod\";\"M_LokNavn\";\"M_LokKode\";\"M_VejKod\";\"M_HusNr\";\"M_Etage\";\"M_SideDoer\";\"M_Bnr\";\"F_Pnr\";\"F_FoedMynKod\";\"F_FoedMynTxt\";\"F_FoedMynKodTxt\";\"F_StatKod\";\"F_KomKod\";\"F_LokNavn\";\"F_LokKode\";\"F_VejKod\";\"F_HusNr\";\"F_Etage\";\"F_SideDoer\";\"F_Bnr\"\n" +
                "\"0101001234\";\"2000\";;\"9516\";\"\";\"0\";;\"13-01-2000\";\"\";\"2903641234\";\"6666\";\"\";;\"5100\";\"956\";\"Nuuk\";\"0600\";\"0254\";\"0018\";\"1\";\"tv\";\"1234\";\"0101641234\";\"8888\";\"\";;\"5100\";\"956\";\"Nuuk\";\"0600\";\"0254\";\"0018\";\"1\";\"tv\";\"1234\"";
        Assertions.assertEquals(
                this.csvToJsonString(expected),
                this.csvToJsonString(contents.trim())
        );

    }


}
