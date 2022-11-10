package dk.magenta.datafordeler.cpr.records.person.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.person.CprBitemporalPersonRecord;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Storage for data on a Person's civil status,
 * referenced by {@link dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData}
 */
@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + CivilStatusDataRecord.TABLE_NAME, indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + CivilStatusDataRecord.TABLE_NAME + CprBitemporalPersonRecord.DB_FIELD_ENTITY, columnList = CprBitemporalPersonRecord.DB_FIELD_ENTITY + DatabaseEntry.REF),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + CivilStatusDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + CivilStatusDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_TO, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_TO),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + CivilStatusDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_FROM, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + CivilStatusDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_TO, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_TO),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + CivilStatusDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_CORRECTION_OF, columnList = CprBitemporalRecord.DB_FIELD_CORRECTION_OF + DatabaseEntry.REF),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + CivilStatusDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REPLACED_BY, columnList = CprBitemporalRecord.DB_FIELD_REPLACED_BY + DatabaseEntry.REF)
})
public class CivilStatusDataRecord extends CprBitemporalPersonRecord<CivilStatusDataRecord> {

    public static final String TABLE_NAME = "cpr_person_civilstatus_record";

    public CivilStatusDataRecord() {
    }

    public CivilStatusDataRecord(String correctionMarking, String civilStatus, String spouseCpr, LocalDate spouseBirthdate, boolean spouseBirthdateUncertain, String spouseName, boolean spouseNameMarking) {
        this.correctionMarking = correctionMarking;
        this.civilStatus = civilStatus;
        this.spouseCpr = spouseCpr;
        this.spouseBirthdate = spouseBirthdate;
        this.spouseBirthdateUncertain = spouseBirthdateUncertain;
        this.spouseName = spouseName;
        this.spouseNameMarking = spouseNameMarking;
    }

    @JsonIgnore
    public String getFieldName() {
        return TABLE_NAME;
    }

    public static final String DB_FIELD_CORRECTION_MARKING = "correctionMarking";
    public static final String IO_FIELD_CORRECTION_MARKING = "retFortrydMarkering";
    @Column(name = DB_FIELD_CORRECTION_MARKING, length = 1)
    @JsonIgnore
    //@JsonProperty(value = IO_FIELD_CORRECTION_MARKING)
    @XmlElement(name = IO_FIELD_CORRECTION_MARKING)
    private String correctionMarking;

    public String getCorrectionMarking() {
        return this.correctionMarking;
    }

    public void setCorrectionMarking(String correctionMarking) {
        this.correctionMarking = correctionMarking;
    }


    /*
        U = Ugift
        G = Gift
        F = Fraskilt
        E = Enke/enkemand
        P = Registreret partnerskab
        O = Ophævet partnerskab
        L = Længestlevende partner
        D = Død
    * */

    public static final String DB_FIELD_CIVIL_STATUS = "civilStatus";
    public static final String IO_FIELD_CIVIL_STATUS = "civilstand";
    @Column(name = DB_FIELD_CIVIL_STATUS)
    @JsonProperty(value = IO_FIELD_CIVIL_STATUS)
    @XmlElement(name = IO_FIELD_CIVIL_STATUS)
    private String civilStatus;

    public String getCivilStatus() {
        return this.civilStatus;
    }

    public void setCivilStatus(String civilStatus) {
        this.civilStatus = civilStatus;
    }

    public static final String DB_FIELD_SPOUSE_CPR = "spouseCpr";
    public static final String IO_FIELD_SPOUSE_CPR = "ægtefælleCpr";
    @Column(name = DB_FIELD_SPOUSE_CPR)
    @JsonProperty(value = IO_FIELD_SPOUSE_CPR)
    @XmlElement(name = IO_FIELD_SPOUSE_CPR)
    private String spouseCpr;

    public String getSpouseCpr() {
        return this.spouseCpr;
    }

    public void setSpouseCpr(String spouseCpr) {
        this.spouseCpr = spouseCpr;
    }


    public static final String DB_FIELD_SPOUSE_BIRTHDATE = "spouseBirthdate";
    public static final String IO_FIELD_SPOUSE_BIRTHDATE = "ægtefælleFødselsdato";
    @Column(name = DB_FIELD_SPOUSE_BIRTHDATE)
    @JsonProperty(value = IO_FIELD_SPOUSE_BIRTHDATE)
    @XmlElement(name = IO_FIELD_SPOUSE_BIRTHDATE)
    private LocalDate spouseBirthdate;

