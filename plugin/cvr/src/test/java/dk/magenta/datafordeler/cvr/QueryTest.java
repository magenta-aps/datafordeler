package dk.magenta.datafordeler.cvr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.fapi.OutputWrapper;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.cvr.data.company.CompanyEntity;
import dk.magenta.datafordeler.cvr.data.company.CompanyEntityManager;
import dk.magenta.datafordeler.cvr.data.company.CompanyOutputWrapper;
import dk.magenta.datafordeler.cvr.data.company.CompanyQuery;
import dk.magenta.datafordeler.cvr.data.companyunit.CompanyUnitEntity;
import dk.magenta.datafordeler.cvr.data.companyunit.CompanyUnitEntityManager;
import dk.magenta.datafordeler.cvr.data.companyunit.CompanyUnitOutputWrapper;
import dk.magenta.datafordeler.cvr.data.companyunit.CompanyUnitQuery;
import dk.magenta.datafordeler.cvr.data.participant.ParticipantEntity;
import dk.magenta.datafordeler.cvr.data.participant.ParticipantEntityManager;
import dk.magenta.datafordeler.cvr.data.participant.ParticipantOutputWrapper;
import dk.magenta.datafordeler.cvr.data.participant.ParticipantQuery;
import org.hibernate.Session;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.*;

