package dk.magenta.datafordeler.cvr;

import dk.magenta.datafordeler.cvr.records.CvrBitemporalRecord;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BitemporalSet<R extends CvrBitemporalRecord> implements Set<R> {

    Set<R> inner;

    public BitemporalSet(Set<R> inner) {
        this.inner = inner;
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


    //--------------------------------------------------------------------------
    // passthrough methods

    @Override
    public int size() {
        return this.inner.size();
    }

    @Override
    public boolean isEmpty() {
        return this.inner.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.inner.contains(o);
    }

    @Override
    public Iterator<R> iterator() {
        return this.inner.iterator();
    }

    @Override
    public void forEach(Consumer<? super R> action) {
        this.inner.forEach(action);
    }

    @Override
    public Object[] toArray() {
        return this.inner.toArray();
    }

    @Override
    public <T> T[] toArray(T[] ts) {
        return this.inner.toArray(ts);
    }

    @Override
    public <T> T[] toArray(IntFunction<T[]> generator) {
        return this.inner.toArray(generator);
    }

    @Override
    public boolean add(R record) {
        return this.inner.add(record);
    }

    @Override
    public boolean remove(Object o) {
        return this.inner.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return this.inner.containsAll(collection);
    }

    @Override
    public boolean addAll(Collection<? extends R> collection) {
        return this.inner.addAll(collection);
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return this.inner.retainAll(collection);
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return this.inner.removeAll(collection);
    }

    @Override
    public boolean removeIf(Predicate<? super R> filter) {
        return this.inner.removeIf(filter);
    }

    @Override
    public void clear() {
        this.inner.clear();
    }

    @Override
    public Spliterator<R> spliterator() {
        return this.inner.spliterator();
    }

    @Override
    public Stream<R> stream() {
        return this.inner.stream();
    }

    @Override
    public Stream<R> parallelStream() {
        return this.inner.parallelStream();
    }
}
