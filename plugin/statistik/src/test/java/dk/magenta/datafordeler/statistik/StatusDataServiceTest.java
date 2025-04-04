package dk.magenta.datafordeler.statistik;

import com.fasterxml.jackson.core.JsonProcessingException;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.util.InputStreamReader;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.statistik.services.StatisticsService;
import dk.magenta.datafordeler.statistik.services.StatusDataService;
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
public class StatusDataServiceTest extends TestBase {

    @MockitoSpyBean
    private StatusDataService statusDataService;

    @BeforeEach
    public void initialize() throws Exception {
        this.setPath();
        this.loadPersonData("statusperson.txt");
        this.loadAllGeoAdress();
    }

    @AfterEach
    public void cleanup() {
        this.deleteAll();
    }

    @Test
    public void testStatusDataService() throws JsonProcessingException {
        when(statusDataService.getTimeintervallimit()).thenReturn(false);
        when(statusDataService.getWriteToLocalFile()).thenReturn(false);

        ResponseEntity<String> response;// = restTemplate.exchange("/statistik/status_data/?effectDate=2018-04-16", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);
        //Assertions.assertEquals(403, response.getStatusCodeValue());

        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        testUserDetails.giveAccess(StatistikRolesDefinition.EXECUTE_STATISTIK_ROLE);
        this.applyAccess(testUserDetails);

        response = restTemplate.exchange("/statistik/status_data/?effectDate=2018-07-01&registrationAt=2018-08-01", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);
        Assertions.assertEquals(200, response.getStatusCodeValue());
        Assertions.assertNotNull(response, "Response contains a body");

        String expected = "\"Pnr\";\"FoedAar\";\"Fornavn\";\"Efternavn\";\"Status\";\"FoedMynKod\";\"FoedMynTxt\";\"StatKod\";\"M_Pnr\";\"F_Pnr\";\"CivSt\";\"AegtePnr\";\"KomKod\";\"LokNavn\";\"LokKode\";\"LokKortNavn\";\"VejKod\";\"VejNavn\";\"HusNr\";\"Etage\";\"SideDoer\";\"Bnr\";\"TilFlyDto\";\"FlytProdDto\";\"Postnr\";\"CivDto\";\"CivProdDto\";\"Kirke\";\"ProtectionType\"\n" +
                "\"0101001234\";\"2000\";\"Tester Testmember\";\"Testersen\";\"05\";\"9516\";\"\";\"5100\";\"2903641234\";\"0101641234\";\"G\";\"0202994321\";\"0956\";\"Nuuk\";\"0600\";\"NUK\";\"0254\";\"Qarsaalik\";\"0018\";\"01\";\"tv\";\"1234\";\"30-08-2016\";\"31-08-2016\";\"3900\";\"12-10-2017\";\"13-10-2017\";\"F\";\"1\"";
        Assertions.assertEquals(
                this.csvToJsonString(expected),
                this.csvToJsonString(response.getBody().trim())
        );
    }

    @Test
    public void testFileOutput() throws IOException {
        when(statusDataService.getTimeintervallimit()).thenReturn(false);
        when(statusDataService.getWriteToLocalFile()).thenReturn(true);

        ResponseEntity<String> response = restTemplate.exchange("/statistik/status_data/?effectDate=2018-05-01&registrationAt=2018-08-01", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);
        Assertions.assertEquals(403, response.getStatusCodeValue());

        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        testUserDetails.giveAccess(StatistikRolesDefinition.EXECUTE_STATISTIK_ROLE);
        this.applyAccess(testUserDetails);

        response = restTemplate.exchange("/statistik/status_data/?effectDate=2018-07-01&registrationAt=2018-08-01", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);

        Assertions.assertEquals(200, response.getStatusCodeValue());

        String[] statusFiles = new File(StatisticsService.PATH_FILE).list((dir, name) -> name.startsWith(StatisticsService.ServiceName.STATUS.getIdentifier()));
        Assertions.assertEquals(1, statusFiles.length);

        FileInputStream fileInputStream = new FileInputStream(StatisticsService.PATH_FILE + File.separator + statusFiles[0]);
        String contents = InputStreamReader.readInputStream(
                fileInputStream, "UTF-8"
        );
        fileInputStream.close();

        String expected = "\"Pnr\";\"FoedAar\";\"Fornavn\";\"Efternavn\";\"Status\";\"FoedMynKod\";\"FoedMynTxt\";\"StatKod\";\"M_Pnr\";\"F_Pnr\";\"CivSt\";\"AegtePnr\";\"KomKod\";\"LokNavn\";\"LokKode\";\"LokKortNavn\";\"VejKod\";\"VejNavn\";\"HusNr\";\"Etage\";\"SideDoer\";\"Bnr\";\"TilFlyDto\";\"FlytProdDto\";\"Postnr\";\"CivDto\";\"CivProdDto\";\"Kirke\";\"ProtectionType\"\n" +
                "\"0101001234\";\"2000\";\"Tester Testmember\";\"Testersen\";\"05\";\"9516\";\"\";\"5100\";\"2903641234\";\"0101641234\";\"G\";\"0202994321\";\"0956\";\"Nuuk\";\"0600\";\"NUK\";\"0254\";\"Qarsaalik\";\"0018\";\"01\";\"tv\";\"1234\";\"30-08-2016\";\"31-08-2016\";\"3900\";\"12-10-2017\";\"13-10-2017\";\"F\";\"1\"";
        Assertions.assertEquals(
                this.csvToJsonString(expected),
                this.csvToJsonString(contents.trim())
        );
    }

}
