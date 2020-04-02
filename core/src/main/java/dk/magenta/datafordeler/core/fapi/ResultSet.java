package dk.magenta.datafordeler.core.fapi;

import dk.magenta.datafordeler.core.database.IdentifiedEntity;

import java.util.*;
import java.util.stream.Collectors;

public class ResultSet<E extends IdentifiedEntity> {
    private E primaryEntity;
    private HashMap<Class, HashSet<IdentifiedEntity>> associatedEntities = new HashMap<>();

    public ResultSet(E primaryEntity) {
        this.primaryEntity = primaryEntity;
    }

    public ResultSet(E primaryEntity, Set<IdentifiedEntity> associatedEntities) {
        this.primaryEntity = primaryEntity;
        this.addAssociatedEntities(associatedEntities);
    }

    public ResultSet(Object databaseRow, List<String> classNames) throws ClassNotFoundException {
        if (classNames.size() == 1) {
            this.primaryEntity = (E) cast(databaseRow, classNames.get(0));
        } else {
            Object[] databaseRowItems = (Object[]) databaseRow;
            this.primaryEntity = (E) cast(databaseRowItems[0], classNames.get(0));
            for (int i=1; i<databaseRowItems.length; i++) {
                this.addAssociatedEntity(cast(databaseRowItems[i], classNames.get(i)));
            }
        }
    }

    public ResultSet(Object[] databaseRow, List<String> classNames) throws ClassNotFoundException {
        this.primaryEntity = (E) cast(databaseRow[0], classNames.get(0));
        for (int i=1; i<databaseRow.length; i++) {
            this.addAssociatedEntity(cast(databaseRow[i], classNames.get(i)));
        }
    }

    public void addAssociatedEntity(IdentifiedEntity entity) {
        if (entity != null) {
            Class c = entity.getClass();
            if (!this.associatedEntities.containsKey(c)) {
                this.associatedEntities.put(c, new HashSet<>());
            }
            this.associatedEntities.get(c).add(entity);
        }
    }

    public void addAssociatedEntities(Collection<IdentifiedEntity> entities) {
        for (IdentifiedEntity entity : entities) {
            this.addAssociatedEntity(entity);
        }
    }

    public boolean merge(ResultSet<E> other) {
        if (this.primaryEntity == other.primaryEntity) {
            for (Class c : other.associatedEntities.keySet()) {
                this.addAssociatedEntities(other.associatedEntities.get(c));
            }
            return true;
        }
        return false;
    }

    private static IdentifiedEntity cast(Object object, String className) throws ClassNotFoundException {
        if (object != null) {
            Class entityClass = Class.forName(className);
            return (IdentifiedEntity) entityClass.cast(object);
        }
        return null;
    }

    public E getPrimaryEntity() {
        return this.primaryEntity;
    }

    public <C extends IdentifiedEntity> Set<C> get(Class<C> c) {
        return (Set<C>) this.associatedEntities.get(c);
    }

    public Set<Class> getAssociatedEntityClasses() {
        return this.associatedEntities.keySet();
    }

    public Set<IdentifiedEntity> get(Class c, boolean preferPrimary) {
        Set<IdentifiedEntity> primaryMatch = (this.primaryEntity.getClass() == c) ? Collections.singleton(this.primaryEntity) : null;
        if (preferPrimary && primaryMatch != null) {
            return primaryMatch;
        }
        Set<IdentifiedEntity> match = this.associatedEntities.get(c);
        if (match != null) {
            return match;
        }
        return primaryMatch;
    }

    public List<IdentifiedEntity> all() {
        ArrayList<IdentifiedEntity> all = new ArrayList<>(this.associatedEntities.size()+1);
        all.add(this.primaryEntity);
        for (Class key : this.associatedEntities.keySet()) {
            all.addAll(this.associatedEntities.get(key));
        }
        return all;
    }

    public String toString() {
        return this.primaryEntity.toString() + "(" + this.associatedEntities.values().stream().map(Object::toString).collect(Collectors.joining(", ")) + ")";
    }
}