    public LocalDate getSpouseBirthdate() {
        return this.spouseBirthdate;
    }

    public void setSpouseBirthdate(LocalDate spouseBirthdate) {
        this.spouseBirthdate = spouseBirthdate;
    }


    public static final String DB_FIELD_SPOUSE_BIRTHDATE_UNCERTAIN = "spouseBirthdateUncertain";
    public static final String IO_FIELD_SPOUSE_BIRTHDATE_UNCERTAIN = "ægtefælleFødselsdatoUsikker";
    @Column(name = DB_FIELD_SPOUSE_BIRTHDATE_UNCERTAIN)
    @JsonProperty(value = IO_FIELD_SPOUSE_BIRTHDATE_UNCERTAIN)
    @XmlElement(name = IO_FIELD_SPOUSE_BIRTHDATE_UNCERTAIN)
    private boolean spouseBirthdateUncertain;

    public boolean getSpouseBirthdateUncertain() {
        return this.spouseBirthdateUncertain;
    }

    public void setSpouseBirthdateUncertain(boolean spouseBirthdateUncertain) {
        this.spouseBirthdateUncertain = spouseBirthdateUncertain;
    }


    public static final String DB_FIELD_SPOUSE_NAME = "spouseName";
    public static final String IO_FIELD_SPOUSE_NAME = "ægtefælleNavn";
    @Column(name = DB_FIELD_SPOUSE_NAME)
    @JsonProperty(value = IO_FIELD_SPOUSE_NAME)
    @XmlElement(name = IO_FIELD_SPOUSE_NAME)
    private String spouseName;

    public String isSpouseName() {
        return this.spouseName;
    }

    public void setSpouseName(String spouseName) {
        this.spouseName = spouseName;
    }


    public static final String DB_FIELD_SPOUSE_NAME_MARKING = "spouseNameMarking";
    public static final String IO_FIELD_SPOUSE_NAME_MARKING = "ægtefælleNavnMarkering";
    @Column(name = DB_FIELD_SPOUSE_NAME_MARKING)
    @JsonProperty(value = IO_FIELD_SPOUSE_NAME_MARKING)
    @XmlElement(name = IO_FIELD_SPOUSE_NAME_MARKING)
    private boolean spouseNameMarking;

    public boolean isSpouseNameMarking() {
        return this.spouseNameMarking;
    }

    public void setSpouseNameMarking(boolean spouseNameMarking) {
        this.spouseNameMarking = spouseNameMarking;
    }


    @Override
    public boolean equalData(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equalData(o)) return false;
        CivilStatusDataRecord that = (CivilStatusDataRecord) o;
        return spouseBirthdateUncertain == that.spouseBirthdateUncertain &&
                spouseNameMarking == that.spouseNameMarking &&
                Objects.equals(correctionMarking, that.correctionMarking) &&
                Objects.equals(civilStatus, that.civilStatus) &&
                Objects.equals(spouseCpr, that.spouseCpr) &&
                Objects.equals(spouseBirthdate, that.spouseBirthdate) &&
                Objects.equals(spouseName, that.spouseName);
    }

    @Override
    public boolean hasData() {
        return this.spouseBirthdateUncertain || this.spouseNameMarking || stringNonEmpty(this.correctionMarking) || stringNonEmpty(this.civilStatus) || stringNonEmpty(this.spouseCpr) || this.spouseBirthdate != null || stringNonEmpty(this.spouseName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), correctionMarking, civilStatus, spouseCpr, spouseBirthdate, spouseBirthdateUncertain, spouseName, spouseNameMarking);
    }

    @Override
    public CivilStatusDataRecord clone() {
        CivilStatusDataRecord clone = new CivilStatusDataRecord();
        clone.correctionMarking = this.correctionMarking;
        clone.civilStatus = this.civilStatus;
        clone.spouseCpr = this.spouseCpr;
        clone.spouseBirthdate = this.spouseBirthdate;
        clone.spouseBirthdateUncertain = this.spouseBirthdateUncertain;
        clone.spouseName = this.spouseName;
        clone.spouseNameMarking = this.spouseNameMarking;
        CprBitemporalRecord.copy(this, clone);
        return clone;
    }
}
