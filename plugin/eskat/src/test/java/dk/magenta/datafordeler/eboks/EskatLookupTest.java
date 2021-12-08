package dk.magenta.datafordeler.eboks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.Entity;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.user.DafoUserManager;

import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cvr.CollectiveCvrLookup;
import dk.magenta.datafordeler.cvr.CvrPlugin;
import dk.magenta.datafordeler.cvr.access.CvrRolesDefinition;
import dk.magenta.datafordeler.cvr.entitymanager.CompanyEntityManager;
import dk.magenta.datafordeler.cvr.entitymanager.ParticipantEntityManager;
import dk.magenta.datafordeler.cvr.query.ParticipantRecordQuery;
import dk.magenta.datafordeler.cvr.records.CompanyRecord;
import dk.magenta.datafordeler.cvr.records.CompanyUnitRecord;
import dk.magenta.datafordeler.cvr.records.ParticipantRecord;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EskatLookupTest {

    @Autowired
    TestRestTemplate restTemplate;
    HashSet<Entity> createdEntities = new HashSet<>();
    @Autowired
    private SessionManager sessionManager;
    @Autowired
    private CompanyEntityManager companyEntityManager;
    @Autowired
    private ObjectMapper objectMapper;
    @SpyBean
    private DafoUserManager dafoUserManager;

    @Autowired
    private CvrPlugin plugin;


    private static HashMap<String, String> schemaMap = new HashMap<>();
    static {
        schemaMap.put("_doc", CompanyRecord.schema);
        schemaMap.put("produktionsenhed", CompanyUnitRecord.schema);
        schemaMap.put("deltager", ParticipantRecord.schema);
    }


    private void loadCompany() throws IOException, DataFordelerException {
        InputStream testData = EskatLookupTest.class.getResourceAsStream("/company_in.json");
        JsonNode root = objectMapper.readTree(testData);
        testData.close();
        JsonNode itemList = root.get("hits").get("hits");
        Assert.assertTrue(itemList.isArray());
        ImportMetadata importMetadata = new ImportMetadata();
        for (JsonNode item : itemList) {
            String source = objectMapper.writeValueAsString(item.get("_source").get("Vrvirksomhed"));
            ByteArrayInputStream bais = new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8));
            companyEntityManager.parseData(bais, importMetadata);
            bais.close();
        }
    }


    private HashMap<Long, JsonNode> loadParticipant(String resource) throws IOException, DataFordelerException {
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        InputStream input = ParseTest.class.getResourceAsStream(resource);
        if (input == null) {
            throw new MissingResourceException("Missing resource \""+resource+"\"", resource, "key");
        }
        boolean linedFile = false;
        HashMap<Long, JsonNode> persons = new HashMap<>();
        try {
            importMetadata.setSession(session);

            if (linedFile) {
                int lineNumber = 0;
                Scanner lineScanner = new Scanner(input, "UTF-8").useDelimiter("\n");
                while (lineScanner.hasNext()) {
                    String data = lineScanner.next();

                    JsonNode root = objectMapper.readTree(data);
                    JsonNode itemList = root.get("hits").get("hits");
                    Assert.assertTrue(itemList.isArray());
                    for (JsonNode item : itemList) {
                        String type = item.get("_type").asText();
                        ParticipantEntityManager entityManager = (ParticipantEntityManager) plugin.getRegisterManager().getEntityManager(schemaMap.get(type));
                        JsonNode participantInputNode = item.get("_source").get("Vrdeltagerperson");
                        entityManager.parseData(participantInputNode, importMetadata, session);
                        persons.put(participantInputNode.get("enhedsNummer").asLong(), participantInputNode);
                    }
                    lineNumber++;
                    System.out.println("loaded line " + lineNumber);
                    if (lineNumber >= 10) {
                        break;
                    }
                }
            } else {
                JsonNode root = objectMapper.readTree(input);
                JsonNode itemList = root.get("hits").get("hits");
                Assert.assertTrue(itemList.isArray());
                for (JsonNode item : itemList) {
                    String type = item.get("_type").asText();
                    ParticipantEntityManager entityManager = (ParticipantEntityManager) plugin.getRegisterManager().getEntityManager(schemaMap.get(type));
                    JsonNode unitInputNode = item.get("_source").get("Vrdeltagerperson");
                    entityManager.parseData(unitInputNode, importMetadata, session);
                    persons.put(unitInputNode.get("enhedsNummer").asLong(), unitInputNode);
                }
            }
            transaction.commit();
        } finally {
            session.close();
            QueryManager.clearCaches();
            input.close();
        }
        return persons;
    }


    @Test
    public void testCallForStatusList() throws Exception {
        this.loadCompany();
        TestUserDetails testUserDetails = new TestUserDetails();

        ObjectNode body = objectMapper.createObjectNode();
        HttpEntity<String>  httpEntity = new HttpEntity<String>(body.toString(), new HttpHeaders());

        httpEntity = new HttpEntity<String>(body.toString(), new HttpHeaders());

        testUserDetails.giveAccess(CvrRolesDefinition.READ_CVR_ROLE);
        this.applyAccess(testUserDetails);

        ResponseEntity<String> response = restTemplate.exchange(
                "/eskat/companystatus/lookup",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        //JSONAssert.assertEquals("[\"Aktiv: NORMAL\"]", response.getBody(), false);
    }

    @Test
    public void testCallForCompanyLookup() throws Exception {
        this.loadCompany();
        TestUserDetails testUserDetails = new TestUserDetails();

        ObjectNode body = objectMapper.createObjectNode();
        HttpEntity<String>  httpEntity = new HttpEntity<String>(body.toString(), new HttpHeaders());

        httpEntity = new HttpEntity<String>(body.toString(), new HttpHeaders());

        testUserDetails.giveAccess(CvrRolesDefinition.READ_CVR_ROLE);
        this.applyAccess(testUserDetails);

        ResponseEntity<String> response = restTemplate.exchange(
                "/eskat/company/1/rest/search/?cvrnummer=25052943",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        System.out.println(response.getBody());

        response = restTemplate.exchange(
                "/eskat/company/1/rest/search/?navne=MAGENTA*",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        System.out.println(response.getBody());

    }

    @Test
    public void testCallForCompanyDetailLookup() throws Exception {
        this.loadCompany();
        TestUserDetails testUserDetails = new TestUserDetails();

        ObjectNode body = objectMapper.createObjectNode();
        HttpEntity<String>  httpEntity = new HttpEntity<String>(body.toString(), new HttpHeaders());

        httpEntity = new HttpEntity<String>(body.toString(), new HttpHeaders());

        testUserDetails.giveAccess(CvrRolesDefinition.READ_CVR_ROLE);
        this.applyAccess(testUserDetails);

        ResponseEntity<String> response = restTemplate.exchange(
                "/eskat/companydetail/1/rest/search/?cvrnummer=25052943",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        System.out.println(response.getBody());

        response = restTemplate.exchange(
                "/eskat/companydetail/1/rest/search/?navne=MAGENTA*",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        System.out.println(response.getBody());

    }

    @Test
    public void testCompanyParticipantLookup() throws Exception {
        this.loadCompany();
        loadParticipant("/person.json");
        TestUserDetails testUserDetails = new TestUserDetails();

        ObjectNode body = objectMapper.createObjectNode();
        HttpEntity<String>  httpEntity = new HttpEntity<String>(body.toString(), new HttpHeaders());

        testUserDetails.giveAccess(CvrRolesDefinition.READ_CVR_ROLE);
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);

        ResponseEntity<String> response = restTemplate.exchange(
                "/eskat/companyParticipantConnection/?cpr=1234567890",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals("1234567890", response.getBody());

        response = restTemplate.exchange(
                "/eskat/companyParticipantConnection/?navn=TESTNAVN",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals("1234567890", response.getBody());

        response = restTemplate.exchange(
                "/eskat/companyParticipantConnection/?enhedsNummer=4000004988",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals("1234567890", response.getBody());




        System.out.println(response.getBody());



    }

/*    @Test
    public void testCompanyParticipant2Lookup() throws Exception {
        this.loadCompany();
        loadParticipant("/person.json");
        TestUserDetails testUserDetails = new TestUserDetails();

        ObjectNode body = objectMapper.createObjectNode();
        HttpEntity<String>  httpEntity = new HttpEntity<String>(body.toString(), new HttpHeaders());

        testUserDetails.giveAccess(CvrRolesDefinition.READ_CVR_ROLE);
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);

        ResponseEntity<String> response = restTemplate.exchange(
                "/eskat/eskatparticipant/1/rest/?businessKey=1234567890",
                HttpMethod.GET,
                httpEntity,
                String.class
        );

        System.out.println(response.getBody());



    }*/





    private void applyAccess(TestUserDetails testUserDetails) {
        when(dafoUserManager.getFallbackUser()).thenReturn(testUserDetails);
    }

    private void cleanup() {
        Session session = sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        try {
            for (Entity entity : createdEntities) {
                try {
                    session.delete(entity);
                } catch (Exception e) {
                }
            }
            createdEntities.clear();
        } finally {
            try {
                transaction.commit();
            } catch (Exception e) {
            } finally {
                session.close();
            }
        }
    }
}
