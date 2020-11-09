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
import dk.magenta.datafordeler.core.fapi.OutputWrapper;
import dk.magenta.datafordeler.core.fapi.Query;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.cpr.data.person.PersonCustodyRelationsManager;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonEntityManager;
import dk.magenta.datafordeler.cpr.data.person.PersonRecordQuery;
import dk.magenta.datafordeler.cpr.records.output.PersonRecordOutputWrapper;
import dk.magenta.datafordeler.cpr.records.person.NameRecord;
import dk.magenta.datafordeler.cpr.records.person.data.NameDataRecord;
import org.hamcrest.Matchers;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
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
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;


import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class RecordTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private PersonEntityManager personEntityManager;

    @Autowired
    private PersonCustodyRelationsManager custodyManager;

    @Autowired
    private PersonRecordOutputWrapper personRecordOutputWrapper;

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
        InputStream testData = RecordTest.class.getResourceAsStream(resource);
        personEntityManager.parseData(testData, importMetadata);
        testData.close();
    }

    @Test
    public void testPerson() throws DataFordelerException, IOException {
        Session session = sessionManager.getSessionFactory().openSession();
        ImportMetadata importMetadata = new ImportMetadata();
        importMetadata.setSession(session);
        this.loadPerson("/persondata.txt", importMetadata);
        try {

            PersonRecordQuery query = new PersonRecordQuery();
            OffsetDateTime time = OffsetDateTime.now();
            query.setRegistrationFromBefore(time);
            query.setRegistrationToAfter(time);
            query.setEffectFromBefore(time);
            query.setEffectToAfter(time);
            query.applyFilters(session);

            query.addKommunekode(956);
            Assert.assertEquals(0, QueryManager.getAllEntities(session, query, PersonEntity.class).size());
            query.clearKommunekode();

            query.addKommunekode(958);
            Assert.assertEquals(1, QueryManager.getAllEntities(session, query, PersonEntity.class).size());
            query.clearKommunekode();
            query.addFornavn("Tester");
            Assert.assertEquals(1, QueryManager.getAllEntities(session, query, PersonEntity.class).size());
            query.clearFornavn();

            query.setEfternavn("Tystersen");
            Assert.assertEquals(1, QueryManager.getAllEntities(session, query, PersonEntity.class).size());

            query.setEfternavn("Testersen");
            Assert.assertEquals(0, QueryManager.getAllEntities(session, query, PersonEntity.class).size());

            time = OffsetDateTime.parse("2000-01-01T00:00:00Z");
            query.setEffectFromBefore(time);
            query.setEffectToAfter(time);
            query.applyFilters(session);

            query.setEfternavn("Testersen");
            Assert.assertEquals(1, QueryManager.getAllEntities(session, query, PersonEntity.class).size());



        } finally {
            session.close();
        }
    }

    /**
     * Tests specifically created and matched with data from 'personWithChildrenAndCustodyChange'
     * One person with cpr=0101011234 has 4 children
     * -0101981234
     * -0101121234
     * -0101141234
     * -0101161234
     * The child with cpr=0101141234 gets custody handed over to person with cpr=0101991234
     * Another child with cpr=0101131234 gets custody handed over to person with cpr=0101011234
     *
     * The person with cpr=0101011234 now has lost custody of one child, but gains custody of another child
     *
     *
     * After calculations the person with cpr=0101011234 has custody over
     * -0101981234 (Should not be returned since the child is more then 18 years old)
     * -0101121234
     * -0101131234
     * -0101161234
     *
     * The person with cpr=0101991234 now has custody over
     * -0101141234
     *
     *
     *
     * @throws DataFordelerException
     * @throws IOException
     */
    @Test
    public void testImportPersonWithChildren() throws DataFordelerException, IOException {
        //This test will start failing in year 2030 when the children in this test is no longer children
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            ImportMetadata importMetadata = new ImportMetadata();
            importMetadata.setSession(session);
            this.loadPerson("/personWithChildrenAndCustodyChange.txt", importMetadata);

            PersonRecordQuery query = new PersonRecordQuery();
            query.setPersonnummer("0101011234");
            List<PersonEntity> entities = QueryManager.getAllEntities(session, query, PersonEntity.class);
            PersonEntity personEntity = entities.get(0);
            Assert.assertEquals(4, personEntity.getChildren().size());
            Assert.assertEquals(0, personEntity.getCustody().size());
            //Assert.assertTrue(personEntity.getChildren().stream().anyMatch(child -> child.getChildCprNumber().equals("0101981234")));
            Assert.assertTrue(personEntity.getChildren().stream().anyMatch(child -> child.getChildCprNumber().equals("0101121234")));
            Assert.assertTrue(personEntity.getChildren().stream().anyMatch(child -> child.getChildCprNumber().equals("0101141234")));
            Assert.assertTrue(personEntity.getChildren().stream().anyMatch(child -> child.getChildCprNumber().equals("0101161234")));

            //Find a child and from that the person who has custody over the child
            query = new PersonRecordQuery();
            query.setPersonnummer("0101141234");
            entities = QueryManager.getAllEntities(session, query, PersonEntity.class);
            personEntity = entities.get(0);
            Assert.assertEquals(0, personEntity.getChildren().size());
            Assert.assertEquals(2, personEntity.getCustody().size());
            Assert.assertTrue(personEntity.getCustody().stream().anyMatch(child -> child.getRelationPnr().equals("0101991234")));

            //Find a child and from that the person who has custody over the child
            query = new PersonRecordQuery();
            query.setPersonnummer("0101131234");
            entities = QueryManager.getAllEntities(session, query, PersonEntity.class);
            personEntity = entities.get(0);
            Assert.assertEquals(0, personEntity.getChildren().size());
            Assert.assertEquals(1, personEntity.getCustody().size());
            Assert.assertTrue(personEntity.getCustody().stream().anyMatch(child -> child.getRelationPnr().equals("0101011234")));

            //Find a parent-ish and from that the child-ish custody relation
            query = new PersonRecordQuery();
            query.addCustodyPnr("0101991234");
            entities = QueryManager.getAllEntities(session, query, PersonEntity.class);
            personEntity = entities.get(0);
            Assert.assertEquals("0101141234", personEntity.getPersonnummer());

            //Find a parent-ish and from that the child-ish custody relation
            query = new PersonRecordQuery();
            query.addCustodyPnr("0101011234");
            entities = QueryManager.getAllEntities(session, query, PersonEntity.class);
            personEntity = entities.get(0);
            Assert.assertEquals("0101131234", personEntity.getPersonnummer());

            //Find collective custody of the person '0101011234'
            //0101981234 (Should not be returned since the child is more then 18 years old)
            //0101121234
            //0101131234
            //0101161234
            List<PersonCustodyRelationsManager.ChildInfo> custodyList = custodyManager.findRelations("0101011234");
            Assert.assertEquals(3, custodyList.size());
            Assert.assertFalse(custodyList.stream().anyMatch(child -> child.getPnr().equals("0101981234")));
            Assert.assertTrue(custodyList.stream().anyMatch(child -> child.getPnr().equals("0101121234")));
            Assert.assertTrue(custodyList.stream().anyMatch(child -> child.getPnr().equals("0101131234")));
            Assert.assertTrue(custodyList.stream().anyMatch(child -> child.getPnr().equals("0101161234")));

            List<PersonCustodyRelationsManager.ChildInfo> custodyListFather = custodyManager.findRelations("0101011235");
            //Assert.assertEquals(3, custodyListFather.size());
            Assert.assertFalse(custodyListFather.stream().anyMatch(child -> child.getPnr().equals("0101981234")));
            Assert.assertTrue(custodyListFather.stream().anyMatch(child -> child.getPnr().equals("0101121234")));
            Assert.assertTrue(custodyListFather.stream().anyMatch(child -> child.getPnr().equals("0101141234")));
            Assert.assertTrue(custodyListFather.stream().anyMatch(child -> child.getPnr().equals("0101161234")));

            //Find collective custody of the person '0101011234'
            //0101981234
            List<PersonCustodyRelationsManager.ChildInfo> custodyList2 = custodyManager.findRelations("0101991234");
            Assert.assertEquals(1, custodyList2.size());
            Assert.assertFalse(custodyList2.stream().anyMatch(child -> child.getPnr().equals("0101981234")));
            Assert.assertTrue(custodyList2.stream().anyMatch(child -> child.getPnr().equals("0101141234")));
        }
    }



    @Test
    public void testCallCustodyService() throws Exception {
        //This test will start failing in year 2030 when the children in this test is no longer children
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            ImportMetadata importMetadata = new ImportMetadata();
            importMetadata.setSession(session);
            this.loadPerson("/personWithChildrenAndCustodyChange.txt", importMetadata);
        }

        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());

        TestUserDetails testUserDetails = new TestUserDetails();
        this.applyAccess(testUserDetails);

        //Try fetching with no cpr access rights
        ResponseEntity<String> response = restTemplate.exchange(
                "/cpr/person/custody/1/rest/" + "0101011234",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        //Try fetching with cpr access rights
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);
        response = restTemplate.exchange(
                "/cpr/person/custody/1/rest/" + "0101011234",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(3, objectMapper.readTree(response.getBody()).get("children").size());
        JSONAssert.assertEquals("{\"parent\":\"0101011234\",\"children\":[{\"pnr\":\"0101161234\",\"status\":1},{\"pnr\":\"0101121234\",\"status\":1},{\"pnr\":\"0101131234\",\"status\":1}]}",response.getBody(),false);



        //Try fetching other persons custody
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);
        response = restTemplate.exchange(
                "/cpr/person/custody/1/rest/" + "0101991234",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(1, objectMapper.readTree(response.getBody()).get("children").size());
        JSONAssert.assertEquals("{\"parent\":\"0101991234\",\"children\":[{\"pnr\":\"0101141234\",\"status\":1}]}",response.getBody(),false);
    }


    /**
     * Vi tester en special case hvor en person med et undone navn, får tildelt dette navn igen
     *
     * Ret / fortryd markering (ANNKOR) kan være:
     * K = Ret
     * A = Fortryd
     * Æ = Teknisk ændring
     * @throws DataFordelerException
     * @throws IOException
     */
    @Test
    public void testPersonUndoRedoName() throws DataFordelerException, IOException {
        Session session = sessionManager.getSessionFactory().openSession();
        ImportMetadata importMetadata = new ImportMetadata();
        importMetadata.setSession(session);
        this.loadPerson("/personUndoRedoName.txt", importMetadata);
        try {

            PersonRecordQuery query = new PersonRecordQuery();
            query.setPersonnummer("1111111111");
            List<PersonEntity> entities = QueryManager.getAllEntities(session, query, PersonEntity.class);
            PersonEntity personEntity = entities.get(0);
            Assert.assertTrue("Validate that when setting a name and undoing that name, it must be possible to set that same name again ",
                    personEntity.getName().stream().anyMatch(name -> !name.isUndone()));

        } finally {
            session.close();
        }
    }



    @Test
    public void testPersonAddressSearch() throws DataFordelerException, IOException {
        Session session = sessionManager.getSessionFactory().openSession();
        ImportMetadata importMetadata = new ImportMetadata();
        importMetadata.setSession(session);
        this.loadPerson("/persondata.txt", importMetadata);
        try {

            PersonRecordQuery query = new PersonRecordQuery();
            OffsetDateTime time = OffsetDateTime.now();
            query.setRegistrationFromBefore(time);
            query.setRegistrationToAfter(time);
            query.setEffectFromBefore(time);
            query.setEffectToAfter(time);
            query.applyFilters(session);

            query.addVejkode(2);
            Assert.assertEquals(0, QueryManager.getAllEntities(session, query, PersonEntity.class).size());
            query.clearVejkode();

            query.addVejkode(111);
            Assert.assertEquals(1, QueryManager.getAllEntities(session, query, PersonEntity.class).size());
            query.clearVejkode();

            query.addHouseNo("2");
            Assert.assertEquals(0, QueryManager.getAllEntities(session, query, PersonEntity.class).size());
            query.clearHouseNo();

            query.addHouseNo("3");
            Assert.assertEquals(1, QueryManager.getAllEntities(session, query, PersonEntity.class).size());
            query.clearHouseNo();

            query.addFloor("01");
            Assert.assertEquals(0, QueryManager.getAllEntities(session, query, PersonEntity.class).size());
            query.clearFloor();

            query.addFloor("02");
            Assert.assertEquals(1, QueryManager.getAllEntities(session, query, PersonEntity.class).size());
            query.clearFloor();

            query.addDoor("3");
            Assert.assertEquals(0, QueryManager.getAllEntities(session, query, PersonEntity.class).size());
            query.clearDoor();

            query.addDoor("4");
            Assert.assertEquals(1, QueryManager.getAllEntities(session, query, PersonEntity.class).size());
            query.clearDoor();

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
            Assert.assertEquals(1, personEntity.getConame().size());
            Assert.assertEquals(1, personEntity.getAddress().size());
            Assert.assertEquals(2, personEntity.getAddressName().size());
            Assert.assertEquals(1, personEntity.getBirthPlace().size());
            Assert.assertEquals(0, personEntity.getBirthPlaceVerification().size());
            Assert.assertEquals(1, personEntity.getBirthTime().size());
            Assert.assertEquals(4, personEntity.getChurchRelation().size());
            Assert.assertEquals(3, personEntity.getChurchRelationVerification().size());
            Assert.assertEquals(0, personEntity.getCivilstatus().size());
            Assert.assertEquals(0, personEntity.getCivilstatusAuthorityText().size());
            Assert.assertEquals(0, personEntity.getCivilstatusVerification().size());
            Assert.assertEquals(1, personEntity.getForeignAddress().size());
            Assert.assertEquals(1, personEntity.getEmigration().size());
            Assert.assertEquals(0, personEntity.getMunicipalityMove().size());
            Assert.assertEquals(4, personEntity.getName().size());
            Assert.assertEquals(3, personEntity.getNameAuthorityText().size());
            Assert.assertEquals(4, personEntity.getNameVerification().size());
            Assert.assertEquals(1, personEntity.getMother().size());
            Assert.assertEquals(1, personEntity.getMotherVerification().size());
            Assert.assertEquals(1, personEntity.getFather().size());
            Assert.assertEquals(1, personEntity.getFatherVerification().size());
            Assert.assertEquals(1, personEntity.getCore().size());
            Assert.assertEquals(0, personEntity.getPosition().size());
            Assert.assertEquals(3, personEntity.getStatus().size());
            Assert.assertEquals(3, personEntity.getProtection().size());

        } finally {
            session.close();
        }
    }

    /**
     * Test that when new addresses is added to a person the former added adresses is bitemporally closed
     * @throws IOException
     * @throws DataFordelerException
     */
    @Test
    public void testPersonAddressCloseLastActive() throws IOException, DataFordelerException {
        Session session = sessionManager.getSessionFactory().openSession();
        ImportMetadata importMetadata = new ImportMetadata();
        importMetadata.setSession(session);
        this.loadPerson("/overwrite_cpr_import.txt", importMetadata);
        try {
            PersonRecordQuery query = new PersonRecordQuery();
            query.setPersonnummer("0101010123");
            OffsetDateTime time = OffsetDateTime.now();
            query.setEffectToAfter(time);
            query.setRegistrationToAfter(time);
            query.applyFilters(session);
            List<PersonEntity> entities = QueryManager.getAllEntities(session, query, PersonEntity.class);
            PersonEntity personEntity = entities.get(0);
            Assert.assertEquals(1, personEntity.getAddress().size());
            Assert.assertEquals("F", personEntity.getAddress().iterator().next().getBuildingNumber());
        } finally {
            session.close();
        }
    }

    /**
     * Test that when new addresses is added to a person the former added adresses is bitemporally closed
     * @throws IOException
     * @throws DataFordelerException
     */
    @Test
    public void testCivilstatePersonCloseLastActive() throws IOException, DataFordelerException {
        Session session = sessionManager.getSessionFactory().openSession();
        ImportMetadata importMetadata = new ImportMetadata();
        importMetadata.setSession(session);
        this.loadPerson("/overwrite_civilstate.txt", importMetadata);

        try {
            PersonRecordQuery query = new PersonRecordQuery();
            query.setPersonnummer("0101010123");
            OffsetDateTime time = OffsetDateTime.now();
            query.setEffectToAfter(time);
            query.setRegistrationToAfter(time);
            query.applyFilters(session);

            List<PersonEntity> entities = QueryManager.getAllEntities(session, query, PersonEntity.class);
            PersonEntity personEntity = entities.get(0);
            Assert.assertEquals(1, personEntity.getCivilstatus().size());
            Assert.assertEquals("0101010124", personEntity.getCivilstatus().iterator().next().getSpouseCpr());


        } finally {
            session.close();
        }
    }

    /**
     * Test that when new addresses is added to a person the former added adresses is bitemporally closed
     * @throws IOException
     * @throws DataFordelerException
     */
    @Test
    public void testBirthPersonCloseLastActive() throws IOException, DataFordelerException {
        Session session = sessionManager.getSessionFactory().openSession();
        ImportMetadata importMetadata = new ImportMetadata();
        importMetadata.setSession(session);
        this.loadPerson("/overwrite_birth_import.txt", importMetadata);

        try {
            PersonRecordQuery query = new PersonRecordQuery();
            query.setPersonnummer("0101010123");
            OffsetDateTime time = OffsetDateTime.now();

            query.setEffectToAfter(time);
            query.setRegistrationToAfter(time);
            query.applyFilters(session);

            List<PersonEntity> entities = QueryManager.getAllEntities(session, query, PersonEntity.class);
            PersonEntity personEntity = entities.get(0);
            Assert.assertEquals(1, personEntity.getFather().size());
            Assert.assertEquals(1, personEntity.getMother().size());
            Assert.assertEquals("0101900125", personEntity.getFather().iterator().next().getCprNumber());
            Assert.assertEquals("Bo", personEntity.getFather().iterator().next().getName());
            Assert.assertEquals("0101900124", personEntity.getMother().iterator().next().getCprNumber());
        } finally {
            session.close();
        }
    }





    /**
     * This unittest validates that it is possible to filter out addresschanges that do not happen based on the eventtype A01
     * @throws DataFordelerException
     * @throws IOException
     */
    @Test
    public void testPersonLoadAndFindBusinessEvent() throws DataFordelerException, IOException {
        Session session = sessionManager.getSessionFactory().openSession();
        ImportMetadata importMetadata = new ImportMetadata();
        importMetadata.setSession(session);
        this.loadPerson("/personsWithEvents.txt", importMetadata);
        session.close();

        session = sessionManager.getSessionFactory().openSession();
        PersonRecordQuery query = new PersonRecordQuery();
        query.setPageSize(100);
        query.setEvent("A01");
        query.setEventTimeAfter("2016-10-26T12:00-06:00");
        query.applyFilters(session);
        List<PersonEntity> entities = QueryManager.getAllEntities(session, query, PersonEntity.class);

        List<String> pnrList = entities.stream().map(x -> x.getPersonnummer()).collect(Collectors.toList());

        List<String> a1Events = Arrays.asList("0101011234", "0101011235", "0101011236", "0101011237", "0101011238"
                , "0101011239", "0101011240");
        Collections.sort(a1Events);
        Collections.sort(pnrList);
        Assert.assertTrue(a1Events.equals(pnrList));


        query = new PersonRecordQuery();
        query.setEvent("A02");
        query.setEventTimeAfter("2016-10-26T12:00-06:00");
        query.applyFilters(session);
        entities = QueryManager.getAllEntities(session, query, PersonEntity.class);

        pnrList = entities.stream().map(x -> x.getPersonnummer()).collect(Collectors.toList());

        List<String> a2Events = Arrays.asList("0101011240", "0101011241", "0101011242", "0101011243");
        Collections.sort(a1Events);
        Collections.sort(pnrList);
        Assert.assertThat(pnrList, Matchers.is(a2Events));

        query = new PersonRecordQuery();
        query.setEvent("A01");
        query.setEventTimeAfter("2020-09-01T12:00-06:00");
        query.applyFilters(session);
        entities = QueryManager.getAllEntities(session, query, PersonEntity.class);

        pnrList = entities.stream().map(x -> x.getPersonnummer()).collect(Collectors.toList());

        List<String> a1Events1 = Arrays.asList("0101011238", "0101011239");
        Collections.sort(a1Events1);
        Collections.sort(pnrList);
        Assert.assertThat(pnrList, Matchers.is(a1Events1));

        query = new PersonRecordQuery();
        query.setEvent("A01");
        query.setEventTimeAfter("2020-09-05T12:00-06:00");
        query.applyFilters(session);
        entities = QueryManager.getAllEntities(session, query, PersonEntity.class);

        pnrList = entities.stream().map(x -> x.getPersonnummer()).collect(Collectors.toList());

        List<String> a1Events2 = Arrays.asList("0101011239");
        Collections.sort(a1Events2);
        Collections.sort(pnrList);
        Assert.assertThat(pnrList, Matchers.is(a1Events2));

        query = new PersonRecordQuery();
        query.setEvent("A01");
        query.setEventTimeAfter("2020-09-10T12:00-06:00");
        query.applyFilters(session);
        entities = QueryManager.getAllEntities(session, query, PersonEntity.class);

        pnrList = entities.stream().map(x -> x.getPersonnummer()).collect(Collectors.toList());

        List<String> a1Events3 = Arrays.asList();
        Collections.sort(a1Events3);
        Collections.sort(pnrList);
        Assert.assertThat(pnrList, Matchers.is(a1Events3));
    }

    /**
     * This unittest validates that it is possible to filter out addresschanges that do not happen based on the eventtype A01
     * @throws DataFordelerException
     * @throws IOException
     */
    @Test
    public void testPersonLoadAndFindDataEvent() throws DataFordelerException, IOException {
        Session session = sessionManager.getSessionFactory().openSession();
        ImportMetadata importMetadata = new ImportMetadata();
        importMetadata.setSession(session);
        this.loadPerson("/personsWithNewAdresses.txt", importMetadata);
        this.loadPerson("/personsWithNewAdresses2.txt", importMetadata);
        this.loadPerson("/personsWithNewAdresses3.txt", importMetadata);
        session.close();

        session = sessionManager.getSessionFactory().openSession();
        PersonRecordQuery query = new PersonRecordQuery();
        query.setPersonnummer("0101011234");
        query.setDataEvent("cpr_person_address_record");
        query.applyFilters(session);
        List<PersonEntity> entities = QueryManager.getAllEntities(session, query, PersonEntity.class);

        Assert.assertEquals(2, entities.get(0).getDataEvent().size());
    }

    /**
     * Confirm that new children does not generate events
     * @throws Exception
     */
    @Test
    public void testPersonWithChildrenAndConfirmNoChildrenEvents() throws Exception {
        //This test will start failing in year 2030 when the children in this test is no longer children
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            ImportMetadata importMetadata = new ImportMetadata();
            importMetadata.setSession(session);
            this.loadPerson("/personWithChildrenAndCustodyChange.txt", importMetadata);
        }

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            PersonRecordQuery query = new PersonRecordQuery();
            query.setPersonnummer("0101011234");
            List<PersonEntity> entities = QueryManager.getAllEntities(session, query, PersonEntity.class);
            Assert.assertEquals(1, entities.size());
            Assert.assertEquals(4, entities.get(0).getChildren().size());
            Assert.assertEquals(0, entities.get(0).getDataEvent().size());
        }
    }

    @Test
    public void testPersonIdempotence() throws Exception {
        Session session = sessionManager.getSessionFactory().openSession();
        ImportMetadata importMetadata = new ImportMetadata();
        importMetadata.setSession(session);
        this.loadPerson("/persondata.txt", importMetadata);
        // TODO: check updated
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

            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this.personRecordOutputWrapper.wrapResult(personEntity, query, OutputWrapper.Mode.LEGACY)));

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
