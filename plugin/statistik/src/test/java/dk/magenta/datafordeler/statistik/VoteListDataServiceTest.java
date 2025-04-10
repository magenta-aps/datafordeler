package dk.magenta.datafordeler.statistik;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.util.InputStreamReader;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.statistik.services.StatisticsService;
import dk.magenta.datafordeler.statistik.services.VoteListDataService;
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


/**
 * Denne test laver tests på nedenstående 4 personer, som indlæses fra voter.txt i forbindelse med afvikling af unittests:
 * 0011111111110 - Er født 2011-11-11 og har aktiv bopæl i grønland
 * 0011211111111 - Er født 2011-11-12 og har aktiv bopæl i grønland
 * 0011311111111 - Er født 2011-11-13 og har aktiv bopæl i grønland
 * 0011311111112 - Er født 2011-11-13 og har aktiv bopæl i danmark
 */
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class VoteListDataServiceTest extends TestBase {

    @MockitoSpyBean
    private VoteListDataService voteListDataService;

    @BeforeEach
    public void initialize() throws Exception {
        this.setPath();
        this.loadPersonData("voter.txt");
        this.loadAllGeoAdress();
    }

    @AfterEach
    public void cleanup() {
        this.deleteAll();
    }

    @Test
    public void testNoAccess() throws IOException {
        when(voteListDataService.getTimeintervallimit()).thenReturn(false);
        when(voteListDataService.getWriteToLocalFile()).thenReturn(true);

        ResponseEntity<String> response = restTemplate.exchange("/statistik/vote_list_data/?effectDate=2031-03-20&&registrationAt=2031-03-20&filterTime1=2030-11-01", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);
        Assertions.assertEquals(403, response.getStatusCodeValue());
    }

    @Test
    public void test1Voters18At2029_11_11() throws IOException {
        when(voteListDataService.getTimeintervallimit()).thenReturn(false);
        when(voteListDataService.getWriteToLocalFile()).thenReturn(true);

        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        testUserDetails.giveAccess(StatistikRolesDefinition.EXECUTE_STATISTIK_ROLE);
        this.applyAccess(testUserDetails);


        ResponseEntity<String> response = restTemplate.exchange("/statistik/vote_list_data/?effectDate=2029-11-11&&registrationAt=2029-11-11&filterTime1=2029-11-11", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);
        Assertions.assertEquals(200, response.getStatusCodeValue());
        String[] statusFiles = new File(StatisticsService.PATH_FILE).list((dir, name) -> name.startsWith(StatisticsService.ServiceName.VOTE.getIdentifier()));
        Assertions.assertEquals(1, statusFiles.length);

        FileInputStream fileInputStream = new FileInputStream(StatisticsService.PATH_FILE + File.separator + statusFiles[0]);
        String contents = InputStreamReader.readInputStream(
                fileInputStream, "UTF-8"
        );
        fileInputStream.close();
        Assertions.assertEquals(2, this.csvToJson(contents.trim()).size());
    }

    @Test
    public void test2Voters18At2029_11_12() throws IOException {
        when(voteListDataService.getTimeintervallimit()).thenReturn(false);
        when(voteListDataService.getWriteToLocalFile()).thenReturn(true);

        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        testUserDetails.giveAccess(StatistikRolesDefinition.EXECUTE_STATISTIK_ROLE);
        this.applyAccess(testUserDetails);


        ResponseEntity<String> response = restTemplate.exchange("/statistik/vote_list_data/?effectDate=2029-11-12&&registrationAt=2029-11-12&filterTime1=2029-11-12", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);
        Assertions.assertEquals(200, response.getStatusCodeValue());
        String[] statusFiles = new File(StatisticsService.PATH_FILE).list((dir, name) -> name.startsWith(StatisticsService.ServiceName.VOTE.getIdentifier()));
        Assertions.assertEquals(1, statusFiles.length);

        FileInputStream fileInputStream = new FileInputStream(StatisticsService.PATH_FILE + File.separator + statusFiles[0]);
        String contents = InputStreamReader.readInputStream(
                fileInputStream, "UTF-8"
        );
        fileInputStream.close();
        Assertions.assertEquals(3, this.csvToJson(contents.trim()).size());
    }

    @Test
    public void test3Voters18At2029_11_13() throws IOException {
        when(voteListDataService.getTimeintervallimit()).thenReturn(false);
        when(voteListDataService.getWriteToLocalFile()).thenReturn(true);

        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        testUserDetails.giveAccess(StatistikRolesDefinition.EXECUTE_STATISTIK_ROLE);
        this.applyAccess(testUserDetails);


        ResponseEntity<String> response = restTemplate.exchange("/statistik/vote_list_data/?effectDate=2029-11-13&&registrationAt=2029-11-13&filterTime1=2029-11-13", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);
        Assertions.assertEquals(200, response.getStatusCodeValue());
        String[] statusFiles = new File(StatisticsService.PATH_FILE).list((dir, name) -> name.startsWith(StatisticsService.ServiceName.VOTE.getIdentifier()));
        Assertions.assertEquals(1, statusFiles.length);

        FileInputStream fileInputStream = new FileInputStream(StatisticsService.PATH_FILE + File.separator + statusFiles[0]);
        String contents = InputStreamReader.readInputStream(
                fileInputStream, "UTF-8"
        );
        fileInputStream.close();
        Assertions.assertEquals(4, this.csvToJson(contents.trim()).size());
    }


    @Test
    public void testValidate2VotersIn956() throws IOException {
        when(voteListDataService.getTimeintervallimit()).thenReturn(false);
        when(voteListDataService.getWriteToLocalFile()).thenReturn(true);

        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        testUserDetails.giveAccess(StatistikRolesDefinition.EXECUTE_STATISTIK_ROLE);
        this.applyAccess(testUserDetails);


        ResponseEntity<String> response = restTemplate.exchange("/statistik/vote_list_data/?effectDate=2031-03-20&&registrationAt=2031-03-20&filterTime1=2030-11-01&municipalityFilter=956", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);
        Assertions.assertEquals(200, response.getStatusCodeValue());
        String[] statusFiles = new File(StatisticsService.PATH_FILE).list((dir, name) -> name.startsWith(StatisticsService.ServiceName.VOTE.getIdentifier()));
        Assertions.assertEquals(1, statusFiles.length);

        FileInputStream fileInputStream = new FileInputStream(StatisticsService.PATH_FILE + File.separator + statusFiles[0]);
        String contents = InputStreamReader.readInputStream(
                fileInputStream, "UTF-8"
        );
        fileInputStream.close();
        Assertions.assertEquals(3, this.csvToJson(contents.trim()).size());
    }

    @Test
    public void testValidate1VotersIn957() throws IOException {
        when(voteListDataService.getTimeintervallimit()).thenReturn(false);
        when(voteListDataService.getWriteToLocalFile()).thenReturn(true);

        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        testUserDetails.giveAccess(StatistikRolesDefinition.EXECUTE_STATISTIK_ROLE);
        this.applyAccess(testUserDetails);

        ResponseEntity<String> response = restTemplate.exchange("/statistik/vote_list_data/?effectDate=2031-03-20&&registrationAt=2031-03-20&filterTime1=2030-11-01&municipalityFilter=957", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);
        Assertions.assertEquals(200, response.getStatusCodeValue());
        String[] statusFiles = new File(StatisticsService.PATH_FILE).list((dir, name) -> name.startsWith(StatisticsService.ServiceName.VOTE.getIdentifier()));
        Assertions.assertEquals(1, statusFiles.length);

        FileInputStream fileInputStream = new FileInputStream(StatisticsService.PATH_FILE + File.separator + statusFiles[0]);
        String contents = InputStreamReader.readInputStream(
                fileInputStream, "UTF-8"
        );
        fileInputStream.close();
        Assertions.assertEquals(1, this.csvToJson(contents.trim()).size());
    }

    @Test
    public void testAdHoc() throws IOException {
        when(voteListDataService.getTimeintervallimit()).thenReturn(false);
        when(voteListDataService.getWriteToLocalFile()).thenReturn(true);

        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        testUserDetails.giveAccess(StatistikRolesDefinition.EXECUTE_STATISTIK_ROLE);
        this.applyAccess(testUserDetails);


        ResponseEntity<String> response = restTemplate.exchange("/statistik/vote_list_data/?effectDate=2031-03-20&&registrationAt=2031-03-20&filterTime1=2031-01-18", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);
        Assertions.assertEquals(200, response.getStatusCodeValue());
        String[] statusFiles = new File(StatisticsService.PATH_FILE).list((dir, name) -> name.startsWith(StatisticsService.ServiceName.VOTE.getIdentifier()));
        Assertions.assertEquals(1, statusFiles.length);

        FileInputStream fileInputStream = new FileInputStream(StatisticsService.PATH_FILE + File.separator + statusFiles[0]);
        String contents = InputStreamReader.readInputStream(
                fileInputStream, "UTF-8"
        );
        fileInputStream.close();
        Assertions.assertEquals(4, this.csvToJson(contents.trim()).size());
    }
}
