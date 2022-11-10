package dk.magenta.datafordeler.cpr;

import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.util.BitemporalityComparator;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.CprNontemporalRecord;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Comparator.naturalOrder;

public class CprRecordFilter {

    public static CprBitemporality getBitemporality(CprBitemporalRecord record) {
        return record.getBitemporality();
    }

    /**
     * Helpercomparator for finding the newest record if there is more than one matching.
     */
    private static final Comparator bitemporalComparator = Comparator.comparing(CprRecordFilter::getBitemporality, BitemporalityComparator.ALL)
            .thenComparing(CprNontemporalRecord::getOriginDate, Comparator.nullsLast(naturalOrder()))
            .thenComparing(CprNontemporalRecord::getDafoUpdated)
            .thenComparing(DatabaseEntry::getId);

    /**
     * Find the record that matches as inside the interval of both registration and effect
     * Just select the newest record if more than one matches
     *
     * @param records
     * @param registrationAt
     * @param <R>
     * @return
     */
    public static <R extends CprBitemporalRecord> R filterRecordsByRegistrationAndEffectReturnNewest(Collection<R> records, OffsetDateTime registrationAt) {
        return (R) records.stream().filter(record -> record.getBitemporality().containsRegistration(registrationAt, registrationAt) &&
                record.getBitemporality().containsEffect(registrationAt, registrationAt)).max(bitemporalComparator).orElse(null);
    }


    public static <R extends CprBitemporalRecord> List<R> filterRecordsByRegistrationAndEffect(Collection<R> records, OffsetDateTime registrationAt) {
        return records.stream().filter(record -> record.getBitemporality().containsRegistration(registrationAt, registrationAt) &&
                record.getBitemporality().containsEffect(registrationAt, registrationAt)).collect(Collectors.toList());
    }
}
