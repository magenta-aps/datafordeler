package dk.magenta.datafordeler.statistik;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.ConfigurationException;
import dk.magenta.datafordeler.statistik.reportExecution.AssignmentCleaner;
import dk.magenta.datafordeler.statistik.reportExecution.ReportAssignment;
import dk.magenta.datafordeler.statistik.reportExecution.ReportProgressStatus;
import org.apache.jena.base.Sys;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

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
            Transaction t = session.beginTransaction();
            session.persist(reportAssignment);
            t.commit();



            List<ReportAssignment> reportAssignmentList = QueryManager.getAllEntities(session, ReportAssignment.class, false);
            Assertions.assertFalse(reportAssignmentList.isEmpty());

            System.out.println("Before cleanup:");
            for (ReportAssignment reportAssign : reportAssignmentList) {
                System.out.println(reportAssign.getCreateDateTime());
            }
//
//            AssignmentCleaner assignmentCleaner = new AssignmentCleaner(sessionManager.getSessionFactory(), 0);
//            RunnableFuture<Void> task = new FutureTask<>(assignmentCleaner, null);
//            task.run();
//            try {
//                task.get(); // this will block until Runnable completes
//            } catch (InterruptedException | ExecutionException e) {
//                // handle exception
//            }


            System.out.println("BETWEEN");

            AssignmentCleaner.setup(sessionManager.getSessionFactory(), 0, "* * * * * *");

            Thread.sleep(10000);

            System.out.println("WAITED");

            reportAssignmentList = QueryManager.getAllEntities(session, ReportAssignment.class, false);
            System.out.println("\n");
            System.out.println("After cleanup:");
            for (ReportAssignment reportAssign : reportAssignmentList) {
                System.out.println(reportAssign.getCreateDateTime());
            }
            Assertions.assertEquals(0, reportAssignmentList.size());
        } finally {
            AssignmentCleaner.unSchedule();
        }
    }
}
