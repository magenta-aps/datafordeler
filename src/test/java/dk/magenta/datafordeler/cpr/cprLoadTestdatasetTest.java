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
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
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
        LabeledSequenceInputStream ll1 = new LabeledSequenceInputStream("GLBASETEST", new ByteArrayInputStream("GLBASETEST".getBytes()), "GLBASETEST", testData1);
        ImportInputStream inp1 = new ImportInputStream(ll1);
        personEntityManager.parseData(inp1, importMetadata);
        testData1.close();
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
    public void testLoadingOfTestdataset() throws DataFordelerException, IOException, URISyntaxException {

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            ImportMetadata importMetadata = new ImportMetadata();
            importMetadata.setSession(session);
            this.loadPersonWithOrigin(importMetadata);
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

            Assert.assertEquals(39, persons.size());

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
        }
    }
}
