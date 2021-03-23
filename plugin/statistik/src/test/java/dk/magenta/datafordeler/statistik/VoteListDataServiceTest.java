package dk.magenta.datafordeler.statistik;

import com.fasterxml.jackson.core.JsonProcessingException;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.util.InputStreamReader;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.statistik.services.StatisticsService;
import dk.magenta.datafordeler.statistik.services.StatusDataService;
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
    public void testFileOutput() throws IOException {
        voteListDataService.setWriteToLocalFile(true);

        ResponseEntity<String> response = restTemplate.exchange("/statistik/vote_list_data/?effectDate=2031-03-20&&registrationAt=2031-03-20&filterTime1=2030-11-01&filterTime2=2031-03-20", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);
        Assert.assertEquals(403, response.getStatusCodeValue());

        testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        testUserDetails.giveAccess(StatistikRolesDefinition.EXECUTE_STATISTIK_ROLE);
        testsUtils.applyAccess(testUserDetails);

        response = restTemplate.exchange("/statistik/vote_list_data/?effectDate=2031-03-20&&registrationAt=2031-03-20&filterTime1=2030-11-01&filterTime2=2031-03-20", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);

        Assert.assertEquals(200, response.getStatusCodeValue());

        String[] statusFiles = new File(StatisticsService.PATH_FILE).list((dir, name) -> name.startsWith(StatisticsService.ServiceName.VOTE.getIdentifier()));
        Assert.assertEquals(1, statusFiles.length);

        FileInputStream fileInputStream = new FileInputStream(StatisticsService.PATH_FILE + File.separator + statusFiles[0]);
        String contents = InputStreamReader.readInputStream(
                fileInputStream,"UTF-8"
        );
        fileInputStream.close();



    }

}
