package dk.magenta.datafordeler.statistik;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.ConfigurationException;
import dk.magenta.datafordeler.statistik.reportExecution.AssignmentCleaner;
import dk.magenta.datafordeler.statistik.reportExecution.ReportAssignment;
import dk.magenta.datafordeler.statistik.reportExecution.ReportProgressStatus;
import org.hibernate.Session;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AssignmentCleanerTest {

    @Autowired
    SessionManager sessionManager;

    @Test
    public void testRun() throws ConfigurationException, InterruptedException, SchedulerException {
        ReportAssignment reportAssignment = new ReportAssignment();
        reportAssignment.setReason("reason");
        reportAssignment.setReportStatus(ReportProgressStatus.done);
        reportAssignment.setRegistrationAfter("registration after");
        reportAssignment.setRegistrationBefore("registration before");
        reportAssignment.setTemplateName("template");

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.persist(reportAssignment);
            session.getTransaction().commit();

            List<ReportAssignment> reportAssignmentList = QueryManager.getAllEntities(session, ReportAssignment.class, false);
            Assertions.assertFalse(reportAssignmentList.isEmpty());

            AssignmentCleaner.setup(sessionManager.getSessionFactory(), 0, "* * * * *");
            Thread.sleep(1000);

            reportAssignmentList = QueryManager.getAllEntities(session, ReportAssignment.class, false);
            Assertions.assertEquals(0, reportAssignmentList.size());
        } finally {
            AssignmentCleaner.unSchedule();
        }
    }
}
