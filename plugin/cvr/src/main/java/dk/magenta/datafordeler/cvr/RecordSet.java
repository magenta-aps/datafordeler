package dk.magenta.datafordeler.cvr;

import dk.magenta.datafordeler.cvr.records.CvrRecord;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class RecordSet<R extends CvrRecord> implements Set<R> {

    Set<R> inner;

    public RecordSet(Set<R> inner) {
        this.inner = inner;
    }

    public void traverse(Consumer<RecordSet<? extends CvrRecord>> setCallback, Consumer<CvrRecord> itemCallback) {
        for (R item : this.inner) {
            item.traverse(setCallback, itemCallback);
        }
        if (setCallback != null) {
            setCallback.accept(this);
        }
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
		// Hibernate's own contains()-method is shit when determining this.
		// For Company contactrecords it will return false on set.contains(set.stream().first().get())
		// Basically denying that the member in the set is contained in the set
        if (this.inner.contains(o)) {
            return true;
        }
        for (R item : this.inner) {
            if (item.equals(o)) {
                return true;
            }
        }
        return false;
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
