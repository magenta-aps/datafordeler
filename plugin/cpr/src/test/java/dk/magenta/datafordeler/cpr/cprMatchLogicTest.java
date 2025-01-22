package dk.magenta.datafordeler.cpr;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.fapi.Query;
import dk.magenta.datafordeler.core.io.ImportInputStream;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.util.LabeledSequenceInputStream;
import dk.magenta.datafordeler.cpr.configuration.CprConfiguration;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonEntityManager;
import dk.magenta.datafordeler.cpr.data.person.PersonRecordQuery;
import dk.magenta.datafordeler.cpr.records.person.data.AddressDataRecord;
import org.hibernate.Session;
import org.junit.After;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class cprMatchLogicTest extends TestBase {

    private void loadPersonWithOrigin(ImportMetadata importMetadata) throws DataFordelerException, IOException, URISyntaxException {
        personEntityManager.getRegisterManager().getConfigurationManager().getConfiguration().setPersonRegisterDataCharset(CprConfiguration.Charset.UTF_8);

        InputStream testData1 = cprMatchLogicTest.class.getResourceAsStream("/p1beforeTimestampRemove.txt");
        LabeledSequenceInputStream ll1 = new LabeledSequenceInputStream("d190611.l5555", new ByteArrayInputStream("d190611.l534901".getBytes()), "d190611.l534901", testData1);
        ImportInputStream inp1 = new ImportInputStream(ll1);
        personEntityManager.parseData(inp1, importMetadata);
        testData1.close();

        InputStream testData2 = cprMatchLogicTest.class.getResourceAsStream("/p1AfterTimestampRemove.txt");
        LabeledSequenceInputStream ll2 = new LabeledSequenceInputStream("d190912.l5555", new ByteArrayInputStream("d190612.l534901".getBytes()), "d190611.l534901", testData2);
        ImportInputStream inp2 = new ImportInputStream(ll2);
        personEntityManager.parseData(inp2, importMetadata);
        testData2.close();
    }

    @Test
    public void testSameAdressWhenTimestampIsRemoved() throws DataFordelerException, IOException, URISyntaxException {

        TimeZone.setDefault(TimeZone.getTimeZone("America/Godthab"));

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            ImportMetadata importMetadata = new ImportMetadata();
            importMetadata.setSession(session);
            this.loadPersonWithOrigin(importMetadata);
            session.close();
        }


        try (Session session = sessionManager.getSessionFactory().openSession()) {
            PersonRecordQuery query = new PersonRecordQuery();
            query.setParameter(PersonRecordQuery.PERSONNUMMER, "1111111111");
            OffsetDateTime now = Query.parseDateTime("2018-08-08");


            query.setRegistrationAt(OffsetDateTime.now());
            query.setEffectAt(now);

            query.applyFilters(session);
            List<PersonEntity> persons = QueryManager.getAllEntities(session, query, PersonEntity.class);
            Assert.assertEquals(1, persons.size());

            Set<AddressDataRecord> adresses = persons.get(0).getAddress();

            Set<AddressDataRecord> adresses2 = adresses.stream().filter(d -> !d.isUndone()).collect(Collectors.toSet());
            Assert.assertEquals(1, adresses2.size());
        }
    }

}
