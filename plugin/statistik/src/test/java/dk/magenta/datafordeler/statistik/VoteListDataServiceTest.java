package dk.magenta.datafordeler.statistik;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.util.InputStreamReader;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.statistik.services.StatisticsService;
import dk.magenta.datafordeler.statistik.services.VoteListDataService;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


/**
 * Denne test laver tests på nedenstående 4 personer, som indlæses fra voter.txt i forbindelse med afvikling af unittests:
 * 0011111111110 - Er født 2011-11-11 og har aktiv bopæl i grønland
 * 0011211111111 - Er født 2011-11-12 og har aktiv bopæl i grønland
 * 0011311111111 - Er født 2011-11-13 og har aktiv bopæl i grønland
 * 0011311111112 - Er født 2011-11-13 og har aktiv bopæl i danmark
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class VoteListDataServiceTest extends TestBase {

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TestUtils testsUtils;

    @Autowired
    private VoteListDataService voteListDataService;

    private TestUserDetails testUserDetails;

    @Autowired
    private TestUtil testUtil;

    @Before
    public void initialize() throws Exception {
        testsUtils.setPath();
        testsUtils.loadPersonData("voter.txt");
        this.loadAllGeoAdress(sessionManager);
        voteListDataService.setUseTimeintervallimit(false);
    }

    @After
    public void cleanup() {
        testsUtils.clearPath();
        testsUtils.deleteAll();
    }

    @Test
    public void testNoAccess() throws IOException {
        voteListDataService.setWriteToLocalFile(true);

        ResponseEntity<String> response = restTemplate.exchange("/statistik/vote_list_data/?effectDate=2031-03-20&&registrationAt=2031-03-20&filterTime1=2030-11-01", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);
        Assert.assertEquals(403, response.getStatusCodeValue());
    }

    @Test
    public void test1Voters18At2029_11_11() throws IOException {
        voteListDataService.setWriteToLocalFile(true);

        testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        testUserDetails.giveAccess(StatistikRolesDefinition.EXECUTE_STATISTIK_ROLE);
        testsUtils.applyAccess(testUserDetails);


        ResponseEntity<String> response = restTemplate.exchange("/statistik/vote_list_data/?effectDate=2029-11-11&&registrationAt=2029-11-11&filterTime1=2029-11-11", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);
        Assert.assertEquals(200, response.getStatusCodeValue());
        String[] statusFiles = new File(StatisticsService.PATH_FILE).list((dir, name) -> name.startsWith(StatisticsService.ServiceName.VOTE.getIdentifier()));
        Assert.assertEquals(1, statusFiles.length);

        FileInputStream fileInputStream = new FileInputStream(StatisticsService.PATH_FILE + File.separator + statusFiles[0]);
        String contents = InputStreamReader.readInputStream(
                fileInputStream, "UTF-8"
        );
        fileInputStream.close();
        Assert.assertEquals(2, testUtil.csvToJson(contents.trim()).size());
    }

    @Test
    public void test2Voters18At2029_11_12() throws IOException {
        voteListDataService.setWriteToLocalFile(true);

        testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        testUserDetails.giveAccess(StatistikRolesDefinition.EXECUTE_STATISTIK_ROLE);
        testsUtils.applyAccess(testUserDetails);


        ResponseEntity<String> response = restTemplate.exchange("/statistik/vote_list_data/?effectDate=2029-11-12&&registrationAt=2029-11-12&filterTime1=2029-11-12", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);
        Assert.assertEquals(200, response.getStatusCodeValue());
        String[] statusFiles = new File(StatisticsService.PATH_FILE).list((dir, name) -> name.startsWith(StatisticsService.ServiceName.VOTE.getIdentifier()));
        Assert.assertEquals(1, statusFiles.length);

        FileInputStream fileInputStream = new FileInputStream(StatisticsService.PATH_FILE + File.separator + statusFiles[0]);
        String contents = InputStreamReader.readInputStream(
                fileInputStream, "UTF-8"
        );
        fileInputStream.close();
        Assert.assertEquals(3, testUtil.csvToJson(contents.trim()).size());
    }

    @Test
    public void test3Voters18At2029_11_13() throws IOException {
        voteListDataService.setWriteToLocalFile(true);

        testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        testUserDetails.giveAccess(StatistikRolesDefinition.EXECUTE_STATISTIK_ROLE);
        testsUtils.applyAccess(testUserDetails);


        ResponseEntity<String> response = restTemplate.exchange("/statistik/vote_list_data/?effectDate=2029-11-13&&registrationAt=2029-11-13&filterTime1=2029-11-13", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);
        Assert.assertEquals(200, response.getStatusCodeValue());
        String[] statusFiles = new File(StatisticsService.PATH_FILE).list((dir, name) -> name.startsWith(StatisticsService.ServiceName.VOTE.getIdentifier()));
        Assert.assertEquals(1, statusFiles.length);

        FileInputStream fileInputStream = new FileInputStream(StatisticsService.PATH_FILE + File.separator + statusFiles[0]);
        String contents = InputStreamReader.readInputStream(
                fileInputStream, "UTF-8"
        );
        fileInputStream.close();
        Assert.assertEquals(4, testUtil.csvToJson(contents.trim()).size());
    }


    @Test
    public void testValidate2VotersIn956() throws IOException {
        voteListDataService.setWriteToLocalFile(true);

        testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        testUserDetails.giveAccess(StatistikRolesDefinition.EXECUTE_STATISTIK_ROLE);
        testsUtils.applyAccess(testUserDetails);


        ResponseEntity<String> response = restTemplate.exchange("/statistik/vote_list_data/?effectDate=2031-03-20&&registrationAt=2031-03-20&filterTime1=2030-11-01&municipalityFilter=956", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);
        Assert.assertEquals(200, response.getStatusCodeValue());
        String[] statusFiles = new File(StatisticsService.PATH_FILE).list((dir, name) -> name.startsWith(StatisticsService.ServiceName.VOTE.getIdentifier()));
        Assert.assertEquals(1, statusFiles.length);

        FileInputStream fileInputStream = new FileInputStream(StatisticsService.PATH_FILE + File.separator + statusFiles[0]);
        String contents = InputStreamReader.readInputStream(
                fileInputStream, "UTF-8"
        );
        fileInputStream.close();
        Assert.assertEquals(3, testUtil.csvToJson(contents.trim()).size());
    }

    @Test
    public void testValidate1VotersIn957() throws IOException {
        voteListDataService.setWriteToLocalFile(true);

        testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        testUserDetails.giveAccess(StatistikRolesDefinition.EXECUTE_STATISTIK_ROLE);
        testsUtils.applyAccess(testUserDetails);

        ResponseEntity<String> response = restTemplate.exchange("/statistik/vote_list_data/?effectDate=2031-03-20&&registrationAt=2031-03-20&filterTime1=2030-11-01&municipalityFilter=957", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);
        Assert.assertEquals(200, response.getStatusCodeValue());
        String[] statusFiles = new File(StatisticsService.PATH_FILE).list((dir, name) -> name.startsWith(StatisticsService.ServiceName.VOTE.getIdentifier()));
        Assert.assertEquals(1, statusFiles.length);

        FileInputStream fileInputStream = new FileInputStream(StatisticsService.PATH_FILE + File.separator + statusFiles[0]);
        String contents = InputStreamReader.readInputStream(
                fileInputStream, "UTF-8"
        );
        fileInputStream.close();
        Assert.assertEquals(1, testUtil.csvToJson(contents.trim()).size());
    }

    @Test
    public void testAdHoc() throws IOException {
        voteListDataService.setWriteToLocalFile(true);

        testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        testUserDetails.giveAccess(StatistikRolesDefinition.EXECUTE_STATISTIK_ROLE);
        testsUtils.applyAccess(testUserDetails);


        ResponseEntity<String> response = restTemplate.exchange("/statistik/vote_list_data/?effectDate=2031-03-20&&registrationAt=2031-03-20&filterTime1=2031-01-18", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);
        Assert.assertEquals(200, response.getStatusCodeValue());
        String[] statusFiles = new File(StatisticsService.PATH_FILE).list((dir, name) -> name.startsWith(StatisticsService.ServiceName.VOTE.getIdentifier()));
        Assert.assertEquals(1, statusFiles.length);

        FileInputStream fileInputStream = new FileInputStream(StatisticsService.PATH_FILE + File.separator + statusFiles[0]);
        String contents = InputStreamReader.readInputStream(
                fileInputStream, "UTF-8"
        );
        fileInputStream.close();
        Assert.assertEquals(4, testUtil.csvToJson(contents.trim()).size());
    }
}
