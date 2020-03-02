package dk.magenta.datafordeler.ger.data;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class RawData extends HashMap<String, Object> {

    public String getString(String key) {
        return this.getString(key, false);
    }

    public String getString(String key, boolean dateAsInt) {
        Object value = this.get(key);
        if (value instanceof Long) {
            return ((Long) value).toString();
        }
        if (value instanceof Integer) {
            return ((Integer) value).toString();
        }
        if (value instanceof Date) {
            Date date = (Date) value;
            if (dateAsInt) {
                return Long.toString(dateAsLong(date));
            } else {
                LocalDate localDate = convertDate(date);
                return localDate != null ? localDate.format(DateTimeFormatter.ISO_LOCAL_DATE) : null;
            }
        }
        return (String) value;
    }

    public boolean getBoolean(String key) {
        return "j".equalsIgnoreCase(this.getString(key));
    }

    public Integer getInt(String key) {
        Object value = this.get(key);
        if (value != null) {
            if (value instanceof Long) {
                return ((Long) value).intValue();
            } else if (value instanceof Date) {
                return Long.valueOf(dateAsLong((Date) value)).intValue();
            }
            return (Integer) value;
        }
        return null;
    }

    public Long getLong(String key) {
        Object value = this.get(key);
        if (value != null) {
            if (value instanceof Long) {
                return (Long) value;
            } else if (value instanceof Date) {
                return dateAsLong((Date) value);
            }
        }
        return null;
    }

    public LocalDate getDate(String key) {
        return convertDate(this.get(key));
    }

    public static LocalDate convertDate(Object value) {
        if (value != null) {
            if (value instanceof Date) {
                Date d = (Date) value;
                return LocalDate.of(d.getYear() + 1900, d.getMonth() + 1, d.getDate());
            }
            return LocalDate.parse((String) value, DateTimeFormatter.ISO_LOCAL_DATE);
        }
        return null;
    }

    public UUID getUUID(String key) {
        String value = this.getString(key);
        return value != null ? UUID.fromString(value) : null;
    }

    public Character getCharacter(String key) {
        String value = this.getString(key);
        if (value != null) {
            char[] chars = value.toCharArray();
            if (chars.length > 0) {
                return chars[0];
            }
        }
        return null;
    }

    public static long dateAsLong(Date date) {
        // Excel stores dates as numbers, but storing an actual number may (with the wrong cell formatting) get extracted as a date.
        // Work out which number the extracted date represents
        return dateAsLong(
                LocalDate.of(
                        date.getYear() + 1900,
                        date.getMonth() + 1,
                        date.getDate()
                )
        );
    }

    public static long dateAsLong(LocalDate date) {
        // Excel stores dates as numbers, but storing an actual number may (with the wrong cell formatting) get extracted as a date.
        // Work out which number the extracted date represents
        return Long.valueOf(date.toEpochDay() + 25569
        );
    }
}
