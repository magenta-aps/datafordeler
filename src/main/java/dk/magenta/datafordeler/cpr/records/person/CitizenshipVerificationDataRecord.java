package dk.magenta.datafordeler.cpr.records.person;

import com.fasterxml.jackson.annotation.JsonInclude;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * Storage for data on a Person's church verification,
 * referenced by {@link PersonBaseData}
 */
@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + CitizenshipVerificationDataRecord.TABLE_NAME, indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + CitizenshipVerificationDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + CitizenshipVerificationDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_TO, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_TO),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + CitizenshipVerificationDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_FROM, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + CitizenshipVerificationDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_TO, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_TO),
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CitizenshipVerificationDataRecord extends VerificationDataRecord {

    public static final String TABLE_NAME = "cpr_person_citizenship_verification_record";

    public CitizenshipVerificationDataRecord() {
    }

    public CitizenshipVerificationDataRecord(boolean verified) {
        super(verified);
    }

    @Override
    protected CitizenshipVerificationDataRecord clone() {
        CitizenshipVerificationDataRecord clone = new CitizenshipVerificationDataRecord();
        VerificationDataRecord.copy(this, clone);
        return clone;
    }
}
