package dk.magenta.datafordeler.core.util;

import dk.magenta.datafordeler.core.exception.ConfigurationException;

import java.util.ArrayList;
import java.util.Arrays;

public abstract class CronUtil {

    public static String reformatSchedule(String schedule) throws ConfigurationException {
        try {
            if (schedule == null || schedule.isEmpty())
                return null;

            ArrayList<String> parts =
                    new ArrayList<>(Arrays.asList(schedule.split(" +")));

            // the fancy_cronfield doesn't include seconds
            if (parts.size() == 5) {
                parts.add(0, "0");
            }

            // quartz doesn't accept stars for day-of-week
            if (parts.get(5).equals("*")) {
                parts.set(5, "?");
            }

            return String.join(" ", parts);
        } catch (Exception e) {
            throw new ConfigurationException("Invalid CRON expression: " + schedule, e);
        }
    }

}
