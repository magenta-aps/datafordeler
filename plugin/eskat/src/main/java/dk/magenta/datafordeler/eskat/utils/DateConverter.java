package dk.magenta.datafordeler.eskat.utils;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * General dateconvertes for services used by E-skat
 */
public class DateConverter {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String dateConvert(OffsetDateTime datetime) {
        if (datetime == null) {
            return null;
        } else {
            return datetime.format(formatter);
        }
    }

    public static String dateConvert(LocalDate date) {
        if (date == null) {
            return null;
        } else {
            return date.format(formatter);
        }
    }

    public static LocalDate parseDate(String date) {
        if (date != null) {
            return LocalDate.parse(date, formatter);
        } else {
            return null;
        }
    }
}
