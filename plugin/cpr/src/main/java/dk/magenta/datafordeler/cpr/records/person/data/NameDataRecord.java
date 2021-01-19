package dk.magenta.datafordeler.cpr.records.person.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.person.CprBitemporalPersonRecord;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import java.util.Objects;

/**
 * Storage for data on a Person's name,
 * referenced by {@link dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData}
 */
@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + NameDataRecord.TABLE_NAME, indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + NameDataRecord.TABLE_NAME + CprBitemporalPersonRecord.DB_FIELD_ENTITY, columnList = CprBitemporalPersonRecord.DB_FIELD_ENTITY + DatabaseEntry.REF),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + NameDataRecord.TABLE_NAME + NameDataRecord.DB_FIELD_FIRST_NAMES, columnList = NameDataRecord.DB_FIELD_FIRST_NAMES),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + NameDataRecord.TABLE_NAME + NameDataRecord.DB_FIELD_LAST_NAME, columnList = NameDataRecord.DB_FIELD_LAST_NAME),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + NameDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + NameDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_TO, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_TO),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + NameDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_FROM, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + NameDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_TO, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_TO),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + NameDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_CORRECTION_OF, columnList = CprBitemporalRecord.DB_FIELD_CORRECTION_OF + DatabaseEntry.REF),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + NameDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REPLACED_BY, columnList = CprBitemporalRecord.DB_FIELD_REPLACED_BY + DatabaseEntry.REF)
})
public class NameDataRecord extends CprBitemporalPersonRecord<NameDataRecord> {

    public static final String TABLE_NAME = "cpr_person_name_record";

    public NameDataRecord() {
    }

    public NameDataRecord(String addressingName, String firstNames, boolean firstNamesMarking, String middleName, boolean middleNameMarking, String lastName, boolean lastNameMarking, String egetEfternavn, boolean egetEfternavnMarkering) {
        this.addressingName = addressingName;
        this.firstNames = firstNames;
        this.firstNamesMarking = firstNamesMarking;
        this.middleName = middleName;
        this.middleNameMarking = middleNameMarking;
        this.lastName = lastName;
        this.lastNameMarking = lastNameMarking;
        this.egetEfternavn = egetEfternavn;
        this.egetEfternavnMarkering = egetEfternavnMarkering;
    }

    @JsonIgnore
    public String getFieldName() {
        return TABLE_NAME;
    }

    public static final String DB_FIELD_ADDRESSING_NAME = "addressingName";
    public static final String IO_FIELD_ADDRESSING_NAME = "adresseringsnavn";
    @Column(name = DB_FIELD_ADDRESSING_NAME)
    @JsonProperty(value = IO_FIELD_ADDRESSING_NAME)
    @XmlElement(name = IO_FIELD_ADDRESSING_NAME)
    private String addressingName;

    public String getAddressingName() {
        return this.addressingName;
    }

    public void setAddressingName(String addressingName) {
        this.addressingName = addressingName;
    }



    public static final String DB_FIELD_FIRST_NAMES = "firstNames";
    public static final String IO_FIELD_FIRST_NAMES = "fornavn";
    @Column(name = DB_FIELD_FIRST_NAMES)
    @JsonProperty(value = IO_FIELD_FIRST_NAMES)
    @XmlElement(name = IO_FIELD_FIRST_NAMES)
    private String firstNames;

    public String getFirstNames() {
        return this.firstNames;
    }

    public void setFirstNames(String firstNames) {
        this.firstNames = firstNames;
    }



    public static final String DB_FIELD_FIRST_NAMES_MARKING = "firstNamesMarking";
    public static final String IO_FIELD_FIRST_NAMES_MARKING = "fornavnMarkering";
    @Column(name = DB_FIELD_FIRST_NAMES_MARKING)
    @JsonProperty(value = IO_FIELD_FIRST_NAMES_MARKING)
    @XmlElement(name = IO_FIELD_FIRST_NAMES_MARKING)
    private boolean firstNamesMarking;

    public boolean isFirstNamesMarking() {
        return this.firstNamesMarking;
    }

    public void setFirstNamesMarking(boolean firstNamesMarking) {
        this.firstNamesMarking = firstNamesMarking;
    }



    public static final String DB_FIELD_MIDDLE_NAME = "middleName";
    public static final String IO_FIELD_MIDDLE_NAME = "mellemnavn";
    @Column(name = DB_FIELD_MIDDLE_NAME)
    @JsonProperty(value = IO_FIELD_MIDDLE_NAME)
    @XmlElement(name = IO_FIELD_MIDDLE_NAME)
    private String middleName;

