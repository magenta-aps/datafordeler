package dk.magenta.datafordeler.cvr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.PluginManager;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.cvr.access.CvrRolesDefinition;
import dk.magenta.datafordeler.cvr.entitymanager.CompanyEntityManager;
import dk.magenta.datafordeler.cvr.entitymanager.CompanyUnitEntityManager;
import dk.magenta.datafordeler.cvr.entitymanager.ParticipantEntityManager;
import dk.magenta.datafordeler.cvr.output.CompanyRecordOutputWrapper;
import dk.magenta.datafordeler.cvr.query.CompanyRecordQuery;
import dk.magenta.datafordeler.cvr.query.CompanyUnitRecordQuery;
import dk.magenta.datafordeler.cvr.query.ParticipantRecordQuery;
import dk.magenta.datafordeler.cvr.records.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RecordTest extends TestBase {

    @Autowired
    private ObjectMapper objectMapper;

    private ObjectMapper getObjectMapper() {
        return this.objectMapper.setFilterProvider(
                new SimpleFilterProvider().addFilter(
                        "ParticipantRecordFilter",
                        SimpleBeanPropertyFilter.serializeAllExcept(ParticipantRecord.IO_FIELD_BUSINESS_KEY)
                )
        );
    }

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private CvrPlugin plugin;

    @Autowired
    private TestRestTemplate restTemplate;

    private static final HashMap<String, String> schemaMap = new HashMap<>();

    static {
        schemaMap.put("_doc", CompanyRecord.schema);
        schemaMap.put("produktionsenhed", CompanyUnitRecord.schema);
        schemaMap.put("deltager", ParticipantRecord.schema);
    }

    @SpyBean
    private DafoUserManager dafoUserManager;

    private void applyAccess(TestUserDetails testUserDetails) {
        when(dafoUserManager.getFallbackUser()).thenReturn(testUserDetails);
    }

    private void whitelistLocalhost() {
        when(dafoUserManager.getIpWhitelist()).thenReturn(Collections.singleton("127.0.0.1"));
    }

    private HashMap<Integer, JsonNode> loadCompany() throws IOException, DataFordelerException {
        return loadCompany("/company_in.json");
    }

    private HashMap<Integer, JsonNode> loadCompany(String resource) throws IOException, DataFordelerException {
        InputStream input = RecordTest.class.getResourceAsStream(resource);
        if (input == null) {
            throw new MissingResourceException("Missing resource \"" + resource + "\"", resource, "key");
        }
        return loadCompany(input, false);
    }

    private HashMap<Integer, JsonNode> loadCompany(InputStream input, boolean linedFile) throws IOException, DataFordelerException {
        ImportMetadata importMetadata = new ImportMetadata();
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            ObjectMapper objectMapper = this.getObjectMapper();

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
                QueryManager.clearCaches();
                input.close();
            }
            return companies;
        }
    }

    @Test
    public void testCompany() throws DataFordelerException, IOException {
        this.loadCompany();
        this.loadCompany();
        ObjectMapper objectMapper = this.getObjectMapper();
        HashMap<Integer, JsonNode> companies = this.loadCompany();
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            for (int cvrNumber : companies.keySet()) {
                HashMap<String, Object> filter = new HashMap<>();
                filter.put("cvrNumber", cvrNumber);
                CompanyRecord companyRecord = QueryManager.getItem(session, CompanyRecord.class, filter);
                Assert.assertNotNull("Didn't find cvr number " + cvrNumber, companyRecord);
                compareJson(companies.get(cvrNumber), objectMapper.valueToTree(companyRecord), Collections.singletonList("root"));
            }

            CompanyRecordQuery query = new CompanyRecordQuery();
            OffsetDateTime time = OffsetDateTime.now();
            query.setRegistrationToAfter(time);
            query.setEffectFromBefore(time);
            query.setEffectToAfter(time);
            query.applyFilters(session);

            query.setParameter(CompanyRecordQuery.KOMMUNEKODE, "101");
            Assert.assertEquals(1, QueryManager.getAllEntities(session, query, CompanyRecord.class).size());
            query.clearParameter(CompanyRecordQuery.KOMMUNEKODE);
            query.setParameter(CompanyRecordQuery.TELEFONNUMMER, "33369696");
            Assert.assertEquals(1, QueryManager.getAllEntities(session, query, CompanyRecord.class).size());
            query.clearParameter(CompanyRecordQuery.TELEFONNUMMER);
            query.setParameter(CompanyRecordQuery.EMAILADRESSE, "info@magenta.dk");
            Assert.assertEquals(1, QueryManager.getAllEntities(session, query, CompanyRecord.class).size());
            query.clearParameter(CompanyRecordQuery.EMAILADRESSE);
            query.setParameter(CompanyRecordQuery.REKLAMEBESKYTTELSE, "true");
            Assert.assertEquals(1, QueryManager.getAllEntities(session, query, CompanyRecord.class).size());
            query.clearParameter(CompanyRecordQuery.REKLAMEBESKYTTELSE);
            query.setParameter(CompanyRecordQuery.VIRKSOMHEDSFORM, "80");
            Assert.assertEquals(1, QueryManager.getAllEntities(session, query, CompanyRecord.class).size());
            query.clearParameter(CompanyRecordQuery.VIRKSOMHEDSFORM);
            query.setParameter(CompanyRecordQuery.NAVN, "MAGENTA ApS");
            Assert.assertEquals(1, QueryManager.getAllEntities(session, query, CompanyRecord.class).size());

            query.clearParameter(CompanyRecordQuery.KOMMUNEKODE);

            time = OffsetDateTime.parse("1998-01-01T00:00:00Z");
            query.setRegistrationToAfter(time);
            query.setEffectFromBefore(time);
            query.setEffectToAfter(time);
            query.applyFilters(session);

            query.setParameter(CompanyRecordQuery.KOMMUNEKODE, "101");
            Assert.assertEquals(0, QueryManager.getAllEntities(session, query, CompanyRecord.class).size());
            query.clearParameter(CompanyRecordQuery.KOMMUNEKODE);
            query.setParameter(CompanyRecordQuery.TELEFONNUMMER, "33369696");
            Assert.assertEquals(0, QueryManager.getAllEntities(session, query, CompanyRecord.class).size());
            query.clearParameter(CompanyRecordQuery.TELEFONNUMMER);
            query.setParameter(CompanyRecordQuery.EMAILADRESSE, "info@magenta.dk");
            Assert.assertEquals(0, QueryManager.getAllEntities(session, query, CompanyRecord.class).size());
            query.clearParameter(CompanyRecordQuery.EMAILADRESSE);
            query.setParameter(CompanyRecordQuery.REKLAMEBESKYTTELSE, "true");
            Assert.assertEquals(0, QueryManager.getAllEntities(session, query, CompanyRecord.class).size());
            query.setParameter(CompanyRecordQuery.REKLAMEBESKYTTELSE, "true");
            query.setParameter(CompanyRecordQuery.VIRKSOMHEDSFORM, "80");
            Assert.assertEquals(0, QueryManager.getAllEntities(session, query, CompanyRecord.class).size());
            query.clearParameter(CompanyRecordQuery.VIRKSOMHEDSFORM);
            query.setParameter(CompanyRecordQuery.NAVN, "MAGENTA ApS");
            Assert.assertEquals(0, QueryManager.getAllEntities(session, query, CompanyRecord.class).size());
            query.clearParameter(CompanyRecordQuery.NAVN);
        }
    }

    @Test
    public void testUpdateCompany() throws IOException, DataFordelerException {
        System.out.println("testUpdateCompany");
        this.cleanup();
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            //Assert.assertEquals(0, QueryManager.getAllEntities(session, CompanyRecord.class).size());
            Class[] inspect = new Class[]{CompanyRecord.class, CompanyUnitRecord.class, ParticipantRecord.class};
            for (Class c : inspect) {
                System.out.println(c.getSimpleName() + ": " + QueryManager.getAllEntities(session, c).size());
            }
        }

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            this.cleanup();
            //Assert.assertEquals(0, QueryManager.getAllEntities(session, CompanyRecord.class).size());
            Class[] inspect = new Class[]{CompanyRecord.class, CompanyUnitRecord.class, ParticipantRecord.class};
            for (Class c : inspect) {
                System.out.println(c.getSimpleName() + ": " + QueryManager.getAllEntities(session, c).size());
            }
        }

        loadCompany("/company_in.json");
        loadCompany("/company_in2.json");
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            CompanyRecordQuery query = new CompanyRecordQuery();
            query.setParameter(CompanyRecordQuery.CVRNUMMER, "25052943");
            List<CompanyRecord> records = QueryManager.getAllEntities(session, query, CompanyRecord.class);
            CompanyRecord companyRecord = records.get(0);

            Assert.assertEquals(3, companyRecord.getNames().size());
            Assert.assertEquals(1, companyRecord.getSecondaryNames().size());
            Assert.assertEquals(2, companyRecord.getPostalAddress().size());
            Assert.assertEquals(5, companyRecord.getLocationAddress().size());
            Assert.assertEquals(2, companyRecord.getPhoneNumber().size());
            Assert.assertEquals(0, companyRecord.getFaxNumber().size());
            Assert.assertEquals(2, companyRecord.getEmailAddress().size());
            Assert.assertEquals(2, companyRecord.getLifecycle().size());
            Assert.assertEquals(5, companyRecord.getPrimaryIndustry().size());
            Assert.assertEquals(1, companyRecord.getSecondaryIndustry1().size());
            Assert.assertEquals(0, companyRecord.getSecondaryIndustry2().size());
            Assert.assertEquals(0, companyRecord.getSecondaryIndustry3().size());
            Assert.assertEquals(0, companyRecord.getStatus().size());
            Assert.assertEquals(2, companyRecord.getCompanyStatus().size());
            Assert.assertEquals(16, companyRecord.getYearlyNumbers().size());
            Assert.assertEquals(64, companyRecord.getQuarterlyNumbers().size());
            Assert.assertEquals(14, companyRecord.getAttributes().size());
            Assert.assertEquals(3, companyRecord.getProductionUnits().size());
            Assert.assertEquals(12, companyRecord.getParticipants().size());
            Assert.assertEquals(1, companyRecord.getFusions().size());

            Set<CompanyDataEventRecord> dataEventList = companyRecord.getDataevent();

            long adressEvents = dataEventList.stream().filter(item -> item.getField().equals("cvr_record_address")).count();
            Assert.assertEquals(6, adressEvents);

            CompanyDataEventRecord record = dataEventList.stream().filter(item -> item.getField().equals("cvr_record_address")).findFirst().get();

            long nameEvents = dataEventList.stream().filter(item -> item.getField().equals("cvr_record_company_status")).count();
            Assert.assertEquals(1, nameEvents);

            Assert.assertEquals(2, companyRecord.getFusions().iterator().next().getName().size());
            Assert.assertEquals(1, companyRecord.getFusions().iterator().next().getIncoming().size());
            Assert.assertEquals(2, companyRecord.getFusions().iterator().next().getIncoming().iterator().next().getValues().size());

            Assert.assertEquals(1, companyRecord.getSplits().size());
            Assert.assertEquals(2, companyRecord.getMetadata().getNewestName().size());
            Assert.assertEquals(2, companyRecord.getMetadata().getNewestForm().size());
            Assert.assertEquals(2, companyRecord.getMetadata().getNewestLocation().size());
            Assert.assertEquals(2, companyRecord.getMetadata().getNewestPrimaryIndustry().size());
            Assert.assertEquals(1, companyRecord.getMetadata().getNewestSecondaryIndustry1().size());

            boolean foundParticipantData = false;
            for (CompanyParticipantRelationRecord participantRelationRecord : companyRecord.getParticipants()) {
                if (participantRelationRecord.getRelationParticipantRecord().getUnitNumber() == 4000004988L) {
                    foundParticipantData = true;

                    Assert.assertEquals(2, participantRelationRecord.getRelationParticipantRecord().getNames().size());
                    Assert.assertEquals(5, participantRelationRecord.getRelationParticipantRecord().getLocationAddress().size());

                    Assert.assertEquals(1, participantRelationRecord.getOffices().size());
                    OfficeRelationRecord officeRelationRecord = participantRelationRecord.getOffices().iterator().next();
                    Assert.assertEquals(1, officeRelationRecord.getAttributes().size());
                    Assert.assertEquals(2, officeRelationRecord.getAttributes().iterator().next().getValues().size());
                    Assert.assertEquals(2, officeRelationRecord.getOfficeRelationUnitRecord().getNames().size());
                    Assert.assertEquals(2, officeRelationRecord.getOfficeRelationUnitRecord().getLocationAddress().size());

                    boolean foundOrganization1 = false;
                    boolean foundOrganization2 = false;
                    for (OrganizationRecord organizationRecord : participantRelationRecord.getOrganizations()) {
                        if (organizationRecord.getUnitNumber() == 4004733975L) {
                            foundOrganization1 = true;
                            Assert.assertEquals(2, organizationRecord.getNames().size());
                            Assert.assertEquals(1, organizationRecord.getAttributes().size());
                            Assert.assertEquals(2, organizationRecord.getAttributes().iterator().next().getValues().size());
                        }
                        if (organizationRecord.getUnitNumber() == 4004733976L) {
                            foundOrganization2 = true;
                            Assert.assertEquals(2, organizationRecord.getMemberData().size());
                            for (OrganizationMemberdataRecord organizationMemberdataRecord : organizationRecord.getMemberData()) {
                                if (organizationMemberdataRecord.getIndex() == 0) {
                                    Assert.assertEquals(1, organizationMemberdataRecord.getAttributes().size());
                                }
                                if (organizationMemberdataRecord.getIndex() == 1) {
                                    Assert.assertEquals(3, organizationMemberdataRecord.getAttributes().size());
                                    for (AttributeRecord attributeRecord : organizationMemberdataRecord.getAttributes()) {
                                        if (attributeRecord.getType().equals("FUNKTION")) {
                                            Assert.assertEquals(2, attributeRecord.getValues().size());
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Assert.assertTrue(foundOrganization1);
                    Assert.assertTrue(foundOrganization2);
                }
            }
            Assert.assertTrue(foundParticipantData);

        }

        loadCompany("/company_in3.json");
        try (Session session = sessionManager.getSessionFactory().openSession()) {

            CompanyRecordQuery query = new CompanyRecordQuery();
            query.setParameter(CompanyRecordQuery.CVRNUMMER, "25052943");
            List<CompanyRecord> records = QueryManager.getAllEntities(session, query, CompanyRecord.class);
            CompanyRecord companyRecord = records.get(0);

            Set<CompanyDataEventRecord> listOfdataevents = companyRecord.getDataevent();

            long adressEvents = listOfdataevents.stream().filter(item -> item.getField().equals("cvr_record_address")).count();
            Assert.assertEquals(7, adressEvents);

        }


    }

    @Test
    public void testRestCompany() throws IOException, DataFordelerException {
        loadCompany("/company_in.json");
        whitelistLocalhost();
        ObjectMapper objectMapper = this.getObjectMapper();
        TestUserDetails testUserDetails = new TestUserDetails();
        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        ResponseEntity<String> resp = restTemplate.exchange("/cvr/company/1/rest/search?cvrNummer=25052943", HttpMethod.GET, httpEntity, String.class);
        Assert.assertEquals(403, resp.getStatusCodeValue());

        testUserDetails.giveAccess(CvrRolesDefinition.READ_CVR_ROLE);
        this.applyAccess(testUserDetails);

        resp = restTemplate.exchange("/cvr/company/1/rest/search?cvrnummer=25052943&virkningFra=2000-01-01&virkningTil=2000-01-01&fmt=legacy", HttpMethod.GET, httpEntity, String.class);
        String body = resp.getBody();
        JsonNode data = objectMapper.readTree(body);
        Assert.assertEquals(200, resp.getStatusCodeValue());
        Assert.assertEquals(1, data.get("results").size());
        Assert.assertEquals("25052943", data.get("results").get(0).get("cvrNumberString").asText());
    }

    @Test
    public void testDataOnlyRestCompany() throws IOException, DataFordelerException {
        loadCompany("/company_in.json");
        whitelistLocalhost();
        ObjectMapper objectMapper = this.getObjectMapper();
        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CvrRolesDefinition.READ_CVR_ROLE);
        this.applyAccess(testUserDetails);

        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        ResponseEntity<String> resp = restTemplate.exchange("/cvr/company/1/rest/search?cvrNummer=25052943&fmt=rvd", HttpMethod.GET, httpEntity, String.class);
        Assert.assertEquals(200, resp.getStatusCodeValue());
        JsonNode jsonBody = objectMapper.readTree(resp.getBody());
        JsonNode results = jsonBody.get("results");

        JsonNode firstElement = results.get(0);
        JsonNode registreringer = firstElement.get("registreringer");
        Assert.assertNotNull(registreringer);

        resp = restTemplate.exchange("/cvr/company/1/rest/search?cvrNummer=25052943&fmt=dataonly", HttpMethod.GET, httpEntity, String.class);
        String body = resp.getBody();
        Assert.assertEquals(200, resp.getStatusCodeValue());
        jsonBody = objectMapper.readTree(resp.getBody());
        results = jsonBody.get("results");

        firstElement = results.get(0);
        registreringer = firstElement.get("registreringer");
        Assert.assertNull(registreringer);
    }

    private HashMap<Integer, JsonNode> loadUnit(String resource) throws IOException, DataFordelerException {
        ImportMetadata importMetadata = new ImportMetadata();
        ObjectMapper objectMapper = this.getObjectMapper();
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            InputStream input = RecordTest.class.getResourceAsStream(resource);
            if (input == null) {
                throw new MissingResourceException("Missing resource \"" + resource + "\"", resource, "key");
            }
            boolean linedFile = false;
            HashMap<Integer, JsonNode> units = new HashMap<>();
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
                            CompanyUnitEntityManager entityManager = (CompanyUnitEntityManager) plugin.getRegisterManager().getEntityManager(schemaMap.get(type));
                            JsonNode unitInputNode = item.get("_source").get("VrproduktionsEnhed");
                            entityManager.parseData(unitInputNode, importMetadata, session);
                            units.put(unitInputNode.get("pNummer").asInt(), unitInputNode);
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
                    Assert.assertTrue(itemList.isArray());
                    for (JsonNode item : itemList) {
                        String type = item.get("_type").asText();
                        CompanyUnitEntityManager entityManager = (CompanyUnitEntityManager) plugin.getRegisterManager().getEntityManager(schemaMap.get(type));
                        JsonNode unitInputNode = item.get("_source").get("VrproduktionsEnhed");
                        entityManager.parseData(unitInputNode, importMetadata, session);
                        units.put(unitInputNode.get("pNummer").asInt(), unitInputNode);
                    }
                }
                transaction.commit();
            } finally {
                session.close();
                QueryManager.clearCaches();
                input.close();
            }
            return units;
        }
    }

    @Test
    public void testCompanyUnit() throws DataFordelerException, IOException {
        this.loadUnit("/unit.json");
        this.loadUnit("/unit.json");
        ObjectMapper objectMapper = this.getObjectMapper();
        HashMap<Integer, JsonNode> units = this.loadUnit("/unit.json");
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            for (int pNumber : units.keySet()) {
                HashMap<String, Object> filter = new HashMap<>();
                filter.put("pNumber", pNumber);
                CompanyUnitRecord companyUnitRecord = QueryManager.getItem(session, CompanyUnitRecord.class, filter);
                if (companyUnitRecord == null) {
                    System.out.println("Didn't find p number " + pNumber);
                } else {
                    // For some reason, json serialization omits non-current bitemporal records, though they exist in the fetched data
                    compareJson(units.get(pNumber), objectMapper.valueToTree(companyUnitRecord), Collections.singletonList("root"));
                }
            }


            CompanyUnitRecordQuery query = new CompanyUnitRecordQuery();
            OffsetDateTime time = OffsetDateTime.parse("2017-01-01T00:00:00Z");
            //query.setRegistrationTo(time);
            query.setEffectFromBefore(time);
            query.setEffectToAfter(time);
            query.applyFilters(session);

            query.setParameter(CompanyUnitRecordQuery.PRIMARYINDUSTRY, "478900");
            Assert.assertEquals(1, QueryManager.getAllEntities(session, query, CompanyUnitRecord.class).size());
            query.clearParameter(CompanyUnitRecordQuery.PRIMARYINDUSTRY);
            query.setParameter(CompanyUnitRecordQuery.ASSOCIATED_COMPANY_CVR, "37952273");
            Assert.assertEquals(1, QueryManager.getAllEntities(session, query, CompanyUnitRecord.class).size());
            query.clearParameter(CompanyUnitRecordQuery.ASSOCIATED_COMPANY_CVR);
            query.setParameter(CompanyUnitRecordQuery.P_NUMBER, "1021686405");
            Assert.assertEquals(1, QueryManager.getAllEntities(session, query, CompanyUnitRecord.class).size());
            query.clearParameter(CompanyUnitRecordQuery.P_NUMBER);
            query.setParameter(CompanyUnitRecordQuery.KOMMUNEKODE, "561");
            Assert.assertEquals(1, QueryManager.getAllEntities(session, query, CompanyUnitRecord.class).size());
            query.clearParameter(CompanyUnitRecordQuery.KOMMUNEKODE);


            time = OffsetDateTime.parse("1900-01-01T00:00:00Z");
            query.setRegistrationTo(time);
            query.setEffectFromBefore(time);
            query.setEffectToAfter(time);
            query.applyFilters(session);


            query.setParameter(CompanyUnitRecordQuery.PRIMARYINDUSTRY, "478900");
            Assert.assertEquals(0, QueryManager.getAllEntities(session, query, CompanyUnitRecord.class).size());
            query.clearParameter(CompanyUnitRecordQuery.PRIMARYINDUSTRY);
            query.setParameter(CompanyUnitRecordQuery.ASSOCIATED_COMPANY_CVR, "37952273");
            Assert.assertEquals(0, QueryManager.getAllEntities(session, query, CompanyUnitRecord.class).size());
            query.clearParameter(CompanyUnitRecordQuery.ASSOCIATED_COMPANY_CVR);
            query.setParameter(CompanyUnitRecordQuery.P_NUMBER, "1021686405");
            Assert.assertEquals(1, QueryManager.getAllEntities(session, query, CompanyUnitRecord.class).size());
            query.clearParameter(CompanyUnitRecordQuery.P_NUMBER);
            query.setParameter(CompanyUnitRecordQuery.KOMMUNEKODE, "101");
            Assert.assertEquals(0, QueryManager.getAllEntities(session, query, CompanyUnitRecord.class).size());
            query.clearParameter(CompanyUnitRecordQuery.KOMMUNEKODE);

        }
    }


    @Test
    public void testUpdateCompanyUnit() throws IOException, DataFordelerException {
        loadUnit("/unit.json");
        loadUnit("/unit2.json");
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            CompanyUnitRecordQuery query = new CompanyUnitRecordQuery();
            query.setParameter(CompanyUnitRecordQuery.P_NUMBER, "1020895337");
            List<CompanyUnitRecord> records = QueryManager.getAllEntities(session, query, CompanyUnitRecord.class);
            Assert.assertEquals(1, records.size());
            CompanyUnitRecord companyUnitRecord = records.get(0);
            Assert.assertEquals(2, companyUnitRecord.getNames().size());
            Assert.assertEquals(1, companyUnitRecord.getPostalAddress().size());
            Assert.assertEquals(2, companyUnitRecord.getLocationAddress().size());
            Assert.assertEquals(1, companyUnitRecord.getPhoneNumber().size());
            Assert.assertEquals(0, companyUnitRecord.getFaxNumber().size());
            Assert.assertEquals(2, companyUnitRecord.getEmailAddress().size());
            Assert.assertEquals(2, companyUnitRecord.getLifecycle().size());
            Assert.assertEquals(2, companyUnitRecord.getPrimaryIndustry().size());
            Assert.assertEquals(1, companyUnitRecord.getSecondaryIndustry1().size());
            Assert.assertEquals(0, companyUnitRecord.getSecondaryIndustry2().size());
            Assert.assertEquals(0, companyUnitRecord.getSecondaryIndustry3().size());
            Assert.assertEquals(1, companyUnitRecord.getYearlyNumbers().size());
            Assert.assertEquals(4, companyUnitRecord.getQuarterlyNumbers().size());
            Assert.assertEquals(1, companyUnitRecord.getAttributes().size());
            Assert.assertEquals(0, companyUnitRecord.getParticipants().size());
            Assert.assertEquals(2, companyUnitRecord.getMetadata().getNewestName().size());
            Assert.assertEquals(2, companyUnitRecord.getMetadata().getNewestLocation().size());
            Assert.assertEquals(2, companyUnitRecord.getMetadata().getNewestPrimaryIndustry().size());
            Assert.assertEquals(1, companyUnitRecord.getMetadata().getNewestSecondaryIndustry1().size());
        }
    }

    @Test
    public void testRestCompanyUnit() throws IOException, DataFordelerException {
        loadUnit("/unit.json");
        ObjectMapper objectMapper = this.getObjectMapper();
        whitelistLocalhost();
        TestUserDetails testUserDetails = new TestUserDetails();
        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        ResponseEntity<String> resp = restTemplate.exchange("/cvr/unit/1/rest/search?pnummer=1020895337", HttpMethod.GET, httpEntity, String.class);
        Assert.assertEquals(403, resp.getStatusCodeValue());

        testUserDetails.giveAccess(CvrRolesDefinition.READ_CVR_ROLE);
        this.applyAccess(testUserDetails);

        resp = restTemplate.exchange("/cvr/unit/1/rest/search?pnummer=1020895337&virkningFra=2016-01-01&virkningTil=2016-01-01", HttpMethod.GET, httpEntity, String.class);
        String body = resp.getBody();
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(body)));
        Assert.assertEquals(200, resp.getStatusCodeValue());
    }

    private HashMap<Long, JsonNode> loadParticipant(String resource) throws IOException, DataFordelerException {
        System.out.println("LoadParticipant");
        ObjectMapper objectMapper = this.getObjectMapper();
        ImportMetadata importMetadata = new ImportMetadata();
        try (Session session = sessionManager.getSessionFactory().openSession()) {
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
                    Scanner lineScanner = new Scanner(input, "UTF-8").useDelimiter("\n");
                    while (lineScanner.hasNext()) {
                        String data = lineScanner.next();

                        JsonNode root = objectMapper.readTree(data);
                        JsonNode itemList = root.get("hits").get("hits");
                        Assert.assertTrue(itemList.isArray());
                        for (JsonNode item : itemList) {
                            String type = item.get("_type").asText();
                            ParticipantEntityManager entityManager = (ParticipantEntityManager) plugin.getRegisterManager().getEntityManager(schemaMap.get(type));
                            JsonNode participantInputNode = item.get("_source").get("Vrdeltagerperson");
                            entityManager.parseData(participantInputNode, importMetadata, session);
                            persons.put(participantInputNode.get("enhedsNummer").asLong(), participantInputNode);
                        }
                        lineNumber++;
                        if (lineNumber >= 10) {
                            break;
                        }
                    }
                } else {
                    JsonNode root = objectMapper.readTree(input);
                    JsonNode itemList = root.get("hits").get("hits");
                    Assert.assertTrue(itemList.isArray());
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
    }

    @Test
    public void testParticipant() throws DataFordelerException, IOException {
        loadParticipant("/person.json");
        loadParticipant("/person.json");
        ObjectMapper objectMapper = this.getObjectMapper();
        HashMap<Long, JsonNode> persons = loadParticipant("/person.json");
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            for (long participantNumber : persons.keySet()) {
                HashMap<String, Object> filter = new HashMap<>();
                filter.put("unitNumber", participantNumber);
                ParticipantRecord participantRecord = QueryManager.getItem(session, ParticipantRecord.class, filter);
                if (participantRecord == null) {
                    System.out.println("Didn't find participant number " + participantNumber);
                } else {
                    compareJson(persons.get(participantNumber), objectMapper.valueToTree(participantRecord), Collections.singletonList("root"));
                }
            }

            ParticipantRecordQuery query = new ParticipantRecordQuery();
            OffsetDateTime time = OffsetDateTime.now();
            //query.setRegistrationTo(time);
            query.setEffectFromBefore(time);
            query.setEffectToAfter(time);
            query.applyFilters(session);

            query.setParameter(ParticipantRecordQuery.UNITNUMBER, "4000004988");
            Assert.assertEquals(1, QueryManager.getAllEntities(session, query, ParticipantRecord.class).size());
            query.clearParameter(ParticipantRecordQuery.UNITNUMBER);

            query.setParameter(ParticipantRecordQuery.NAVN, "Morten*");
            Assert.assertEquals(1, QueryManager.getAllEntities(session, query, ParticipantRecord.class).size());
            query.clearParameter(ParticipantRecordQuery.NAVN);

            query.setParameter(ParticipantRecordQuery.KOMMUNEKODE, "101");
            Assert.assertEquals(1, QueryManager.getAllEntities(session, query, ParticipantRecord.class).size());
            query.clearParameter(ParticipantRecordQuery.KOMMUNEKODE);



            time = OffsetDateTime.parse("1900-01-01T00:00:00Z");
            //query.setRegistrationTo(time);
            query.setEffectFromBefore(time);
            query.setEffectToAfter(time);
            query.applyFilters(session);



            query.setParameter(ParticipantRecordQuery.UNITNUMBER, "4000004988");
            Assert.assertEquals(1, QueryManager.getAllEntities(session, query, ParticipantRecord.class).size());
            query.clearParameter(ParticipantRecordQuery.UNITNUMBER);
            query.setParameter(ParticipantRecordQuery.NAVN, "Morten*");
            Assert.assertEquals(1, QueryManager.getAllEntities(session, query, ParticipantRecord.class).size());
            query.clearParameter(ParticipantRecordQuery.NAVN);
            query.setParameter(ParticipantRecordQuery.KOMMUNEKODE, "101");
            Assert.assertEquals(0, QueryManager.getAllEntities(session, query, ParticipantRecord.class).size());
            query.clearParameter(ParticipantRecordQuery.KOMMUNEKODE);
        }
    }

    @Test
    public void testUpdateParticipant() throws IOException, DataFordelerException {
        loadParticipant("/person.json");
        loadParticipant("/person2.json");
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            ParticipantRecordQuery query = new ParticipantRecordQuery();
            query.setParameter(ParticipantRecordQuery.UNITNUMBER, "4000004988");
            List<ParticipantRecord> records = QueryManager.getAllEntities(session, query, ParticipantRecord.class);
            Assert.assertEquals(1, records.size());
            ParticipantRecord participantRecord = records.get(0);
            Assert.assertEquals(2, participantRecord.getNames().size());
            Assert.assertEquals(1, participantRecord.getPostalAddress().size());
            Assert.assertEquals(5, participantRecord.getLocationAddress().size());
            Assert.assertEquals(1, participantRecord.getBusinessAddress().size());
            Assert.assertEquals(1, participantRecord.getPhoneNumber().size());
            Assert.assertEquals(0, participantRecord.getFaxNumber().size());
            Assert.assertEquals(1, participantRecord.getEmailAddress().size());
            Assert.assertEquals(5, participantRecord.getCompanyRelation().size());
            Assert.assertEquals(0, participantRecord.getAttributes().size());
            Assert.assertEquals(1, participantRecord.getMetadata().getMetadataContactData().size());

            boolean foundCompanyData = false;
            for (CompanyParticipantRelationRecord relationRecord : participantRecord.getCompanyRelation()) {
                if (relationRecord.getCompanyUnitNumber() == 4001248508L) {
                    foundCompanyData = true;
                    Assert.assertEquals(3, relationRecord.getRelationCompanyRecord().getNames().size());
                    Assert.assertEquals(0, relationRecord.getRelationCompanyRecord().getStatus().size());
                    Assert.assertEquals(2, relationRecord.getRelationCompanyRecord().getCompanyStatus().size());
                    Assert.assertEquals(2, relationRecord.getRelationCompanyRecord().getForm().size());
                }
            }
            Assert.assertTrue(foundCompanyData);

        }
    }


    @Test
    public void testRestParticipant() throws IOException, DataFordelerException {
        loadParticipant("/person.json");
        ObjectMapper objectMapper = this.getObjectMapper();
        whitelistLocalhost();
        TestUserDetails testUserDetails = new TestUserDetails();
        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        ResponseEntity<String> resp = restTemplate.exchange("/cvr/participant/1/rest/search?enhedsNummer=4000004988", HttpMethod.GET, httpEntity, String.class);
        Assert.assertEquals(403, resp.getStatusCodeValue());

        testUserDetails.giveAccess(CvrRolesDefinition.READ_CVR_ROLE);
        this.applyAccess(testUserDetails);

        resp = restTemplate.exchange("/cvr/participant/1/rest/search?enhedsNummer=4000004988&virkningFra=2001-01-01&virkningTil=2001-01-01&format=legacy", HttpMethod.GET, httpEntity, String.class);
        Assert.assertEquals(200, resp.getStatusCodeValue());
        String body = resp.getBody();
        Assert.assertNull(objectMapper.readTree(body).get("results").get(0).get("forretningsnoegle"));
    }

    @Test
    public void testRestParticipantPnr() throws IOException, DataFordelerException {
        loadParticipant("/person_pnr.json");
        whitelistLocalhost();
        TestUserDetails testUserDetails = new TestUserDetails();
        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());

        testUserDetails.giveAccess(CvrRolesDefinition.READ_CVR_ROLE);
        this.applyAccess(testUserDetails);

        // Test that participant_pnr yields the pnr
        ResponseEntity<String> resp = restTemplate.exchange("/cvr/participant_pnr/1/rest/search?enhedsNummer=4000004988&virkningFra=2001-01-01&virkningTil=2001-01-01&format=legacy", HttpMethod.GET, httpEntity, String.class);
        String body = resp.getBody();
        Assert.assertEquals(1234567890L, objectMapper.readTree(body).get("results").get(0).get("forretningsnoegle").asLong());

        // Test that participant does not yield the pnr
        resp = restTemplate.exchange("/cvr/participant/1/rest/search?enhedsNummer=4000004988&virkningFra=2001-01-01&virkningTil=2001-01-01&format=legacy", HttpMethod.GET, httpEntity, String.class);
        body = resp.getBody();
        Assert.assertNull(objectMapper.readTree(body).get("results").get(0).get("forretningsnoegle"));
    }

    @Test
    public void testCollectiveLookup() throws IOException, DataFordelerException {
        whitelistLocalhost();
        ObjectMapper objectMapper = this.getObjectMapper();
        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CvrRolesDefinition.READ_CVR_ROLE);
        this.applyAccess(testUserDetails);

        loadParticipant("/person.json");
        loadCompany();

        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());

        ResponseEntity<String> resp = restTemplate.exchange("/cvr/company/1/rest/search?cvrNummer=25052943&fmt=dataonly", HttpMethod.GET, httpEntity, String.class);
        JsonNode responseNode = objectMapper.readTree(resp.getBody());

        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseNode));

        Assert.assertEquals(1, responseNode.get("results").size());

        ResponseEntity<String> resp2 = restTemplate.exchange("/cvr/company/1/rest/search?navn=MAGENTA ApS", HttpMethod.GET, httpEntity, String.class);
        Assert.assertEquals(1, responseNode.get("results").size());
    }

    /**
     * Test the use of different searchparameters for cvr-lookup
     *
     * @throws IOException
     * @throws DataFordelerException
     */
    @Test
    public void testCollectiveLookupDifferentSearchParameters() throws IOException, DataFordelerException {
        whitelistLocalhost();
        ObjectMapper objectMapper = this.getObjectMapper();
        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CvrRolesDefinition.READ_CVR_ROLE);
        this.applyAccess(testUserDetails);

        loadParticipant("/person.json");
        loadCompany();

        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());


        ResponseEntity<String> resp = restTemplate.exchange("/cvr/company/1/rest/search?cvrNummer=25052943&fmt=dataonly", HttpMethod.GET, httpEntity, String.class);
        JsonNode responseNode = objectMapper.readTree(resp.getBody());
        JsonNode result1 = responseNode.get("results");

        resp = restTemplate.exchange("/cvr/company/1/rest/search?reklamebeskyttet=true&fmt=dataonly", HttpMethod.GET, httpEntity, String.class);
        responseNode = objectMapper.readTree(resp.getBody());
        JsonNode result2 = responseNode.get("results");
        Assert.assertEquals(result1, result2);

        resp = restTemplate.exchange("/cvr/company/1/rest/search?navne=MAGENTA ApS&fmt=dataonly", HttpMethod.GET, httpEntity, String.class);
        responseNode = objectMapper.readTree(resp.getBody());
        JsonNode result3 = responseNode.get("results");
        Assert.assertEquals(result2, result3);

        resp = restTemplate.exchange("/cvr/company/1/rest/search?telefonNummer=33369696&fmt=dataonly", HttpMethod.GET, httpEntity, String.class);
        responseNode = objectMapper.readTree(resp.getBody());
        JsonNode result4 = responseNode.get("results");
        Assert.assertEquals(result3, result4);

        resp = restTemplate.exchange("/cvr/company/1/rest/search?elektroniskPost=info@magenta.dk&fmt=dataonly", HttpMethod.GET, httpEntity, String.class);
        responseNode = objectMapper.readTree(resp.getBody());
        JsonNode result5 = responseNode.get("results");
        Assert.assertEquals(result4, result5);

        resp = restTemplate.exchange("/cvr/company/1/rest/search?virksomhedsform=80&fmt=dataonly", HttpMethod.GET, httpEntity, String.class);
        responseNode = objectMapper.readTree(resp.getBody());
        JsonNode result6 = responseNode.get("results");
        Assert.assertEquals(result5, result6);

        resp = restTemplate.exchange("/cvr/company/1/rest/search?kommunekode=101&fmt=dataonly", HttpMethod.GET, httpEntity, String.class);
        responseNode = objectMapper.readTree(resp.getBody());
        JsonNode result7 = responseNode.get("results");
        Assert.assertEquals(result6, result7);

        resp = restTemplate.exchange("/cvr/company/1/rest/search?vejkode=5520&fmt=dataonly", HttpMethod.GET, httpEntity, String.class);
        responseNode = objectMapper.readTree(resp.getBody());
        JsonNode result8 = responseNode.get("results");
        Assert.assertEquals(result7, result8);

        resp = restTemplate.exchange("/cvr/company/1/rest/search?etage=3&fmt=dataonly", HttpMethod.GET, httpEntity, String.class);
        responseNode = objectMapper.readTree(resp.getBody());
        JsonNode result9 = responseNode.get("results");
        Assert.assertEquals(result8, result9);

    }


    /**
     * Checks that all items in n1 are also present in n2
     *
     * @param n1
     * @param n2
     * @param path
     * @throws JsonProcessingException
     */
    private void compareJson(JsonNode n1, JsonNode n2, List<String> path) throws JsonProcessingException {
        ObjectMapper objectMapper = this.getObjectMapper();
        if (n1 == null && n2 != null) {
            System.out.println("Mismatch: " + n1 + " != " + n2 + " at " + path);
        } else if (n1 != null && n2 == null) {
            System.out.println("Mismatch: " + n1 + " != " + n2 + " at " + path);
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
                    System.out.println("Mismatch: missing field " + field + " at " + path);
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
                System.out.println("Mismatch: Array[" + a1.size() + "] != Array[" + a2.size() + "] at " + path);
                System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(a2));
            } else {

                for (int i = 0; i < a1.size(); i++) {
                    boolean found = false;
                    for (int j = 0; j < a2.size(); j++) {
                        if (a1.get(i).asText().equals(a2.get(j).asText())) {
                            found = true;
                        }
                    }
                    if (!found) {
                        System.out.println("Mismatch: Didn't find item " + a1.get(i) + " in " + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(a2) + " at " + path);
                    }
                }
            }


        } else if (!n1.asText().equals(n2.asText())) {
            boolean skip = false;
            try {
                if (OffsetDateTime.parse(n1.asText()).isEqual(OffsetDateTime.parse(n2.asText()))) {
                    skip = true;
                }
            } catch (DateTimeParseException e) {
            }
            if (!skip) {
                System.out.println("Mismatch: " + n1.asText() + " (" + n1.getNodeType().name() + ") != " + n2.asText() + " (" + n2.getNodeType().name() + ") at " + path);
            }
        }
    }


    @Autowired
    PluginManager pluginManager;

    @Test
    public void testLookup() throws IOException, DataFordelerException {
        loadCompany();
        // Mostly for our own sake during developement
        CompanyRecordQuery query = new CompanyRecordQuery();
        OffsetDateTime now = OffsetDateTime.now();
        query.setRegistrationAt(now);
        query.setEffectAt(now);
        query.setParameter(CompanyRecordQuery.CVRNUMMER, "25052943");

        Plugin geoPlugin = pluginManager.getPluginByName("geo");
        if (geoPlugin != null) {

            Session session = sessionManager.getSessionFactory().openSession();
            query.applyFilters(session);
            //System.out.println(QueryManager.getFirstQuery(session, query));
            QueryManager.getAllEntitySets(session, query, CompanyRecord.class);

            //AccessAddressQuery q = new AccessAddressQuery();
            //q.setMunicipalityCode();

        }
    }

    @SpyBean
    private DirectLookup directLookup;

    @Test
    public void testEnrich() throws IOException, DataFordelerException {
        this.cleanup();
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            List<ParticipantRecord> items = QueryManager.getAllEntities(session, ParticipantRecord.class);
            System.out.println("There are "+items.size()+" participants");
            if (items.size() > 0) {
                System.out.println(items.get(0).getId()+": "+items.get(0).getUnitNumber());
                for (ParticipantRecord participantRecord : items) {
                    session.delete(participantRecord);
                }
            }
            //Assert.assertEquals(0, items.size());
        }


        loadParticipant("/person.json");
        ParticipantRecordQuery query = new ParticipantRecordQuery();
        query.setParameter(ParticipantRecordQuery.NAVN, "Morten*");
        List<ParticipantRecord> records;
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            records = QueryManager.getAllEntities(session, query, ParticipantRecord.class);
        }
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Assert.assertEquals(1, records.size());
            ParticipantRecord record = records.get(0);
            Assert.assertNull(record.getBusinessKey());
            ParticipantRecord mockParticipant = new ParticipantRecord();
            mockParticipant.setBusinessKey(1234567890L);
            doReturn(mockParticipant).when(directLookup).participantLookup(anyString());

            loadParticipant("/person.json");
            records = QueryManager.getAllEntities(session, query, ParticipantRecord.class);
            Assert.assertEquals(1, records.size());
            record = records.get(0);
            Assert.assertEquals(Long.valueOf(1234567890L), record.getBusinessKey());
        }
    }


}
