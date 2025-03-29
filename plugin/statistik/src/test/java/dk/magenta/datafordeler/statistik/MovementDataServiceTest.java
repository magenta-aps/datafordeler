package dk.magenta.datafordeler.statistik;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.util.InputStreamReader;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.statistik.services.MovementDataService;
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
public class MovementDataServiceTest extends TestBase {

    @Autowired
    private MovementDataService movementDataService;

    @BeforeEach
    public void initialize() throws Exception {
        testsUtils.deleteAll();
        this.setPath();
        this.loadAllGeoAdress(sessionManager);
        testsUtils.loadPersonData("movedperson.txt");
        testsUtils.loadPersonData("movedpersonExample2.txt");
        testsUtils.loadPersonData("movedpersonExample3.txt");
    }

    @AfterEach
    public void cleanup() {
        testsUtils.deleteAll();
    }

    @Test
    public void testServiceMovedPerson() throws Exception {
        movementDataService.setWriteToLocalFile(false);
        TestUserDetails testUserDetails = new TestUserDetails();

        HttpEntity<String> httpEntity = new HttpEntity<>("", new HttpHeaders());
        ResponseEntity<String> response;


        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        testUserDetails.giveAccess(StatistikRolesDefinition.EXECUTE_STATISTIK_ROLE);
        testsUtils.applyAccess(testUserDetails);

        response = restTemplate.exchange("/statistik/movement_data/?pnr=0101001234&registrationAfter=1900-01-01&registrationBefore=2018-08-01", HttpMethod.GET, httpEntity, String.class);
        Assertions.assertEquals(200, response.getStatusCodeValue());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertFalse(response.getBody().isEmpty());
        String expected = "\"Pnr\";\"FoedAar\";\"PnrGaeld\";\"Status\";\"FoedMynKod\";\"StatKod\";\"M_Pnr\";\"F_Pnr\";\"AegtePnr\";\"ProdDto\";\"ProdFilDto\";\"FlyDto\";\"FraLand\";\"FraKomKod\";\"FraLokKortNavn\";\"FraVejKod\";\"FraHusNr\";\"FraEtage\";\"FraSideDoer\";\"FraBnr\";\"TilLand\";\"TilKomKod\";\"TilLokKortNavn\";\"TilVejKod\";\"TilHusNr\";\"TilEtage\";\"TilSideDoer\";\"TilBnr\"\n" +
                "\"0101001234\";\"2000\";;\"05\";\"9516\";;\"2903641234\";\"0101641234\";\"1111111111\";\"31-08-2016\";\"\";\"31-08-2016\";;\"0956\";\"NUK\";\"0254\";\"0018\";\"\";\"\";\"5678\";;\"0956\";\"NUK\";\"0254\";\"0018\";\"01\";\"tv\";\"1234\"\n" +
                "\"0101001234\";\"2000\";;;\"9516\";;\"2903641234\";\"0101641234\";\"1111111111\";\"01-03-2018\";\"\";\"01-03-2012\";\"0\";;;;;;;;;\"0956\";\"NUK\";\"0254\";\"0018\";\"\";\"\";\"5678\"";
        compareJSONARRAYWithIgnoredValues(
                this.csvToJsonString(expected),
                this.csvToJsonString(response.getBody().trim()), "FlyDto");
    }

    @Test
    public void testServiceMovedPersonEx2() throws Exception {
        movementDataService.setWriteToLocalFile(false);
        TestUserDetails testUserDetails = new TestUserDetails();

        HttpEntity<String> httpEntity = new HttpEntity<>("", new HttpHeaders());
        ResponseEntity<String> response;


        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        testUserDetails.giveAccess(StatistikRolesDefinition.EXECUTE_STATISTIK_ROLE);
        testsUtils.applyAccess(testUserDetails);

        response = restTemplate.exchange("/statistik/movement_data/?pnr=0101011235&registrationAfter=1900-01-01&registrationBefore=2019-08-01", HttpMethod.GET, httpEntity, String.class);
        Assertions.assertNotNull(response.getBody());

        Assertions.assertFalse(response.getBody().isEmpty());
        String expected = "\"Pnr\";\"FoedAar\";\"PnrGaeld\";\"Status\";\"FoedMynKod\";\"StatKod\";\"M_Pnr\";\"F_Pnr\";\"AegtePnr\";\"ProdDto\";\"ProdFilDto\";\"FlyDto\";\"FraLand\";\"FraKomKod\";\"FraLokKortNavn\";\"FraVejKod\";\"FraHusNr\";\"FraEtage\";\"FraSideDoer\";\"FraBnr\";\"TilLand\";\"TilKomKod\";\"TilLokKortNavn\";\"TilVejKod\";\"TilHusNr\";\"TilEtage\";\"TilSideDoer\";\"TilBnr\"\n" +
                "\"0101011235\";\"2005\";;\"05\";;;\"1111111112\";\"1111111111\";;\"09-05-2019\";\"\";\"11-04-2019\";;\"0956\";\"\";\"0243\";\"\";\"\";\"\";\"1654\";;\"0957\";\"\";\"0102\";\"0002\";\"\";\"\";\"\"";
        compareJSONARRAYWithIgnoredValues(
                this.csvToJsonString(expected),
                this.csvToJsonString(response.getBody().trim()),
                "FlyDto"
        );
    }


