package dk.magenta.datafordeler.eboks.utils;

import dk.magenta.datafordeler.core.util.Bitemporality;
import dk.magenta.datafordeler.core.util.BitemporalityComparator;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cvr.records.CvrBitemporalDataMetaRecord;
import dk.magenta.datafordeler.cvr.records.CvrBitemporalRecord;

import java.util.Collection;
import java.util.Comparator;

import static java.util.Comparator.naturalOrder;

public class FilterUtilities {

    private static Comparator bitemporalCprComparator = Comparator.comparing(FilterUtilities::getCprBitemporality, BitemporalityComparator.ALL)
            .thenComparing(CprBitemporalRecord::getId, Comparator.nullsLast(naturalOrder()));

    private static Comparator bitemporalCvrComparator = Comparator.comparing(FilterUtilities::getCvrBitemporality, BitemporalityComparator.ALL)
            .thenComparing(CvrBitemporalRecord::getId, Comparator.nullsLast(naturalOrder()));


    /**
     * Find the newest unclosed record from the list of records
     * Records with a missing OriginDate is also removed since they are considered invalid
     *
     * @param records
     * @param <R>
     * @return
     */
    public static <R extends CprBitemporalRecord> R findNewestUnclosedCpr(Collection<R> records) {
        return (R) records.stream().filter(r -> r.getBitemporality().registrationTo == null &&
                r.getBitemporality().effectTo == null).max(bitemporalCprComparator).orElse(null);
    }

    /**
     * Find the newest unclosed record from the list of records
     * Records with a missing OriginDate is also removed since they are considered invalid
     *
     * @param records
     * @param <R>
     * @return
     */
    public static <R extends CvrBitemporalRecord> R findNewestUnclosedCvr(Collection<R> records) {
        return (R) records.stream().filter(r -> r.getBitemporality().registrationTo == null &&
                r.getBitemporality().effectTo == null).max(bitemporalCvrComparator).orElse(null);
    }

    /**
     * Find the newest record from the list of records
     * Records with a missing OriginDate is also removed since they are considered invalid
     *
     * @param records
     * @param <R>
     * @return
     */
    public static <R extends CvrBitemporalRecord> R findNewestCvr(Collection<R> records) {
        return (R) records.stream().filter(r -> r.getBitemporality().registrationTo == null).max(bitemporalCvrComparator).orElse(null);
    }


    public static CprBitemporality getCprBitemporality(CprBitemporalRecord record) {
        return record.getBitemporality();
    }


    public static Bitemporality getCvrBitemporality(CvrBitemporalDataMetaRecord record) {
        return record.getBitemporality();
    }
}