import static org.mockito.Mockito.when;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class QueryTest {

    @Autowired
    private CvrPlugin plugin;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private CompanyEntityManager companyEntityManager;

    @Autowired
    private CompanyUnitEntityManager companyUnitEntityManager;

    @Autowired
    private ParticipantEntityManager participantEntityManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestRestTemplate restTemplate;

    @SpyBean
    private DafoUserManager dafoUserManager;

    private CompanyOutputWrapper companyOutputWrapper = new CompanyOutputWrapper();
    private CompanyUnitOutputWrapper companyUnitOutputWrapper = new CompanyUnitOutputWrapper();
    private ParticipantOutputWrapper participantOutputWrapper = new ParticipantOutputWrapper();

    private void loadCompany(ImportMetadata importMetadata) throws IOException, DataFordelerException {
        boolean inTransaction = importMetadata.isTransactionInProgress();
        if (!inTransaction) {
            importMetadata.getSession().beginTransaction();
            importMetadata.setTransactionInProgress(true);
        }
        InputStream testData = QueryTest.class.getResourceAsStream("/company_in.json");
        JsonNode root = objectMapper.readTree(testData);
        JsonNode itemList = root.get("hits").get("hits");
        Assert.assertTrue(itemList.isArray());
        for (JsonNode item : itemList) {
            companyEntityManager.parseData(item.get("_source").get("Vrvirksomhed"), importMetadata, importMetadata.getSession());
        }
        if (!inTransaction) {
            importMetadata.getSession().getTransaction().commit();
            importMetadata.setTransactionInProgress(false);
        }
    }

    private void unloadCompany(ImportMetadata importMetadata) {
        boolean inTransaction = importMetadata.isTransactionInProgress();
        if (!inTransaction) {
            importMetadata.getSession().beginTransaction();
            importMetadata.setTransactionInProgress(true);
        }
        List<CompanyEntity> units = QueryManager.getAllEntities(importMetadata.getSession(), CompanyEntity.class);
        for (CompanyEntity unit : units) {
            importMetadata.getSession().delete(unit);
        }
        if (!inTransaction) {
            importMetadata.getSession().getTransaction().commit();
            importMetadata.setTransactionInProgress(false);
        }
    }

    private void loadUnit(ImportMetadata importMetadata) throws IOException, DataFordelerException {
        boolean inTransaction = importMetadata.isTransactionInProgress();
        if (!inTransaction) {
            importMetadata.getSession().beginTransaction();
            importMetadata.setTransactionInProgress(true);
        }
        InputStream testData = QueryTest.class.getResourceAsStream("/unit.json");
        JsonNode root = objectMapper.readTree(testData);
        JsonNode itemList = root.get("hits").get("hits");
        Assert.assertTrue(itemList.isArray());
        for (JsonNode item : itemList) {
            companyUnitEntityManager.parseData(item.get("_source").get("VrproduktionsEnhed"), importMetadata, importMetadata.getSession());
        }
        if (!inTransaction) {
            importMetadata.getSession().getTransaction().commit();
            importMetadata.setTransactionInProgress(false);
        }
    }

    private void unloadUnit(ImportMetadata importMetadata) {
        boolean inTransaction = importMetadata.isTransactionInProgress();
        if (!inTransaction) {
            importMetadata.getSession().beginTransaction();
            importMetadata.setTransactionInProgress(true);
        }
        List<CompanyUnitEntity> units = QueryManager.getAllEntities(importMetadata.getSession(), CompanyUnitEntity.class);
        for (CompanyUnitEntity unit : units) {
            importMetadata.getSession().delete(unit);
        }
        if (!inTransaction) {
            importMetadata.getSession().getTransaction().commit();
            importMetadata.setTransactionInProgress(false);
        }
    }

    private void loadParticipant(ImportMetadata importMetadata) throws IOException, DataFordelerException {
        boolean inTransaction = importMetadata.isTransactionInProgress();
        if (!inTransaction) {
            importMetadata.getSession().beginTransaction();
            importMetadata.setTransactionInProgress(true);
        }
        InputStream testData = QueryTest.class.getResourceAsStream("/person.json");
        JsonNode root = objectMapper.readTree(testData);
        JsonNode itemList = root.get("hits").get("hits");
        Assert.assertTrue(itemList.isArray());
        for (JsonNode item : itemList) {
            participantEntityManager.parseData(item.get("_source").get("Vrdeltagerperson"), importMetadata, importMetadata.getSession());
        }
        if (!inTransaction) {
            importMetadata.getSession().getTransaction().commit();
            importMetadata.setTransactionInProgress(false);
        }
    }

    private void unloadParticipant(ImportMetadata importMetadata) {
        boolean inTransaction = importMetadata.isTransactionInProgress();
        if (!inTransaction) {
            importMetadata.getSession().beginTransaction();
            importMetadata.setTransactionInProgress(true);
        }
        List<ParticipantEntity> units = QueryManager.getAllEntities(importMetadata.getSession(), ParticipantEntity.class);
        for (ParticipantEntity unit : units) {
            importMetadata.getSession().delete(unit);
        }
        if (!inTransaction) {
            importMetadata.getSession().getTransaction().commit();
            importMetadata.setTransactionInProgress(false);
        }
    }

    private void applyAccess(TestUserDetails testUserDetails) {
        when(dafoUserManager.getFallbackUser()).thenReturn(testUserDetails);
    }

    private ResponseEntity<String> restSearch(ParameterMap parameters, String type) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        HttpEntity<String> httpEntity = new HttpEntity<String>("", headers);
        return this.restTemplate.exchange("/cvr/"+type+"/1/rest/search?" + parameters.asUrlParams(), HttpMethod.GET, httpEntity, String.class);
    }

    private static HashSet<String> ignoreKeys = new HashSet<String>();
    static {
        ignoreKeys.add("sidstImporteret");
    }
    private static void assertJsonEquality(JsonNode node1, JsonNode node2, boolean ignoreArrayOrdering, boolean printDifference) {
        try {
            Assert.assertEquals(node1.isNull(), node2.isNull());
            Assert.assertEquals(node1.isArray(), node2.isArray());
            Assert.assertEquals(node1.isObject(), node2.isObject());
            Assert.assertEquals(node1.isLong(), node2.isLong());
            Assert.assertEquals(node1.isInt(), node2.isInt());
            Assert.assertEquals(node1.isShort(), node2.isShort());
            Assert.assertEquals(node1.isBoolean(), node2.isBoolean());
            Assert.assertEquals(node1.isTextual(), node2.isTextual());
            if (node1.isArray()) {
                Assert.assertEquals(node1.size(), node2.size());
                if (ignoreArrayOrdering) {
                    for (int i = 0; i < node1.size(); i++) {
                        boolean match = false;
                        for (int j = 0; j < node2.size(); j++) {
                            try {
                                assertJsonEquality(node1.get(i), node2.get(j), true, false);
                                match = true;
                            } catch (AssertionError e) {
                            }
                        }
                        if (!match) {
                            throw new AssertionError();
                        }
                    }
                } else {
                    for (int i = 0; i < node1.size(); i++) {
                        assertJsonEquality(node1.get(i), node2.get(i), false, printDifference);
                    }
                }
            } else if (node1.isObject()) {
                Assert.assertEquals(node1.size(), node2.size());
                Iterator<String> keys = node1.fieldNames();
                while (keys.hasNext()) {
                    String key = keys.next();
                    Assert.assertNotNull(node2.get(key));
                    if (!ignoreKeys.contains(key)) {
                        assertJsonEquality(node1.get(key), node2.get(key), ignoreArrayOrdering, printDifference);
                    }
                }
            } else {
                Assert.assertEquals(node1.asText(), node2.asText());
            }
        } catch (AssertionError e) {
            if (printDifference) {
                System.out.println("\n" + node1 + "\n != \n" + node2 + "\n\n\n");
            }
            throw e;
        }
    }

    private static final String RESTRICTION_KOMMUNE_TEST = "TestMunicipality";
    private void addTestMunicipalityAreaRestriction() {
        plugin.getAreaRestrictionDefinition().getAreaRestrictionTypeByName(
                CvrAreaRestrictionDefinition.RESTRICTIONTYPE_KOMMUNEKODER
        ).addChoice(RESTRICTION_KOMMUNE_TEST, "Testing", null, "101");
    }

    /*
        Company tests
     */

    @Test
    public void testCompanyIdempotence() throws IOException, DataFordelerException {
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = sessionManager.getSessionFactory().openSession();
        importMetadata.setSession(session);
        try {
            loadCompany(importMetadata);

            List<CompanyEntity> entities = QueryManager.getAllEntities(session, CompanyEntity.class);
            JsonNode firstImport = objectMapper.valueToTree(entities);

            loadCompany(importMetadata);


            entities = QueryManager.getAllEntities(session, CompanyEntity.class);
            JsonNode secondImport = objectMapper.valueToTree(entities);

            assertJsonEquality(firstImport, secondImport, true, true);
        } finally {
            QueryManager.clearCaches();
            unloadCompany(importMetadata);
            session.close();
        }
    }


    @Test
    public void testCompanyQuery() throws IOException, DataFordelerException {
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = sessionManager.getSessionFactory().openSession();
        importMetadata.setSession(session);
        try {
            loadCompany(importMetadata);
            UUID expectedUUID = UUID.fromString("2334456b-d2ca-372d-aa60-4a2ba7fed7cd");

            CompanyEntity companyEntity = QueryManager.getEntity(session, expectedUUID, CompanyEntity.class);
            Object wrappedEntity = companyOutputWrapper.wrapResult(companyEntity, new CompanyQuery());
            Assert.assertTrue(wrappedEntity instanceof ObjectNode);
            ObjectNode objectNode = (ObjectNode) wrappedEntity;
            Assert.assertEquals(91, objectNode.get("registreringer").size());

            List<CompanyEntity> entities;
            CompanyQuery query;

            query = new CompanyQuery();
            query.setEmailadresse("info@magenta.dk");
            entities = QueryManager.getAllEntities(session, query, CompanyEntity.class);
            Assert.assertEquals(1, entities.size());
            Assert.assertEquals(expectedUUID, entities.get(0).getUUID());

            query = new CompanyQuery();
            query.addKommuneKode(101);
            entities = QueryManager.getAllEntities(session, query, CompanyEntity.class);
            Assert.assertEquals(1, entities.size());
            Assert.assertEquals(expectedUUID, entities.get(0).getUUID());

            query = new CompanyQuery();
            query.setVirksomhedsform(80);
            entities = QueryManager.getAllEntities(session, query, CompanyEntity.class);
            Assert.assertEquals(1, entities.size());
            Assert.assertEquals(expectedUUID, entities.get(0).getUUID());

            query = new CompanyQuery();
            query.setCvrNumre("25052943");
            entities = QueryManager.getAllEntities(session, query, CompanyEntity.class);
            Assert.assertEquals(1, entities.size());
            Assert.assertEquals(expectedUUID, entities.get(0).getUUID());

            query = new CompanyQuery();
            query.setTelefonnummer("33369696");
            entities = QueryManager.getAllEntities(session, query, CompanyEntity.class);
            Assert.assertEquals(1, entities.size());
            Assert.assertEquals(expectedUUID, entities.get(0).getUUID());

            query = new CompanyQuery();
            query.setVirksomhedsnavn("MAGENTA ApS");
            entities = QueryManager.getAllEntities(session, query, CompanyEntity.class);
            Assert.assertEquals(1, entities.size());
            Assert.assertEquals(expectedUUID, entities.get(0).getUUID());

            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(companyOutputWrapper.wrapResult(entities.get(0), new CompanyQuery())));

        } finally {
            QueryManager.clearCaches();
            unloadCompany(importMetadata);
            session.close();
        }
    }


    @Test
    public void testCompanyRestQuery() throws IOException, DataFordelerException {
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = sessionManager.getSessionFactory().openSession();
        importMetadata.setSession(session);
        try {
            loadCompany(importMetadata);

            TestUserDetails testUserDetails = new TestUserDetails();
            testUserDetails.giveAccess(CvrRolesDefinition.READ_CVR_ROLE);
            this.applyAccess(testUserDetails);

            ParameterMap searchParameters = new ParameterMap();
            searchParameters.add("cvrnummer", "25052943");
            searchParameters.add("registrationFrom", "1999-11-30");
            searchParameters.add("registrationTo", "1999-12-01");


            ResponseEntity<String> response = restSearch(searchParameters, "company");
            Assert.assertEquals(200, response.getStatusCode().value());
            JsonNode jsonBody = objectMapper.readTree(response.getBody());

            ObjectNode entity = (ObjectNode) jsonBody.get("results").get(0);
            Assert.assertEquals(25052943, entity.get("cvrnummer").asInt());
            Assert.assertEquals(CompanyEntity.generateUUID(25052943).toString(), entity.get("UUID").asText());
            ArrayNode registrations = (ArrayNode) entity.get("registreringer");
            Assert.assertEquals(1, registrations.size());
            ObjectNode registration = (ObjectNode) registrations.get(0);
            Assert.assertTrue(OffsetDateTime.parse("1999-11-29T16:37:00+01:00").isEqual(OffsetDateTime.parse(registration.get("registreringFra").textValue())));
            Assert.assertTrue(OffsetDateTime.parse("2000-02-29T11:44:00+01:00").isEqual(OffsetDateTime.parse(registration.get("registreringTil").textValue())));

            JsonNode formNodeList = registration.get("virksomhedsform");
            Assert.assertEquals(1, formNodeList.size());
            JsonNode formNode = formNodeList.get(0);
            Assert.assertTrue(OffsetDateTime.parse("1999-11-15T01:00+01:00").isEqual(OffsetDateTime.parse(formNode.get("virkningFra").asText())));
            Assert.assertTrue(formNode.get("virkningTil").isNull());
            Assert.assertEquals("80", formNode.get("formkode").asText());
            Assert.assertEquals("E&S", formNode.get("dataleverandør").asText());


            JsonNode lifecycleNodeList = registration.get("livscyklus");
            Assert.assertEquals(1, formNodeList.size());
            JsonNode lifecycleNode = lifecycleNodeList.get(0);
            Assert.assertTrue(OffsetDateTime.parse("1999-11-15T01:00+01:00").isEqual(OffsetDateTime.parse(lifecycleNode.get("virkningFra").asText())));
            Assert.assertTrue(lifecycleNode.get("virkningTil").isNull());
            Assert.assertEquals("1999-11-15", lifecycleNode.get("startDato").asText());


            JsonNode unitNodeList = registration.get("produktionsenheder");
            Assert.assertEquals(1, unitNodeList.size());
            JsonNode unitNode = unitNodeList.get(0);
            Assert.assertTrue(OffsetDateTime.parse("1999-11-15T01:00+01:00").isEqual(OffsetDateTime.parse(unitNode.get("virkningFra").asText())));
            Assert.assertTrue(unitNode.get("virkningTil").isNull());
            Assert.assertEquals(1007256945L, unitNode.get("pnummer").asLong());


            // Retrieve what was registered at 2015-01-01, with effect 2015-01-01
            searchParameters = new ParameterMap();
            searchParameters.add("cvrnummer", "25052943");
            searchParameters.add("registrationFrom", "2015-01-01");
            searchParameters.add("registrationTo", "2015-01-01");
            searchParameters.add("effectFrom", "2015-01-01");
            searchParameters.add("effectTo", "2015-01-01");

            response = restSearch(searchParameters, "company");
            Assert.assertEquals(200, response.getStatusCode().value());
            jsonBody = objectMapper.readTree(response.getBody());

            entity = (ObjectNode) jsonBody.get("results").get(0);
            registrations = (ArrayNode) entity.get("registreringer");
            Assert.assertEquals(1, registrations.size());
            registration = (ObjectNode) registrations.get(0);
            Assert.assertNotNull(registration.get("deltagerRelationer"));
            Assert.assertNotNull(registration.get("livscyklus"));
            Assert.assertNotNull(registration.get("virksomhedsform"));
            Assert.assertNotNull(registration.get("produktionsenheder"));
            Assert.assertNotNull(registration.get("telefon"));
            Assert.assertNotNull(registration.get("navn"));
            Assert.assertNull(registration.get("email"));
            Assert.assertNull(registration.get("hovedbranche"));
            Assert.assertNull(registration.get("beliggenhedsadresse"));
        } finally {
            QueryManager.clearCaches();
            unloadCompany(importMetadata);
            session.close();
        }
    }


    @Test
    public void testCompanyAccess() throws Exception {
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = sessionManager.getSessionFactory().openSession();
        importMetadata.setSession(session);
        try {
            loadCompany(importMetadata);

            addTestMunicipalityAreaRestriction();
            TestUserDetails testUserDetails = new TestUserDetails();

            ParameterMap searchParameters = new ParameterMap();
            ResponseEntity<String> response;// = restSearch(searchParameters, "company");
            //Assert.assertEquals(403, response.getStatusCode().value());

            testUserDetails.giveAccess(CvrRolesDefinition.READ_CVR_ROLE);
            this.applyAccess(testUserDetails);

            searchParameters.add("kommunekode", "101");
            response = restSearch(searchParameters, "company");
            Assert.assertEquals(200, response.getStatusCode().value());
            JsonNode jsonBody = objectMapper.readTree(response.getBody());
            JsonNode results = jsonBody.get("results");

            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(results));

            Assert.assertTrue(results.isArray());
            Assert.assertEquals(1, results.size());
            Assert.assertEquals(CompanyEntity.generateUUID(25052943).toString(), results.get(0).get("UUID").asText());

            searchParameters = new ParameterMap();
            searchParameters.add("navn", "MAGENTA ApS");
            response = restSearch(searchParameters, "company");
            Assert.assertEquals(200, response.getStatusCode().value());
            jsonBody = objectMapper.readTree(response.getBody());
            results = jsonBody.get("results");
            Assert.assertTrue(results.isArray());
            Assert.assertEquals(1, results.size());
            Assert.assertEquals(CompanyEntity.generateUUID(25052943).toString(), results.get(0).get("UUID").asText());

            testUserDetails.giveAccess(
                    plugin.getAreaRestrictionDefinition().getAreaRestrictionTypeByName(
                            CvrAreaRestrictionDefinition.RESTRICTIONTYPE_KOMMUNEKODER
                    ).getRestriction(
                            CvrAreaRestrictionDefinition.RESTRICTION_KOMMUNE_SERMERSOOQ
                    )
            );
            this.applyAccess(testUserDetails);

            searchParameters.add("kommunekode", "10*");
            response = restSearch(searchParameters, "company");
            Assert.assertEquals(200, response.getStatusCode().value());
            jsonBody = objectMapper.readTree(response.getBody());
            results = jsonBody.get("results");
            Assert.assertTrue(results.isArray());
            Assert.assertEquals(0, results.size());


            testUserDetails.giveAccess(
                    plugin.getAreaRestrictionDefinition().getAreaRestrictionTypeByName(
                            CvrAreaRestrictionDefinition.RESTRICTIONTYPE_KOMMUNEKODER
                    ).getRestriction(
                            RESTRICTION_KOMMUNE_TEST
                    )
            );
            this.applyAccess(testUserDetails);

            response = restSearch(searchParameters, "company");
            Assert.assertEquals(200, response.getStatusCode().value());
            jsonBody = objectMapper.readTree(response.getBody());
            results = jsonBody.get("results");
            Assert.assertTrue(results.isArray());


            Assert.assertEquals(1, results.size());
            Assert.assertEquals(CompanyEntity.generateUUID(25052943).toString(), results.get(0).get("UUID").asText());
        } finally {
            QueryManager.clearCaches();
            unloadCompany(importMetadata);
            session.close();
        }
    }



    /*
        CompanyUnit tests
     */

    public void testCompanyUnitIdempotence() throws Exception {
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = sessionManager.getSessionFactory().openSession();
        importMetadata.setSession(session);
        try {
            loadUnit(importMetadata);
            List<CompanyUnitEntity> entities = QueryManager.getAllEntities(session, CompanyUnitEntity.class);
            Assert.assertFalse(entities.isEmpty());
            JsonNode firstImport = objectMapper.valueToTree(entities);

            loadUnit(importMetadata);
            entities = QueryManager.getAllEntities(session, CompanyUnitEntity.class);
            JsonNode secondImport = objectMapper.valueToTree(entities);

            assertJsonEquality(firstImport, secondImport, true, true);
        } finally {
            unloadUnit(importMetadata);
            session.close();
        }
    }


    @Test
    public void testCompanyUnitQuery() throws Exception {
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = sessionManager.getSessionFactory().openSession();
        importMetadata.setSession(session);
        try {
            loadUnit(importMetadata);
            importMetadata.setTransactionInProgress(false);

            UUID expectedUUID = UUID.fromString("92ce7d3e-b261-31d6-a401-df408167dd1b");

            CompanyUnitEntity companyUnitEntity = QueryManager.getEntity(session, expectedUUID, CompanyUnitEntity.class);
            Object wrapped = companyUnitOutputWrapper.wrapResult(companyUnitEntity, new CompanyQuery());
            Assert.assertTrue(wrapped instanceof ObjectNode);
            ObjectNode objectNode = (ObjectNode) wrapped;
            Assert.assertEquals(1, objectNode.get("registreringer").size());

            CompanyUnitQuery query = new CompanyUnitQuery();
            query.setPrimaryIndustry("855900");
            List<CompanyUnitEntity> entities = QueryManager.getAllEntities(session, query, CompanyUnitEntity.class);

            Assert.assertEquals(1, entities.size());
            Assert.assertEquals(expectedUUID, entities.get(0).getUUID());

            query = new CompanyUnitQuery();
            query.addKommuneKode(101);
            entities = QueryManager.getAllEntities(session, query, CompanyUnitEntity.class);
            Assert.assertEquals(5, entities.size());
            List<UUID> expected = Arrays.asList(UUID.fromString("cd834835-384b-3026-8fd8-ec24095aa446"),
                    UUID.fromString("ebebf16f-11a8-3276-903b-8d3b1179722b"),
                    UUID.fromString("418c6bf2-e4fe-31d4-bf05-4d2c1e6c0380"),
                    UUID.fromString("7aa1f12b-315a-316f-9e08-f8724d7a09d9"),
                    UUID.fromString("c2a6a3da-c46e-3689-a39a-9727a94bb5c5"));
            for (CompanyUnitEntity e : entities) {
                Assert.assertTrue(expected.contains(e.getUUID()));
            }

            query = new CompanyUnitQuery();
            query.setAssociatedCompanyCvrNummer("36238208");
            entities = QueryManager.getAllEntities(session, query, CompanyUnitEntity.class);
            Assert.assertEquals(1, entities.size());

        } finally {
            unloadUnit(importMetadata);
            session.close();
        }
    }

    @Test
    public void testCompanyUnitAccess() throws Exception {
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = sessionManager.getSessionFactory().openSession();
        importMetadata.setSession(session);
        try {
            loadUnit(importMetadata);

            addTestMunicipalityAreaRestriction();
            TestUserDetails testUserDetails = new TestUserDetails();

            ParameterMap searchParameters = new ParameterMap();
            ResponseEntity<String> response;

            testUserDetails.giveAccess(CvrRolesDefinition.READ_CVR_ROLE);
            this.applyAccess(testUserDetails);

            searchParameters.add("kommunekode", "101");
            response = restSearch(searchParameters, "companyunit");
            Assert.assertEquals(200, response.getStatusCode().value());
            JsonNode jsonBody = objectMapper.readTree(response.getBody());

            JsonNode results = jsonBody.get("results");

            //System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonBody));

            Assert.assertTrue(results.isArray());
            Assert.assertEquals(5, results.size());
            boolean found = false;
            String expected = CompanyUnitEntity.generateUUID(1010255879).toString();
            for (JsonNode j : results) {
                if (expected.equals(j.get("UUID").asText())) {
                    found = true;
                }
            }
            Assert.assertTrue(found);

            testUserDetails.giveAccess(
                    plugin.getAreaRestrictionDefinition().getAreaRestrictionTypeByName(
                            CvrAreaRestrictionDefinition.RESTRICTIONTYPE_KOMMUNEKODER
                    ).getRestriction(
                            CvrAreaRestrictionDefinition.RESTRICTION_KOMMUNE_SERMERSOOQ
                    )
            );
            this.applyAccess(testUserDetails);

            searchParameters = new ParameterMap();
            searchParameters.add("kommunekode", "10*");
            response = restSearch(searchParameters, "companyunit");
            Assert.assertEquals(200, response.getStatusCode().value());
            jsonBody = objectMapper.readTree(response.getBody());
            results = jsonBody.get("results");
            Assert.assertTrue(results.isArray());
            Assert.assertEquals(0, results.size());


            testUserDetails.giveAccess(
                    plugin.getAreaRestrictionDefinition().getAreaRestrictionTypeByName(
                            CvrAreaRestrictionDefinition.RESTRICTIONTYPE_KOMMUNEKODER
                    ).getRestriction(
                            RESTRICTION_KOMMUNE_TEST
                    )
            );
            this.applyAccess(testUserDetails);

            response = restSearch(searchParameters, "companyunit");
            Assert.assertEquals(200, response.getStatusCode().value());
            jsonBody = objectMapper.readTree(response.getBody());
            results = jsonBody.get("results");
            Assert.assertTrue(results.isArray());


            Assert.assertEquals(5, results.size());
            expected = CompanyUnitEntity.generateUUID(1010255879).toString();
            for (JsonNode j : results) {
                if (expected.equals(j.get("UUID").asText())) {
                    found = true;
                }
            }
            Assert.assertTrue(found);
        } finally {
            unloadUnit(importMetadata);
            session.close();
        }
    }

    /*
        Participant tests
     */

    public void testParticipantIdempotence() throws IOException, DataFordelerException {
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = sessionManager.getSessionFactory().openSession();
        importMetadata.setSession(session);
        try {
            loadParticipant(importMetadata);

            List<ParticipantEntity> entities = QueryManager.getAllEntities(session, ParticipantEntity.class);
            JsonNode firstImport = objectMapper.valueToTree(entities);

            loadParticipant(importMetadata);
            entities = QueryManager.getAllEntities(session, ParticipantEntity.class);
            JsonNode secondImport = objectMapper.valueToTree(entities);

            assertJsonEquality(firstImport, secondImport, true, true);
        } finally {
            unloadParticipant(importMetadata);
            session.close();
        }
    }

    @Test
    public void testParticipantQuery() throws IOException, DataFordelerException {
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = sessionManager.getSessionFactory().openSession();
        importMetadata.setSession(session);
        try {
            loadParticipant(importMetadata);

            ParticipantQuery query = new ParticipantQuery();
            query.setNavn("Morten Kjærsgaard");

            List<ParticipantEntity> entities = QueryManager.getAllEntities(session, query, ParticipantEntity.class);
            List<Object> wrapped = participantOutputWrapper.wrapResults(entities, query, OutputWrapper.Mode.LEGACY);

            Assert.assertEquals(1, wrapped.size());
            Assert.assertTrue(wrapped.get(0) instanceof ObjectNode);
            ObjectNode objectNode = (ObjectNode) wrapped.get(0);
            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectNode));
            Assert.assertEquals(4, objectNode.get("registreringer").size());

            String firstImport = objectMapper.writeValueAsString(wrapped);

            loadParticipant(importMetadata);
            entities = QueryManager.getAllEntities(session, query, ParticipantEntity.class);
            wrapped = participantOutputWrapper.wrapResults(entities, query, OutputWrapper.Mode.LEGACY);
            String secondImport = objectMapper.writeValueAsString(wrapped);

            assertJsonEquality(objectMapper.readTree(firstImport), objectMapper.readTree(secondImport), true, true);

            query = new ParticipantQuery();
            query.addKommuneKode(101);
            entities = QueryManager.getAllEntities(session, query, ParticipantEntity.class);
            Assert.assertEquals(1, entities.size());

            query = new ParticipantQuery();
            query.setKommuneKode("*");
            entities = QueryManager.getAllEntities(session, query, ParticipantEntity.class);
            Assert.assertEquals(1, entities.size());

        } finally {
            unloadParticipant(importMetadata);
            session.close();
        }
    }


    @Test
    public void testParticipantAccess() throws Exception {
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = sessionManager.getSessionFactory().openSession();
        importMetadata.setSession(session);
        try {
            loadParticipant(importMetadata);

            addTestMunicipalityAreaRestriction();
            TestUserDetails testUserDetails = new TestUserDetails();

            ParameterMap searchParameters = new ParameterMap();
            ResponseEntity<String> response;

            testUserDetails.giveAccess(CvrRolesDefinition.READ_CVR_ROLE);
            this.applyAccess(testUserDetails);

            searchParameters.add("kommunekode", "101");
            response = restSearch(searchParameters, "participant");
            Assert.assertEquals(200, response.getStatusCode().value());
            JsonNode jsonBody = objectMapper.readTree(response.getBody());
            JsonNode results = jsonBody.get("results");
            Assert.assertTrue(results.isArray());
            Assert.assertEquals(1, results.size());
            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(results.get(0)));
            Assert.assertEquals(ParticipantEntity.generateUUID("PERSON", 4000004988L).toString(), results.get(0).get("UUID").asText());

            testUserDetails.giveAccess(
                    plugin.getAreaRestrictionDefinition().getAreaRestrictionTypeByName(
                            CvrAreaRestrictionDefinition.RESTRICTIONTYPE_KOMMUNEKODER
                    ).getRestriction(
                            CvrAreaRestrictionDefinition.RESTRICTION_KOMMUNE_SERMERSOOQ
                    )
            );
            this.applyAccess(testUserDetails);

            searchParameters = new ParameterMap();
            searchParameters.add("kommunekode", "10*");
            response = restSearch(searchParameters, "participant");
            Assert.assertEquals(200, response.getStatusCode().value());
            jsonBody = objectMapper.readTree(response.getBody());
            results = jsonBody.get("results");
            Assert.assertTrue(results.isArray());
            Assert.assertEquals(0, results.size());


            testUserDetails.giveAccess(
                    plugin.getAreaRestrictionDefinition().getAreaRestrictionTypeByName(
                            CvrAreaRestrictionDefinition.RESTRICTIONTYPE_KOMMUNEKODER
                    ).getRestriction(
                            RESTRICTION_KOMMUNE_TEST
                    )
            );
            this.applyAccess(testUserDetails);

            response = restSearch(searchParameters, "participant");
            Assert.assertEquals(200, response.getStatusCode().value());
            jsonBody = objectMapper.readTree(response.getBody());
            results = jsonBody.get("results");
            Assert.assertTrue(results.isArray());


            Assert.assertEquals(1, results.size());
            Assert.assertEquals(ParticipantEntity.generateUUID("PERSON", 4000004988L).toString(), results.get(0).get("UUID").asText());
        } finally {
            unloadParticipant(importMetadata);
        }
    }

}
