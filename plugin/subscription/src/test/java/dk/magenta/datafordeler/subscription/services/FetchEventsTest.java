package dk.magenta.datafordeler.subscription.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.setup.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonEntityManager;
import dk.magenta.datafordeler.cpr.data.person.PersonRecordQuery;
import dk.magenta.datafordeler.cvr.CvrPlugin;
import dk.magenta.datafordeler.cvr.access.CvrRolesDefinition;
import dk.magenta.datafordeler.cvr.entitymanager.CompanyEntityManager;
import dk.magenta.datafordeler.cvr.records.CompanyRecord;
import dk.magenta.datafordeler.cvr.records.CompanyUnitRecord;
import dk.magenta.datafordeler.cvr.records.ParticipantRecord;
import dk.magenta.datafordeler.subscription.data.subscriptionModel.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.mockito.Mockito.when;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class FetchEventsTest {

    @Autowired
    TestRestTemplate restTemplate;


    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private ObjectMapper objectMapper;
    @SpyBean
    private DafoUserManager dafoUserManager;

    @Autowired
    private CvrPlugin plugin;


    @Autowired
    private PersonEntityManager personEntityManager;

    private final OffsetDateTime timestampInitial = OffsetDateTime.now(ZoneOffset.UTC);

    private static final HashMap<String, String> schemaMap = new HashMap<>();

    static {
        schemaMap.put("_doc", CompanyRecord.schema);
        schemaMap.put("produktionsenhed", CompanyUnitRecord.schema);
        schemaMap.put("deltager", ParticipantRecord.schema);
    }

    @Before
    public void setUp() throws Exception {
        try (Session session = sessionManager.getSessionFactory().openSession()) {


            ImportMetadata importMetadata = new ImportMetadata();
            importMetadata.setSession(session);
            InputStream testData = FindCprBusinessEvent.class.getResourceAsStream("/personsWithEvents.txt");
            personEntityManager.parseData(testData, importMetadata);
            testData.close();

            testData = FindCprBusinessEvent.class.getResourceAsStream("/personsChangingAdresses.txt");
            personEntityManager.parseData(testData, importMetadata);
            testData.close();

            testData = FindCprBusinessEvent.class.getResourceAsStream("/personsChangingAdressesAppend2.txt");
            personEntityManager.parseData(testData, importMetadata);
            testData.close();

            loadCompany("/company_in.json");
            loadCompany("/company_in2.json");
            loadCompany("/company_in3.json");

            Transaction tx = session.beginTransaction();

            Subscriber subscriber = new Subscriber("PITU/GOV/DIA/magenta_services".replaceAll("/", "_"));

            BusinessEventSubscription subscriptionT1 = new BusinessEventSubscription("BE1", "cpr.businessevent.A01", subscriber);
            BusinessEventSubscription subscriptionT2 = new BusinessEventSubscription("BE2", "cpr.businessevent.A02", subscriber);
            BusinessEventSubscription subscriptionT3 = new BusinessEventSubscription("BE3", "cpr.businessevent.A03", subscriber);

            DataEventSubscription subscriptionDE1 = new DataEventSubscription("DE1", "cpr.dataevent.cpr_person_address_record", subscriber);
            DataEventSubscription subscriptionDE2 = new DataEventSubscription("DE2", "cpr.dataevent.anything", subscriber);
            DataEventSubscription subscriptionDE3 = new DataEventSubscription("DE3", "cvr.dataevent.anything", subscriber);
            DataEventSubscription subscriptionDE4 = new DataEventSubscription("DE4", "cvr.dataevent.cpr_person_address_record.after.kommunekode=957", subscriber);
            DataEventSubscription subscriptionDE5 = new DataEventSubscription("DE5", "cvr.dataevent.cpr_person_address_record.before.kommunekode=957", subscriber);
            DataEventSubscription subscriptionDE7 = new DataEventSubscription("DE6", "cvr.dataevent.cvr_record_address", subscriber);
            DataEventSubscription subscriptionDE6 = new DataEventSubscription("DE7", "cvr.dataevent.cvr_record_address.before.kommunekode=956", subscriber);
            DataEventSubscription subscriptionDE8 = new DataEventSubscription("DE8", "cvr.dataevent.cvr_record_address.after.kommunekode=956", subscriber);

            CprList cprList = new CprList("L1");
            cprList.addCprString("0101011235");
            cprList.addCprString("0101011236");
            cprList.addCprString("0101011237");
            cprList.addCprString("0101011238");
            cprList.addCprString("0101011239");
            cprList.addCprString("0101011240");
            cprList.addCprString("0101011241");
            cprList.addCprString("0101011242");
            cprList.addCprString("0101011243");
            cprList.addCprString("0101011244");
            cprList.addCprString("0101011245");
            cprList.addCprString("0101011246");
            cprList.addCprString("0101011247");
            cprList.addCprString("0101011248");
            cprList.addCprString("0101011249");
            cprList.addCprString("0101011250");
            session.save(cprList);

            CvrList cvrList = new CvrList("L2");
            cvrList.addCvrString("25052943");
            session.save(cvrList);

            subscriptionT1.setCprList(cprList);
            subscriptionT2.setCprList(cprList);
            subscriptionT3.setCprList(cprList);

            subscriptionDE1.setCprList(cprList);
            subscriptionDE2.setCprList(cprList);
            subscriptionDE4.setCprList(cprList);
            subscriptionDE5.setCprList(cprList);

            subscriptionDE3.setCvrList(cvrList);
            subscriptionDE6.setCvrList(cvrList);
            subscriptionDE7.setCvrList(cvrList);
            subscriptionDE8.setCvrList(cvrList);

            subscriber.addBusinessEventSubscription(subscriptionT1);
            subscriber.addBusinessEventSubscription(subscriptionT2);
            subscriber.addBusinessEventSubscription(subscriptionT3);

            subscriber.addDataEventSubscription(subscriptionDE1);
            subscriber.addDataEventSubscription(subscriptionDE2);
            subscriber.addDataEventSubscription(subscriptionDE3);
            subscriber.addDataEventSubscription(subscriptionDE4);
            subscriber.addDataEventSubscription(subscriptionDE5);
            subscriber.addDataEventSubscription(subscriptionDE6);
            subscriber.addDataEventSubscription(subscriptionDE7);
            subscriber.addDataEventSubscription(subscriptionDE8);
            session.save(subscriber);
            tx.commit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void applyAccess(TestUserDetails testUserDetails) {
        when(dafoUserManager.getFallbackUser()).thenReturn(testUserDetails);
    }


    /**
     * Test that it is possible to call a service for fetching events
     */
    @Test
    public void testGetCPRBusinessEvents() throws IOException {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("uxp-client", "PITU/GOV/DIA/magenta_services");

        HttpEntity<String> httpEntity = new HttpEntity<String>("", httpHeaders);
        TestUserDetails testUserDetails = new TestUserDetails();
        //testUserDetails.setIdentity("user1");
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        testUserDetails.giveAccess(CvrRolesDefinition.READ_CVR_ROLE);
        this.applyAccess(testUserDetails);

        ResponseEntity<String> response = restTemplate.exchange(
                "/subscription/1/findCprBusinessEvent/fetchEvents?subscription=BE1&timestamp.GTE=2016-10-26T12:00-06:00&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ObjectNode responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        JsonNode results = responseContent.get("results");
        Assert.assertEquals(6, results.size());

        response = restTemplate.exchange(
                "/subscription/1/findCprBusinessEvent/fetchEvents?subscription=BE1&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(6, results.size());

        response = restTemplate.exchange(
                "/subscription/1/findCprBusinessEvent/fetchEvents?subscription=BE2&timestamp.GTE=2016-10-26T12:00-06:00&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(4, results.size());

        response = restTemplate.exchange(
                "/subscription/1/findCprBusinessEvent/fetchEvents?subscription=BE3&timestamp.GTE=2016-10-26T12:00-06:00&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(0, results.size());

        response = restTemplate.exchange(
                "/subscription/1/findCprBusinessEvent/fetchEvents?subscription=BE4&timestamp.GTE=2016-10-26T12:00-06:00&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        response = restTemplate.exchange(
                "/subscription/1/findCprBusinessEvent/fetchEvents?subscription=BE1&timestamp.GTE=2016-10-26T12:00-06:00&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(6, results.size());

        response = restTemplate.exchange(
                "/subscription/1/findCprBusinessEvent/fetchEvents?subscription=BE1&timestamp.GTE=2020-09-01T12:00-06:00&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(2, results.size());

        response = restTemplate.exchange(
                "/subscription/1/findCprBusinessEvent/fetchEvents?subscription=BE1&timestamp.GTE=2020-09-05T12:00-06:00&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(1, results.size());

        response = restTemplate.exchange(
                "/subscription/1/findCprBusinessEvent/fetchEvents?subscription=BE1&timestamp.GTE=2020-09-10T12:00-06:00&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(0, results.size());

        response = restTemplate.exchange(
                "/subscription/1/findCprBusinessEvent/fetchEvents?subscription=BE1&timestamp.GTE=2020091&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        response = restTemplate.exchange(
                "/subscription/1/findCprBusinessEvent/fetchEvents?subscription=BE1&timestamp.GTE=FOOBAR&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        response = restTemplate.exchange(
                "/subscription/1/findCprBusinessEvent/fetchEvents?subscription=BE1&timestamp.LTE=2020091&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        response = restTemplate.exchange(
                "/subscription/1/findCprBusinessEvent/fetchEvents?subscription=BE1&timestamp.LTE=FOOBAR&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

    }


    /**
     * Test that it is possible to call a service for fetching events
     */
    @Test
    public void testGetCPRDataAddressChangeEvents() throws IOException {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("uxp-client", "PITU/GOV/DIA/magenta_services");

        HttpEntity<String> httpEntity = new HttpEntity<String>("", httpHeaders);
        TestUserDetails testUserDetails = new TestUserDetails();
        //testUserDetails.setIdentity("user1");
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        testUserDetails.giveAccess(CvrRolesDefinition.READ_CVR_ROLE);
        this.applyAccess(testUserDetails);

        ResponseEntity<String> response = null;
        ObjectNode responseContent = null;
        JsonNode results = null;

        response = restTemplate.exchange(
                "/subscription/1/findCprDataEvent/fetchEvents?subscription=DE1&timestamp.GTE=2010-11-26T12:00-06:00&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");

        System.out.println(results);
        Assert.assertEquals(6, results.size());

        response = restTemplate.exchange(
                "/subscription/1/findCprDataEvent/fetchEvents?subscription=DE1&includeMeta=true&timestamp.GTE=2010-11-26T12:00-06:00&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");

        Assert.assertEquals(6, results.size());

        response = restTemplate.exchange(
                "/subscription/1/findCprDataEvent/fetchEvents?subscription=DE1&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(6, results.size());

        response = restTemplate.exchange(
                "/subscription/1/findCprDataEvent/fetchEvents?subscription=DE1&timestamp.GTE=2020-01-26T12:00-06:00&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(2, results.size());

        response = restTemplate.exchange(
                "/subscription/1/findCprDataEvent/fetchEvents?subscription=DE1&timestamp.GTE=2020-10-26T12:00-06:00&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(0, results.size());


        response = restTemplate.exchange(
                "/subscription/1/findCprDataEvent/fetchEvents?subscription=DE1&timestamp.GTE=2020102&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        response = restTemplate.exchange(
                "/subscription/1/findCprDataEvent/fetchEvents?subscription=DE1&timestamp.GTE=FOOBAR&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        response = restTemplate.exchange(
                "/subscription/1/findCprDataEvent/fetchEvents?subscription=DE1&timestamp.LTE=2020102&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        response = restTemplate.exchange(
                "/subscription/1/findCprDataEvent/fetchEvents?subscription=DE1&timestamp.LTE=FOOBAR&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    /**
     * Test that it is possible to call a service for fetching events
     */
    @Test
    public void testGetCPRDataAnythingChangeEvents() throws IOException {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("uxp-client", "PITU/GOV/DIA/magenta_services");

        HttpEntity<String> httpEntity = new HttpEntity<String>("", httpHeaders);
        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        testUserDetails.giveAccess(CvrRolesDefinition.READ_CVR_ROLE);
        testUserDetails.setIdentity("PITU/GOV/DIA/magenta_services");
        this.applyAccess(testUserDetails);

        OffsetDateTime timestamp = OffsetDateTime.now(ZoneOffset.UTC);
        ResponseEntity<String> response;
        ObjectNode responseContent;
        JsonNode results;

        response = restTemplate.exchange(
                "/subscription/1/findCprDataEvent/fetchEvents?subscription=DE2&timestamp.GTE=1920-09-26T12:00-06:00&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(9, results.size());

        response = restTemplate.exchange(
                "/subscription/1/findCprDataEvent/fetchEvents?subscription=DE2&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(9, results.size());

        response = restTemplate.exchange(
                "/subscription/1/findCprDataEvent/fetchEvents?subscription=DE2&timestamp.GTE=" + timestamp + "&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(0, results.size());

        try (Session session = sessionManager.getSessionFactory().openSession()) {

            ImportMetadata importMetadata = new ImportMetadata();
            importMetadata.setSession(session);
            InputStream testData = FindCprBusinessEvent.class.getResourceAsStream("/personsChangingAdressesAppend.txt");
            personEntityManager.parseData(testData, importMetadata);
            testData.close();
        } catch (DataFordelerException e) {
            e.printStackTrace();
        }

        response = restTemplate.exchange(
                "/subscription/1/findCprDataEvent/fetchEvents?subscription=DE2&timestamp.GTE=2020-01-26T12:00-06:00&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(4, results.size());

        response = restTemplate.exchange(
                "/subscription/1/findCprDataEvent/fetchEvents?subscription=DE2&timestamp.GTE=2020012&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        response = restTemplate.exchange(
                "/subscription/1/findCprDataEvent/fetchEvents?subscription=DE2&timestamp.GTE=FOOBAR&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        response = restTemplate.exchange(
                "/subscription/1/findCprDataEvent/fetchEvents?subscription=DE2&timestamp.LTE=2020012&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        response = restTemplate.exchange(
                "/subscription/1/findCprDataEvent/fetchEvents?subscription=DE2&timestamp.LTE=FOOBAR&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

    }


    /**
     * Test that it is possible to call a service for fetching events
     */
    @Test
    public void testGetCPRDataWithMetadataEvents() throws IOException, InvalidClientInputException {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("uxp-client", "PITU/GOV/DIA/magenta_services");

        HttpEntity<String> httpEntity = new HttpEntity<String>("", httpHeaders);
        TestUserDetails testUserDetails = new TestUserDetails();
        //testUserDetails.setIdentity("user1");
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        testUserDetails.giveAccess(CvrRolesDefinition.READ_CVR_ROLE);
        this.applyAccess(testUserDetails);


        try (Session session = sessionManager.getSessionFactory().openSession()) {
            PersonRecordQuery query = new PersonRecordQuery();
            query.addParameter(PersonRecordQuery.PERSONNUMMER, "0101011235");
            query.addParameter(PersonRecordQuery.PERSONNUMMER, "0101011236");
            query.addParameter(PersonRecordQuery.PERSONNUMMER, "0101011237");
            query.addParameter(PersonRecordQuery.PERSONNUMMER, "0101011238");
            query.addParameter(PersonRecordQuery.PERSONNUMMER, "0101011239");
            query.addParameter(PersonRecordQuery.PERSONNUMMER, "0101011240");
            query.addParameter(PersonRecordQuery.PERSONNUMMER, "0101011241");
            query.addParameter(PersonRecordQuery.PERSONNUMMER, "0101011242");
            query.addParameter(PersonRecordQuery.PERSONNUMMER, "0101011243");
            List<PersonEntity> entities = QueryManager.getAllEntities(session, query, PersonEntity.class);
            System.out.println(entities);
        }


        ResponseEntity<String> response = restTemplate.exchange(
                "/subscription/1/findCprDataEvent/fetchEvents?subscription=DE4&timestamp.GTE=2010-11-26T12:00-06:00&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ObjectNode responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        JsonNode results = responseContent.get("results");

        Assert.assertEquals(6, results.size());

        response = restTemplate.exchange(
                "/subscription/1/findCprDataEvent/fetchEvents?subscription=DE4&includeMeta=true&timestamp.GTE=2010-11-26T12:00-06:00&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");

        Assert.assertEquals(3, results.size());
        List pnrs = Arrays.asList("0101011238", "0101011237", "0101011240");
        Assert.assertTrue(pnrs.contains(results.get(0).get("pnr").asText()));
        Assert.assertTrue(pnrs.contains(results.get(1).get("pnr").asText()));
        Assert.assertTrue(pnrs.contains(results.get(2).get("pnr").asText()));


        response = restTemplate.exchange(
                "/subscription/1/findCprDataEvent/fetchEvents?subscription=DE5&includeMeta=true&timestamp.GTE=2010-11-26T12:00-06:00&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");

        Assert.assertEquals(5, results.size());
        pnrs = Arrays.asList("0101011239", "0101011238", "0101011237", "0101011236", "0101011235");
        Assert.assertTrue(pnrs.contains(results.get(0).get("pnr").asText()));
        Assert.assertTrue(pnrs.contains(results.get(1).get("pnr").asText()));
        Assert.assertTrue(pnrs.contains(results.get(2).get("pnr").asText()));

        response = restTemplate.exchange(
                "/subscription/1/findCprDataEvent/fetchEvents?subscription=DE5&includeMeta=true&timestamp.GTE=2010112&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        response = restTemplate.exchange(
                "/subscription/1/findCprDataEvent/fetchEvents?subscription=DE5&includeMeta=true&timestamp.GTE=FOOBAR&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        response = restTemplate.exchange(
                "/subscription/1/findCprDataEvent/fetchEvents?subscription=DE5&includeMeta=true&timestamp.LTE=2010112&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        response = restTemplate.exchange(
                "/subscription/1/findCprDataEvent/fetchEvents?subscription=DE5&includeMeta=true&timestamp.LTE=FOOBAR&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

    }

    /**
     * Test that it is possible to call a service for fetching events
     */
    @Test
    public void testGetCVRDataAddressChangeEvents() throws IOException, InterruptedException {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("uxp-client", "PITU/GOV/DIA/magenta_services");

        HttpEntity<String> httpEntity = new HttpEntity<String>("", httpHeaders);
        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        testUserDetails.giveAccess(CvrRolesDefinition.READ_CVR_ROLE);
        testUserDetails.setIdentity("PITU/GOV/DIA/magenta_services");
        this.applyAccess(testUserDetails);

        ResponseEntity<String> response;
        ObjectNode responseContent;
        JsonNode results;

        response = restTemplate.exchange(
                "/subscription/1/findCvrDataEvent/fetchEvents?subscription=DE3&timestamp.GTE=1980-11-26T12:00-06:00&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(1, results.size());

        response = restTemplate.exchange(
                "/subscription/1/findCvrDataEvent/fetchEvents?subscription=DE3&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(1, results.size());

        response = restTemplate.exchange(
                "/subscription/1/findCvrDataEvent/fetchEvents?subscription=DE3&timestamp.GTE=2010-01-20T12:00-06:00&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(1, results.size());

        response = restTemplate.exchange(
                "/subscription/1/findCvrDataEvent/fetchEvents?subscription=DE3&timestamp.GTE=2020-10-26T12:00-06:00&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(0, results.size());

        response = restTemplate.exchange(
                "/subscription/1/findCvrDataEvent/fetchEvents?subscription=DE6&includeMeta=true&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(1, results.size());


        response = restTemplate.exchange(
                "/subscription/1/findCvrDataEvent/fetchEvents?subscription=DE3&timestamp.GTE=2020102&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        response = restTemplate.exchange(
                "/subscription/1/findCvrDataEvent/fetchEvents?subscription=DE3&timestamp.GTE=FOOBAR&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        response = restTemplate.exchange(
                "/subscription/1/findCvrDataEvent/fetchEvents?subscription=DE3&timestamp.LTE=2020102&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        response = restTemplate.exchange(
                "/subscription/1/findCvrDataEvent/fetchEvents?subscription=DE3&timestamp.LTE=FOOBAR&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());


    }

    private HashMap<Integer, JsonNode> loadCompany(String resource) throws IOException, DataFordelerException {
        InputStream input = FetchEventsTest.class.getResourceAsStream(resource);
        if (input == null) {
            throw new MissingResourceException("Missing resource \"" + resource + "\"", resource, "key");
        }
        return loadCompany(input, false);
    }

    private HashMap<Integer, JsonNode> loadCompany(InputStream input, boolean linedFile) throws IOException, DataFordelerException {
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();

        HashMap<Integer, JsonNode> companies = new HashMap<>();
        try {
            importMetadata.setSession(session);

            if (linedFile) {
                int lineNumber = 0;
                Scanner lineScanner = new Scanner(input, StandardCharsets.UTF_8).useDelimiter("\n");
                while (lineScanner.hasNext()) {
                    String data = lineScanner.next();

                    JsonNode root = objectMapper.readTree(data);
                    JsonNode itemList = root.get("hits").get("hits");
                    Assert.assertTrue(itemList.isArray());
                    for (JsonNode item : itemList) {
                        String type = item.get("_type").asText();
                        CompanyEntityManager entityManager = (CompanyEntityManager) plugin.getRegisterManager().getEntityManager(schemaMap.get(type));
                        JsonNode companyInputNode = item.get("_source").get("Vrvirksomhed");
                        entityManager.parseData(companyInputNode, importMetadata, session);
                        companies.put(companyInputNode.get("cvrNummer").asInt(), companyInputNode);
                    }
                    lineNumber++;
                    if (lineNumber % 100 == 0) {
                        System.out.println("loaded line " + lineNumber);
                    }
                }
            } else {
                JsonNode root = objectMapper.readTree(input);
                JsonNode itemList = root.get("hits").get("hits");
                Assert.assertTrue(itemList.isArray());
                for (JsonNode item : itemList) {
                    String type = item.get("_type").asText();
                    CompanyEntityManager entityManager = (CompanyEntityManager) plugin.getRegisterManager().getEntityManager(schemaMap.get(type));
                    JsonNode companyInputNode = item.get("_source").get("Vrvirksomhed");
                    entityManager.parseData(companyInputNode, importMetadata, session);
                    companies.put(companyInputNode.get("cvrNummer").asInt(), companyInputNode);
                }
            }
            transaction.commit();
        } finally {
            session.close();
            QueryManager.clearCaches();
            input.close();
        }
        return companies;
    }


}
