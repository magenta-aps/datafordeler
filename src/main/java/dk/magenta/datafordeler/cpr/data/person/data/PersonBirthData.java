package dk.magenta.datafordeler.cpr.data.person.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.data.DetailData;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Storage for data on a Person's birth,
 * referenced by {@link dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData}
 */
@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_person_birth", indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_person_birth_time", columnList = PersonBirthData.DB_FIELD_BIRTH_DATETIME)
})
public class PersonBirthData extends DetailData {

    public static final String DB_FIELD_BIRTH_PLACE_CODE = "birthPlaceCode";
    public static final String IO_FIELD_BIRTH_PLACE_CODE = "cprFødselsregistreringsstedskode";
    @Column(name = DB_FIELD_BIRTH_PLACE_CODE)
    @JsonProperty(value = IO_FIELD_BIRTH_PLACE_CODE)
    @XmlElement(name = IO_FIELD_BIRTH_PLACE_CODE)
    private Integer birthPlaceCode;

    public Integer getBirthPlaceCode() {
        return this.birthPlaceCode;
    }

    public void setBirthPlaceCode(Integer birthPlaceCode) {
        this.birthPlaceCode = birthPlaceCode;
    }



    public static final String DB_FIELD_BIRTH_PLACE_NAME = "birthPlaceName";
    public static final String IO_FIELD_BIRTH_PLACE_NAME = "cprFødselsregistreringsstedsnavn";
    @Column(name = DB_FIELD_BIRTH_PLACE_NAME)
    @JsonProperty(value = IO_FIELD_BIRTH_PLACE_NAME)
    @XmlElement(name = IO_FIELD_BIRTH_PLACE_NAME)
    private String birthPlaceName;

    public String getBirthPlaceName() {
        return this.birthPlaceName;
    }

    public void setBirthPlaceName(String birthPlaceName) {
        this.birthPlaceName = birthPlaceName;
    }



    public static final String DB_FIELD_BIRTH_DATETIME = "birthDatetime";
    public static final String IO_FIELD_BIRTH_DATETIME = "fødselsdato";
    @Column(name = DB_FIELD_BIRTH_DATETIME)
    @JsonProperty(value = IO_FIELD_BIRTH_DATETIME)
    @XmlElement(name = IO_FIELD_BIRTH_DATETIME)
    private LocalDateTime birthDatetime;

    public LocalDateTime getBirthDatetime() {
        return this.birthDatetime;
    }

    public void setBirthDatetime(LocalDateTime birthDatetime) {
        this.birthDatetime = birthDatetime;
    }



    public static final String DB_FIELD_BIRTH_DATETIME_UNCERTAIN = "birthDatetimeUncertain";
    public static final String IO_FIELD_BIRTH_DATETIME_UNCERTAIN = "fødselsdatoUsikkerhedsmarkering";
    @Column(name = DB_FIELD_BIRTH_DATETIME_UNCERTAIN)
    @JsonProperty(value = IO_FIELD_BIRTH_DATETIME_UNCERTAIN)
    @XmlElement(name = IO_FIELD_BIRTH_DATETIME_UNCERTAIN)
    private boolean birthDatetimeUncertain;

    public boolean isBirthDatetimeUncertain() {
        return this.birthDatetimeUncertain;
    }

    public void setBirthDatetimeUncertain(boolean birthDatetimeUncertain) {
        this.birthDatetimeUncertain = birthDatetimeUncertain;
    }



    //Ikke i grunddatamodellen

    @Transient
    private int foedselsraekkefoelge;

    public int getFoedselsraekkefoelge() {
        return this.foedselsraekkefoelge;
    }

    public void setFoedselsraekkefoelge(int foedselsraekkefoelge) {
        this.foedselsraekkefoelge = foedselsraekkefoelge;
    }


    public static final String DB_FIELD_BIRTH_AUTHORITY_TEXT = "birthAuthorityText";
    public static final String IO_FIELD_BIRTH_AUTHORITY_TEXT = "fødselsMyndighedTekst";
    @Column(name = DB_FIELD_BIRTH_AUTHORITY_TEXT)
    @JsonProperty(value = IO_FIELD_BIRTH_AUTHORITY_TEXT)
    @XmlElement(name = IO_FIELD_BIRTH_AUTHORITY_TEXT)
    private Integer birthAuthorityText;

    public Integer getBirthAuthorityText() {
        return this.birthAuthorityText;
    }

    public void setBirthAuthorityText(Integer birthAuthorityText) {
        this.birthAuthorityText = birthAuthorityText;
    }



    public static final String DB_FIELD_BIRTH_SUPPLEMENTAL_TEXT = "birthSupplementalText";
    public static final String IO_FIELD_BIRTH_SUPPLEMENTAL_TEXT = "fødselsSupplerendeTekst";
    @Column(name = DB_FIELD_BIRTH_SUPPLEMENTAL_TEXT)
    @JsonProperty(value = IO_FIELD_BIRTH_SUPPLEMENTAL_TEXT)
    @XmlElement(name = IO_FIELD_BIRTH_SUPPLEMENTAL_TEXT)
    private String birthSupplementalText;

    public String getBirthSupplementalText() {
        return this.birthSupplementalText;
    }

    public void setBirthSupplementalText(String birthSupplementalText) {
        this.birthSupplementalText = birthSupplementalText;
    }

    @Override
    public Map<String, Object> databaseFields() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(DB_FIELD_BIRTH_PLACE_CODE, this.birthPlaceCode);
        map.put(DB_FIELD_BIRTH_PLACE_NAME, this.birthPlaceName);
        map.put(DB_FIELD_BIRTH_DATETIME, this.birthDatetime);
        map.put(DB_FIELD_BIRTH_DATETIME_UNCERTAIN, this.birthDatetimeUncertain);
        return map;
    }

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        //Person
        map.put("birthPlaceCode", this.birthPlaceCode);
        map.put("birthPlaceName", this.birthPlaceName);
        map.put("birthAuthorityText", this.birthAuthorityText);
        map.put("birthDatetime", this.birthDatetime);
        map.put("birthDatetimeUncertain", this.birthDatetimeUncertain);

        //Ikke i grunddatamodellen
        //map.put("foedselsraekkefoelge", this.foedselsraekkefoelge);
        return map;
    }

    @Override
    protected PersonBirthData clone() {
        PersonBirthData clone = new PersonBirthData();
        clone.birthPlaceCode = this.birthPlaceCode;
        clone.birthPlaceName = this.birthPlaceName;
        clone.birthAuthorityText = this.birthAuthorityText;
        clone.birthDatetime = this.birthDatetime;
        clone.birthDatetimeUncertain = this.birthDatetimeUncertain;
        clone.foedselsraekkefoelge = this.foedselsraekkefoelge;
        clone.birthSupplementalText = this.birthSupplementalText;
        clone.setDafoUpdated(this.getDafoUpdated());
        return clone;
    }
}
