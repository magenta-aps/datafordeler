package dk.magenta.datafordeler.cpr.direct;

import dk.magenta.datafordeler.core.AbstractTask;
import dk.magenta.datafordeler.core.command.Worker;
import dk.magenta.datafordeler.core.exception.ConfigurationException;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.cpr.configuration.CprConfigurationManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobDataMap;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

public class CprDirectPasswordUpdate extends Worker implements Runnable {

    protected Logger log = LogManager.getLogger(this.getClass().getSimpleName());

    public static class Task extends AbstractTask<CprDirectPasswordUpdate> {
        public static final String DATA_CONFIGURATIONMANAGER = "configurationManager";
        public static final String DATA_DIRECTLOOKUP = "directLookup";

        @Override
        protected CprDirectPasswordUpdate createWorker(JobDataMap dataMap) {
            CprConfigurationManager configurationManager = (CprConfigurationManager) dataMap.get(DATA_CONFIGURATIONMANAGER);
            CprDirectLookup directLookup = (CprDirectLookup) dataMap.get(DATA_DIRECTLOOKUP);
            return new CprDirectPasswordUpdate(configurationManager, directLookup);
        }
    }


    private final CprConfigurationManager configurationManager;
    private final CprDirectLookup directLookup;
    private final SecureRandom random = new SecureRandom();

    public CprDirectPasswordUpdate(CprConfigurationManager configurationManager, CprDirectLookup directLookup) {
        this.configurationManager = configurationManager;
        this.directLookup = directLookup;
    }

    private static final String ALPHA_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String ALPHA_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMERIC = "0123456789";
    private static final String SPECIAL_CHARS = "~`!@#$%^*()_-+=,./{}[];:";

    public String generatePassword(int len) {
        String[] buckets = new String[]{ALPHA_LOWER, ALPHA_UPPER, NUMERIC, SPECIAL_CHARS};
        char[] password = new char[len];
        for (int i = 0; i < len; i++) {
            // Round-robin the buckets, and pick a random char from the current one
            String bucket = buckets[i % buckets.length];
            int index = random.nextInt(bucket.length());
            password[i] = bucket.charAt(index);
        }
        for (int i = 0; i < password.length; i++) {
            // Randomly shuffle two chars in the array
            int randomIndex = random.nextInt(password.length);
            char temp = password[i];
            password[i] = password[randomIndex];
            password[randomIndex] = temp;
        }
        return new String(password);
    }

    @Override
    public void run() {
        try {
            log.info("Updating CPR Direkte password");
            // Make sure we can access the local password storage
            String oldPassword = this.configurationManager.getConfiguration().getDirectPassword();
            // Generate a new password
            String newPassword = this.generatePassword(8);
            // Update remote pw
            directLookup.login(newPassword);
            log.info("Remote password updated");
            // If success, update local pw
            this.configurationManager.setDirectPassword(newPassword);
            log.info("Password saved");
        } catch (GeneralSecurityException | IOException | ConfigurationException | DataStreamException e) {
            e.printStackTrace();
        }
    }
}
