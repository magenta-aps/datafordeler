package dk.magenta.datafordeler.core.database;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.fapi.ResultSet;
import dk.magenta.datafordeler.plugindemo.fapi.DemoRecordQuery;
import dk.magenta.datafordeler.plugindemo.model.DemoDataRecord;
import dk.magenta.datafordeler.plugindemo.model.DemoEntityRecord;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
public class DatabaseTest {

    @Autowired
    SessionManager sessionManager;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String domain = "test";

    @Test
    public void testGetAllEntities() throws DataFordelerException {
        Session session = sessionManager.getSessionFactory().openSession();
        try {
            Transaction transaction = session.beginTransaction();

            UUID uuid1 = UUID.randomUUID();
            DemoEntityRecord demoEntity1 = new DemoEntityRecord();
            demoEntity1.setPostnr(1455);
            demoEntity1.setIdentification(new Identification(uuid1, domain));
            DemoDataRecord record1 = new DemoDataRecord("København K");
            record1.setBitemporality("2017-02-21T16:02:50+01:00", null, "2017-02-22T13:59:30+01:00", "2017-12-31T23:59:59+01:00");
            demoEntity1.addBitemporalRecord(record1, session);
            session.save(demoEntity1);


            UUID uuid2 = UUID.randomUUID();
            DemoEntityRecord demoEntity2 = new DemoEntityRecord();
            demoEntity2.setPostnr(9999);
            demoEntity2.setIdentification(new Identification(uuid2, domain));
            DemoDataRecord record2 = new DemoDataRecord("NameWith%");
            record2.setBitemporality("2017-02-21T16:02:50+01:00", null, "2017-02-22T13:59:30+01:00", "2017-12-31T23:59:59+01:00");
            demoEntity2.addBitemporalRecord(record2, session);
            session.save(demoEntity2);

            transaction.commit();

            DemoRecordQuery demoQuery1 = new DemoRecordQuery();
            demoQuery1.setPostnr(1455);
            List<DemoEntityRecord> results1 = QueryManager.getAllEntities(session, demoQuery1, DemoEntityRecord.class);
            Assert.assertEquals(1, results1.size());
            Assert.assertEquals(uuid1, results1.get(0).getUUID());

            DemoRecordQuery demoQuery2 = new DemoRecordQuery();
            demoQuery2.setPostnr("1455");
            List<DemoEntityRecord> results2 = QueryManager.getAllEntities(session, demoQuery2, DemoEntityRecord.class);
            Assert.assertEquals(1, results2.size());
            Assert.assertEquals(uuid1, results2.get(0).getUUID());

            DemoRecordQuery demoQuery3 = new DemoRecordQuery();
            demoQuery3.setPostnr("1*");
            List<DemoEntityRecord> results3 = QueryManager.getAllEntities(session, demoQuery3, DemoEntityRecord.class);
            Assert.assertEquals(1, results3.size());
            Assert.assertEquals(uuid1, results3.get(0).getUUID());

            DemoRecordQuery demoQuery4 = new DemoRecordQuery();
            demoQuery4.setPostnr("2*");
            List<DemoEntityRecord> results4 = QueryManager.getAllEntities(session, demoQuery4, DemoEntityRecord.class);
            Assert.assertEquals(0, results4.size());

            DemoRecordQuery demoQuery5 = new DemoRecordQuery();
            demoQuery5.setBynavn("København K");
            List<DemoEntityRecord> results5 = QueryManager.getAllEntities(session, demoQuery5, DemoEntityRecord.class);
            Assert.assertEquals(1, results5.size());
            Assert.assertEquals(uuid1, results5.get(0).getUUID());

            DemoRecordQuery demoQuery6 = new DemoRecordQuery();
            demoQuery6.setBynavn("København*");
            List<DemoEntityRecord> results6 = QueryManager.getAllEntities(session, demoQuery6, DemoEntityRecord.class);
            Assert.assertEquals(1, results6.size());
            Assert.assertEquals(uuid1, results6.get(0).getUUID());

            DemoRecordQuery demoQuery7 = new DemoRecordQuery();
            demoQuery7.setBynavn("Roskilde");
            List<DemoEntityRecord> results7 = QueryManager.getAllEntities(session, demoQuery7, DemoEntityRecord.class);
            Assert.assertEquals(0, results7.size());

            DemoRecordQuery demoQuery8 = new DemoRecordQuery();
            demoQuery8.setBynavn("København K");
            demoQuery8.setPage(2);
            List<DemoEntityRecord> results8 = QueryManager.getAllEntities(session, demoQuery8, DemoEntityRecord.class);
            Assert.assertEquals(0, results8.size());

            DemoRecordQuery demoQuery9 = new DemoRecordQuery();
            demoQuery9.setBynavn("København K");
            demoQuery9.setPageSize(1);
            List<DemoEntityRecord> results9 = QueryManager.getAllEntities(session, demoQuery9, DemoEntityRecord.class);
            Assert.assertEquals(1, results9.size());
            Assert.assertEquals(uuid1, results9.get(0).getUUID());

            DemoRecordQuery demoQuery10 = new DemoRecordQuery();
            demoQuery10.setBynavn("København X");
            List<DemoEntityRecord> results10 = QueryManager.getAllEntities(session, demoQuery10, DemoEntityRecord.class);
            Assert.assertEquals(0, results10.size());

            DemoRecordQuery demoQuery11 = new DemoRecordQuery();
            demoQuery11.setBynavn("NameWith%");
            List<DemoEntityRecord> results11 = QueryManager.getAllEntities(session, demoQuery11, DemoEntityRecord.class);
            Assert.assertEquals(1, results11.size());

            DemoRecordQuery demoQuery12 = new DemoRecordQuery();
            demoQuery12.setBynavn("*With%");
            List<DemoEntityRecord> results12 = QueryManager.getAllEntities(session, demoQuery12, DemoEntityRecord.class);
            Assert.assertEquals(1, results12.size());
        } finally {
            session.close();
        }
    }
}
