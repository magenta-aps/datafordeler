package dk.magenta.datafordeler.cpr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.cpr.data.person.PersonCustodyRelationsManager;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonRecordQuery;
import dk.magenta.datafordeler.cpr.records.person.data.AddressDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.ParentDataRecord;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;


@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RecordTest extends TestBase {

    private void loadPerson(String resource, ImportMetadata importMetadata) throws DataFordelerException, IOException {
        InputStream testData = RecordTest.class.getResourceAsStream(resource);
        personEntityManager.parseData(testData, importMetadata);
        testData.close();
    }

    /**
     * This test reconstructs a situation where a person gets a new address and a new historic address.
     * After that the person gets the same old address again.
     * This test tests that when a new active address is assigned, it closes old historic address
     *
     * @throws DataFordelerException
     * @throws IOException
     */
    @Test
    public void testPersonWithReverts() throws DataFordelerException, IOException {
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            ImportMetadata importMetadata = new ImportMetadata();
            importMetadata.setSession(session);
            this.loadPerson("/personwithReverts.txt", importMetadata);
            
            PersonRecordQuery query = new PersonRecordQuery();
            OffsetDateTime time = OffsetDateTime.now();
            query.setRegistrationToAfter(time);
            query.setEffectAt(time.withYear(2018));
            query.setParameter(PersonRecordQuery.PERSONNUMMER, "0211081111");

            List<PersonEntity> entities = QueryManager.getAllEntities(session, query, PersonEntity.class);
            Assertions.assertEquals(1, entities.size());
            PersonEntity personEntity = entities.get(0);
            List<AddressDataRecord> addressList = personEntity.getAddress().stream().filter(e -> !e.isUndone()).collect(Collectors.toList());
            Assertions.assertEquals(1, addressList.size());
        }
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
            query.setRegistrationAt(time);
            query.setEffectAt(time);
            query.applyFilters(session);

            query.setParameter(PersonRecordQuery.KOMMUNEKODE, 956);
            Assertions.assertEquals(0, QueryManager.getAllEntities(session, query, PersonEntity.class).size());

            query.setParameter(PersonRecordQuery.KOMMUNEKODE, 958);
            Assertions.assertEquals(1, QueryManager.getAllEntities(session, query, PersonEntity.class).size());
            query.clearParameter(PersonRecordQuery.KOMMUNEKODE);

            query.setParameter(PersonRecordQuery.FORNAVNE, "Tester");
            Assertions.assertEquals(1, QueryManager.getAllEntities(session, query, PersonEntity.class).size());
            query.clearParameter(PersonRecordQuery.FORNAVNE);

            query.setParameter(PersonRecordQuery.EFTERNAVN, "Tystersen");
            Assertions.assertEquals(1, QueryManager.getAllEntities(session, query, PersonEntity.class).size());

            query.setParameter(PersonRecordQuery.EFTERNAVN, "Testersen");
            Assertions.assertEquals(0, QueryManager.getAllEntities(session, query, PersonEntity.class).size());

            time = OffsetDateTime.parse("2000-01-01T00:00:00Z");
            query.setEffectFromBefore(time);
            query.setEffectToAfter(time);
            query.applyFilters(session);

            query.setParameter(PersonRecordQuery.EFTERNAVN,"Testersen");
            Assertions.assertEquals(1, QueryManager.getAllEntities(session, query, PersonEntity.class).size());


        } finally {
            session.close();
        }
    }

    @Test
    public void testExperimentPerson() throws DataFordelerException, IOException {
        Session session = sessionManager.getSessionFactory().openSession();
        Assertions.assertEquals(0, QueryManager.getAllEntities(session, PersonEntity.class).size());

        ImportMetadata importMetadata = new ImportMetadata();
        importMetadata.setSession(session);
        this.loadPerson("/persondata.txt", importMetadata);
        
        try {

            PersonRecordQuery query = new PersonRecordQuery();
            OffsetDateTime time = OffsetDateTime.now();
            query.setRegistrationAt(time);
            query.setEffectAt(time);
            query.applyFilters(session);

            Assertions.assertEquals(1, QueryManager.getAllEntities(session, query, PersonEntity.class).size());

            query.setBirthTimeBefore(LocalDateTime.now());
            List<PersonEntity> personList = QueryManager.getAllEntities(session, query, PersonEntity.class);
            Assertions.assertEquals(1, personList.size());

            query.setBirthTimeBefore(null);
            personList = QueryManager.getAllEntities(session, query, PersonEntity.class);
            Assertions.assertEquals(1, personList.size());

            query.setBirthTimeBefore(LocalDateTime.of(1999, Month.JULY, 29, 19, 30, 40));
            personList = QueryManager.getAllEntities(session, query, PersonEntity.class);
            Assertions.assertEquals(0, personList.size());

            query.setBirthTimeBefore(LocalDateTime.of(2001, Month.JULY, 29, 19, 30, 40));
            personList = QueryManager.getAllEntities(session, query, PersonEntity.class);
            Assertions.assertEquals(1, personList.size());

            query.setBirthTimeAfter(LocalDateTime.now());
            personList = QueryManager.getAllEntities(session, query, PersonEntity.class);
            Assertions.assertEquals(0, personList.size());

            query.setBirthTimeAfter(null);
            personList = QueryManager.getAllEntities(session, query, PersonEntity.class);
            Assertions.assertEquals(1, personList.size());

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
     * <p>
     * The person with cpr=0101011234 now has lost custody of one child, but gains custody of another child
     * <p>
     * <p>
     * After calculations the person with cpr=0101011234 has custody over
     * -0101981234 (Should not be returned since the child is more then 18 years old)
     * -0101121234
     * -0101131234
     * -0101161234
     * <p>
     * The person with cpr=0101991234 now has custody over
     * -0101141234
     *
     * @throws DataFordelerException
     * @throws IOException
     */
    @Test
    public void testImportPersonWithChildren() throws DataFordelerException, IOException {
        //This test will start failing in year 2030 when the children in this test is no longer children
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            ImportMetadata importMetadata = new ImportMetadata();
            importMetadata.setSession(session);
            this.loadPerson("/personWithChildrenAndCustodyChange.txt", importMetadata);
            

            PersonRecordQuery query = new PersonRecordQuery();
            query.setParameter(PersonRecordQuery.PERSONNUMMER, "0101011234");
            List<PersonEntity> entities = QueryManager.getAllEntities(session, query, PersonEntity.class);
            PersonEntity personEntity = entities.get(0);
            Assertions.assertEquals(4, personEntity.getChildren().size());
            Assertions.assertEquals(0, personEntity.getCustody().size());
            //Assertions.assertTrue(personEntity.getChildren().stream().anyMatch(child -> child.getChildCprNumber().equals("0101981234")));
            Assertions.assertTrue(personEntity.getChildren().stream().anyMatch(child -> child.getChildCprNumber().equals("0101121234")));
            Assertions.assertTrue(personEntity.getChildren().stream().anyMatch(child -> child.getChildCprNumber().equals("0101141234")));
            Assertions.assertTrue(personEntity.getChildren().stream().anyMatch(child -> child.getChildCprNumber().equals("0101161234")));

            //Find a child and from that the person who has custody over the child
            query = new PersonRecordQuery();
            query.setParameter(PersonRecordQuery.PERSONNUMMER, "0101141234");
            entities = QueryManager.getAllEntities(session, query, PersonEntity.class);
            personEntity = entities.get(0);
            Assertions.assertEquals(0, personEntity.getChildren().size());
            Assertions.assertEquals(2, personEntity.getCustody().size());
            Assertions.assertTrue(personEntity.getCustody().stream().anyMatch(child -> child.getRelationPnr().equals("0101991234")));

            //Find a child and from that the person who has custody over the child
            query = new PersonRecordQuery();
            query.setParameter(PersonRecordQuery.PERSONNUMMER, "0101131234");
            entities = QueryManager.getAllEntities(session, query, PersonEntity.class);
            personEntity = entities.get(0);
            Assertions.assertEquals(0, personEntity.getChildren().size());
            Assertions.assertEquals(1, personEntity.getCustody().size());
            Assertions.assertTrue(personEntity.getCustody().stream().anyMatch(child -> child.getRelationPnr().equals("0101011234")));

            //Find a parent-ish and from that the child-ish custody relation
            query = new PersonRecordQuery();
            query.setParameter(PersonRecordQuery.CUSTODYPNR, "0101991234");
            entities = QueryManager.getAllEntities(session, query, PersonEntity.class);
            personEntity = entities.get(0);
            Assertions.assertEquals("0101141234", personEntity.getPersonnummer());

            //Find a parent-ish and from that the child-ish custody relation
            query = new PersonRecordQuery();
            query.setParameter(PersonRecordQuery.CUSTODYPNR, "0101011234");
            entities = QueryManager.getAllEntities(session, query, PersonEntity.class);
            personEntity = entities.get(0);
            Assertions.assertEquals("0101131234", personEntity.getPersonnummer());

            //Find collective custody of the person '0101011234'
            //0101981234 (Should not be returned since the child is more then 18 years old)
            //0101121234
            //0101131234
            //0101161234
            List<PersonCustodyRelationsManager.ChildInfo> custodyList = custodyManager.findRelations("0101011234");
            Assertions.assertEquals(3, custodyList.size());
            Assertions.assertFalse(custodyList.stream().anyMatch(child -> child.getPnr().equals("0101981234")));
            Assertions.assertTrue(custodyList.stream().anyMatch(child -> child.getPnr().equals("0101121234")));
            Assertions.assertTrue(custodyList.stream().anyMatch(child -> child.getPnr().equals("0101131234")));
            Assertions.assertTrue(custodyList.stream().anyMatch(child -> child.getPnr().equals("0101161234")));

            List<PersonCustodyRelationsManager.ChildInfo> custodyListFather = custodyManager.findRelations("0101011235");
            //Assertions.assertEquals(3, custodyListFather.size());
            Assertions.assertFalse(custodyListFather.stream().anyMatch(child -> child.getPnr().equals("0101981234")));
            Assertions.assertTrue(custodyListFather.stream().anyMatch(child -> child.getPnr().equals("0101121234")));
            Assertions.assertTrue(custodyListFather.stream().anyMatch(child -> child.getPnr().equals("0101141234")));
            Assertions.assertTrue(custodyListFather.stream().anyMatch(child -> child.getPnr().equals("0101161234")));

            //Find collective custody of the person '0101011234'
            //0101981234
            List<PersonCustodyRelationsManager.ChildInfo> custodyList2 = custodyManager.findRelations("0101991234");
            Assertions.assertEquals(1, custodyList2.size());
            Assertions.assertFalse(custodyList2.stream().anyMatch(child -> child.getPnr().equals("0101981234")));
            Assertions.assertTrue(custodyList2.stream().anyMatch(child -> child.getPnr().equals("0101141234")));
        }
    }


    @Test
    public void testFindSiblings() throws Exception {

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            ImportMetadata importMetadata = new ImportMetadata();
            importMetadata.setSession(session);
            this.loadPerson("/personsWithEvents.txt", importMetadata);

            PersonRecordQuery query = new PersonRecordQuery();
            query.setParameter(PersonRecordQuery.PERSONNUMMER, "0101011234");
            List<PersonEntity> entities = QueryManager.getAllEntities(session, query, PersonEntity.class);
            PersonEntity personEntity = entities.get(0);

            String fatherPnr = personEntity.getFather().current().get(0).getCprNumber();
            String motherPnr = personEntity.getMother().current().get(0).getCprNumber();

            String hql = "SELECT personEntity " +
                    "FROM " + PersonEntity.class.getCanonicalName() + " personEntity " +
                    "JOIN " + ParentDataRecord.class.getCanonicalName() + " mother ON mother." + ParentDataRecord.DB_FIELD_ENTITY + "=personEntity " +
                    "JOIN " + ParentDataRecord.class.getCanonicalName() + " father ON father." + ParentDataRecord.DB_FIELD_ENTITY + "=personEntity " +
                    "WHERE mother." + ParentDataRecord.DB_FIELD_CPR_NUMBER + "='" + motherPnr + "' " +
                    "AND father." + ParentDataRecord.DB_FIELD_CPR_NUMBER + "='" + fatherPnr + "'";


            Query<PersonEntity> query2 = session.createQuery(hql, PersonEntity.class);

            List<PersonEntity> resultList = query2.getResultList();
            Assertions.assertEquals(17, resultList.size());
            Set<String> personnumre = resultList.stream().map(PersonEntity::getPersonnummer).collect(Collectors.toSet());
            Set<String> expected = new HashSet<>();
            for (int i=101011234; i<=101011250; i++) {
                expected.add("0" + i);
            }
            Assertions.assertEquals(expected, personnumre);
        }

    }


    @Test
    public void testCallCustodyService() throws Exception {
        //This test will start failing in year 2030 when the children in this test is no longer children
        try (Session session = sessionManager.getSessionFactory().openSession()) {
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
        Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        //Try fetching with cpr access rights
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);
        response = restTemplate.exchange(
                "/cpr/person/custody/1/rest/" + "0101011234",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(3, objectMapper.readTree(response.getBody()).get("children").size());
        JSONAssert.assertEquals("{\"parent\":\"0101011234\",\"children\":[{\"pnr\":\"0101161234\",\"status\":1},{\"pnr\":\"0101121234\",\"status\":1},{\"pnr\":\"0101131234\",\"status\":1}]}", response.getBody(), false);


        //Try fetching other persons custody
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);
        response = restTemplate.exchange(
                "/cpr/person/custody/1/rest/" + "0101991234",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(1, objectMapper.readTree(response.getBody()).get("children").size());
        JSONAssert.assertEquals("{\"parent\":\"0101991234\",\"children\":[{\"pnr\":\"0101141234\",\"status\":1}]}", response.getBody(), false);
    }


    /**
     * Vi tester en special case hvor en person med et undone navn, får tildelt dette navn igen
     * <p>
     * Ret / fortryd markering (ANNKOR) kan være:
     * K = Ret
     * A = Fortryd
     * Æ = Teknisk ændring
     *
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
            query.setParameter(PersonRecordQuery.PERSONNUMMER, "1111111111");
            List<PersonEntity> entities = QueryManager.getAllEntities(session, query, PersonEntity.class);
            PersonEntity personEntity = entities.get(0);
            Assertions.assertTrue(
                    personEntity.getName().stream().anyMatch(name -> !name.isUndone()),
                    "Validate that when setting a name and undoing that name, it must be possible to set that same name again "
            );

        } finally {
            session.close();
        }
    }

    @Test
    public void testPersonWithUndoneNewAddress() throws DataFordelerException, IOException {
        Session session = sessionManager.getSessionFactory().openSession();
        ImportMetadata importMetadata = new ImportMetadata();
        Transaction transaction = session.beginTransaction();
        importMetadata.setTransactionInProgress(true);
        importMetadata.setSession(session);
        this.loadPerson("/undoneNewAdress1.txt", importMetadata);
        this.loadPerson("/undoneNewAdress2.txt", importMetadata);
        transaction.commit();
        
        try {

            PersonRecordQuery query = new PersonRecordQuery();
            query.setParameter(PersonRecordQuery.PERSONNUMMER, "0101011234");
            List<PersonEntity> entities = QueryManager.getAllEntities(session, query, PersonEntity.class);
            PersonEntity personEntity = entities.get(0);

            Assertions.assertTrue(
                    personEntity.getAddress().stream().anyMatch(add -> !add.isUndone() && add.getRegistrationTo() == null && add.getEffectTo() == null),
                    "Validate that the address is still correct after undoing a new adress "
            );

        } finally {
            session.close();
        }
    }


    @Test
    public void testPersonCorrectHandlingOfClosingRecords() throws DataFordelerException, IOException {
        Session session = sessionManager.getSessionFactory().openSession();
        ImportMetadata importMetadata = new ImportMetadata();
        Transaction transaction = session.beginTransaction();
        importMetadata.setTransactionInProgress(true);
        importMetadata.setSession(session);
        this.loadPerson("/d111111.111111", importMetadata);
        this.loadPerson("/d111111.111118", importMetadata);

        this.loadPerson("/d111114.111110", importMetadata);
        this.loadPerson("/d111114.111111", importMetadata);
        this.loadPerson("/d111114.111112", importMetadata);

        transaction.commit();
        try {

            PersonRecordQuery query = new PersonRecordQuery();
            query.setParameter(PersonRecordQuery.PERSONNUMMER, "1111111111");
            List<PersonEntity> entities = QueryManager.getAllEntities(session, query, PersonEntity.class);
            PersonEntity personEntity = entities.get(0);

            List<AddressDataRecord> list = personEntity.getAddress().stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());

            Assertions.assertTrue(
                    personEntity.getAddress().stream().anyMatch(add -> !add.isUndone() && add.getRegistrationTo() == null && add.getEffectTo() == null),
                    "Validate that the person has an active address "
            );

            query.setParameter(PersonRecordQuery.PERSONNUMMER, "1111111113");
            entities = QueryManager.getAllEntities(session, query, PersonEntity.class);
            personEntity = entities.get(0);

            Assertions.assertFalse(
                    personEntity.getAddress().stream().anyMatch(add -> !add.isUndone() && add.getRegistrationTo() == null && add.getEffectTo() == null),
                    "Validate that the person does not have an active address"
            );

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
            query.setRegistrationAt(time);
            query.setEffectAt(time);
            query.applyFilters(session);

            Assertions.assertEquals(1, QueryManager.getAllEntities(session, query, PersonEntity.class).size());

            query.setParameter(PersonRecordQuery.VEJKODE, 2);
            Assertions.assertEquals(0, QueryManager.getAllEntities(session, query, PersonEntity.class).size());
            query.clearParameter(PersonRecordQuery.VEJKODE);

            query.setParameter(PersonRecordQuery.VEJKODE, 111);
            Assertions.assertEquals(1, QueryManager.getAllEntities(session, query, PersonEntity.class).size());
            query.clearParameter(PersonRecordQuery.VEJKODE);

            query.setParameter(PersonRecordQuery.HOUSENO, "2");
            Assertions.assertEquals(0, QueryManager.getAllEntities(session, query, PersonEntity.class).size());
            query.clearParameter(PersonRecordQuery.HOUSENO);

            query.setParameter(PersonRecordQuery.HOUSENO, "3");
            Assertions.assertEquals(1, QueryManager.getAllEntities(session, query, PersonEntity.class).size());
            query.clearParameter(PersonRecordQuery.HOUSENO);

            query.setParameter(PersonRecordQuery.FLOOR, "01");
            Assertions.assertEquals(0, QueryManager.getAllEntities(session, query, PersonEntity.class).size());
            query.clearParameter(PersonRecordQuery.FLOOR);

            query.setParameter(PersonRecordQuery.FLOOR, "02");
            Assertions.assertEquals(1, QueryManager.getAllEntities(session, query, PersonEntity.class).size());
            query.clearParameter(PersonRecordQuery.FLOOR);

            query.setParameter(PersonRecordQuery.DOOR, "3");
            Assertions.assertEquals(0, QueryManager.getAllEntities(session, query, PersonEntity.class).size());
            query.clearParameter(PersonRecordQuery.DOOR);

            query.setParameter(PersonRecordQuery.DOOR, "4");
            Assertions.assertEquals(1, QueryManager.getAllEntities(session, query, PersonEntity.class).size());
            query.clearParameter(PersonRecordQuery.DOOR);

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
            query.setParameter(PersonRecordQuery.PERSONNUMMER, "0101001234");
            List<PersonEntity> entities = QueryManager.getAllEntities(session, query, PersonEntity.class);
            PersonEntity personEntity = entities.get(0);
            Assertions.assertEquals(1, personEntity.getConame().size());
            Assertions.assertEquals(1, personEntity.getAddress().size());
            Assertions.assertEquals(1, personEntity.getBirthPlace().size());
            Assertions.assertEquals(0, personEntity.getBirthPlaceVerification().size());
            Assertions.assertEquals(1, personEntity.getBirthTime().size());
            Assertions.assertEquals(4, personEntity.getChurchRelation().size());
            Assertions.assertEquals(3, personEntity.getChurchRelationVerification().size());
            Assertions.assertEquals(0, personEntity.getCivilstatus().size());
            Assertions.assertEquals(0, personEntity.getCivilstatusAuthorityText().size());
            Assertions.assertEquals(0, personEntity.getCivilstatusVerification().size());
            Assertions.assertEquals(1, personEntity.getForeignAddress().size());
            Assertions.assertEquals(1, personEntity.getEmigration().size());
            Assertions.assertEquals(0, personEntity.getMunicipalityMove().size());
            Assertions.assertEquals(3, personEntity.getNameAuthorityText().size());
            Assertions.assertEquals(1, personEntity.getMother().size());
            Assertions.assertEquals(1, personEntity.getMotherVerification().size());
            Assertions.assertEquals(1, personEntity.getFather().size());
            Assertions.assertEquals(1, personEntity.getFatherVerification().size());
            Assertions.assertEquals(1, personEntity.getCore().size());
            Assertions.assertEquals(0, personEntity.getPosition().size());
            Assertions.assertEquals(3, personEntity.getStatus().size());
            Assertions.assertEquals(3, personEntity.getProtection().size());

        } finally {
            session.close();
        }
    }

    /**
     * Test that when new addresses is added to a person the former added adresses is bitemporally closed
     *
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
            query.setParameter(PersonRecordQuery.PERSONNUMMER, "0101010123");
            OffsetDateTime time = OffsetDateTime.now();
            query.setEffectToAfter(time);
            query.setRegistrationToAfter(time);
            query.applyFilters(session);
            List<PersonEntity> entities = QueryManager.getAllEntities(session, query, PersonEntity.class);
            PersonEntity personEntity = entities.get(0);
            Assertions.assertEquals(1, personEntity.getAddress().size());
            Assertions.assertEquals("F", personEntity.getAddress().iterator().next().getBuildingNumber());
        } finally {
            session.close();
        }
    }

    /**
     * Test that when new addresses is added to a person the former added adresses is bitemporally closed
     *
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
            query.setParameter(PersonRecordQuery.PERSONNUMMER, "0101010123");
            OffsetDateTime time = OffsetDateTime.now();
            query.setEffectToAfter(time);
            query.setRegistrationToAfter(time);
            query.applyFilters(session);

            List<PersonEntity> entities = QueryManager.getAllEntities(session, query, PersonEntity.class);
            PersonEntity personEntity = entities.get(0);
            Assertions.assertEquals(1, personEntity.getCivilstatus().size());
            Assertions.assertEquals("0101010124", personEntity.getCivilstatus().iterator().next().getSpouseCpr());


        } finally {
            session.close();
        }
    }

    /**
     * Test that when new addresses is added to a person the former added adresses is bitemporally closed
     *
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
            query.setParameter(PersonRecordQuery.PERSONNUMMER, "0101010123");
            OffsetDateTime time = OffsetDateTime.now();

            query.setEffectToAfter(time);
            query.setRegistrationToAfter(time);
            query.applyFilters(session);

            List<PersonEntity> entities = QueryManager.getAllEntities(session, query, PersonEntity.class);
            PersonEntity personEntity = entities.get(0);
            Assertions.assertEquals(1, personEntity.getFather().size());
            Assertions.assertEquals(1, personEntity.getMother().size());
            Assertions.assertEquals("0101900125", personEntity.getFather().iterator().next().getCprNumber());
            Assertions.assertEquals("Bo", personEntity.getFather().iterator().next().getName());
            Assertions.assertEquals("0101900124", personEntity.getMother().iterator().next().getCprNumber());
        } finally {
            session.close();
        }
    }


    /**
     * This unittest validates that it is possible to filter out addresschanges that do not happen based on the eventtype A01
     *
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
        Assertions.assertTrue(a1Events.equals(pnrList));


        query = new PersonRecordQuery();
        query.setEvent("A02");
        query.setEventTimeAfter("2016-10-26T12:00-06:00");
        query.applyFilters(session);
        entities = QueryManager.getAllEntities(session, query, PersonEntity.class);

        pnrList = entities.stream().map(x -> x.getPersonnummer()).collect(Collectors.toList());

        List<String> a2Events = Arrays.asList("0101011240", "0101011241", "0101011242", "0101011243");
        Collections.sort(a1Events);
        Collections.sort(pnrList);
        Assertions.assertEquals(pnrList, a2Events);

        query = new PersonRecordQuery();
        query.setEvent("A01");
        query.setEventTimeAfter("2020-09-01T12:00-06:00");
        query.applyFilters(session);
        entities = QueryManager.getAllEntities(session, query, PersonEntity.class);

        pnrList = entities.stream().map(x -> x.getPersonnummer()).collect(Collectors.toList());

        List<String> a1Events1 = Arrays.asList("0101011238", "0101011239");
        Collections.sort(a1Events1);
        Collections.sort(pnrList);
        Assertions.assertEquals(pnrList, a1Events1);

        query = new PersonRecordQuery();
        query.setEvent("A01");
        query.setEventTimeAfter("2020-09-05T12:00-06:00");
        query.applyFilters(session);
        entities = QueryManager.getAllEntities(session, query, PersonEntity.class);

        pnrList = entities.stream().map(x -> x.getPersonnummer()).collect(Collectors.toList());

        List<String> a1Events2 = Arrays.asList("0101011239");
        Collections.sort(a1Events2);
        Collections.sort(pnrList);
        Assertions.assertEquals(pnrList, a1Events2);

        query = new PersonRecordQuery();
        query.setEvent("A01");
        query.setEventTimeAfter("2020-09-10T12:00-06:00");
        query.applyFilters(session);
        entities = QueryManager.getAllEntities(session, query, PersonEntity.class);

        pnrList = entities.stream().map(x -> x.getPersonnummer()).collect(Collectors.toList());

        List<String> a1Events3 = Arrays.asList();
        Collections.sort(a1Events3);
        Collections.sort(pnrList);
        Assertions.assertEquals(pnrList, a1Events3);
    }

    /**
     * This unittest validates that it is possible to filter out addresschanges that do not happen based on the eventtype A01
     *
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
        query.setParameter(PersonRecordQuery.PERSONNUMMER, "0101011234");
        query.setDataEvent("cpr_person_address_record");
        query.applyFilters(session);
        List<PersonEntity> entities = QueryManager.getAllEntities(session, query, PersonEntity.class);

        Assertions.assertEquals(2, entities.get(0).getDataEvent().size());
    }

    /**
     * Confirm that new children does not generate events
     *
     * @throws Exception
     */
    @Test
    public void testPersonWithChildrenAndConfirmNoChildrenEvents() throws Exception {
        //This test will start failing in year 2030 when the children in this test is no longer children
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            ImportMetadata importMetadata = new ImportMetadata();
            importMetadata.setSession(session);
            this.loadPerson("/personWithChildrenAndCustodyChange.txt", importMetadata);
            
        }

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            PersonRecordQuery query = new PersonRecordQuery();
            query.setParameter(PersonRecordQuery.PERSONNUMMER, "0101011234");
            List<PersonEntity> entities = QueryManager.getAllEntities(session, query, PersonEntity.class);
            Assertions.assertEquals(1, entities.size());
            Assertions.assertEquals(4, entities.get(0).getChildren().size());
            Assertions.assertEquals(0, entities.get(0).getDataEvent().size());
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
        Assertions.assertEquals(403, resp.getStatusCodeValue());

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

            query.setParameter(PersonRecordQuery.KOMMUNEKODE, 958);
            PersonEntity personEntity = QueryManager.getAllEntities(session, query, PersonEntity.class).get(0);

            //System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this.personRecordOutputWrapper.wrapResult(personEntity, query, OutputWrapper.Mode.LEGACY)));

        } finally {
            session.close();
        }
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
}
