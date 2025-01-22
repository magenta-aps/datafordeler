package dk.magenta.datafordeler.statistik;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.statistik.reportExecution.ReportAssignment;
import dk.magenta.datafordeler.statistik.reportExecution.ReportProgressStatus;
import dk.magenta.datafordeler.statistik.reportExecution.ReportSyncHandler;
import dk.magenta.datafordeler.statistik.services.BirthDataService;
import dk.magenta.datafordeler.statistik.services.StatisticsService;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.jpa.QueryHints;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReportProgressServiceTest extends TestBase {

    @Autowired
    private BirthDataService birthDataService;//Just one of the reportservices to use in test

    @Test
    public void testQueueReport() throws Exception {

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            List<ReportAssignment> existingSubscriptions = QueryManager.getAllItems(session, ReportAssignment.class);
            Assert.assertEquals(0, existingSubscriptions.size());
        }

        String reportUuid;
        String reportCollectionUuid;

        try (Session sessionSync = sessionManager.getSessionFactory().openSession()) {
            ReportSyncHandler repSync = new ReportSyncHandler(sessionSync);
            ReportAssignment report = new ReportAssignment();
            reportUuid = report.getReportUuid();
            reportCollectionUuid = report.getCollectionUuid();
            report.setTemplateName("REPORT1");
            Assert.assertTrue(repSync.createReportStatusObject(report));
        }

        try (Session sessionSync = sessionManager.getSessionFactory().openSession()) {

            ReportSyncHandler repSync = new ReportSyncHandler(sessionSync);

            CriteriaBuilder builder = sessionSync.getCriteriaBuilder();
            CriteriaQuery<ReportAssignment> criteria = builder.createQuery(ReportAssignment.class);
            Root<ReportAssignment> page = criteria.from(ReportAssignment.class);
            criteria.select(page);
            criteria.where(builder.and(
                    builder.equal(page.get(ReportAssignment.DB_FIELD_REPORTUUID), reportCollectionUuid),
                    builder.equal(page.get(ReportAssignment.DB_FIELD_REPORT_STATUS), ReportProgressStatus.started)
            ));

            TypedQuery<ReportAssignment> query = sessionSync.createQuery(criteria);
            query.setHint(QueryHints.HINT_CACHEABLE, true);

            Assert.assertEquals(1, repSync.getReportList(reportCollectionUuid, ReportProgressStatus.started).size());
        }


        try (Session session = sessionManager.getSessionFactory().openSession()) {
            List<ReportAssignment> existingSubscriptions = QueryManager.getAllItems(session, ReportAssignment.class);
            Assert.assertEquals(1, existingSubscriptions.size());
        }

        try (Session sessionSync = sessionManager.getSessionFactory().openSession()) {
            ReportSyncHandler repSync = new ReportSyncHandler(sessionSync);
            ReportAssignment report = new ReportAssignment();
            report.setTemplateName("REPORT2");
            Assert.assertFalse(repSync.createReportStatusObject(report));
        }

        try (Session sessionSync = sessionManager.getSessionFactory().openSession()) {
            ReportSyncHandler repSync = new ReportSyncHandler(sessionSync);
            repSync.setReportStatus(reportUuid, ReportProgressStatus.done);
        }

        try (Session sessionSync = sessionManager.getSessionFactory().openSession()) {
            ReportSyncHandler repSync = new ReportSyncHandler(sessionSync);
            ReportAssignment report = new ReportAssignment();
            report.setTemplateName("REPORT2");
            Assert.assertTrue(repSync.createReportStatusObject(report));
        }

        try (Session sessionSync = sessionManager.getSessionFactory().openSession()) {
            ReportSyncHandler repSync = new ReportSyncHandler(sessionSync);
            ReportAssignment report = new ReportAssignment();
            report.setTemplateName("REPORT1");
            Assert.assertFalse(repSync.createReportStatusObject(report));
        }
    }


    @Test
    public void testRejectSimultaniousGet() throws Exception {
        birthDataService.setWriteToLocalFile(false);
        birthDataService.setUseTimeintervallimit(false);

        try (Session sessionSync = sessionManager.getSessionFactory().openSession()) {
            ReportSyncHandler repSync = new ReportSyncHandler(sessionSync);
            ReportAssignment report = new ReportAssignment();
            report.setTemplateName(StatisticsService.ServiceName.BIRTH.getIdentifier());
            Assert.assertTrue(repSync.createReportStatusObject(report));
        }

        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        testUserDetails.giveAccess(StatistikRolesDefinition.EXECUTE_STATISTIK_ROLE);
        testsUtils.applyAccess(testUserDetails);

        ResponseEntity<String> response = restTemplate.exchange("/statistik/birth_data/?registrationAfter=2000-01-01&afterDate=1999-01-01", HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);
        Assert.assertEquals(409, response.getStatusCodeValue());
    }

    @Test
    public void testRejectSimultaniousPost() throws Exception {
        birthDataService.setWriteToLocalFile(true);
        birthDataService.setUseTimeintervallimit(false);

        try (Session sessionSync = sessionManager.getSessionFactory().openSession()) {
            ReportSyncHandler repSync = new ReportSyncHandler(sessionSync);
            ReportAssignment report = new ReportAssignment();
            report.setTemplateName(StatisticsService.ServiceName.BIRTH.getIdentifier());
            Assert.assertTrue(repSync.createReportStatusObject(report));
        }

        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        testUserDetails.giveAccess(StatistikRolesDefinition.EXECUTE_STATISTIK_ROLE);
        testsUtils.applyAccess(testUserDetails);

        ResponseEntity<String> response = restTemplate.exchange("/statistik/birth_data/?registrationAfter=2000-01-01", HttpMethod.POST, new HttpEntity<>("", new HttpHeaders()), String.class);
        Assert.assertEquals(409, response.getStatusCodeValue());

    }

    @Test
    public void testReportProgressService() throws Exception {
        birthDataService.setWriteToLocalFile(true);
        birthDataService.setUseTimeintervallimit(false);

        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        testUserDetails.giveAccess(StatistikRolesDefinition.EXECUTE_STATISTIK_ROLE);
        testsUtils.applyAccess(testUserDetails);

        try (Session sessionSync = sessionManager.getSessionFactory().openSession()) {
            ReportSyncHandler repSync = new ReportSyncHandler(sessionSync);
            ReportAssignment report = new ReportAssignment();
            report.setTemplateName(StatisticsService.ServiceName.BIRTH.getIdentifier());
            Assert.assertTrue(repSync.createReportStatusObject(report));

            ResponseEntity<String> response = restTemplate.exchange("/statistik/collective_report/reportstatus/?collectionUuid=" + report.getCollectionUuid(), HttpMethod.GET, new HttpEntity<>("", new HttpHeaders()), String.class);
            Assert.assertEquals("started,\n", response.getBody());
        }
    }


}
