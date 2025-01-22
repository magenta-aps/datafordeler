package dk.magenta.datafordeler.cpr.records.person.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.person.CprBitemporalPersonRecord;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Storage for data on a Person's birth verification,
 */
@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + BirthPlaceVerificationDataRecord.TABLE_NAME, indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + BirthPlaceVerificationDataRecord.TABLE_NAME + CprBitemporalPersonRecord.DB_FIELD_ENTITY, columnList = CprBitemporalPersonRecord.DB_FIELD_ENTITY + DatabaseEntry.REF),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + BirthPlaceVerificationDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + BirthPlaceVerificationDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_TO, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_TO),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + BirthPlaceVerificationDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_FROM, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + BirthPlaceVerificationDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_TO, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_TO),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + BirthPlaceVerificationDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REPLACED_BY, columnList = CprBitemporalRecord.DB_FIELD_REPLACED_BY + DatabaseEntry.REF)
})
public class BirthPlaceVerificationDataRecord extends VerificationDataRecord<BirthPlaceVerificationDataRecord> {

    public static final String TABLE_NAME = "cpr_person_birthplace_verification_record";


    @OneToMany(fetch = FetchType.LAZY, mappedBy = DB_FIELD_CORRECTION_OF)
    private final Set<BirthPlaceVerificationDataRecord> correctors = new HashSet<>();

    public Set<BirthPlaceVerificationDataRecord> getCorrectors() {
        return this.correctors;
    }

    public BirthPlaceVerificationDataRecord() {
    }

    public BirthPlaceVerificationDataRecord(boolean verified) {
        super(verified);
    }

    @JsonIgnore
    public String getFieldName() {
        return TABLE_NAME;
    }

    @Override
    public BirthPlaceVerificationDataRecord clone() {
        BirthPlaceVerificationDataRecord clone = new BirthPlaceVerificationDataRecord();
        VerificationDataRecord.copy(this, clone);
        return clone;
    }
}
