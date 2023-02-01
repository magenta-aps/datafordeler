package dk.magenta.datafordeler.cpr.synchronization;

import dk.magenta.datafordeler.core.AbstractTask;
import dk.magenta.datafordeler.core.command.Worker;
import dk.magenta.datafordeler.cpr.data.person.PersonEntityManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobDataMap;

import java.time.*;

public class SubscriptionTimerTask extends Worker {
    private final Logger log = LogManager.getLogger(SubscriptionTimerTask.class.getCanonicalName());
    private final PersonEntityManager personManager;

    public SubscriptionTimerTask(PersonEntityManager personManager) {
        this.personManager = personManager;
    }

    public static class Task extends AbstractTask<SubscriptionTimerTask> {
        @Override
        protected SubscriptionTimerTask createWorker(JobDataMap dataMap) {
            return new SubscriptionTimerTask(
                    (PersonEntityManager) dataMap.get("personManager")
            );
        }
    }

    @Override
    public void run() {
        try {
            //Subscriptions have to be initiated before 12 (DK), which means 8 gl-time
            LocalDateTime localDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(12, 0));
            OffsetDateTime deadline = OffsetDateTime.of(
                    localDateTime,
                    ZoneId.of("Europe/Copenhagen").getRules().getOffset(localDateTime)
            );
            if (OffsetDateTime.now().plusMinutes(30).isBefore(deadline)) {
                this.personManager.createSubscriptionFile();
            } else {
                log.error("It is too late for subscriptions, wait until tomorrow");
            }
        } catch (Exception e) {
            log.error("Failed to upload subscriptions", e);
        }
    }
}