    public String getMiddleName() {
        return this.middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }



    public static final String DB_FIELD_MIDDLE_NAME_MARKING = "middleNameMarking";
    public static final String IO_FIELD_MIDDLE_NAME_MARKING = "mellemnavnMarkering";
    @Column(name = DB_FIELD_MIDDLE_NAME_MARKING)
    @JsonProperty(value = IO_FIELD_MIDDLE_NAME_MARKING)
    @XmlElement(name = IO_FIELD_MIDDLE_NAME_MARKING)
    private boolean middleNameMarking;

    public boolean isMiddleNameMarking() {
        return this.middleNameMarking;
    }

    public void setMiddleNameMarking(boolean middleNameMarking) {
        this.middleNameMarking = middleNameMarking;
    }



    public static final String DB_FIELD_LAST_NAME = "lastName";
    public static final String IO_FIELD_LAST_NAME = "efternavn";
    @Column(name = DB_FIELD_LAST_NAME)
    @JsonProperty(value = IO_FIELD_LAST_NAME)
    @XmlElement(name = IO_FIELD_LAST_NAME)
    private String lastName;

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }



    public static final String DB_FIELD_LAST_NAME_MARKING = "lastNameMarking";
    public static final String IO_FIELD_LAST_NAME_MARKING = "efternavnMarkering";
    @Column(name = DB_FIELD_LAST_NAME_MARKING)
    @JsonProperty(value = IO_FIELD_LAST_NAME_MARKING)
    @XmlElement(name = IO_FIELD_LAST_NAME_MARKING)
    private boolean lastNameMarking;

    public boolean isLastNameMarking() {
        return this.lastNameMarking;
    }

    public void setLastNameMarking(boolean lastNameMarking) {
        this.lastNameMarking = lastNameMarking;
    }


    //Ikke i grunddatamodellen

    @Transient
    private String egetEfternavn;

    public String getEgetEfternavn() {
        return this.egetEfternavn;
    }

    public void setEgetEfternavn(String egetEfternavn) {
        this.egetEfternavn = egetEfternavn;
    }


    @Transient
    private boolean egetEfternavnMarkering;

    public boolean isEgetEfternavnMarkering() {
        return this.egetEfternavnMarkering;
    }

    public void setEgetEfternavnMarkering(boolean egetEfternavnMarkering) {
        this.egetEfternavnMarkering = egetEfternavnMarkering;
    }



    @Override
    public boolean equalData(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equalData(o)) return false;
        NameDataRecord that = (NameDataRecord) o;
        return firstNamesMarking == that.firstNamesMarking &&
                middleNameMarking == that.middleNameMarking &&
                lastNameMarking == that.lastNameMarking &&
                egetEfternavnMarkering == that.egetEfternavnMarkering &&
                Objects.equals(addressingName, that.addressingName) &&
                Objects.equals(firstNames, that.firstNames) &&
                Objects.equals(middleName, that.middleName) &&
                Objects.equals(lastName, that.lastName) &&
                Objects.equals(egetEfternavn, that.egetEfternavn);
    }

    @Override
    public boolean hasData() {
        return this.firstNamesMarking || this.middleNameMarking || this.lastNameMarking || this.egetEfternavnMarkering ||
                stringNonEmpty(this.addressingName) || stringNonEmpty(this.firstNames) || stringNonEmpty(this.middleName) || stringNonEmpty(this.lastName) || stringNonEmpty(this.egetEfternavn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), addressingName, firstNames, firstNamesMarking, middleName, middleNameMarking, lastName, lastNameMarking, egetEfternavn, egetEfternavnMarkering);
    }

    @Override
    public NameDataRecord clone() {
        NameDataRecord clone = new NameDataRecord();
        clone.addressingName = this.addressingName;
        clone.firstNames = this.firstNames;
        clone.firstNamesMarking = this.firstNamesMarking;
        clone.middleName = this.middleName;
        clone.middleNameMarking = this.middleNameMarking;
        clone.lastName = this.lastName;
        clone.lastNameMarking = this.lastNameMarking;
        clone.egetEfternavn = this.egetEfternavn;
        clone.egetEfternavnMarkering = this.egetEfternavnMarkering;
        CprBitemporalRecord.copy(this, clone);
        return clone;
    }
}
