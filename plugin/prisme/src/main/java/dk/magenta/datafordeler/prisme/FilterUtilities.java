package dk.magenta.datafordeler.prisme;

import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.util.BitemporalityComparator;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.CprNontemporalRecord;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static java.util.Comparator.naturalOrder;

public class FilterUtilities {

    private static Comparator bitemporalComparator = Comparator.comparing(FilterUtilities::getBitemporality, BitemporalityComparator.ALL)
            .thenComparing(CprNontemporalRecord::getOriginDate, Comparator.nullsLast(naturalOrder()))
            .thenComparing(CprNontemporalRecord::getDafoUpdated)
            .thenComparing(DatabaseEntry::getId);

    private static Comparator effectStartComparator = Comparator.comparing(FilterUtilities::getBitemporality, BitemporalityComparator.EFFECT_FROM)
            .thenComparing(CprNontemporalRecord::getId);


    /**
     * Find the newest unclosed record from the list of records
     * Records with a missing OriginDate is also removed since they are considered invalid
     * @param records
     * @param <R>
     * @return
     */
    public static <R extends CprBitemporalRecord> R findNewestUnclosed(Collection<R> records) {
        return (R) records.stream().filter(r -> r.getBitemporality().registrationTo == null &&
                 r.getBitemporality().effectTo == null).max(bitemporalComparator).orElse(null);
    }

    /**
     * Get a sorted list of the items with the items sorted by effectFrom, and the newest item first
     * @param records
     * @param <R>
     * @return
     */
    public static <R extends CprBitemporalRecord> List<R> sortRecordsOnEffect(Collection<R> records) {
        ArrayList<R> recordList = new ArrayList<>(records);
        recordList.sort(bitemporalComparator.reversed());
        return recordList;
    }



    public static CprBitemporality getBitemporality(CprBitemporalRecord record) {
        return record.getBitemporality();
    }
}
