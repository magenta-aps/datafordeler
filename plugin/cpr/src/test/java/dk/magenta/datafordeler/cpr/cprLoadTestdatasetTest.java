package dk.magenta.datafordeler.cpr;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.io.ImportInputStream;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.util.LabeledSequenceInputStream;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonRecordQuery;
import org.hibernate.Session;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Test that it is possible to load and clear data which is dedicated for demopurpose
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class cprLoadTestdatasetTest extends TestBase {

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

        InputStream testData3 = cprLoadTestdatasetTest.class.getResourceAsStream("/GLBASETEST_MEDCOM");
        LabeledSequenceInputStream labeledInputStream3 = new LabeledSequenceInputStream("GLBASETEST_MEDCOM", new ByteArrayInputStream("GLBASETEST_MEDCOM".getBytes()), "GLBASETEST_MEDCOM", testData3);
        ImportInputStream inputstream3 = new ImportInputStream(labeledInputStream3);
        personEntityManager.parseData(inputstream3, importMetadata);
        testData3.close();
    }

    /**
     * Confirm that all the loaded persons is cleared again
     *
     */
    @Test
    public void test_D_ReadingDemoDataset() {
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            PersonRecordQuery query = new PersonRecordQuery();
            query.setEffectAt(OffsetDateTime.now());
            query.setRegistrationAt(OffsetDateTime.now());
            query.applyFilters(session);
            query.setPageSize(100);
            List<PersonEntity> persons = QueryManager.getAllEntities(session, query, PersonEntity.class);
            Assert.assertEquals(0, persons.size());
        }
    }

}
