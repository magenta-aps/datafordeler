package dk.magenta.datafordeler.core.io;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.Engine;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.exception.DuplicateSequenceNumberException;
import dk.magenta.datafordeler.core.exception.MismatchingRegistrationBoundaryException;
import dk.magenta.datafordeler.core.exception.SkippedSequenceNumberException;
import dk.magenta.datafordeler.core.gapi.GapiTestBase;
import dk.magenta.datafordeler.core.io.storagetest.TestData;
import dk.magenta.datafordeler.core.io.storagetest.TestEffect;
import dk.magenta.datafordeler.core.io.storagetest.TestEntity;
import dk.magenta.datafordeler.core.io.storagetest.TestRegistration;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StorageTest extends GapiTestBase {

    @Autowired
    SessionManager sessionManager;

    @Autowired
    Engine engine;

    @Rule
    public final ExpectedException exception = ExpectedException.none();


    private static final String domain = "test";

    @Test
    public void storageTest() throws DataFordelerException {
        UUID uuid = UUID.randomUUID();
        TestEntity testEntity = new TestEntity(uuid, domain);
        TestRegistration testRegistration = new TestRegistration("2017-02-21T16:02:50+01:00", null, 1);
        TestEffect testEffect1 = new TestEffect(testRegistration, "2017-02-22T13:59:30+01:00", "2017-12-31T23:59:59+01:00");
        TestEffect testEffect2 = new TestEffect(testRegistration, "2017-02-22T13:59:30+01:00", "2017-12-31T23:59:59+01:00");
        TestEffect testEffect3 = new TestEffect(testRegistration, "2017-02-22T13:59:30+01:00", "2017-12-31T23:59:59+01:00");
        TestEffect testEffect4 = new TestEffect(testRegistration, "2017-12-31T23:59:59+01:00", "2018-12-31T23:59:59+01:00");
        TestEffect testEffect5 = new TestEffect(testRegistration, "2017-12-31T23:59:59+01:00", "2018-12-31T23:59:59+01:00");
        TestEffect testEffect6 = new TestEffect(testRegistration, "2017-12-31T23:59:59+01:00", "2018-12-31T23:59:59+01:00");

        TestData testData1 = new TestData(8000, "Århus C");
        testData1.addEffect(testEffect1);
        TestData testData2 = new TestData(8200, "Århus N");
        testData2.addEffect(testEffect2);
        TestData testData3 = new TestData(8210, "Århus V");
        testData3.addEffect(testEffect3);
        TestData testData4 = new TestData(8000, "Aarhus C");
        testData4.addEffect(testEffect4);
        TestData testData5 = new TestData(8200, "Aarhus N");
        testData5.addEffect(testEffect5);
        TestData testData6 = new TestData(8210, "Aarhus V");
        testData6.addEffect(testEffect6);

        Session session = sessionManager.getSessionFactory().openSession();
        try {
            Transaction transaction = session.beginTransaction();
            QueryManager.saveRegistration(session, testEntity, testRegistration);

            Assert.assertEquals(2, testRegistration.getEffects().size());

            for (TestEffect testEffect : testRegistration.getEffects()) {
                Assert.assertEquals(3, testEffect.getDataItems().size());
            }

            transaction.commit();
        } finally {
            session.close();
        }

        this.deleteEntity(uuid);
    }


    @Test
    public void duplicateSequenceNumberTest() throws DataFordelerException {
        exception.expect(DuplicateSequenceNumberException.class);
        UUID uuid = UUID.randomUUID();
        TestEntity testEntity = new TestEntity(uuid, domain);
        TestRegistration testRegistration1 = new TestRegistration("2017-02-21T16:02:50+01:00", "2017-06-01T00:00:00+01:00", 1);
        testRegistration1.setRegisterChecksum(UUID.randomUUID().toString());
        TestEffect testEffect1 = new TestEffect(testRegistration1, "2017-02-22T13:59:30+01:00", "2017-12-31T23:59:59+01:00");
        TestData testData1 = new TestData(8000, "Århus C");
        testData1.addEffect(testEffect1);

        TestRegistration testRegistration2 = new TestRegistration("2017-06-01T00:00:00+01:00", null, 1);
        testRegistration2.setRegisterChecksum(UUID.randomUUID().toString());
        TestEffect testEffect2 = new TestEffect(testRegistration2, "2017-02-22T13:59:30+01:00", "2017-12-31T23:59:59+01:00");
        TestData testData2 = new TestData(8000, "Århus C");
        testData2.addEffect(testEffect2);

        Session session = sessionManager.getSessionFactory().openSession();
        try {
            Transaction transaction = session.beginTransaction();
            QueryManager.saveRegistration(session, testEntity, testRegistration1);
            session.flush();
            QueryManager.saveRegistration(session, testEntity, testRegistration2);
            transaction.commit();
        } finally {
            session.close();
        }

        this.deleteEntity(uuid);
    }


    @Test
    public void mismatchingRegistrationBoundaryTest() throws DataFordelerException {
        exception.expect(MismatchingRegistrationBoundaryException.class);
        UUID uuid = UUID.randomUUID();
        TestEntity testEntity = new TestEntity(uuid, domain);
        TestRegistration testRegistration1 = new TestRegistration("2017-02-21T16:02:50+01:00", "2017-06-02T00:00:00+01:00", 1);
        testRegistration1.setRegisterChecksum(UUID.randomUUID().toString());
        TestEffect testEffect1 = new TestEffect(testRegistration1, "2017-02-22T13:59:30+01:00", "2017-12-31T23:59:59+01:00");
        TestData testData1 = new TestData(8000, "Århus C");
        testData1.addEffect(testEffect1);

        TestRegistration testRegistration2 = new TestRegistration("2017-06-01T00:00:00+01:00", null, 2);
        testRegistration2.setRegisterChecksum(UUID.randomUUID().toString());
        TestEffect testEffect2 = new TestEffect(testRegistration2, "2017-02-22T13:59:30+01:00", "2017-12-31T23:59:59+01:00");
        TestData testData2 = new TestData(8000, "Århus C");
        testData2.addEffect(testEffect2);

        Session session = sessionManager.getSessionFactory().openSession();
        try {
            Transaction transaction = session.beginTransaction();
            QueryManager.saveRegistration(session, testEntity, testRegistration1);
            QueryManager.saveRegistration(session, testEntity, testRegistration2);
            transaction.commit();
        } finally {
            session.close();
        }

        this.deleteEntity(uuid);
    }

    @Ignore
    @Test
    public void SkippedSequenceNumberTest() throws DataFordelerException {
        exception.expect(SkippedSequenceNumberException.class);
        UUID uuid = UUID.randomUUID();
        TestEntity testEntity = new TestEntity(uuid, domain);
        TestRegistration testRegistration1 = new TestRegistration("2017-02-21T16:02:50+01:00", "2017-06-01T00:00:00+01:00", 1);
        testRegistration1.setRegisterChecksum(UUID.randomUUID().toString());
        TestEffect testEffect1 = new TestEffect(testRegistration1, "2017-02-22T13:59:30+01:00", "2017-12-31T23:59:59+01:00");
        TestData testData1 = new TestData(8000, "Århus C");
        testData1.addEffect(testEffect1);

        TestRegistration testRegistration2 = new TestRegistration("2017-06-01T00:00:00+01:00", null, 3);
        testRegistration2.setRegisterChecksum(UUID.randomUUID().toString());
        TestEffect testEffect2 = new TestEffect(testRegistration2, "2017-02-22T13:59:30+01:00", "2017-12-31T23:59:59+01:00");
        TestData testData2 = new TestData(8000, "Århus C");
        testData2.addEffect(testEffect2);

        Session session = sessionManager.getSessionFactory().openSession();
        try {
            Transaction transaction = session.beginTransaction();
            QueryManager.saveRegistration(session, testEntity, testRegistration1);
            QueryManager.saveRegistration(session, testEntity, testRegistration2);
            transaction.commit();
        } finally {
            session.close();
        }

        this.deleteEntity(uuid);
    }

}