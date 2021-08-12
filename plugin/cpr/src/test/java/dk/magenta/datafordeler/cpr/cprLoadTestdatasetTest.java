package dk.magenta.datafordeler.cpr;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.io.ImportInputStream;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.LabeledSequenceInputStream;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonEntityManager;
import dk.magenta.datafordeler.cpr.data.person.PersonRecordQuery;
import dk.magenta.datafordeler.cpr.records.person.data.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;

/**
 * Test that it is possible to load and clear data which is dedicated for demopurpose
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class cprLoadTestdatasetTest {


    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private PersonEntityManager personEntityManager;


    private static HashMap<String, String> schemaMap = new HashMap<>();
    static {
        schemaMap.put("person", PersonEntity.schema);
    }

    @SpyBean
    private DafoUserManager dafoUserManager;


    private void loadPersonWithOrigin(ImportMetadata importMetadata) throws DataFordelerException, IOException, URISyntaxException {
        InputStream testData1 = cprLoadTestdatasetTest.class.getResourceAsStream("/GLBASETEST");
        LabeledSequenceInputStream labeledInputStream = new LabeledSequenceInputStream("GLBASETEST", new ByteArrayInputStream("GLBASETEST".getBytes()), "GLBASETEST", testData1);
        ImportInputStream inputstream = new ImportInputStream(labeledInputStream);
        personEntityManager.parseData(inputstream, importMetadata);
        testData1.close();

        InputStream testData2 = cprLoadTestdatasetTest.class.getResourceAsStream("/GLBASETEST2");
        LabeledSequenceInputStream labeledInputStream2 = new LabeledSequenceInputStream("GLBASETEST2", new ByteArrayInputStream("GLBASETEST2".getBytes()), "GLBASETEST2", testData2);
        ImportInputStream inputstream2 = new ImportInputStream(labeledInputStream2);
        personEntityManager.parseData(inputstream2, importMetadata);
        testData2.close();
    }


    @After
    public void clean() {
        Session session = sessionManager.getSessionFactory().openSession();
        session.close();
    }

    /**
     * This test is parly used for the generation of information about persons in testdata
     * @throws DataFordelerException
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void test_A_LoadingOfDemoDataset() throws DataFordelerException, IOException, URISyntaxException {

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            ImportMetadata importMetadata = new ImportMetadata();
            importMetadata.setTransactionInProgress(true);
            importMetadata.setSession(session);
            this.loadPersonWithOrigin(importMetadata);
            tx.commit();
            session.close();
        }

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            PersonRecordQuery query = new PersonRecordQuery();
            query.setEffectToAfter(OffsetDateTime.now());
            query.setEffectFromBefore(OffsetDateTime.now());
            query.setRegistrationToAfter(OffsetDateTime.now());
            query.setRegistrationFromBefore(OffsetDateTime.now());
            /*query.addKommunekode(956);
            query.addKommunekode(960);*/
            query.applyFilters(session);
            query.setPageSize(100);
            List<PersonEntity> persons = QueryManager.getAllEntities(session, query, PersonEntity.class);
            Assert.assertEquals(44, persons.size());

            for(PersonEntity person : persons) {
                System.out.print(person.getPersonnummer());
                if(person.getName().size()>0) {
                    NameDataRecord name = person.getName().iterator().next();
                    System.out.println(" - "+name.getFirstNames()+" "+name.getMiddleName()+" "+name.getLastName());
                }
                if(person.getAddress().size()>0) {
                    AddressDataRecord add = person.getAddress().iterator().next();
                    System.out.print("Kommunekode: "+add.getMunicipalityCode());
                    System.out.print(" Vejkode: "+add.getRoadCode());
                    System.out.println(" Husnummer: "+add.getHouseNumber());

                }
                if(person.getForeignAddress().size()>0) {
                    ForeignAddressDataRecord add = person.getForeignAddress().iterator().next();
                    System.out.print("Udenlandskadresse: "+add.getAddressLine1());
                    System.out.print(" "+add.getAddressLine2());
                    System.out.print(" "+add.getAddressLine3());
                    System.out.println(" "+ add.getAddressLine5());
                }
                System.out.println(person.getPersonnummer());
                Assert.assertEquals(1, person.getCivilstatus().size());//ALWAYS 1
                if(person.getCivilstatus().size()>0) {
                    CivilStatusDataRecord civil = person.getCivilstatus().iterator().next();
                    System.out.println("Civilstand: "+civil.getCivilStatus());
                }
                Assert.assertEquals(1, person.getBirthTime().size());
                if(person.getBirthTime().size()>0) {//ALWAYS 1
                    BirthTimeDataRecord birth = person.getBirthTime().iterator().next();
                    System.out.println("Fødselstidspunkt: "+birth.getBirthDatetime());
                }
                System.out.println("---------------------------------------------");
            }

            query = new PersonRecordQuery();
            query.applyFilters(session);
            query.setPageSize(100);
            persons = QueryManager.getAllEntities(session, PersonEntity.class);
            Assert.assertEquals(44, persons.size());

            query = new PersonRecordQuery();
            query.setPersonnummer("1111111111");
            query.addPersonnummer("1111111112");
            query.addPersonnummer("1111111113");
            persons = QueryManager.getAllEntities(session, query, PersonEntity.class);
            Assert.assertEquals(3, persons.size());
            Assert.assertEquals(2, persons.get(0).getAddress().size());
            Assert.assertEquals(2, persons.get(1).getAddress().size());
            Assert.assertEquals(2, persons.get(2).getAddress().size());
        }
    }


    @Test
    public void test_C_ClearingDemoDataset() throws DataFordelerException, IOException, URISyntaxException {
        personEntityManager.cleanDemoData();
    }

    /**
     * Confirm that all the loaded persons is cleared again
     * @throws DataFordelerException
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void test_D_ReadingDemoDataset() throws DataFordelerException, IOException, URISyntaxException {

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            PersonRecordQuery query = new PersonRecordQuery();
            query.setEffectToAfter(OffsetDateTime.now());
            query.setEffectFromBefore(OffsetDateTime.now());
            query.setRegistrationToAfter(OffsetDateTime.now());
            query.setRegistrationFromBefore(OffsetDateTime.now());
            query.applyFilters(session);
            query.setPageSize(100);
            List<PersonEntity> persons = QueryManager.getAllEntities(session, query, PersonEntity.class);
            Assert.assertEquals(0, persons.size());
        }
    }

}
