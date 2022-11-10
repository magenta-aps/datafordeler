package dk.magenta.datafordeler.cpr.synchronization;

import dk.magenta.datafordeler.cpr.data.person.PersonEntityManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.OffsetDateTime;
import java.util.TimerTask;

public class SubscriptionTimerTask extends TimerTask {
    private final Logger log = LogManager.getLogger(SubscriptionTimerTask.class.getCanonicalName());
    private final PersonEntityManager personManager;

    public SubscriptionTimerTask(PersonEntityManager personManager) {
        this.personManager = personManager;
    }

    @Override
    public void run() {
        try {
            //Subscriptions has to be initiated before 12, which means 8 gl-time
            if (OffsetDateTime.now().getHour() < 7) {
                this.personManager.createSubscriptionFile();
            } else {
                log.error("It is too late for subscriptions, wait until tomorrow");
            }
        } catch (Exception e) {
            log.error("Failed to upload subscriptions", e);
        }
    }
}
