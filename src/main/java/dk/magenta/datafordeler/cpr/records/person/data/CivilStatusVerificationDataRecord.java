package dk.magenta.datafordeler.cpr.records.person.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import java.util.Objects;

/**
 * Storage for data on a Person's civil status verification,
 * referenced by {@link dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData}
 */
@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + CivilStatusVerificationDataRecord.TABLE_NAME, indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + CivilStatusVerificationDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + CivilStatusVerificationDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_TO, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_TO),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + CivilStatusVerificationDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_FROM, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + CivilStatusVerificationDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_TO, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_TO),
})
public class CivilStatusVerificationDataRecord extends VerificationDataRecord {

    public static final String TABLE_NAME = "cpr_person_civilstatus_verification_record";

    public CivilStatusVerificationDataRecord() {
    }

    public CivilStatusVerificationDataRecord(boolean verified, String correctionMarking) {
        super(verified);
        this.correctionMarking = correctionMarking;
    }

    public static final String DB_FIELD_CORRECTION_MARKING = "correctionMarking";
    public static final String IO_FIELD_CORRECTION_MARKING = "retFortrydMarkering";
    @Column(name = DB_FIELD_CORRECTION_MARKING, length = 1)
    @JsonProperty(value = IO_FIELD_CORRECTION_MARKING)
    @XmlElement(name = IO_FIELD_CORRECTION_MARKING)
    private String correctionMarking;

    public String getCorrectionMarking() {
        return this.correctionMarking;
    }

    public void setCorrectionMarking(String correctionMarking) {
        this.correctionMarking = correctionMarking;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CivilStatusVerificationDataRecord that = (CivilStatusVerificationDataRecord) o;
        return Objects.equals(correctionMarking, that.correctionMarking);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), correctionMarking);
    }

    @Override
    protected CivilStatusVerificationDataRecord clone() {
        CivilStatusVerificationDataRecord clone = new CivilStatusVerificationDataRecord();
        clone.setCorrectionMarking(this.getCorrectionMarking());
        VerificationDataRecord.copy(this, clone);
        return clone;
    }
}
