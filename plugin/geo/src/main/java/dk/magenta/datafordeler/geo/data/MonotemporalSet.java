package dk.magenta.datafordeler.geo.data;

import dk.magenta.datafordeler.geo.data.common.GeoMonotemporalRecord;
import dk.magenta.datafordeler.geo.data.common.GeoNontemporalRecord;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class MonotemporalSet<R extends GeoMonotemporalRecord> implements Set<R> {

    Set<R> inner;

    public MonotemporalSet(Set<R> inner) {
        this.inner = inner;
    }

    /**
     * Get the record that is current
     *
     * @return
     */
    public R current() {
        return this.stream().filter(r -> r.getRegistrationTo() == null).max(Comparator.comparing(GeoNontemporalRecord::getDafoUpdated)).orElse(null);
    }

    public R at(OffsetDateTime dateTime) {
        return this.stream().filter(
                r -> (r.getRegistrationFrom() == null || r.getRegistrationFrom().isBefore(dateTime) || r.getRegistrationFrom().isEqual(dateTime))
                        && (r.getRegistrationTo() == null || r.getRegistrationTo().isAfter(dateTime) || r.getRegistrationTo().isEqual(dateTime))
        ).max(Comparator.comparing(GeoNontemporalRecord::getDafoUpdated)).orElse(null);
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
    public boolean add(R geoMonotemporalRecord) {
        return this.inner.add(geoMonotemporalRecord);
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
