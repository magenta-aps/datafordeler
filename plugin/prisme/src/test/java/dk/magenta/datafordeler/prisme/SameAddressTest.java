package dk.magenta.datafordeler.prisme;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.Entity;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.InputStreamReader;
import dk.magenta.datafordeler.cpr.CprAreaRestrictionDefinition;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cpr.data.person.PersonEntityManager;
import org.hamcrest.CoreMatchers;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.skyscreamer.jsonassert.JSONAssert;
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
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.StringJoiner;

import static org.mockito.Mockito.when;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SameAddressTest extends TestBase {

    HashSet<Entity> createdEntities = new HashSet<>();

    @Before
    public void load() throws IOException, DataFordelerException {
        this.loadAllGeoAdress(sessionManager);
    }


    public void loadPerson() throws Exception {
        InputStream testData = SameAddressTest.class.getResourceAsStream("/person.txt");
        InputStream testData2 = SameAddressTest.class.getResourceAsStream("/person2.txt");
        InputStream testData3 = SameAddressTest.class.getResourceAsStream("/person3.txt");
        InputStream testDataPersonAdmin = SameAddressTest.class.getResourceAsStream("/personAdminAdd.txt");
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = sessionManager.getSessionFactory().openSession();
        importMetadata.setSession(session);
        Transaction transaction = session.beginTransaction();
        importMetadata.setTransactionInProgress(true);
        personEntityManager.parseData(testData, importMetadata);
        personEntityManager.parseData(testData2, importMetadata);
        personEntityManager.parseData(testData3, importMetadata);
        personEntityManager.parseData(testDataPersonAdmin, importMetadata);
        transaction.commit();
        session.close();
        testData.close();
        testData2.close();
        testData3.close();
        testDataPersonAdmin.close();
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
        String testData = InputStreamReader.readInputStream(SameAddressTest.class.getResourceAsStream("/person.txt"));
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

    /**
     * Validate that the sameadress service finds the cpr-number of all persons that does have an address which is the same as the cpr-number that is used when calling the service
     *
     * @throws Exception
     */
    @Test
    public void test3PersonPrisme() throws Exception {
        loadPerson();


        try {
            TestUserDetails testUserDetails = new TestUserDetails();

            HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());

            testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
            this.applyAccess(testUserDetails);
            ResponseEntity<String> response = restTemplate.exchange(
                    "/prisme/sameaddress/1/" + "0101001234",
                    HttpMethod.GET,
                    httpEntity,
                    String.class
            );
            Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
            Assert.assertTrue(objectMapper.readTree(response.getBody()).size() > 0);

            JSONAssert.assertEquals("{\"cprNumber\":\"0101001234\",\"municipalitycode\":956,\"roadcode\":254,\"housenumber\":\"18\",\"floor\":\"1\",\"door\":\"tv\",\"buildingNo\":\"3197\",\"localityCode\":600,\"roadName\":\"Qarsaalik\",\"sameAddressCprs\":[\"0101001242\",\"0101001243\",\"0101001244\",\"0101001234\",\"0101001236\",\"0101001237\",\"0101001238\",\"0101001239\",\"0101001240\",\"0101001241\",\"0101001245\",\"0101001246\",\"0101001247\",\"0101001248\",\"0101001249\",\"0101001251\"]}", response.getBody(), false);

            Assert.assertThat(response.getBody(), CoreMatchers.containsString("\"municipalitycode\":956"));
            Assert.assertThat(response.getBody(), CoreMatchers.containsString("\"roadcode\":254"));
            Assert.assertThat(response.getBody(), CoreMatchers.containsString("\"housenumber\":\"18\""));
            Assert.assertThat(response.getBody(), CoreMatchers.containsString("\"floor\":\"1\""));
            Assert.assertThat(response.getBody(), CoreMatchers.containsString("\"door\":\"tv\""));


            testUserDetails.giveAccess(
                    cprPlugin.getAreaRestrictionDefinition().getAreaRestrictionTypeByName(
                            CprAreaRestrictionDefinition.RESTRICTIONTYPE_KOMMUNEKODER
                    ).getRestriction(
                            CprAreaRestrictionDefinition.RESTRICTION_KOMMUNE_KUJALLEQ
                    )
            );
            this.applyAccess(testUserDetails);
            response = restTemplate.exchange(
                    "/prisme/sameaddress/1/" + "0101001234",
                    HttpMethod.GET,
                    httpEntity,
                    String.class
            );
            Assert.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

            testUserDetails.giveAccess(
                    cprPlugin.getAreaRestrictionDefinition().getAreaRestrictionTypeByName(
                            CprAreaRestrictionDefinition.RESTRICTIONTYPE_KOMMUNEKODER
                    ).getRestriction(
                            CprAreaRestrictionDefinition.RESTRICTION_KOMMUNE_SERMERSOOQ
                    )
            );
            this.applyAccess(testUserDetails);
            response = restTemplate.exchange(
                    "/prisme/sameaddress/1/" + "0101001234",
                    HttpMethod.GET,
                    httpEntity,
                    String.class
            );
            Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
            Assert.assertTrue(objectMapper.readTree(response.getBody()).size() > 0);

            response = restTemplate.exchange(
                    "/prisme/sameaddress/1/" + "0101005551",
                    HttpMethod.GET,
                    httpEntity,
                    String.class
            );
            Assert.assertThat(response.getBody(), CoreMatchers.containsString("\"roadName\":\"Administrativ\""));
            Assert.assertTrue(objectMapper.readTree(response.getBody()).size() > 0);


        } finally {
            cleanup();
        }
    }

    /**
     * @throws Exception
     */
    @Test
    public void testCohabitantPersonPrisme() throws Exception {
        loadPerson();

        TestUserDetails testUserDetails = new TestUserDetails();
        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());

        ResponseEntity<String> response = restTemplate.exchange(
                "/prisme/cpr/cohabitationinformation/1/search/?cpr=" + "0101001234,0101001236",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);


        response = restTemplate.exchange(
                "/prisme/cpr/cohabitationinformation/1/search/?cpr=" + "0101001234,0101001235",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("{\"cpr1\":\"0101001234\",\"cpr2\":\"0101001235\",\"Cohabitation\":false,\"ResidentDate\":null}", response.getBody(), false);

        response = restTemplate.exchange(
                "/prisme/cpr/cohabitationinformation/1/search/?cpr=" + "0101001234,0101001236",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("{\"cpr1\":\"0101001234\",\"cpr2\":\"0101001236\",\"Cohabitation\":true,\"ResidentDate\":\"2008-07-03\"}", response.getBody(), false);

        response = restTemplate.exchange(
                "/prisme/cpr/cohabitationinformation/1/search/?cpr=" + "0101001234,1212121212",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        response = restTemplate.exchange(
                "/prisme/cpr/cohabitationinformation/1/search/?cpr=" + "0101001234,0101001235",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("{\"cpr1\":\"0101001234\",\"cpr2\":\"0101001235\",\"Cohabitation\":false,\"ResidentDate\":null}", response.getBody(), false);
    }

    private void applyAccess(TestUserDetails testUserDetails) {
        when(dafoUserManager.getFallbackUser()).thenReturn(testUserDetails);
    }

    private void cleanupEntities() {
        Session session = sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        try {
            for (Entity entity : createdEntities) {
                session.delete(entity);
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
