package dk.magenta.datafordeler.combined;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.InputStreamReader;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cpr.data.person.PersonEntityManager;
import dk.magenta.datafordeler.cpr.direct.CprDirectLookup;
import org.apache.commons.collections.IteratorUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.Mockito.when;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class CprVoterFunctionalityTest extends TestBase {

    /*
    Denne funktionalitet er implementeret efter dokument på denne sti: https://docs.google.com/document/d/1bFfPJhYmbzcNyiO740TA27w3rYwWg6_kYoWANrSkQkI/edit
     */

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private PersonEntityManager personEntityManager;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @SpyBean
    private DafoUserManager dafoUserManager;

    public void loadPerson(String personfile) throws Exception {
        InputStream testData = CprVoterFunctionalityTest.class.getResourceAsStream(personfile);
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = sessionManager.getSessionFactory().openSession();
        importMetadata.setSession(session);
        Transaction transaction = session.beginTransaction();
        importMetadata.setTransactionInProgress(true);
        personEntityManager.parseData(testData, importMetadata);
        transaction.commit();
        session.close();
        testData.close();
    }

    public void loadManyPersons(int count) throws Exception {
        this.loadManyPersons(count, 0);
    }

    public void loadManyPersons(int count, int start) throws Exception {
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = sessionManager.getSessionFactory().openSession();
        importMetadata.setSession(session);
        Transaction transaction = session.beginTransaction();
        importMetadata.setTransactionInProgress(true);
        String testData = InputStreamReader.readInputStream(CprVoterFunctionalityTest.class.getResourceAsStream("/person.txt"));
        String[] lines = testData.split("\n");
        for (int i = start; i < count + start; i++) {
            StringJoiner sb = new StringJoiner("\n");
            String newCpr = String.format("%010d", i);
            for (int j = 0; j < lines.length; j++) {
                String line = lines[j];
                line = line.substring(0, 3) + newCpr + line.substring(13);
                sb.add(line);
            }
            ByteArrayInputStream bais = new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
            personEntityManager.parseData(bais, importMetadata);
            bais.close();
        }
        transaction.commit();
        session.close();
    }


    @Before
    public void load() throws Exception {
        this.loadAllGeoAdress(sessionManager);
    }


    @Test
    public void testFetcingOfLocalitiesForVoting() throws Exception {
        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        ResponseEntity<String> response;

        TestUserDetails testUserDetails = new TestUserDetails();
        httpEntity = new HttpEntity<String>("", new HttpHeaders());
        this.applyAccess(testUserDetails);
        response = restTemplate.exchange(
                "/combined/localityList/1/search/",
                HttpMethod.GET,
                httpEntity,
                String.class
        );

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        List<String> expected = Arrays.asList("1706","1700","1704","1701","1601","1606","1600","1602","1605","1603","1608","1610","1607","1604","1609","1200","0600","1403","1203","0906","1504","0703","1502","1201","1101","0803","0902","0701","0601","0605","1503","1100","1500","0900","1003","1507","1000","0500","1400","0801","0501","0908","0905","0702","1204","1505","1506","0800","1202","1004","0700","1501","0204","0321","0305","0300","0104","0201","0108","0200","0103","0302","0102","0202","0106","0100","1805","1803","1804","1802","1800","1806","1900","1902","1901","1906");
        ArrayList<JsonNode> result = (ArrayList<JsonNode>)IteratorUtils.toList(objectMapper.readTree(response.getBody()).get("results").elements());
        List<String> valueResult = result.stream().map(element -> element.asText()).collect(Collectors.toList());
        Assert.assertTrue(expected.size() == valueResult.size() && expected.containsAll(valueResult) && valueResult.containsAll(expected));
    }

    /**
     * Validate that different conditions deliveres different lists of voters
     *
     * Dansk indfødsret
     * Over 18 år på datoen “valgdato”
     * Haft fast registreret adresse indenfor grønlandske kommuner (>950) i mindst 6 måneder siden “valgdato”
     * Ingen umyndighedsmarkering fra CPR
     * Ingen markering af flytning væk fra Danmark, Grønland, færøerne inden for de sidste 3 år (Det er så tæt vi kommer på opfyldelsen af riget, som beskrevet under afsnittet “Undtagelser, forbehold og håndtering af disse”)
     *
     * @throws Exception
     */
    @Test
    public void testFetcingOfVoterLandstingsvalg() throws Exception {
        this.loadPerson("/voters/voter_born_2010_loc_600.txt");
        this.loadPerson("/voters/voter_born_2011_loc_600.txt");
        this.loadPerson("/voters/voter_born_2010_german.txt");
        this.loadPerson("/voters/voter_born_2010_loc_601.txt");
        this.loadPerson("/voters/voter_born_2010_custody.txt");
        this.loadPerson("/voters/voter_born_2010_danish.txt");

        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        ResponseEntity<String> response;

        TestUserDetails testUserDetails = new TestUserDetails();
        httpEntity = new HttpEntity<String>("", new HttpHeaders());

        //Confirm that the list of voters can only be fetched with someone who has access to CPR-data
        this.applyAccess(testUserDetails);
        response = restTemplate.exchange(
                "/combined/cpr/voterlist/1/landstingsvalg/?valgdato=2020-01-01",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        Assert.assertEquals("Forbidden", objectMapper.readTree(response.getBody()).get("error").asText());
        Assert.assertEquals(null, objectMapper.readTree(response.getBody()).get("results"));

        //Confirm that an election in year 2020 returns no voters, since no voters is over 18 years in the testset
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        response = restTemplate.exchange(
                "/combined/cpr/voterlist/1/landstingsvalg/?valgdato=2020-01-01",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals("", objectMapper.readTree(response.getBody()).get("results").asText());

        //Confirm that an election in year 2030 returns the voters "0101101236", "0101101234", "0101111234"
        response = restTemplate.exchange(
                "/combined/cpr/voterlist/1/landstingsvalg/?valgdato=2030-01-01",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        List<String> expected = Arrays.asList("0101101236", "0101101234", "0101111234");
        List<String> result = objectMapper.readTree(response.getBody()).get("results").findValuesAsText("pnr");
        Assert.assertTrue(expected.size() == result.size() && expected.containsAll(result) && result.containsAll(expected));

        //Confirm that an election in year 2028 returns the voters "0101101236", "0101101234"
        response = restTemplate.exchange(
                "/combined/cpr/voterlist/1/landstingsvalg/?valgdato=2028-01-02",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        expected = Arrays.asList("0101101236", "0101101234");
        result = objectMapper.readTree(response.getBody()).get("results").findValuesAsText("pnr");
        Assert.assertTrue(expected.size() == result.size() && expected.containsAll(result) && result.containsAll(expected));

        //Confirm that an election in year 2030 limitated to locality 0600 returns the voters "0101101234", "0101111234"
        response = restTemplate.exchange(
                "/combined/cpr/voterlist/1/landstingsvalg/?valgdato=2030-01-01&lokalitet_kode=0600",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        expected = Arrays.asList("0101101234", "0101111234");
        result = objectMapper.readTree(response.getBody()).get("results").findValuesAsText("pnr");
        Assert.assertTrue(expected.size() == result.size() && expected.containsAll(result) && result.containsAll(expected));

        //Confirm that an election in year 2030 limitated to locality 0601 returns the voters "0101101236"
        response = restTemplate.exchange(
                "/combined/cpr/voterlist/1/landstingsvalg/?valgdato=2030-01-01&lokalitet_kode=0601",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        expected = Arrays.asList("0101101236");
        result = objectMapper.readTree(response.getBody()).get("results").findValuesAsText("pnr");
        Assert.assertTrue(expected.size() == result.size() && expected.containsAll(result) && result.containsAll(expected));

        //Confirm that an election in year 2030 limitated to kommunekode 957 returns the voters ""
        response = restTemplate.exchange(
                "/combined/cpr/voterlist/1/landstingsvalg/?valgdato=2030-01-01&kommune_kode=957",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        expected = Arrays.asList();
        result = objectMapper.readTree(response.getBody()).get("results").findValuesAsText("pnr");
        Assert.assertTrue(expected.size() == result.size() && expected.containsAll(result) && result.containsAll(expected));

        //Confirm that an election in year 2030 limitated to kommunekode 956 returns the voters "0101101236", "0101101234", "0101111234"
        response = restTemplate.exchange(
                "/combined/cpr/voterlist/1/landstingsvalg/?valgdato=2030-01-01&kommune_kode=956",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        expected = Arrays.asList("0101101236", "0101101234", "0101111234");
        result = objectMapper.readTree(response.getBody()).get("results").findValuesAsText("pnr");
        Assert.assertTrue(expected.size() == result.size() && expected.containsAll(result) && result.containsAll(expected));

        //Confirm that an election in year 2030 limitated to born after 2010-05-01 returns the voters "0101111234"
        response = restTemplate.exchange(
                "/combined/cpr/voterlist/1/landstingsvalg/?valgdato=2030-01-01&foedsel.GTE=2010-05-01",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        expected = Arrays.asList("0101111234");
        result = objectMapper.readTree(response.getBody()).get("results").findValuesAsText("pnr");
        Assert.assertTrue(expected.size() == result.size() && expected.containsAll(result) && result.containsAll(expected));

        //Confirm that an election in year 2030 limitated to born after 2010-05-01 returns the voters "0101101236", "0101101234"
        response = restTemplate.exchange(
                "/combined/cpr/voterlist/1/landstingsvalg/?valgdato=2030-01-01&foedsel.LTE=2010-05-01",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        expected = Arrays.asList("0101101236", "0101101234");
        result = objectMapper.readTree(response.getBody()).get("results").findValuesAsText("pnr");
        Assert.assertTrue(expected.size() == result.size() && expected.containsAll(result) && result.containsAll(expected));
    }


    private void applyAccess(TestUserDetails testUserDetails) {
        when(dafoUserManager.getFallbackUser()).thenReturn(testUserDetails);
    }

    @SpyBean
    private CprDirectLookup cprDirectLookup;
}
