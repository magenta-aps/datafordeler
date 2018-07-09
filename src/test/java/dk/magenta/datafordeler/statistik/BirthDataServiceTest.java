package dk.magenta.datafordeler.statistik;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.util.InputStreamReader;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.statistik.services.BirthDataService;
import dk.magenta.datafordeler.statistik.services.StatisticsService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BirthDataServiceTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PersonTestsUtils testsUtils;

    TestUserDetails testUserDetails;

    @Autowired
    private BirthDataService birthDataService;

    @Before
    public void initialize() throws Exception {
        testsUtils.setPath();
        testsUtils.loadPersonData("bornperson.txt");
        testsUtils.loadGladdrregData();
    }

    @After
    public void cleanup() {
        testsUtils.clearPath();
        testsUtils.deleteAll();
    }

    @Test
    public void testService() {
        birthDataService.setWriteToLocalFile(false);

        ResponseEntity<String> response = restTemplate.exchange("/statistik/birth_data/", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);
        Assert.assertEquals(403, response.getStatusCodeValue());

        testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        testsUtils.applyAccess(testUserDetails);

        response = restTemplate.exchange("/statistik/birth_data/?registrationAfter=2000-01-01", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);
        Assert.assertEquals(200, response.getStatusCodeValue());

        Assert.assertNotNull("Response contains a body", response);
        Assert.assertEquals("\"B_Pnr\";\"B_FoedAar\";\"B_PnrGaeld\";\"B_FoedMynKod\";\"B_FoedMynTxt\";\"B_StatKod\";\"B_ProdDto\";\"M_Pnr\";\"M_FoedMynKod\";\"M_FoedMynTxt\";\"M_StatKod\";\"M_KomKod\";\"M_LokNavn\";\"M_LokKode\";\"M_VejKod\";\"M_HusNr\";\"M_Etage\";\"M_SideDoer\";\"M_Bnr\";\"F_Pnr\";\"F_FoedMynKod\";\"F_FoedMynTxt\";\"F_StatKod\";\"F_KomKod\";\"F_LokNavn\";\"F_LokKode\";\"F_VejKod\";\"F_HusNr\";\"F_Etage\";\"F_SideDoer\";\"F_Bnr\"\n" +
                        "\"0101001234\";\"2000\";;\"9516\";\"0\";;\"13-01-2000\";\"2903641234\";\"6666\";\"0\";\"5100\";\"955\";\"Paamiut\";\"PAA\";\"0001\";\"0005\";\"1\";\"tv\";\"1234\";\"0101641234\";\"8888\";\"0\";\"5100\";\"955\";\"Paamiut\";\"PAA\";\"0001\";\"0005\";\"1\";\"tv\";\"1234\"",
                response.getBody().trim()
        );
        System.out.println("Body response: "+response.getBody());
        
        response = restTemplate.exchange("/statistik/birth_data/?registrationAfter=2000-01-15", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);
        Assert.assertEquals(204, response.getStatusCodeValue());
    }

    @Test
    public void testFileOutput() throws IOException {
        birthDataService.setWriteToLocalFile(true);

        ResponseEntity<String> response = restTemplate.exchange("/statistik/birth_data/?registrationAfter=2000-01-01", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);
        Assert.assertEquals(403, response.getStatusCodeValue());

        testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        testsUtils.applyAccess(testUserDetails);

        response = restTemplate.exchange("/statistik/birth_data/?registrationAfter=2000-01-01", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);
        Assert.assertEquals(200, response.getStatusCodeValue());
        Assert.assertNull(response.getBody());

        String[] birthFiles = new File(StatisticsService.PATH_FILE).list((dir, name) -> name.startsWith("birth"));
        Assert.assertEquals(1, birthFiles.length);

        FileInputStream fileInputStream = new FileInputStream(StatisticsService.PATH_FILE + File.separator + birthFiles[0]);
        String contents = InputStreamReader.readInputStream(
                fileInputStream,"UTF-8"
        );
        fileInputStream.close();

        Assert.assertEquals(
                "\"B_Pnr\";\"B_FoedAar\";\"B_PnrGaeld\";\"B_FoedMynKod\";\"B_FoedMynTxt\";\"B_StatKod\";\"B_ProdDto\";\"M_Pnr\";\"M_FoedMynKod\";\"M_FoedMynTxt\";\"M_StatKod\";\"M_KomKod\";\"M_LokNavn\";\"M_LokKode\";\"M_VejKod\";\"M_HusNr\";\"M_Etage\";\"M_SideDoer\";\"M_Bnr\";\"F_Pnr\";\"F_FoedMynKod\";\"F_FoedMynTxt\";\"F_StatKod\";\"F_KomKod\";\"F_LokNavn\";\"F_LokKode\";\"F_VejKod\";\"F_HusNr\";\"F_Etage\";\"F_SideDoer\";\"F_Bnr\"\n" +
                        "\"0101001234\";\"2000\";;\"9516\";\"0\";;\"13-01-2000\";\"2903641234\";\"6666\";\"0\";\"5100\";\"955\";\"Paamiut\";\"PAA\";\"0001\";\"0005\";\"1\";\"tv\";\"1234\";\"0101641234\";\"8888\";\"0\";\"5100\";\"955\";\"Paamiut\";\"PAA\";\"0001\";\"0005\";\"1\";\"tv\";\"1234\"",
                contents.trim()
        );

    }

}
