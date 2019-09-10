package dk.magenta.datafordeler.eboks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.Entity;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.InputStreamReader;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cpr.data.person.PersonEntityManager;
import dk.magenta.datafordeler.cvr.access.CvrRolesDefinition;
import dk.magenta.datafordeler.cvr.entitymanager.CompanyEntityManager;
import dk.magenta.datafordeler.ger.GerPlugin;
import dk.magenta.datafordeler.ger.data.company.CompanyEntity;
import dk.magenta.datafordeler.ger.data.responsible.ResponsibleEntity;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.StringJoiner;

import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EboksLookupTest {

    @Autowired
    TestRestTemplate restTemplate;
    HashSet<Entity> createdEntities = new HashSet<>();
    @Autowired
    private SessionManager sessionManager;
    @Autowired
    private CompanyEntityManager companyEntityManager;
    @Autowired
    private PersonEntityManager personEntityManager;
    @Autowired
    private ObjectMapper objectMapper;
    @SpyBean
    private DafoUserManager dafoUserManager;
    @Autowired
    private GerPlugin gerPlugin;

    public void loadDkPerson() throws Exception {
        InputStream testData = EboksLookupTest.class.getResourceAsStream("/personDK.txt");
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

    public void loadManyPersons(int count, int start) throws Exception {
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = sessionManager.getSessionFactory().openSession();
        importMetadata.setSession(session);
        Transaction transaction = session.beginTransaction();
        importMetadata.setTransactionInProgress(true);
        String testData = InputStreamReader.readInputStream(EboksLookupTest.class.getResourceAsStream("/personGL.txt"));
        String[] lines = testData.split("\n");
        for (int i = start; i < count + start; i++) {
            StringJoiner sb = new StringJoiner("\n");
            String newCpr = String.format("%010d", i);
            for (int j = 0; j < lines.length; j++) {
                String line = lines[j];
                line = line.substring(0, 3) + newCpr + line.substring(13);
                sb.add(line);
            }
            ByteArrayInputStream bais = new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));
            personEntityManager.parseData(bais, importMetadata);
            bais.close();
        }
        transaction.commit();
        session.close();
    }


    private void loadCompany() throws IOException, DataFordelerException {
        InputStream testData = EboksLookupTest.class.getResourceAsStream("/company_in.json");
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

    private void loadGerCompany() throws IOException, DataFordelerException {
        InputStream testData = EboksLookupTest.class.getResourceAsStream("/GER.test.xlsx");
        Session session = sessionManager.getSessionFactory().openSession();
        try {
            dk.magenta.datafordeler.ger.data.company.CompanyEntityManager companyEntityManager = (dk.magenta.datafordeler.ger.data.company.CompanyEntityManager) gerPlugin.getRegisterManager().getEntityManager(CompanyEntity.schema);
            ImportMetadata importMetadata = new ImportMetadata();
            importMetadata.setSession(session);
            companyEntityManager.parseData(testData, importMetadata);
        } finally {
            session.close();
            testData.close();
        }
    }

    private void loadGerParticipant() throws IOException, DataFordelerException {
        InputStream testData = EboksLookupTest.class.getResourceAsStream("/GER.test.xlsx");
        Session session = sessionManager.getSessionFactory().openSession();
        try {
            dk.magenta.datafordeler.ger.data.responsible.ResponsibleEntityManager responsibleEntityManager = (dk.magenta.datafordeler.ger.data.responsible.ResponsibleEntityManager) gerPlugin.getRegisterManager().getEntityManager(ResponsibleEntity.schema);
            ImportMetadata importMetadata = new ImportMetadata();
            importMetadata.setSession(session);
            responsibleEntityManager.parseData(testData, importMetadata);
        } finally {
            session.close();
            testData.close();
        }
    }

    public void loadManyCompanies(int count) throws Exception {
        this.loadManyCompanies(count, 0);
    }

    public void loadManyCompanies(int count, int start) throws Exception {
        ImportMetadata importMetadata = new ImportMetadata();
        String testData = InputStreamReader.readInputStream(EboksLookupTest.class.getResourceAsStream("/company_in.json"));
        for (int i = start; i < count + start; i++) {
            String altered = testData.replaceAll("25052943", "1" + String.format("%07d", i)).replaceAll("\n", "");
            ByteArrayInputStream bais = new ByteArrayInputStream(altered.getBytes(StandardCharsets.UTF_8));
            companyEntityManager.parseData(bais, importMetadata);
            bais.close();
        }
    }

    @Test
    public void testSurveilence() throws Exception {


        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
                "/eboks/recipient/lookup?cpr=" + "{cprs}" + "&cvr={cvrs}",
                HttpMethod.GET,
                httpEntity,
                String.class, "1111", "1111"
        );
        Assert.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

    }


    @Test
    public void testCompanyAndCprLookupPrisme() throws Exception {
        loadCompany();
        loadGerCompany();
        loadDkPerson();
        loadManyPersons(5, 0);

        TestUserDetails testUserDetails = new TestUserDetails();


        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());

        ObjectNode body = objectMapper.createObjectNode();
        ArrayList cprList = new ArrayList();
        cprList.add("0000000000");
        cprList.add("0000000001");
        cprList.add("0000000002");
        cprList.add("0000000003");
        cprList.add("0101001234");
        cprList.add("0000000004");
        cprList.add("0000000005");
        cprList.add("0000000006");

        ArrayList cvrList = new ArrayList();
        cvrList.add("25052943");
        cvrList.add("0000000007");
        cvrList.add("0000000008");
        cvrList.add("12345678");
        cvrList.add("0000000009");

        httpEntity = new HttpEntity<String>(body.toString(), new HttpHeaders());

        String cprs = String.join(",", cprList);
        String cvrs = String.join(",", cvrList);

        ResponseEntity<String> response = restTemplate.exchange(
                "/eboks/recipient/lookup?cpr=" + "{cprs}" + "&cvr={cvrs}",
                HttpMethod.GET,
                httpEntity,
                String.class, cprs, cvrs
        );
        Assert.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());


        testUserDetails.giveAccess(CvrRolesDefinition.READ_CVR_ROLE);
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);


        response = restTemplate.exchange(
                "/eboks/recipient/lookup?cpr=" + "{cprs}" + "&cvr={cvrs}",
                HttpMethod.GET,
                httpEntity,
                String.class, cprs, cvrs
        );

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        JSONAssert.assertEquals("{\"valid\":{\"cpr\":[\"0000000003\",\"0000000004\",\"0000000000\",\"0000000001\",\"0000000002\"],\"cvr\":[\"12345678\",\"25052943\"]},\"invalid\":{\"cpr\":[{\"nr\":\"0000000005\",\"reason\":\"Missing\"},{\"nr\":\"0000000006\",\"reason\":\"Missing\"},{\"nr\":\"0101001234\",\"reason\":\"NotFromGreenland\"}],\"cvr\":[{\"nr\":\"0000000007\",\"reason\":\"Missing\"},{\"nr\":\"0000000008\",\"reason\":\"Missing\"},{\"nr\":\"0000000009\",\"reason\":\"Missing\"}]}}", response.getBody(), false);
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
