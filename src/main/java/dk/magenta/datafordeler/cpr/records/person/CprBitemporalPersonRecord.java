package dk.magenta.datafordeler.cpr.records.person;

import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;

import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
public abstract class CprBitemporalPersonRecord<S extends CprBitemporalPersonRecord<S>> extends CprBitemporalRecord<PersonEntity, S> {

    public static final String DB_FIELD_ENTITY = CprBitemporalRecord.DB_FIELD_ENTITY;

    public abstract S clone();

    public boolean updateBitemporalityByCloning() {
        return false;
    }

}
