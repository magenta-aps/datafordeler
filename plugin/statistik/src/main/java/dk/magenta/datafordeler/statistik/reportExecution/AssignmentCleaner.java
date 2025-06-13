package dk.magenta.datafordeler.statistik.reportExecution;

import dk.magenta.datafordeler.core.AbstractTask;
import dk.magenta.datafordeler.core.command.Worker;
import dk.magenta.datafordeler.core.exception.ConfigurationException;
import dk.magenta.datafordeler.core.util.CronUtil;
import org.apache.jena.base.Sys;
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

    private static final Logger log = LogManager.getLogger(AssignmentCleaner.class.getCanonicalName());

    public AssignmentCleaner(SessionFactory sessionFactory, int daysToLive) {
        this.sessionFactory = sessionFactory;
        this.daysToLive = daysToLive;
    }

    public static class Task extends AbstractTask<AssignmentCleaner> {
        @Override
        protected AssignmentCleaner createWorker(JobDataMap dataMap) {
            SessionFactory sessionFactory = (SessionFactory) dataMap.get("sessionFactory");
            int daysToLive = (int) dataMap.get("daysToLive");
            return new AssignmentCleaner(sessionFactory, daysToLive);
        }
    }

    @Override
    public void run() {
        log.info("Starting " + getClass().getSimpleName() + ".run()");
        try (Session session = sessionFactory.openSession()) {
            log.info("run1");
            Transaction transaction = session.beginTransaction();
            log.info("run2");
            try {
                LocalDateTime deadline = LocalDateTime.now().minus(daysToLive, ChronoUnit.DAYS);
                log.info("run4");
                log.info("Cleaning up " + deadline);
                Query query = session.createQuery("delete from ReportAssignment where createDateTime < :deadline");
                query.setParameter("deadline", deadline);
                log.info("run5");
                query.executeUpdate();
                log.info("run6");
                transaction.commit();
                log.info("Cleaned up " + deadline);
            } catch (Exception e) {
                transaction.rollback();
                e.printStackTrace();
                throw e;
            }
        }
        log.info("DONE");
    }

    public static void setup(SessionFactory sessionFactory, int daysToLive, String cronSchedule) throws ConfigurationException {
        log.info("Setting up");
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

            log.info("Trigger created");

            JobDataMap jobData = new JobDataMap();
            jobData.put("sessionFactory", sessionFactory);
            jobData.put("daysToLive", daysToLive);
            log.info("JobData created");

            JobDetail job = JobBuilder.newJob(AssignmentCleaner.Task.class)
                    .withIdentity("assignmentCleaner")
                    .setJobData(jobData)
                    .build();
            log.info("Job created");

            scheduler.scheduleJob(job, Collections.singleton(trigger), true);
            scheduler.start();
            log.info("Scheduler started");

        } catch (SchedulerException e) {
            log.error("Failed to schedule AssignmentCleaner", e);
        }
    }

    public static void unSchedule() throws SchedulerException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.unscheduleJob(TriggerKey.triggerKey("assignmentCleaner"));
    }
}
