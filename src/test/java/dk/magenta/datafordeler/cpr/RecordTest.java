package dk.magenta.datafordeler.cpr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonEntityManager;
import dk.magenta.datafordeler.cpr.data.person.PersonOutputWrapper;
import dk.magenta.datafordeler.cpr.data.person.PersonRecordQuery;
import dk.magenta.datafordeler.cpr.records.output.PersonRecordOutputWrapper;
import org.hibernate.Session;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RecordTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private PersonEntityManager personEntityManager;

    @Autowired
    private PersonRecordOutputWrapper personRecordOutputWrapper;

    //@Autowired
    private PersonOutputWrapper personOutputWrapper = new PersonOutputWrapper();

    @Autowired
    private CprPlugin plugin;

    @Autowired
    private TestRestTemplate restTemplate;

    private static HashMap<String, String> schemaMap = new HashMap<>();
    static {
        schemaMap.put("person", PersonEntity.schema);
    }

    @SpyBean
    private DafoUserManager dafoUserManager;

    private void applyAccess(TestUserDetails testUserDetails) {
        when(dafoUserManager.getFallbackUser()).thenReturn(testUserDetails);
    }

    private void loadPerson(String resource, ImportMetadata importMetadata) throws DataFordelerException, IOException {
        InputStream testData = ParseTest.class.getResourceAsStream(resource);
        personEntityManager.parseData(testData, importMetadata);
        testData.close();
    }

    @Test
    public void testCompany() throws DataFordelerException, IOException {
        Session session = sessionManager.getSessionFactory().openSession();
        ImportMetadata importMetadata = new ImportMetadata();
        importMetadata.setSession(session);
        this.loadPerson("/persondata.txt", importMetadata);
        try {


            PersonRecordQuery query = new PersonRecordQuery();
            OffsetDateTime time = OffsetDateTime.now();
            query.setRegistrationTo(time);
            query.setEffectFrom(time);
            query.setEffectTo(time);
            query.applyFilters(session);

            query.addKommunekode(956);
            Assert.assertEquals(0, QueryManager.getAllEntities(session, query, PersonEntity.class).size());
            query.clearKommunekode();

            query.addKommunekode(958);
            Assert.assertEquals(1, QueryManager.getAllEntities(session, query, PersonEntity.class).size());
            query.clearKommunekode();
            query.setFornavn("Tester");
            Assert.assertEquals(1, QueryManager.getAllEntities(session, query, PersonEntity.class).size());
            query.setFornavn(null);

            query.setEfternavn("Tystersen");
            Assert.assertEquals(1, QueryManager.getAllEntities(session, query, PersonEntity.class).size());

            query.setEfternavn("Testersen");
            Assert.assertEquals(0, QueryManager.getAllEntities(session, query, PersonEntity.class).size());

            time = OffsetDateTime.parse("2000-01-01T00:00:00Z");
            query.setEffectFrom(time);
            query.setEffectTo(time);
            query.applyFilters(session);

            query.setEfternavn("Testersen");
            Assert.assertEquals(1, QueryManager.getAllEntities(session, query, PersonEntity.class).size());

        } finally {
            session.close();
        }
    }

    @Test
    public void testUpdatePerson() throws IOException, DataFordelerException {
        Session session = sessionManager.getSessionFactory().openSession();
        ImportMetadata importMetadata = new ImportMetadata();
        importMetadata.setSession(session);
        this.loadPerson("/persondata.txt", importMetadata);
        this.loadPerson("/persondata2.txt", importMetadata);
        try {
            PersonRecordQuery query = new PersonRecordQuery();
            query.setPersonnummer("0101001234");
            List<PersonEntity> entities = QueryManager.getAllEntities(session, query, PersonEntity.class);
            PersonEntity personEntity = entities.get(0);

            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(personEntity));

            Assert.assertEquals(1, personEntity.getConame().size());
            Assert.assertEquals(1, personEntity.getAddress().size());
            Assert.assertEquals(2, personEntity.getAddressName().size());
            Assert.assertEquals(1, personEntity.getBirthPlace().size());
            Assert.assertEquals(1, personEntity.getBirthPlaceVerification().size());
            Assert.assertEquals(1, personEntity.getBirthTime().size());
            Assert.assertEquals(3, personEntity.getChurchRelation().size());
            Assert.assertEquals(3, personEntity.getChurchRelationVerification().size());
            Assert.assertEquals(0, personEntity.getCivilstatus().size());
            Assert.assertEquals(0, personEntity.getCivilstatusAuthorityText().size());
            Assert.assertEquals(0, personEntity.getCivilstatusVerification().size());
            Assert.assertEquals(1, personEntity.getForeignAddress().size());
            Assert.assertEquals(1, personEntity.getEmigration().size());
            Assert.assertEquals(1, personEntity.getMunicipalityMove().size());
            Assert.assertEquals(3, personEntity.getName().size());
            Assert.assertEquals(3, personEntity.getNameAuthorityText().size());
            Assert.assertEquals(2, personEntity.getNameVerification().size());
            Assert.assertEquals(1, personEntity.getMother().size());
            Assert.assertEquals(1, personEntity.getMotherVerification().size());
            Assert.assertEquals(1, personEntity.getFather().size());
            Assert.assertEquals(1, personEntity.getFatherVerification().size());
            Assert.assertEquals(1, personEntity.getCore().size());
            Assert.assertEquals(1, personEntity.getPosition().size());
            Assert.assertEquals(1, personEntity.getStatus().size());
            Assert.assertEquals(2, personEntity.getProtection().size());

        } finally {
            session.close();
        }
    }

