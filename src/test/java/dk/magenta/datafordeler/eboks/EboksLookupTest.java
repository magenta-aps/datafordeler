package dk.magenta.datafordeler.eboks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.Entity;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.InputStreamReader;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cvr.CvrPlugin;
import dk.magenta.datafordeler.cvr.access.CvrAreaRestrictionDefinition;
import dk.magenta.datafordeler.cvr.access.CvrRolesDefinition;
import dk.magenta.datafordeler.cvr.entitymanager.CompanyEntityManager;
import dk.magenta.datafordeler.ger.GerPlugin;
import dk.magenta.datafordeler.ger.data.company.CompanyEntity;
import dk.magenta.datafordeler.ger.data.responsible.ResponsibleEntity;
import dk.magenta.datafordeler.gladdrreg.GladdrregPlugin;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import java.util.HashSet;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EboksLookupTest {

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private CompanyEntityManager companyEntityManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GladdrregPlugin gladdrregPlugin;

    @Autowired
    TestRestTemplate restTemplate;

    @SpyBean
    private DafoUserManager dafoUserManager;

    @Autowired
    private CvrPlugin cvrPlugin;

    @Autowired
    private GerPlugin gerPlugin;

    @Autowired
    private EboksRecieveLookupService eboksRecieveLookupService;

    HashSet<Entity> createdEntities = new HashSet<>();


    private void loadCompany() throws IOException, DataFordelerException {
        InputStream testData = EboksLookupTest.class.getResourceAsStream("/company_in.json");
        JsonNode root = objectMapper.readTree(testData);
        testData.close();
        JsonNode itemList = root.get("hits").get("hits");
        Assert.assertTrue(itemList.isArray());
        ImportMetadata importMetadata = new ImportMetadata();
        for (JsonNode item : itemList) {
            String source = objectMapper.writeValueAsString(item.get("_source").get("Vrvirksomhed"));
            ByteArrayInputStream bais = new ByteArrayInputStream(source.getBytes("UTF-8"));
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
            ByteArrayInputStream bais = new ByteArrayInputStream(altered.getBytes("UTF-8"));
            companyEntityManager.parseData(bais, importMetadata);
            bais.close();
        }
    }


    @Test
    public void testCompanyAndCprLookupPrisme() throws IOException, DataFordelerException {
        loadCompany();

        try {

            TestUserDetails testUserDetails = new TestUserDetails();


            HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
            ResponseEntity<String> response = restTemplate.exchange(
                    "/eboks/cvr/1/" + 25052943,
                    HttpMethod.GET,
                    httpEntity,
                    String.class
            );
            Assert.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());


            testUserDetails.giveAccess(CvrRolesDefinition.READ_CVR_ROLE);
            this.applyAccess(testUserDetails);
            response = restTemplate.exchange(
                    "/eboks/cvr/1/" + 25052943,
                    HttpMethod.GET,
                    httpEntity,
                    String.class
            );
            Assert.assertEquals(HttpStatus.OK, response.getStatusCode());


            testUserDetails.giveAccess(
                    cvrPlugin.getAreaRestrictionDefinition().getAreaRestrictionTypeByName(
                            CvrAreaRestrictionDefinition.RESTRICTIONTYPE_KOMMUNEKODER
                    ).getRestriction(
                            CvrAreaRestrictionDefinition.RESTRICTION_KOMMUNE_SERMERSOOQ
                    )
            );
            this.applyAccess(testUserDetails);
            response = restTemplate.exchange(
                    "/eboks/cvr/1/" + 25052943,
                    HttpMethod.GET,
                    httpEntity,
                    String.class
            );
            Assert.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());


            testUserDetails.giveAccess(
                    cvrPlugin.getAreaRestrictionDefinition().getAreaRestrictionTypeByName(
                            CvrAreaRestrictionDefinition.RESTRICTIONTYPE_KOMMUNEKODER
                    ).getRestriction(
                            CvrAreaRestrictionDefinition.RESTRICTION_KOMMUNE_KUJALLEQ
                    )
            );
            testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
            this.applyAccess(testUserDetails);
            response = restTemplate.exchange(
                    "/eboks/cvr/1/" + 25052943 + "?returnParticipantDetails=1",
                    HttpMethod.GET,
                    httpEntity,
                    String.class
            );
            Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        } finally {
            cleanup();
        }
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
                } catch (Exception e) {}
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
