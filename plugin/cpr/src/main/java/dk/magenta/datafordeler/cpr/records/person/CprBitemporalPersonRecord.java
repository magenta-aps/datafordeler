package dk.magenta.datafordeler.cpr.records.person;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class CprBitemporalPersonRecord<S extends CprBitemporalPersonRecord<S>> extends CprBitemporalRecord<PersonEntity, S> {

    public static final String DB_FIELD_ENTITY = CprBitemporalRecord.DB_FIELD_ENTITY;

    @JsonIgnore
    public String getFieldName() {
        return "";
    }

    public abstract S clone();

    public boolean updateBitemporalityByCloning() {
        return false;
    }

    @JsonIgnore
    public boolean isActiveRecord() {
        return this.getEffectTo() == null && this.getRegistrationTo() == null && !this.isUndone();
    }

}
