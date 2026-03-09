package dk.magenta.datafordeler.cpr.direct;

import dk.magenta.datafordeler.core.AbstractTask;
import dk.magenta.datafordeler.core.JobReporter;
import dk.magenta.datafordeler.core.command.Worker;
import dk.magenta.datafordeler.core.exception.ConfigurationException;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.cpr.configuration.CprConfigurationManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobDataMap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

public class CprDirectPasswordUpdate extends Worker implements Runnable {

    protected Logger log = LogManager.getLogger(this.getClass().getSimpleName());

    public static class Task extends AbstractTask<CprDirectPasswordUpdate> {
        public static final String DATA_CONFIGURATIONMANAGER = "configurationManager";
        public static final String DATA_DIRECTLOOKUP = "directLookup";
        public static final String DATA_JOB_REPORTER = "jobReporter";

        @Override
        protected CprDirectPasswordUpdate createWorker(JobDataMap dataMap) {
            CprConfigurationManager configurationManager = (CprConfigurationManager) dataMap.get(DATA_CONFIGURATIONMANAGER);
            CprDirectLookup directLookup = (CprDirectLookup) dataMap.get(DATA_DIRECTLOOKUP);
            JobReporter jobReporter = (JobReporter) dataMap.get(DATA_JOB_REPORTER);
            return new CprDirectPasswordUpdate(configurationManager, directLookup, jobReporter);
        }
    }


    private final CprConfigurationManager configurationManager;
    private final CprDirectLookup directLookup;
    private final JobReporter jobReporter;
    private final SecureRandom random = new SecureRandom();

    public CprDirectPasswordUpdate(CprConfigurationManager configurationManager, CprDirectLookup directLookup, JobReporter jobReporter) {
        this.configurationManager = configurationManager;
        this.directLookup = directLookup;
        this.jobReporter = jobReporter;
    }

    private static final String ALPHA_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String ALPHA_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMERIC = "0123456789";
    private static final String SPECIAL_CHARS = "~!@#$%^*()_-+=,./{}[];:";

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
        log.info("Running update of CPR direct password");
        try {
            // Make sure we can access the local password storage. If there is an exception, do not attempt to create a new password
            String oldPassword = this.configurationManager.getConfiguration().getDirectPassword();
            log.info("Obtained old password");
            // Generate a new password
            String newPassword = this.generatePassword(8);
            File tempPasswordStore = new File("/app/data/password.txt");
            try (FileWriter fw = new FileWriter(tempPasswordStore)) {
                fw.write(newPassword);
            }
            log.info("Generated new password");
            // Update remote pw
            directLookup.login(newPassword);
            log.info("Updated remote password");
            // If success, update local pw
            this.configurationManager.setDirectPassword(newPassword);
            log.info("Stored new password");
            this.jobReporter.reportJobSuccess("cpr_direct_password_update");
            tempPasswordStore.delete();
        } catch (GeneralSecurityException | IOException | ConfigurationException | DataStreamException e) {
            log.error(e.getMessage());
        }
    }
}
