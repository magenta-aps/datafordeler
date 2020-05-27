package dk.magenta.datafordeler.prisme;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cpr.direct.CprDirectLookup;
import dk.magenta.datafordeler.cvr.CvrPlugin;
import dk.magenta.datafordeler.cvr.DirectLookup;
import dk.magenta.datafordeler.cvr.access.CvrRolesDefinition;
import dk.magenta.datafordeler.cvr.entitymanager.CompanyEntityManager;
import dk.magenta.datafordeler.cvr.records.CompanyRecord;
import dk.magenta.datafordeler.cvr.records.ParticipantRecord;
import org.hibernate.Session;
import org.junit.After;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CvrOwnerHistoryTest extends TestBase {

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private CompanyEntityManager companyEntityManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    TestRestTemplate restTemplate;

    @SpyBean
    private DafoUserManager dafoUserManager;

    @Autowired
    private CvrPlugin cvrPlugin;

    @SpyBean
    private DirectLookup directLookup;

    @Test
    public void testCompanyOwnerHistoryPrisme() throws IOException, DataFordelerException {
        loadAllGeoAdress(sessionManager);
        loadCompany(cvrPlugin, sessionManager, objectMapper);
        loadInteressentskab(cvrPlugin, sessionManager, objectMapper);

        ParticipantRecord participant1 = new ParticipantRecord();
        participant1.setBusinessKey(1111111111L);
        Mockito.doReturn(participant1).when(directLookup).participantLookup(ArgumentMatchers.eq("4000032977"));

        ParticipantRecord participant2 = new ParticipantRecord();
        participant2.setBusinessKey(1111111112L);
        Mockito.doReturn(participant2).when(directLookup).participantLookup(ArgumentMatchers.eq("4000448343"));

        ParticipantRecord participant3 = new ParticipantRecord();
        participant2.setBusinessKey(1111111113L);
        Mockito.doReturn(participant2).when(directLookup).participantLookup(ArgumentMatchers.eq("4000355373"));

        ParticipantRecord participant4 = new ParticipantRecord();
        participant2.setBusinessKey(1111111114L);
        Mockito.doReturn(participant2).when(directLookup).participantLookup(ArgumentMatchers.eq("4000417793"));

        ParticipantRecord participant5 = new ParticipantRecord();
        participant2.setBusinessKey(1111111115L);
        Mockito.doReturn(participant2).when(directLookup).participantLookup(ArgumentMatchers.eq("4000004988"));

        ParticipantRecord participant6 = new ParticipantRecord();
        participant2.setBusinessKey(1111111116L);
        Mockito.doReturn(participant2).when(directLookup).participantLookup(ArgumentMatchers.eq("4006182867"));

        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CvrRolesDefinition.READ_CVR_ROLE);
        this.applyAccess(testUserDetails);

        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
                "/prisme/cvr/ownerhistory/1/" + 88888885,
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);

        Assert.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        httpEntity = new HttpEntity<String>("", new HttpHeaders());
        response = restTemplate.exchange(
                "/prisme/cvr/ownerhistory/1/" + 25052943,
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());


        httpEntity = new HttpEntity<String>("", new HttpHeaders());
        response = restTemplate.exchange(
                "/prisme/cvr/ownerhistory/1/" + 11111111,
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());


        response = restTemplate.exchange(
                "/prisme/cvr/ownerhistory/1/" + 88888885,
                HttpMethod.GET,
                httpEntity,
                String.class
        );

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        System.out.println(response.getBody());

    }

    @Test
    public void testFindChanges() throws IOException, DataFordelerException {
        loadAllGeoAdress(sessionManager);
        loadCompany(cvrPlugin, sessionManager, objectMapper);
        loadInteressentskab(cvrPlugin, sessionManager, objectMapper);

        ArrayList companyFormsList = new ArrayList();
        companyFormsList.add("10");
        companyFormsList.add("30");
        companyFormsList.add("50");

        String companyForms = String.join(",", companyFormsList);

        TestUserDetails testUserDetails = new TestUserDetails();
        this.applyAccess(testUserDetails);

        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
                "/prisme/cvr/companychanges/1/lookup?" + "companyForms={companyForms}&updatedSince=2020-01-01",
                HttpMethod.GET,
                httpEntity,
                String.class, companyForms
        );
        Assert.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());


        testUserDetails.giveAccess(CvrRolesDefinition.READ_CVR_ROLE);
        this.applyAccess(testUserDetails);

        httpEntity = new HttpEntity<String>("", new HttpHeaders());
        response = restTemplate.exchange(
                "/prisme/cvr/companychanges/1/lookup?" + "companyForms={companyForms}&updatedSince=2020-01-01",
                HttpMethod.GET,
                httpEntity,
                String.class, companyForms
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertTrue(objectMapper.readTree(response.getBody()).get("changedList").size() == 1);


        companyFormsList.add("80");
        companyForms = String.join(",", companyFormsList);
        httpEntity = new HttpEntity<String>("", new HttpHeaders());
        response = restTemplate.exchange(
                "/prisme/cvr/companychanges/1/lookup?" + "companyForms={companyForms}&updatedSince=2020-01-01",
                HttpMethod.GET,
                httpEntity,
                String.class, companyForms
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertTrue(objectMapper.readTree(response.getBody()).get("changedList").size() == 2);

        Date currentDate = new Date();
        String localDateTime = currentDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().plusWeeks(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        httpEntity = new HttpEntity<String>("", new HttpHeaders());
        response = restTemplate.exchange(
                "/prisme/cvr/companychanges/1/lookup?" + "companyForms={companyForms}&updatedSince=" + localDateTime,
                HttpMethod.GET,
                httpEntity,
                String.class, companyForms
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertTrue(objectMapper.readTree(response.getBody()).get("changedList").size() == 0);

    }


    protected void loadInteressentskab(CvrPlugin cvrPlugin, SessionManager sessionManager, ObjectMapper objectMapper) throws IOException, DataFordelerException {
        InputStream testData = CvrCombinedTest.class.getResourceAsStream("/company_interessentskab.json");
        Session session = sessionManager.getSessionFactory().openSession();
        try {
            dk.magenta.datafordeler.cvr.entitymanager.CompanyEntityManager companyEntityManager = (dk.magenta.datafordeler.cvr.entitymanager.CompanyEntityManager) cvrPlugin.getRegisterManager().getEntityManager(CompanyRecord.schema);
            JsonNode root = objectMapper.readTree(testData);
            testData.close();
            JsonNode itemList = root.get("hits").get("hits");
            Assert.assertTrue(itemList.isArray());
            ImportMetadata importMetadata = new ImportMetadata();
            importMetadata.setSession(session);

            for (JsonNode item : itemList) {
                String source = objectMapper.writeValueAsString(item.get("_source").get("Vrvirksomhed"));
                ByteArrayInputStream bais = new ByteArrayInputStream(source.getBytes("UTF-8"));
                companyEntityManager.parseData(bais, importMetadata);
                bais.close();
            }
        } finally {
            session.close();
        }
    }


    private void applyAccess(TestUserDetails testUserDetails) {
        when(dafoUserManager.getFallbackUser()).thenReturn(testUserDetails);
    }

}
