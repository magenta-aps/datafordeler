package dk.magenta.datafordeler.statistik;

import com.fasterxml.jackson.core.JsonProcessingException;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.util.InputStreamReader;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.statistik.services.DeathDataService;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;



@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DeathDataServiceTest extends TestBase {

    @Autowired
    private DeathDataService deathDataService;

    @BeforeEach
    public void initialize() throws Exception {
        this.setPath();
        testsUtils.loadPersonData("deadperson.txt");
        this.loadAllGeoAdress(sessionManager);
        deathDataService.setUseTimeintervallimit(false);
    }

    @AfterEach
    public void cleanup() {
        testsUtils.deleteAll();
    }

    @Test
    public void testService() throws JsonProcessingException {
        deathDataService.setWriteToLocalFile(false);

        ResponseEntity<String> response = restTemplate.exchange("/statistik/death_data/?registrationAfter=2017-01-01", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);
        Assertions.assertEquals(403, response.getStatusCodeValue());

        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        testUserDetails.giveAccess(StatistikRolesDefinition.EXECUTE_STATISTIK_ROLE);
        testsUtils.applyAccess(testUserDetails);

        response = restTemplate.exchange("/statistik/death_data/?registrationAfter=2000-01-01", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);
        Assertions.assertEquals(200, response.getStatusCodeValue());
        Assertions.assertNotNull(response, "Response contains a body");
        String expected = "\"Status\";\"DoedDto\";\"ProdDto\";\"ProdFilDto\";\"Pnr\";\"CivSt\";\"FoedAar\";\"M_Pnr\";\"F_Pnr\";\"AegtePnr\";\"PnrGaeld\";\"StatKod\";\"FoedMynKod\";\"FoedMynTxt\";\"KomKod\";\"LokNavn\";\"LokKortNavn\";\"LokKode\";\"VejKod\";\"HusNr\";\"Etage\";\"SideDoer\";\"Bnr\"\n" +
                "\"90\";\"30-08-2017\";\"31-08-2017\";\"\";\"0101501234\";\"\";\"2000\";\"2903641234\";\"0101641234\";;;;\"9516\";\"\";\"956\";\"Nuuk\";\"NUK\";\"0600\";\"0254\";\"0018\";\"1\";\"tv\";\"1234\"";
        Assertions.assertEquals(
                this.csvToJsonString(expected),
                this.csvToJsonString(response.getBody().trim())
        );
    }


    @Test
    public void testFileOutput() throws IOException {
        deathDataService.setWriteToLocalFile(true);
        ResponseEntity<String> response = restTemplate.exchange("/statistik/death_data/?registrationAfter=2017-01-01", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);
        Assertions.assertEquals(403, response.getStatusCodeValue());

        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        testUserDetails.giveAccess(StatistikRolesDefinition.EXECUTE_STATISTIK_ROLE);
        testsUtils.applyAccess(testUserDetails);

        response = restTemplate.exchange("/statistik/death_data/?registrationAfter=2017-01-01", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);

        Assertions.assertEquals(200, response.getStatusCodeValue());

        String[] deathFiles = new File(StatisticsService.PATH_FILE).list((dir, name) -> name.startsWith(StatisticsService.ServiceName.DEATH.getIdentifier()));
        Assertions.assertEquals(1, deathFiles.length);

        FileInputStream fileInputStream = new FileInputStream(StatisticsService.PATH_FILE + File.separator + deathFiles[0]);
        String contents = InputStreamReader.readInputStream(
                fileInputStream, "UTF-8"
        );
        fileInputStream.close();

        String expected = "\"Status\";\"DoedDto\";\"ProdDto\";\"ProdFilDto\";\"Pnr\";\"CivSt\";\"FoedAar\";\"M_Pnr\";\"F_Pnr\";\"AegtePnr\";\"PnrGaeld\";\"StatKod\";\"FoedMynKod\";\"FoedMynTxt\";\"KomKod\";\"LokNavn\";\"LokKortNavn\";\"LokKode\";\"VejKod\";\"HusNr\";\"Etage\";\"SideDoer\";\"Bnr\"\n" +
                "\"90\";\"30-08-2017\";\"31-08-2017\";\"\";\"0101501234\";\"\";\"2000\";\"2903641234\";\"0101641234\";;;;\"9516\";\"\";\"956\";\"Nuuk\";\"NUK\";\"0600\";\"0254\";\"0018\";\"1\";\"tv\";\"1234\"";
        Assertions.assertEquals(
                this.csvToJsonString(expected),
                this.csvToJsonString(contents.trim())
        );

    }

}
