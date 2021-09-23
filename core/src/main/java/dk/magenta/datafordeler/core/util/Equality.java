package dk.magenta.datafordeler.core.util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Contains static methods that are useful for comparing objects
 */
public abstract class Equality {

    /**
     * Compares two nullable Strings for equality
     */
    public static boolean equal(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    public static <C extends Comparable> int compare(C o1, C o2, Class<C> cClass) {
        return compare(o1, o2, cClass, false);
    }

    public static <C extends Comparable> int compare(C o1, C o2, Class<C> cClass, boolean nullsLast) {
        if (o1 == null && o2 == null) return 0;
        if (o1 == null) return nullsLast ? 1 : -1;
        if (o2 == null) return nullsLast ? -1 : 1;
        return o1.compareTo(o2);
    }

    /**
     * Compares two nullable OffsetDateTimes for equality
     */
    public static boolean equal(OffsetDateTime a, OffsetDateTime b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.isEqual(b);
    }

    /**
     * Compares two nullable LocalDates for equality
     */
    public static boolean equal(LocalDate a, LocalDate b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.isEqual(b);
    }

    public static boolean equalDate(OffsetDateTime a, OffsetDateTime b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return equal(a.toLocalDate(), b.toLocalDate());
    }

    /**
     * Add feature for comparing of timestamps recieved from CPR (We do not trust the precission very much)
     * @param a
     * @param b
     * @return
     */
    public static boolean cprDomainEqualDate(OffsetDateTime a, OffsetDateTime b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        long diff = Duration.between(a, b).abs().toHours();
        return diff < 48;
    }

    /**
     * Add feature for comparing of timestamps recieved from CPR (We do not trust the precission very much)
     * This method returns true if B is more than 48 hours after A
     * @param a
     * @param b
     * @return
     */
    public static boolean cprDomainBafterA(OffsetDateTime a, OffsetDateTime b) {
        if (a == null) {
            return false;
        } else if (b == null) {
            return true;
        } else {
            long diff = Duration.between(a, b).toHours();
            return diff > 48;
        }
    }
}