/*
    @Test
    public void testRestCompany() throws IOException, DataFordelerException {
        loadCompany("/company_in.json");
        TestUserDetails testUserDetails = new TestUserDetails();
        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        ResponseEntity<String> resp = restTemplate.exchange("/cvr/company/1/rest/search?cvrnummer=25052943", HttpMethod.GET, httpEntity, String.class);
        Assert.assertEquals(403, resp.getStatusCodeValue());

        testUserDetails.giveAccess(CvrRolesDefinition.READ_CVR_ROLE);
        this.applyAccess(testUserDetails);

        resp = restTemplate.exchange("/cvr/company/1/rest/search?cvrnummer=25052943&virkningFra=2000-01-01&virkningTil=2000-01-01", HttpMethod.GET, httpEntity, String.class);
        String body = resp.getBody();
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(body)));
    }
*/

    @Test
    public void testPersonOutputWrapper() throws IOException, DataFordelerException {

        Session session = sessionManager.getSessionFactory().openSession();
        try {
            ImportMetadata importMetadata = new ImportMetadata();
            importMetadata.setSession(session);
            this.loadPerson("/persondata.txt", importMetadata);

            PersonRecordQuery query = new PersonRecordQuery();
            /*OffsetDateTime time = OffsetDateTime.now();
            query.setRegistrationTo(time);
            query.setEffectFrom(time);
            query.setEffectTo(time);*/
            query.applyFilters(session);

            query.addKommunekode(958);
            PersonEntity personEntity = QueryManager.getAllEntities(session, query, PersonEntity.class).get(0);

            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString((ObjectNode) this.personRecordOutputWrapper.wrapResult(personEntity, query)));
            System.out.println("--------------------");
            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString((ObjectNode) this.personOutputWrapper.wrapResult(personEntity, query)));

        } finally {
            session.close();
        }
    }

    /**
     * Checks that all items in n1 are also present in n2
     * @param n1
     * @param n2
     * @param path
     * @throws JsonProcessingException
     */
    private void compareJson(JsonNode n1, JsonNode n2, List<String> path) throws JsonProcessingException {
        if (n1 == null && n2 != null) {
            System.out.println("Mismatch: "+n1+" != "+n2+" at "+path);
        } else if (n1 != null && n2 == null) {
            System.out.println("Mismatch: "+n1+" != "+n2+" at "+path);
        } else if (n1.isObject() && n2.isObject()) {
            ObjectNode o1 = (ObjectNode) n1;
            ObjectNode o2 = (ObjectNode) n2;
            Set<String> f2 = new HashSet<>();
            Iterator<String> o2Fields = o2.fieldNames();
            while (o2Fields.hasNext()) {
                f2.add(o2Fields.next());
            }

            Iterator<String> o1Fields = o1.fieldNames();
            while (o1Fields.hasNext()) {
                String field = o1Fields.next();
                if (!f2.contains(field)) {
                    System.out.println("Mismatch: missing field "+field+" at "+path);
                } else {
                    ArrayList<String> subpath = new ArrayList<>(path);
                    subpath.add(field);
                    compareJson(o1.get(field), o2.get(field), subpath);
                }
            }

        } else if (n1.isArray() && n2.isArray()) {
            ArrayNode a1 = (ArrayNode) n1;
            ArrayNode a2 = (ArrayNode) n2;

            if (a1.size() != a2.size()) {
                System.out.println("Mismatch: Array["+a1.size()+"] != Array["+a2.size()+"] at "+path);
                System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(a2));
            } else {

                for (int i = 0; i < a1.size(); i++) {
                    boolean found = false;
                    for (int j=0; j<a2.size(); j++) {
                        if (a1.get(i).asText().equals(a2.get(j).asText())) {
                            found = true;
                        }
                    }
                    if (!found) {
                        System.out.println("Mismatch: Didn't find item "+a1.get(i)+" in "+objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(a2)+" at "+path);
                    }
                }
            }


        } else if (!n1.asText().equals(n2.asText())){
            boolean skip = false;
            try {
                if (OffsetDateTime.parse(n1.asText()).isEqual(OffsetDateTime.parse(n2.asText()))) {
                    skip = true;
                }
            } catch (DateTimeParseException e) {}
            if (!skip) {
                System.out.println("Mismatch: " + n1.asText() + " (" + n1.getNodeType().name() + ") != " + n2.asText() + " (" + n2.getNodeType().name() + ") at " + path);
            }
        }
    }
}
