package dk.magenta.datafordeler.combined;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.InputStreamReader;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonEntityManager;
import dk.magenta.datafordeler.cpr.data.person.PersonRecordQuery;
import dk.magenta.datafordeler.cpr.direct.CprDirectLookup;
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
import java.time.OffsetDateTime;
import java.util.List;
import java.util.StringJoiner;

import static org.mockito.Mockito.when;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class CprVoterFunctionalityTest extends TestBase {

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
        //this.loadPerson("/voter.txt");

        //this.loadPerson("/person.txt");
        //loadManyPersons(50);
    }


    @Test
    public void testFetcingOfLocalitiesForVoting() throws Exception {
        this.loadPerson("/different_persons.txt");

        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        ResponseEntity<String> response;

        TestUserDetails testUserDetails = new TestUserDetails();
        httpEntity = new HttpEntity<String>("", new HttpHeaders());
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);
        response = restTemplate.exchange(
                "/combined/localityList/1/search/",
                HttpMethod.GET,
                httpEntity,
                String.class
        );

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        String results = "[\"1706\",\"1700\",\"1704\",\"1701\",\"1601\",\"1606\",\"1600\",\"1602\",\"1605\",\"1603\",\"1608\",\"1610\",\"1607\",\"1604\",\"1609\",\"1200\",\"0600\",\"1403\",\"1203\",\"0906\",\"1504\",\"0703\",\"1502\",\"1201\",\"1101\",\"0803\",\"0902\",\"0701\",\"0601\",\"0605\",\"1503\",\"1100\",\"1500\",\"0900\",\"1003\",\"1507\",\"1000\",\"0500\",\"1400\",\"0801\",\"0501\",\"0908\",\"0905\",\"0702\",\"1204\",\"1505\",\"1506\",\"0800\",\"1202\",\"1004\",\"0700\",\"1501\",\"0204\",\"0321\",\"0305\",\"0300\",\"0104\",\"0201\",\"0108\",\"0200\",\"0103\",\"0302\",\"0102\",\"0202\",\"0106\",\"0100\",\"1805\",\"1803\",\"1804\",\"1802\",\"1800\",\"1806\",\"1900\",\"1902\",\"1901\",\"1906\"]";
        Assert.assertEquals(results, objectMapper.readTree(response.getBody()).get("results").toString());
    }


    @Test
    public void testFetcingOfVoter() throws Exception {
        //this.loadPerson("/different_persons.txt");

        this.loadPerson("/voters/voter_born_2010_loc_600.txt");
        this.loadPerson("/voters/voter_born_2011_loc_600.txt");
        this.loadPerson("/voters/voter_born_2010_german.txt");
        this.loadPerson("/voters/voter_born_2010_loc_601.txt");

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            ImportMetadata importMetadata = new ImportMetadata();
            importMetadata.setSession(session);
            PersonRecordQuery query = new PersonRecordQuery();
            OffsetDateTime time = OffsetDateTime.now();
            query.setRegistrationToAfter(time);

            List<PersonEntity> entities = QueryManager.getAllEntities(session, query, PersonEntity.class);
            System.out.println(entities);
        }





        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        ResponseEntity<String> response;

        TestUserDetails testUserDetails = new TestUserDetails();
        httpEntity = new HttpEntity<String>("", new HttpHeaders());
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);
        response = restTemplate.exchange(
                "/combined/cpr/voterlist/1/landstingsvalg/?valgdato=2030-01-01&order_by=pnr&foedsel.LTE=2020-01-01",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        System.out.println(response.getBody());

        response = restTemplate.exchange(
                "/combined/cpr/voterlist/1/landstingsvalg/?valgdato=2030-01-01&order_by=efternavn&foedsel.LTE=2020-01-01",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        System.out.println(response.getBody());


        /*response = restTemplate.exchange(
                "/combined/cpr/voterlist/1/landstingsvalg/?valgdato=2030-01-01",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        System.out.println(response.getBody());*/

    }


    private void applyAccess(TestUserDetails testUserDetails) {
        when(dafoUserManager.getFallbackUser()).thenReturn(testUserDetails);
    }

    @SpyBean
    private CprDirectLookup cprDirectLookup;
}
