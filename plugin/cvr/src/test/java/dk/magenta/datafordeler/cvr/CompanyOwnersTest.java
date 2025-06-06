package dk.magenta.datafordeler.cvr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.plugin.EntityManager;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.cvr.access.CvrRolesDefinition;
import dk.magenta.datafordeler.cvr.query.CompanyRecordQuery;
import dk.magenta.datafordeler.cvr.records.CompanyRecord;
import org.hibernate.Session;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.Mockito.when;


@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class CompanyOwnersTest extends TestBase {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockitoSpyBean
    private DafoUserManager dafoUserManager;

    private void applyAccess(TestUserDetails testUserDetails) {
        when(dafoUserManager.getFallbackUser()).thenReturn(testUserDetails);
    }
    @Test
    public void testOwners() throws DataFordelerException, URISyntaxException, JsonProcessingException {
        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CvrRolesDefinition.READ_CVR_ROLE);
        this.applyAccess(testUserDetails);

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            CompanyRecordQuery query = new CompanyRecordQuery();
            OffsetDateTime time = OffsetDateTime.now();
            query.setRegistrationAt(time);
            query.setEffectAt(time);
            query.applyFilters(session);
            List<CompanyRecord> companyList = QueryManager.getAllEntities(session, query, CompanyRecord.class);
            Assertions.assertEquals(0, companyList.size());
        }
        ImportMetadata importMetadata = new ImportMetadata();

        registerManager.setCvrDemoCompanyFile(ParseTest.class.getResource("/company_in4.json").toURI().toString());
        registerManager.setCvrDemoParticipantFile(ParseTest.class.getResource("/person.json").toURI().toString());

        for (EntityManager entityManager : this.registerManager.getEntityManagers()) {
            if (entityManager.getSchema().equals("virksomhed") || entityManager.getSchema().equals("deltager")) {
                InputStream stream = this.registerManager.pullRawData(this.registerManager.getEventInterface(entityManager), entityManager, importMetadata);
                entityManager.parseData(stream, importMetadata);
            }
        }

        try (Session session = sessionManager.getSessionFactory().openSession()) {

            CompanyRecordQuery query = new CompanyRecordQuery();
            OffsetDateTime time = OffsetDateTime.now();
            query.setRegistrationAt(time);
            query.setEffectAt(time);
            query.applyFilters(session);

            List<CompanyRecord> companyList = QueryManager.getAllEntities(session, query, CompanyRecord.class);
            Assertions.assertEquals(1, companyList.size());

            ResponseEntity<String> response = restTemplate.exchange(
                    "/cvr/owners/"+companyList.get(0).getCvrNumberString(),
                    HttpMethod.GET,
                    new HttpEntity<>("", new HttpHeaders()),
                    String.class
            );
            Assertions.assertEquals(200, response.getStatusCode().value());
            ObjectNode data = (ObjectNode) objectMapper.readTree(response.getBody());
            ArrayNode legale = (ArrayNode) data.get("legale_ejere");
            ArrayNode reelle = (ArrayNode) data.get("reelle_ejere");
            Assertions.assertEquals(1, legale.size());
            Assertions.assertEquals(1, reelle.size());

            Assertions.assertEquals("Morten Kjærsgaard", reelle.get(0).get("deltager").get("navne").get(0).get("navn").asText());
            Assertions.assertEquals("0.7396", reelle.get(0).get("ejerandel").get(0).get("ejerandel").asText());

            Assertions.assertEquals("MAGENTA ApS", legale.get(0).get("deltager").get("navne").get(0).get("navn").asText());
            Assertions.assertEquals("1", legale.get(0).get("ejerandel").get(0).get("ejerandel").get("fra").asText());
            Assertions.assertEquals("1", legale.get(0).get("ejerandel").get(0).get("ejerandel").get("til").asText());
        }
    }
}
