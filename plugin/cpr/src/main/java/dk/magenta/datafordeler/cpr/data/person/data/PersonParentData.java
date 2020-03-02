package dk.magenta.datafordeler.cpr.data.person.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.cpr.CprPlugin;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Storage for data on a Person's parentage,
 * referenced by {@link dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData}
 */
@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_person_parent")
public class PersonParentData extends AuthorityDetailData {

    public static final String DB_FIELD_IS_MOTHER = "isMother";
    @Column(name = DB_FIELD_IS_MOTHER)
    @JsonIgnore
    @XmlTransient
    private boolean isMother;

    @JsonIgnore
    @XmlTransient
    public boolean isMother() {
        return this.isMother;
    }

    public void setMother(boolean mother) {
        isMother = mother;
    }


    public static final String DB_FIELD_CPR_NUMBER = "cprNumber";
    public static final String IO_FIELD_CPR_NUMBER = "personnummer";
    @Column(name = DB_FIELD_CPR_NUMBER)
    @JsonProperty(value = IO_FIELD_CPR_NUMBER)
    @XmlElement(name = IO_FIELD_CPR_NUMBER)
    private String cprNumber;

    public String getCprNumber() {
        return this.cprNumber;
    }

    public void setCprNumber(String cprNumber) {
        this.cprNumber = cprNumber;
    }



    public static final String DB_FIELD_BIRTHDATE = "birthDate";
    public static final String IO_FIELD_BIRTHDATE = "foedselsdato";
    @Column(name = DB_FIELD_BIRTHDATE)
    @JsonProperty(value = IO_FIELD_BIRTHDATE)
    @XmlElement(name = IO_FIELD_BIRTHDATE)
    private LocalDate birthDate;

    public LocalDate getBirthDate() {
        return this.birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }



    public static final String DB_FIELD_BIRTHDATE_UNCERTAIN = "birthDateUncertain";
    public static final String IO_FIELD_BIRTHDATE_UNCERTAIN = "foedselsdatoUsikker";
    @Column(name = DB_FIELD_BIRTHDATE_UNCERTAIN)
    @JsonProperty(value = IO_FIELD_BIRTHDATE_UNCERTAIN)
    @XmlElement(name = IO_FIELD_BIRTHDATE_UNCERTAIN)
    private boolean birthDateUncertain;

    public boolean isBirthDateUncertain() {
        return this.birthDateUncertain;
    }

    public void setBirthDateUncertain(boolean birthDateUncertain) {
        this.birthDateUncertain = birthDateUncertain;
    }



    public static final String DB_FIELD_NAME = "name";
    public static final String IO_FIELD_NAME = "navn";
    @Column(name = DB_FIELD_NAME)
    @JsonProperty(value = IO_FIELD_NAME)
    @XmlElement(name = IO_FIELD_NAME)
    private String name;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }



    public static final String DB_FIELD_NAME_MARKING = "nameMarking";
    public static final String IO_FIELD_NAME_MARKING = "navneMarkering";
    @Column(name = DB_FIELD_NAME_MARKING)
    @JsonProperty(value = IO_FIELD_NAME_MARKING)
    @XmlElement(name = IO_FIELD_NAME_MARKING)
    private boolean nameMarking;

    public boolean hasNameMarking() {
        return this.nameMarking;
    }

    public void setNameMarking(boolean nameMarking) {
        this.nameMarking = nameMarking;
    }


    public Map<String, Object> databaseFields() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(DB_FIELD_BIRTHDATE, this.birthDate);
        map.put(DB_FIELD_BIRTHDATE_UNCERTAIN, this.birthDateUncertain);
        map.put(DB_FIELD_CPR_NUMBER, this.cprNumber);
        map.put(DB_FIELD_IS_MOTHER, this.isMother);
        map.put(DB_FIELD_NAME, this.name);
        map.put(DB_FIELD_NAME_MARKING, this.nameMarking);
        return map;
    }


    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>(super.asMap());
        map.put("cprNumber", this.cprNumber);
        map.put("birthDate", this.birthDate);
        map.put("birthDateUncertain", this.birthDateUncertain);
        map.put("name", this.name);
        map.put("nameMarking", this.nameMarking);
        return map;
    }

    @Override
    protected PersonParentData clone() {
        PersonParentData clone = new PersonParentData();
        clone.cprNumber = this.cprNumber;
        clone.birthDate = this.birthDate;
        clone.birthDateUncertain = this.birthDateUncertain;
        clone.isMother = this.isMother;
        clone.name = this.name;
        clone.nameMarking = this.nameMarking;
        clone.setAuthority(this.getAuthority());
        clone.setDafoUpdated(this.getDafoUpdated());
        return clone;
    }
}
