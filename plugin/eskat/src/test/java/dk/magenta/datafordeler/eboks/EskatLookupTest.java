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

import dk.magenta.datafordeler.cvr.access.CvrRolesDefinition;
import dk.magenta.datafordeler.cvr.entitymanager.CompanyEntityManager;
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
import java.util.HashSet;

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
        JSONAssert.assertEquals("[\"Aktiv: NORMAL\"]", response.getBody(), false);
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
