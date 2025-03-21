package dk.magenta.datafordeler.eskat;

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
import dk.magenta.datafordeler.cvr.CvrPlugin;
import dk.magenta.datafordeler.cvr.access.CvrRolesDefinition;
import dk.magenta.datafordeler.cvr.entitymanager.CompanyEntityManager;
import dk.magenta.datafordeler.cvr.entitymanager.CompanyUnitEntityManager;
import dk.magenta.datafordeler.cvr.entitymanager.ParticipantEntityManager;
import dk.magenta.datafordeler.cvr.records.CompanyRecord;
import dk.magenta.datafordeler.cvr.records.CompanyUnitRecord;
import dk.magenta.datafordeler.cvr.records.ParticipantRecord;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.Scanner;

import static org.mockito.Mockito.when;

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
    @MockitoSpyBean
    private DafoUserManager dafoUserManager;

    @Autowired
    private CvrPlugin plugin;


    private static final HashMap<String, String> schemaMap = new HashMap<>();

    static {
        schemaMap.put("_doc", CompanyRecord.schema);
        schemaMap.put("produktionsenhed", CompanyUnitRecord.schema);
        schemaMap.put("deltager", ParticipantRecord.schema);
    }


    private void loadAllCompany() throws IOException, DataFordelerException {
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = sessionManager.getSessionFactory().openSession();
        //Transaction transaction = session.beginTransaction();
        importMetadata.setSession(session);
        this.loadCompanyFile("/company_in2.json", importMetadata);
        this.loadCompanyFile("/company_in.json", importMetadata);
        this.loadUnit();
        this.loadParticipant("/person.json");
        Transaction tx = session.getTransaction();
        if (tx.isActive()) {
            tx.commit();
        }
    }

    private void loadCompanyFile(String resource, ImportMetadata importMetadata) throws IOException, DataFordelerException {
        InputStream testData = EskatLookupTest.class.getResourceAsStream(resource);
        JsonNode root = objectMapper.readTree(testData);
        testData.close();
        JsonNode itemList = root.get("hits").get("hits");
        Assertions.assertTrue(itemList.isArray());

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
            throw new MissingResourceException("Missing resource \"" + resource + "\"", resource, "key");
        }
        boolean linedFile = false;
        HashMap<Long, JsonNode> persons = new HashMap<>();
        try {
            importMetadata.setSession(session);

            if (linedFile) {
                int lineNumber = 0;
                Scanner lineScanner = new Scanner(input, StandardCharsets.UTF_8).useDelimiter("\n");
                while (lineScanner.hasNext()) {
                    String data = lineScanner.next();

                    JsonNode root = objectMapper.readTree(data);
                    JsonNode itemList = root.get("hits").get("hits");
                    Assertions.assertTrue(itemList.isArray());
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
                Assertions.assertTrue(itemList.isArray());
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
    public void loadUnit() throws IOException, DataFordelerException {
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        try {
            importMetadata.setSession(session);
            InputStream input = ParseTest.class.getResourceAsStream("/unit.json");
            JsonNode root = objectMapper.readTree(input);
            JsonNode itemList = root.get("hits").get("hits");
            Assertions.assertTrue(itemList.isArray());
            for (JsonNode item : itemList) {
                String type = item.get("_type").asText();
                CompanyUnitEntityManager entityManager = (CompanyUnitEntityManager) plugin.getRegisterManager().getEntityManager(schemaMap.get(type));
                entityManager.parseData(item.get("_source").get("VrproduktionsEnhed"), importMetadata, session);
            }
        } finally {
            transaction.commit();
            session.close();
            QueryManager.clearCaches();
        }
    }


    /**
     * Test of feature for "aktuelle virksomhedsstatusser."
     * This service supports functionality as requested by skattestyrelsen to support functionality in E-skat
     *
     * @throws Exception
     */
    @Test
    public void testCallForStatusList() throws Exception {
        this.loadAllCompany();
        TestUserDetails testUserDetails = new TestUserDetails();

        ObjectNode body = objectMapper.createObjectNode();
        HttpEntity<String> httpEntity = new HttpEntity<String>(body.toString(), new HttpHeaders());

        httpEntity = new HttpEntity<String>(body.toString(), new HttpHeaders());

        testUserDetails.giveAccess(CvrRolesDefinition.READ_CVR_ROLE);
        this.applyAccess(testUserDetails);

        ResponseEntity<String> response = restTemplate.exchange(
                "/eskat/1/companystatus/list",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("[\"Ophørt: OPLØST EFTER FRIVILLIG LIKVIDATION\",\"Aktiv: NORMAL\",\"Ophørt: UNDER FRIVILLIG LIKVIDATION\"]", response.getBody(), false);
    }

    /**
     * Test of feature for searching for companies based on different attributes.
     *
     * @throws Exception
     */
    @Test
    public void testCallForCompanyLookup() throws Exception {
        this.loadAllCompany();
        TestUserDetails testUserDetails = new TestUserDetails();
        ObjectNode body = objectMapper.createObjectNode();
        HttpEntity<String> httpEntity;
        httpEntity = new HttpEntity<String>(body.toString(), new HttpHeaders());

        ResponseEntity<String> response;

        response = restTemplate.exchange(
                "/eskat/company/1/rest/search/?cvrnummer=25052943",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        Assertions.assertEquals(false, response.getBody().contains("25052943"));

        testUserDetails.giveAccess(CvrRolesDefinition.READ_CVR_ROLE);
        this.applyAccess(testUserDetails);

        response = restTemplate.exchange(
                "/eskat/company/1/rest/search/?cvrnummer=25052943",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(true, response.getBody().contains("25052943"));
        Assertions.assertEquals(false, response.getBody().contains("25052944"));
        response = restTemplate.exchange(
                "/eskat/company/1/rest/search/?cvrnummer=25052944*",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(false, response.getBody().contains("25052943"));
        Assertions.assertEquals(false, response.getBody().contains("25052944"));

        response = restTemplate.exchange(
                "/eskat/company/1/rest/search/?navne=MAGENT*",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(true, response.getBody().contains("25052943"));

        response = restTemplate.exchange(
                "/eskat/company/1/rest/search/?navne=magent*",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(false, response.getBody().contains("25052943"));

        response = restTemplate.exchange(
                "/eskat/company/1/rest/search/?navne=MAGENH*",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(false, response.getBody().contains("25052943"));

        response = restTemplate.exchange(
                "/eskat/company/1/rest/search/?companyStatus=NORMAL",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(true, response.getBody().contains("25052943"));
        response = restTemplate.exchange(
                "/eskat/company/1/rest/search/?companyStatus=UNORMAL",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(false, response.getBody().contains("25052943"));


        response = restTemplate.exchange(
                "/eskat/company/1/rest/search/?companyStartDate.GTE=2020-01-01",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(false, response.getBody().contains("25052943"));

        response = restTemplate.exchange(
                "/eskat/company/1/rest/search/?companyStartDate.GTE=1985-01-01",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(true, response.getBody().contains("25052943"));

        response = restTemplate.exchange(
                "/eskat/company/1/rest/search/?companyStartDate.LTE=1985-01-01",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(false, response.getBody().contains("25052943"));

    }

    @Test
    public void testCallForCompanyTimestampStatusLookup() throws Exception {
        this.loadAllCompany();
        TestUserDetails testUserDetails = new TestUserDetails();
        ObjectNode body = objectMapper.createObjectNode();
        HttpEntity<String> httpEntity;
        httpEntity = new HttpEntity<String>(body.toString(), new HttpHeaders());

        ResponseEntity<String> response;

        response = restTemplate.exchange(
                "/eskat/company/1/rest/search/?cvrnummer=25052943",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        Assertions.assertEquals(false, response.getBody().contains("25052943"));

        testUserDetails.giveAccess(CvrRolesDefinition.READ_CVR_ROLE);
        this.applyAccess(testUserDetails);

        response = restTemplate.exchange(
                "/eskat/company/1/rest/search/?companyStatus=NORMAL&companyStatusValidity.GTE=1999-01-01&companyStatusValidity.LTE=2000-01-01",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(true, response.getBody().contains("25052943"));
        response = restTemplate.exchange(
                "/eskat/company/1/rest/search/?companyStatus=NORMAL&companyStatusValidity.GTE=2000-01-01&companyStatusValidity.LTE=2001-01-01",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(false, response.getBody().contains("25052943"));
        response = restTemplate.exchange(
                "/eskat/company/1/rest/search/?companyStatus=UNORMAL&companyStatusValidity.GTE=1999-01-01&companyStatusValidity.LTE=2000-01-01",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(false, response.getBody().contains("25052943"));

        response = restTemplate.exchange(
                "/eskat/company/1/rest/search/?companyStatus=UNORMAL&companyStatusValidity.GTE=2000-01-01&companyStatusValidity.LTE=2001-01-01",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(false, response.getBody().contains("25052943"));
    }

    @Test
    public void testCallForCompanyDetailLookup() throws Exception {
        this.loadAllCompany();
        TestUserDetails testUserDetails = new TestUserDetails();

        ObjectNode body = objectMapper.createObjectNode();
        HttpEntity<String> httpEntity;
        ResponseEntity<String> response;

        httpEntity = new HttpEntity<String>(body.toString(), new HttpHeaders());

        response = restTemplate.exchange(
                "/eskat/companydetail/1/rest/25052943",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        Assertions.assertEquals(false, response.getBody().contains("25052943"));

        testUserDetails.giveAccess(CvrRolesDefinition.READ_CVR_ROLE);
        this.applyAccess(testUserDetails);

        response = restTemplate.exchange(
                "/eskat/companydetail/1/rest/25052943",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("{\n" +
                "  \"cvrNummer\" : 25052943,\n" +
                "  \"navn\" : \"MAGENTA\",\n" +
                "  \"coName\" : \"c/o Dummy CO\",\n" +
                "  \"postnummer\" : 1112,\n" +
                "  \"postboks\" : \"1234\",\n" +
                "  \"postdistrikt\" : \"København K\",\n" +
                "  \"kommuneKode\" : 955,\n" +
                "  \"telefonNummer\" : \"33369696\",\n" +
                "  \"elektroniskPost\" : \"info@magenta.dk\",\n" +
                "  \"nyesteVirksomhedsform\" : \"80/Anpartsselskab\",\n" +
                "  \"branchetekst\" : \"Konsulentbistand vedrørende informationsteknologi\",\n" +
                "  \"branchekode\" : \"620200\"\n" +
                "}", response.getBody(), false);

        response = restTemplate.exchange(
                "/eskat/companydetail/1/rest/25052944",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assertions.assertEquals(false, response.getBody().contains("25052944"));
    }

    @Test
    public void testCompanyParticipantLookup() throws Exception {
        this.loadAllCompany();
        TestUserDetails testUserDetails = new TestUserDetails();

        ObjectNode body = objectMapper.createObjectNode();
        HttpEntity<String> httpEntity = new HttpEntity<String>(body.toString(), new HttpHeaders());
        ResponseEntity<String> response;

        response = restTemplate.exchange(
                "/eskat/companyParticipantConnection/1/rest/search?cpr=1234567890",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        Assertions.assertEquals(false, response.getBody().contains("25052943"));


        testUserDetails.giveAccess(CvrRolesDefinition.READ_CVR_ROLE);
        this.applyAccess(testUserDetails);

        response = restTemplate.exchange(
                "/eskat/companyParticipantConnection/1/rest/search?cpr=1234567890",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        Assertions.assertEquals(false, response.getBody().contains("25052943"));

        testUserDetails.giveAccess(CvrRolesDefinition.READ_CVR_ROLE);
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);

        response = restTemplate.exchange(
                "/eskat/companyParticipantConnection/1/rest/search?cpr=1234567890",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(true, response.getBody().contains("25052943"));

        response = restTemplate.exchange(
                "/eskat/companyParticipantConnection/1/rest/search?personNavn=TESTNAVN",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(true, response.getBody().contains("25052943"));

        response = restTemplate.exchange(
                "/eskat/companyParticipantConnection/1/rest/search?personNavn=TESTNA*",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(true, response.getBody().contains("25052943"));

        response = restTemplate.exchange(
                "/eskat/companyParticipantConnection/1/rest/search?personNavn=TESTNU*",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(false, response.getBody().contains("25052943"));

        response = restTemplate.exchange(
                "/eskat/companyParticipantConnection/1/rest/search?enhedsNummer=4000004988",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        response = restTemplate.exchange(
                "/eskat/companyParticipantConnection/1/rest/search?cvr=25052943",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(true, response.getBody().contains("25052943"));

        response = restTemplate.exchange(
                "/eskat/companyParticipantConnection/1/rest/search?cvr=25052*",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(true, response.getBody().contains("25052943"));

        response = restTemplate.exchange(
                "/eskat/companyParticipantConnection/1/rest/search?cvr=25053*",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(false, response.getBody().contains("25052943"));

        response = restTemplate.exchange(
                "/eskat/companyParticipantConnection/1/rest/search?firmaNavn=MAGENTA",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(true, response.getBody().contains("25052943"));

        response = restTemplate.exchange(
                "/eskat/companyParticipantConnection/1/rest/search?firmaNavn=*GENTA",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(true, response.getBody().contains("25052943"));

        response = restTemplate.exchange(
                "/eskat/companyParticipantConnection/1/rest/search?firmaNavn=*PENTA",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(false, response.getBody().contains("25052943"));

        response = restTemplate.exchange(
                "/eskat/companyParticipantConnection/1/rest/search?aktivitet=Aktiv",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(true, response.getBody().contains("25052943"));

        response = restTemplate.exchange(
                "/eskat/companyParticipantConnection/1/rest/search?aktivitet=!Aktiv",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(false, response.getBody().contains("25052943"));

        response = restTemplate.exchange(
                "/eskat/companyParticipantConnection/1/rest/search?aktivitet=Aktiv&companystartTime.LTE=1970-01-01",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(false, response.getBody().contains("25052943"));

        response = restTemplate.exchange(
                "/eskat/companyParticipantConnection/1/rest/search?aktivitet=Aktiv&companystartTime.LTE=2000-01-01",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(true, response.getBody().contains("25052943"));

        response = restTemplate.exchange(
                "/eskat/companyParticipantConnection/1/rest/search?aktivitet=Aktiv&companystartTime.GTE=1990-01-01",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(true, response.getBody().contains("25052943"));

        response = restTemplate.exchange(
                "/eskat/companyParticipantConnection/1/rest/search?aktivitet=Aktiv&companystartTime.GTE=2020-01-01",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(false, response.getBody().contains("25052943"));

        response = restTemplate.exchange(
                "/eskat/companyParticipantConnection/1/rest/search?aktivitet=Aktiv&companyendTime.LTE=2022-01-01",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(false, response.getBody().contains("25052943"));
    }

    @Test
    public void testCompanyPunitLookup() throws Exception {
        this.loadAllCompany();

        TestUserDetails testUserDetails = new TestUserDetails();

        ObjectNode body = objectMapper.createObjectNode();
        HttpEntity<String> httpEntity = new HttpEntity<String>(body.toString(), new HttpHeaders());
        ResponseEntity<String> response;

        response = restTemplate.exchange(
                "/eskat/punit/1/rest/1020895337",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        Assertions.assertEquals(false, response.getBody().contains("1020895337"));

        testUserDetails.giveAccess(CvrRolesDefinition.READ_CVR_ROLE);
        this.applyAccess(testUserDetails);

        response = restTemplate.exchange(
                "/eskat/punit/1/rest/1020895337",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(true, response.getBody().contains("1020895337"));

        response = restTemplate.exchange(
                "/eskat/punit/1/rest/1020895338",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assertions.assertEquals(false, response.getBody().contains("1020895337"));
        Assertions.assertEquals(false, response.getBody().contains("1020895338"));
    }


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
