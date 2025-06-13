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
            Transaction transaction = session.beginTransaction();
            try {
                transaction.begin();
                LocalDateTime deadline = LocalDateTime.now().minus(daysToLive, ChronoUnit.DAYS);
                log.info("Cleaning up " + deadline);
                Query query = session.createQuery("delete from ReportAssignment where createDateTime < :deadline");
                query.setParameter("deadline", deadline);
                query.executeUpdate();
                transaction.commit();
                log.info("Cleaned up " + deadline);
            } catch (Exception e) {
                transaction.rollback();
                e.printStackTrace();
                throw e;
            }
        }
    }

    public static void setup(SessionFactory sessionFactory, int daysToLive, String cronSchedule) throws ConfigurationException {
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
            jobData.put("sessionFactory", sessionFactory);
            jobData.put("daysToLive", daysToLive);

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
