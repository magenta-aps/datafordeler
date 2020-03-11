package dk.magenta.datafordeler.core.fapi;

import dk.magenta.datafordeler.core.database.IdentifiedEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ResultSet<E extends IdentifiedEntity> {
    E primaryEntity;
    HashSet<IdentifiedEntity> associatedEntities = new HashSet<>();

    public ResultSet(E primaryEntity) {
        this.primaryEntity = primaryEntity;
    }

    public ResultSet(E primaryEntity, Set<IdentifiedEntity> associatedEntities) {
        this.primaryEntity = primaryEntity;
        this.associatedEntities.addAll(associatedEntities);
    }

    public ResultSet(Object[] databaseRow, List<String> classNames) throws ClassNotFoundException {
        this.primaryEntity = (E) cast(databaseRow[0], classNames.get(0));
        for (int i=1; i<databaseRow.length; i++) {
            this.associatedEntities.add(cast(databaseRow[i], classNames.get(i)));
        }
    }

    private static IdentifiedEntity cast(Object object, String className) throws ClassNotFoundException {
        Class entityClass = Class.forName(className);
        return (IdentifiedEntity) entityClass.cast(object);
    }
}
