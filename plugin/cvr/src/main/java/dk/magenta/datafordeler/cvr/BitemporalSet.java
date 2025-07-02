package dk.magenta.datafordeler.cvr;

import dk.magenta.datafordeler.cvr.records.CvrBitemporalRecord;
import dk.magenta.datafordeler.cvr.records.CvrRecord;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BitemporalSet<R extends CvrBitemporalRecord, P extends CvrRecord> extends RecordSet<R, P> implements Set<R> {

    public BitemporalSet(Set<R> inner, P parent, String field) {
        super(inner, parent, field);
    }
    public BitemporalSet(Set<R> inner, P parent, String field, String clause) {
        super(inner, parent, field, clause);
    }

    /**
     * Get the record that is current
     *
     * @return
     */
    public List<R> current() {
        return this.currentStream().collect(Collectors.toList());
    }

    /**
     * Get the stream but with all records removed that is closed in either registrationtime or effecttime
     *
     * @return
     */
    public Stream<R> currentStream() {
        return this.stream().filter(r -> r.getRegistrationTo() == null).filter(r -> r.getEffectTo() == null);
    }

    /**
     * Get the last registered record
     *
     * @param removeClosedRegistration
     * @param removeClosedRegistrationAndEffect
     * @return
     */
    public R getLast(boolean removeClosedRegistration, boolean removeClosedRegistrationAndEffect) {
        Stream<R> stream;
        if (removeClosedRegistrationAndEffect) {
            stream = this.currentStream();
        } else if (removeClosedRegistration) {
            stream = this.currentRegistrationStream();
        } else {
            stream = this.stream();
        }
        return stream.reduce((first, second) -> second).orElse(null);
    }

    /**
     * Get the last registered record
     *
     * @param removeClosedRegistration
     * @param removeClosedRegistrationAndEffect
     * @return
     */
    public R getFirst(boolean removeClosedRegistration, boolean removeClosedRegistrationAndEffect) {
        if (removeClosedRegistrationAndEffect) {
            return this.currentStream().findFirst().orElse(null);
        } else if (removeClosedRegistration) {
            return this.currentRegistrationStream().findFirst().orElse(null);
        } else {
            return this.stream().findFirst().orElse(null);
        }
    }

    /**
     * Get the records that is current on registartiontime
     *
     * @return
     */
    public List<R> currentRegistration() {
        return this.stream().filter(r -> r.getRegistrationTo() == null).collect(Collectors.toList());
    }

    /**
     * Get the stream but with only records that are closed in registration-to
     *
     * @return
     */
    public Stream<R> currentRegistrationStream() {
        return this.stream().filter(r -> r.getRegistrationTo() == null);
    }

    public List<R> registeredAt(OffsetDateTime dateTime) {
        return this.stream().filter(
                r -> (r.getRegistrationFrom() == null || r.getRegistrationFrom().isBefore(dateTime) || r.getRegistrationFrom().isEqual(dateTime))
                        && (r.getRegistrationTo() == null || r.getRegistrationTo().isAfter(dateTime) || r.getRegistrationTo().isEqual(dateTime))
        ).collect(Collectors.toList());
    }

    public R at(OffsetDateTime dateTime) {
        return this.stream().filter(
                r -> (r.getRegistrationFrom() == null || r.getRegistrationFrom().isBefore(dateTime) || r.getRegistrationFrom().isEqual(dateTime))
                        && (r.getRegistrationTo() == null || r.getRegistrationTo().isAfter(dateTime) || r.getRegistrationTo().isEqual(dateTime))
        ).filter(
                r -> (r.getEffectFrom() == null || r.getEffectFrom().isBefore(dateTime) || r.getEffectFrom().isEqual(dateTime))
                        && (r.getEffectTo() == null || r.getEffectTo().isAfter(dateTime) || r.getEffectTo().isEqual(dateTime))
        ).findFirst().orElse(null);
    }

    public List<R> ordered() {
        ArrayList<R> list = new ArrayList<>(this.inner);
        // Sortering efter registrationFrom, registrationTo, effectFrom, effectTo
        list.sort(
                Comparator.comparing(R::getRegistrationFrom, Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing(R::getRegistrationTo, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(R::getEffectFrom, Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing(R::getEffectTo, Comparator.nullsLast(Comparator.naturalOrder()))
        );
        return list;
    }
}
