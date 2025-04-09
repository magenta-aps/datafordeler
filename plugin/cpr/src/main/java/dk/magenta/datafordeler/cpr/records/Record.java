package dk.magenta.datafordeler.cpr.records;

import dk.magenta.datafordeler.core.exception.ParseException;
import org.apache.jena.base.Sys;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Base record, extending a hashmap for holding values.
 * A {@link dk.magenta.datafordeler.cpr.parsers.CprSubParser} will parse lines into
 * records based on the first three characters in the line
 */
public abstract class Record extends HashMap<String, String> {

    private static final Pattern leadingZero = Pattern.compile("^0+");

    private String origin;

    private String line;

    public Record(String line) throws ParseException {
        if (line == null) {
            throw new ParseException("Invalid NULL input.");
        }
        this.line = line;
        this.obtain("type", 1, 3, false);
        String thisType = this.getRecordType();
    }

    public String getOrigin() {
        return this.origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    protected String substr(String line, int position, int length) {
        return line.substring(Math.min(position - 1, line.length()), Math.min(position + length - 1, line.length())).trim();
    }

    protected void obtain(String key, int position, int length) {
        this.obtain(key, position, length, false);
    }

    protected void obtain(Mapping mapping) {
        for (String key : mapping.keySet()) {
            this.obtain(key, mapping.get(key).getLeft(), mapping.get(key).getRight());
        }
    }

    protected void obtain(String key, int position, int length, boolean truncateLeadingZeroes) {
        String value = this.substr(this.line, position, length);
        if (truncateLeadingZeroes) {
            value = leadingZero.matcher(value).replaceAll("");
        }
        this.put(key, value);
    }

    protected void clean() {
        this.line = null;
    }

    //public abstract DoubleHashMap<String,String,PersonBaseData> populateBaseData(String timestamp);

    protected static String normalizeDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            return null;
        }
        try {
            if (Integer.parseInt(date) == 0) {
                return null;
            }
        } catch (Exception e) {
        }
        return date;
    }

    protected boolean getMarking(String key) {
        String value = this.get(key);
        return value != null && value.equals("*");
    }

    protected String getMarkingCharacter(String key) {
        return this.get(key);
    }

    public String getLine() {
        return this.line;
    }


    public abstract String getRecordType();

    private static final ZoneId timezone = ZoneId.of("Europe/Copenhagen");

    public String getRecordClass() {
        String[] classParts = this.getClass().getCanonicalName().split("\\.");
        return classParts[classParts.length - 1];
    }

    public String getString(String key, boolean stripLeadingZeroes) {
        String value = this.get(key);
        if (value != null && stripLeadingZeroes) {
            value = value.replaceAll("^0+", "");
        }
        return value;
    }

    public int getInt(String key) {
        return this.getInt(key, false, null);
    }

    public int getInt(String key, Integer fallback) {
        return this.getInt(key, false, fallback);
    }

    public int getInt(String key, boolean lenient) {
        return this.getInt(key, lenient, null);
    }

    public int getInt(String key, boolean lenient, Integer fallback) {
        String value = this.get(key);
        if (value == null && fallback != null) {
            return fallback;
        }
        if (lenient) {
            value = value.replaceAll("[^\\d]", "");
        }
        try {
            return Integer.parseInt(value, 10);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public long getLong(String key) {
        return this.getLong(key, false);
    }

    public long getLong(String key, boolean lenient) {
        String value = this.get(key);
        if (lenient) {
            value = value.replaceAll("[^\\d]", "");
        }
        try {
            return Long.parseLong(value, 10);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public boolean getBoolean(String key) {
        return this.getBoolean(key, null);
    }

    public boolean getBoolean(String key, Boolean fallback) {
        String value = this.get(key);
        if (value == null) {
            if (fallback != null) {
                return fallback;
            }
        }
        value = value.toLowerCase();
        return value.equals("1") || value.equals("yes") || value.equals("true") || value.equals("ja") || value.equals("y") || value.equals("*");
    }

    public Character getChar(String key) {
        String value = this.get(key);
        return value != null && !value.isEmpty() ? value.charAt(0) : null;
    }


    private static final DateTimeFormatter yearParser = DateTimeFormatter.ofPattern("uuuu");

    public Year getYear(String key) {
        String value = this.get(key);
        if (value != null && !value.isEmpty() && value.length() >= 4) {
            value = value.substring(0, 4);
            try {
                return Year.parse(value, yearParser);
            } catch (DateTimeParseException e) {
            }
        }
        return null;
    }

    private static final DateTimeFormatter monthParser = DateTimeFormatter.ofPattern("uuuuMM");

    public YearMonth getMonth(String key) {
        String value = this.get(key);
        if (value != null && !value.isEmpty() && value.length() >= 6) {
            value = value.substring(0, 6);
            try {
                return YearMonth.parse(value, monthParser);
            } catch (DateTimeParseException e) {
            }
            Year year = this.getYear(key);
            if (year != null) {
                return YearMonth.of(year.getValue(), 1);
            }
        }
        return null;
    }

    private static final DateTimeFormatter[] dateParsers = {
            DateTimeFormatter.BASIC_ISO_DATE,
            DateTimeFormatter.ISO_LOCAL_DATE
    };

    public LocalDate getDate(String key) {
        String value = this.get(key);
        if (value != null && !value.isEmpty()) {
            for (DateTimeFormatter parser : dateParsers) {
                try {
                    return LocalDate.parse(value, parser);
                } catch (DateTimeParseException e) {
                }
            }
            YearMonth yearMonth = this.getMonth(key);
            if (yearMonth != null) {
                return LocalDate.of(yearMonth.getYear(), yearMonth.getMonth(), 1);
            }
        }
        return null;
    }

    private static final DateTimeFormatter[] timeParsers = {
            DateTimeFormatter.ISO_LOCAL_TIME,
            DateTimeFormatter.ofPattern("HH.mm.ss"),
            DateTimeFormatter.ofPattern("HH-mm-ss")
    };

    public LocalTime getTime(String key) {
        String value = this.get(key);
        if (value != null && !value.isEmpty()) {
            for (DateTimeFormatter parser : timeParsers) {
                try {
                    return LocalTime.parse(value, parser);
                } catch (DateTimeParseException e) {
                }
            }
        }
        return null;
    }

    private static final DateTimeFormatter[] datetimeParsers = {
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("uuuuMMddHHmmss"),
            DateTimeFormatter.ofPattern("uuuuMMddHHmm")
    };

    public LocalDateTime getDateTime(String key) {
        String value = this.get(key);
        if (value != null && !value.isEmpty()) {
            if (value.length() == 12 && value.endsWith("99")) {
                value = value.substring(0, 10) + "00";
            }
            if (value.equals("000000000000")) {
                return null;
            }
            for (DateTimeFormatter parser : datetimeParsers) {
                try {
                    return LocalDateTime.parse(value, parser);
                } catch (DateTimeParseException e) {
                }
            }
            LocalDate localDate = this.getDate(key);
            if (localDate != null) {
                return LocalDateTime.of(localDate, LocalTime.MIDNIGHT);
            }
        }
        return null;
    }


    public LocalDateTime getDateTime(String dateKey, String timeKey) {
        LocalDate localDate = this.getDate(dateKey);
        LocalTime localTime = this.getTime(timeKey);
        if (localDate != null) {
            return LocalDateTime.of(localDate, localTime != null ? localTime : LocalTime.MIDNIGHT);
        }
        return null;
    }

    public OffsetDateTime getOffsetDateTime(String key) {
        LocalDateTime dateTime = this.getDateTime(key);
        if (dateTime != null) {
            return OffsetDateTime.from(
                    ZonedDateTime.of(
                            dateTime,
                            timezone
                    )
            );
        }
        return null;
    }

    public boolean has(String key) {
        String value = this.getString(key, true);
        return value != null && !value.isEmpty();
    }

    public boolean hasAll(String... keys) {
        for (String key : keys) {
            if (!this.has(key)) {
                return false;
            }
        }
        return true;
    }

    public boolean hasAny(String... keys) {
        for (String key : keys) {
            if (this.has(key)) {
                return true;
            }
        }
        return false;
    }

    public static <T> T firstSet(T... times) {
        for (T time : times) {
            if (time != null) {
                return time;
            }
        }
        return null;
    }

}
