package dk.magenta.datafordeler.cvr;

import dk.magenta.datafordeler.cvr.records.CvrRecord;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class RecordSet<R extends CvrRecord, P extends CvrRecord> implements Set<R> {

    Set<R> inner;
    P parent;
    String field;
    String clause;
    Class<R> recordClass;
    RecordSet<R, P> parentRecordSet;

    public RecordSet(Set<R> inner, Class<R> recordClass, P parent, String field) {
        this(inner, recordClass, parent, field, null);
    }
    public RecordSet(Set<R> inner, Class<R> recordClass, P parent, String field, String clause) {
        this(inner, recordClass, parent, field, clause, null);
    }

    public RecordSet(Set<R> inner, Class<R> recordClass, P parent, String field, String clause, RecordSet<R, P> parentRecordSet) {
        this.inner = inner;
        this.recordClass = recordClass;
        this.parent = parent;
        this.field = field;
        this.clause = clause;
        this.parentRecordSet = parentRecordSet;
    }

    public void traverse(Consumer<RecordSet<? extends CvrRecord, ? extends CvrRecord>> setCallback, Consumer<CvrRecord> itemCallback) {
        for (R item : this.inner) {
            item.traverse(setCallback, itemCallback);
        }
        if (setCallback != null) {
            setCallback.accept(this);
        }
    }

    public Class<R> getRecordClass() {
        return this.recordClass;
    }

    public P getParent() {
        return this.parent;
    }

    public String getField() {
        return this.field;
    }

    public String getClause() {
        return this.clause;
    }

    public RecordSet<R, P> getParentRecordSet() {
        return this.parentRecordSet;
    }

    //    public Class getRecordClass() {
//        return this.inner.;
//    }

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

    public boolean addSuper(CvrRecord record) throws ClassCastException {
        return this.inner.add(this.recordClass.cast(record));
    }


    @Override
    public boolean remove(Object o) {
        if (this.recordClass.isInstance(o)) {
            R record = this.recordClass.cast(o);
            return this.remove(record);
        }
        return false;
    }

    public boolean remove(R o) {
        if (this.inner.remove(o)) {
            return true;
        }
        if (this.inner.removeAll(Collections.singletonList(o))) {
            return true;
        }
        if (this.inner.removeIf(r -> Objects.equals(r.getId(), o.getId()))) {
            return true;
        }
        if (this.parentRecordSet != null) {
            return this.parentRecordSet.remove(o);
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return this.inner.containsAll(collection);
    }

    @Override
    public boolean addAll(Collection<? extends R> collection) {
        return this.inner.addAll(collection);
    }
    public boolean addAllSuper(Collection<? extends CvrRecord> collection) throws ClassCastException {
        boolean changed = false;
        for (CvrRecord record : collection) {
            if (this.addSuper(record)) {
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return this.inner.retainAll(collection);
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        boolean all = true;
        for (Object o : collection) {
            if (this.recordClass.isInstance(o)) {
                if (!this.remove(this.recordClass.cast(o))) {
                    all = false;
                }
            }
        }
        return all;
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
