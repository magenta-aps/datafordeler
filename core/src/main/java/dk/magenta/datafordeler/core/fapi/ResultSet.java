package dk.magenta.datafordeler.core.fapi;

import dk.magenta.datafordeler.core.database.IdentifiedEntity;

import java.util.*;
import java.util.stream.Collectors;

public class ResultSet<E extends IdentifiedEntity> {
    E primaryEntity;
    HashMap<Class, HashSet<IdentifiedEntity>> associatedEntities = new HashMap<>();

    public ResultSet(E primaryEntity) {
        this.primaryEntity = primaryEntity;
    }

    public ResultSet(E primaryEntity, Set<IdentifiedEntity> associatedEntities) {
        this.primaryEntity = primaryEntity;
        this.addAssociatedEntities(associatedEntities);
    }

    public ResultSet(Object databaseRow, List<String> classNames) throws ClassNotFoundException {
        this((Object[]) databaseRow, classNames);
    }

    public ResultSet(Object[] databaseRow, List<String> classNames) throws ClassNotFoundException {
        this.primaryEntity = (E) cast(databaseRow[0], classNames.get(0));
        for (int i=1; i<databaseRow.length; i++) {
            this.addAssociatedEntity(cast(databaseRow[i], classNames.get(i)));
        }
    }

    private void addAssociatedEntity(IdentifiedEntity entity) {
        Class c = entity.getClass();
        if (!this.associatedEntities.containsKey(c)) {
            this.associatedEntities.put(c, new HashSet<>());
        }
        this.associatedEntities.get(c).add(entity);
    }

    private void addAssociatedEntities(Collection<IdentifiedEntity> entities) {
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
        Class entityClass = Class.forName(className);
        return (IdentifiedEntity) entityClass.cast(object);
    }

    public E getPrimaryEntity() {
        return this.primaryEntity;
    }

    public Set<IdentifiedEntity> get(Class c) {
        return this.associatedEntities.get(c);
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

    public String toString() {
        return this.primaryEntity.toString() + "(" + this.associatedEntities.values().stream().map(Object::toString).collect(Collectors.joining(", ")) + ")";
    }
}
