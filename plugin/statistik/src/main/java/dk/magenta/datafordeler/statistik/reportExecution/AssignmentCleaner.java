package dk.magenta.datafordeler.statistik.reportExecution;

import dk.magenta.datafordeler.core.AbstractTask;
import dk.magenta.datafordeler.core.JobReporter;
import dk.magenta.datafordeler.core.command.Worker;
import dk.magenta.datafordeler.core.exception.ConfigurationException;
import dk.magenta.datafordeler.core.util.CronUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;


public class AssignmentCleaner extends Worker implements Runnable {

    private SessionFactory sessionFactory;
    private int daysToLive;
    private JobReporter jobReporter;

    private static final Logger log = LogManager.getLogger(AssignmentCleaner.class.getCanonicalName());

    public AssignmentCleaner(SessionFactory sessionFactory, int daysToLive, JobReporter jobReporter) {
        this.sessionFactory = sessionFactory;
        this.daysToLive = daysToLive;
        this.jobReporter = jobReporter;
    }

    public static class Task extends AbstractTask<AssignmentCleaner> {

        public static final String DATA_SESSIONFACTORY = "sessionFactory";
        public static final String DATA_DAYS_TO_LIVE = "daysToLive";
        public static final String DATA_JOB_REPORTER = "jobReporter";

        @Override
        protected AssignmentCleaner createWorker(JobDataMap dataMap) {
            SessionFactory sessionFactory = (SessionFactory) dataMap.get(DATA_SESSIONFACTORY);
            int daysToLive = (int) dataMap.get(DATA_DAYS_TO_LIVE);
            JobReporter jobReporter = (JobReporter)  dataMap.get(DATA_JOB_REPORTER);
            return new AssignmentCleaner(sessionFactory, daysToLive, jobReporter);
        }
    }

    @Override
    public void run() {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                LocalDateTime cutoffDate = LocalDateTime.now().minus(daysToLive, ChronoUnit.DAYS);
                Query query = session.createQuery("delete from ReportAssignment where createDateTime < :cutoffDate");
                query.setParameter("cutoffDate", cutoffDate);
                query.executeUpdate();
                transaction.commit();
                this.jobReporter.reportJobSuccess("cleanup_assignments");
            } catch (Exception e) {
                transaction.rollback();
                throw e;
            }
        }
    }

    public static void setup(SessionFactory sessionFactory, int daysToLive, String cronSchedule, JobReporter jobReporter) throws ConfigurationException {
        String s = CronUtil.reformatSchedule(cronSchedule);
        if (s == null) {
            log.error("CronSchedule for AssignmentCleaner is null");
            return;
        }
        try {
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(s);
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(TriggerKey.triggerKey("assignmentCleaner"))
                    .withSchedule(scheduleBuilder).build();

            JobDataMap jobData = new JobDataMap();
            jobData.put(Task.DATA_SESSIONFACTORY, sessionFactory);
            jobData.put(Task.DATA_DAYS_TO_LIVE, daysToLive);
            jobData.put(Task.DATA_JOB_REPORTER, jobReporter);

            JobDetail job = JobBuilder.newJob(AssignmentCleaner.Task.class)
                    .withIdentity("assignmentCleaner")
                    .setJobData(jobData)
                    .build();

            scheduler.scheduleJob(job, Collections.singleton(trigger), true);
            scheduler.start();

        } catch (SchedulerException e) {
            log.error("Failed to schedule AssignmentCleaner", e);
        }
    }

    public static void unSchedule() throws SchedulerException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.unscheduleJob(TriggerKey.triggerKey("assignmentCleaner"));
    }
}