    @Test
    public void testServiceMovedPersonEx3() throws Exception {
        movementDataService.setWriteToLocalFile(false);
        TestUserDetails testUserDetails = new TestUserDetails();

        HttpEntity<String> httpEntity = new HttpEntity<>("", new HttpHeaders());
        ResponseEntity<String> response;


        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        testUserDetails.giveAccess(StatistikRolesDefinition.EXECUTE_STATISTIK_ROLE);
        testsUtils.applyAccess(testUserDetails);

        response = restTemplate.exchange("/statistik/movement_data/?pnr=0101011236&registrationAfter=1900-01-01&registrationBefore=2019-08-01", HttpMethod.GET, httpEntity, String.class);
        Assertions.assertNotNull(response.getBody());

        Assertions.assertFalse(response.getBody().isEmpty());
        String expected = "\"Pnr\";\"FoedAar\";\"PnrGaeld\";\"Status\";\"FoedMynKod\";\"StatKod\";\"M_Pnr\";\"F_Pnr\";\"AegtePnr\";\"ProdDto\";\"ProdFilDto\";\"FlyDto\";\"FraLand\";\"FraKomKod\";\"FraLokKortNavn\";\"FraVejKod\";\"FraHusNr\";\"FraEtage\";\"FraSideDoer\";\"FraBnr\";\"TilLand\";\"TilKomKod\";\"TilLokKortNavn\";\"TilVejKod\";\"TilHusNr\";\"TilEtage\";\"TilSideDoer\";\"TilBnr\"\n" +
                "\"0101011236\";\"1982\";;\"05\";\"9509\";;\"1111111111\";\"111111111\";\"\";\"21-05-2019\";\"\";\"14-02-2019\";;\"0957\";\"\";\"0125\";\"0056\";\"\";\"O-1\";\"\";;\"0957\";\"\";\"0125\";\"0056\";\"\";\"1-1\";\"\"\n" +
                "\"0101011236\";\"1982\";;\"05\";\"9509\";;\"1111111111\";\"111111111\";\"\";\"11-02-2019\";\"\";\"01-02-2019\";;\"0956\";\"\";\"0204\";\"009A\";\"\";\"0402\";\"\";;\"0957\";\"\";\"0125\";\"0056\";\"\";\"O-1\";\"\"\n";

        compareJSONARRAYWithIgnoredValues(
                this.csvToJsonString(expected),
                this.csvToJsonString(response.getBody().trim()),
                "FlyDto"
        );
    }

    @Test
    public void testFileOutput() throws IOException {
        movementDataService.setWriteToLocalFile(true);

        TestUserDetails testUserDetails = new TestUserDetails();

        HttpEntity<String> httpEntity = new HttpEntity<>("", new HttpHeaders());
        ResponseEntity<String> response = restTemplate.exchange("/statistik/movement_data/", HttpMethod.GET, httpEntity, String.class);
        Assertions.assertEquals(403, response.getStatusCodeValue());

        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        testUserDetails.giveAccess(StatistikRolesDefinition.EXECUTE_STATISTIK_ROLE);
        testsUtils.applyAccess(testUserDetails);

        response = restTemplate.exchange("/statistik/movement_data/", HttpMethod.GET, httpEntity, String.class);
        Assertions.assertEquals(400, response.getStatusCodeValue());

        response = restTemplate.exchange("/statistik/movement_data/?pnr=0101001234&registrationAfter=2016-01-01", HttpMethod.GET, httpEntity, String.class);

        Assertions.assertEquals(200, response.getStatusCodeValue());

        String[] movementFiles = new File(StatisticsService.PATH_FILE).list((dir, name) -> name.startsWith(StatisticsService.ServiceName.MOVEMENT.getIdentifier()));
        Assertions.assertEquals(1, movementFiles.length);

        FileInputStream fileInputStream = new FileInputStream(StatisticsService.PATH_FILE + File.separator + movementFiles[0]);
        String contents = InputStreamReader.readInputStream(
                fileInputStream, "UTF-8"
        );

        String expected = "\"Pnr\";\"FoedAar\";\"PnrGaeld\";\"Status\";\"FoedMynKod\";\"StatKod\";\"M_Pnr\";\"F_Pnr\";\"AegtePnr\";\"ProdDto\";\"ProdFilDto\";\"FlyDto\";\"FraLand\";\"FraKomKod\";\"FraLokKortNavn\";\"FraVejKod\";\"FraHusNr\";\"FraEtage\";\"FraSideDoer\";\"FraBnr\";\"TilLand\";\"TilKomKod\";\"TilLokKortNavn\";\"TilVejKod\";\"TilHusNr\";\"TilEtage\";\"TilSideDoer\";\"TilBnr\"\n" +
                "\"0101001234\";\"2000\";\"\";\"05\";\"9516\";\"\";\"2903641234\";\"0101641234\";\"1111111111\";\"31-08-2016\";\"\";\"31-08-2016\";\"\";\"0956\";\"NUK\";\"0254\";\"0018\";\"\";\"\";\"5678\";\"\";\"0956\";\"NUK\";\"0254\";\"0018\";\"01\";\"tv\";\"1234\"\n" +
                "\"0101001234\";\"2000\";\"\";\"\";\"9516\";\"\";\"2903641234\";\"0101641234\";\"1111111111\";\"01-03-2018\";\"\";\"01-03-2012\";\"0\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"\";\"0956\";\"NUK\";\"0254\";\"0018\";\"\";\"\";\"5678\"";

        compareJSONARRAYWithIgnoredValues(
                this.csvToJsonString(expected),
                this.csvToJsonString(contents.trim()),
                "FlyDto"
        );
    }

}
