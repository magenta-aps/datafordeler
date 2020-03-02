package dk.magenta.datafordeler.cpr.data.person.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.cpr.CprPlugin;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Storage for data on a Person's civil status,
 * referenced by {@link dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData}
 */
@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_person_civil_status")
public class PersonCivilStatusData extends AuthorityDetailData {



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







    public Map<String, Object> databaseFields() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(DB_FIELD_CIVIL_STATUS, this.civilStatus);
        map.put(DB_FIELD_SPOUSE_CPR, this.spouseCpr);
        map.put(DB_FIELD_SPOUSE_BIRTHDATE, this.spouseBirthdate);
        map.put(DB_FIELD_SPOUSE_BIRTHDATE_UNCERTAIN, this.spouseBirthdateUncertain);
        map.put(DB_FIELD_SPOUSE_NAME, this.spouseName);
        map.put(DB_FIELD_SPOUSE_NAME_MARKING, this.spouseNameMarking);
        return map;
    }

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>(super.asMap());

        map.put(DB_FIELD_CIVIL_STATUS, this.civilStatus);
        map.put(DB_FIELD_SPOUSE_CPR, this.spouseCpr);
        map.put(DB_FIELD_SPOUSE_BIRTHDATE, this.spouseBirthdate);
        map.put(DB_FIELD_SPOUSE_BIRTHDATE_UNCERTAIN, this.spouseBirthdateUncertain);
        map.put(DB_FIELD_SPOUSE_NAME, this.spouseName);
        map.put(DB_FIELD_SPOUSE_NAME_MARKING, this.spouseNameMarking);

        //OBS: Virkning fra og til mangler i forhold til grunddatamodellen
        return map;
    }

    @Override
    protected PersonCivilStatusData clone() {
        PersonCivilStatusData clone = new PersonCivilStatusData();
        clone.correctionMarking = this.correctionMarking;
        clone.civilStatus = this.civilStatus;
        clone.spouseCpr = this.spouseCpr;
        clone.spouseBirthdate = this.spouseBirthdate;
        clone.spouseBirthdateUncertain = this.spouseBirthdateUncertain;
        clone.spouseName = this.spouseName;
        clone.spouseNameMarking = this.spouseNameMarking;
        clone.setAuthority(this.getAuthority());
        clone.setDafoUpdated(this.getDafoUpdated());
        return clone;
    }
}
