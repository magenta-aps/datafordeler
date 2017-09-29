package dk.magenta.datafordeler.cpr.data.person.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.cpr.data.DetailData;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lars on 21-06-17.
 */
@Entity
@Table(name = "cpr_person_birth")
public class PersonBirthData extends DetailData {

    public static final String DB_FIELD_BIRTH_PLACE_CODE = "birthPlaceCode";
    public static final String IO_FIELD_BIRTH_PLACE_CODE = "cprFoedselsregistreringsstedskode";
    @Column(name = DB_FIELD_BIRTH_PLACE_CODE)
    @JsonProperty(value = IO_FIELD_BIRTH_PLACE_CODE)
    @XmlElement(name = IO_FIELD_BIRTH_PLACE_CODE)
    private String birthPlaceCode;

    public String getBirthPlaceCode() {
        return this.birthPlaceCode;
    }

    public void setBirthPlaceCode(String birthPlaceCode) {
        this.birthPlaceCode = birthPlaceCode;
    }



    public static final String DB_FIELD_BIRTH_PLACE_NAME = "birthPlaceName";
    public static final String IO_FIELD_BIRTH_PLACE_NAME = "cprFoedselsregistreringsstedsnavn";
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
    public static final String IO_FIELD_BIRTH_DATETIME = "foedselsdato";
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
    public static final String IO_FIELD_BIRTH_DATETIME_UNCERTAIN = "foedselsdatoUsikkerhedsmarkering";
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
        map.put("birthDatetime", this.birthDatetime);
        map.put("birthDatetimeUncertain", this.birthDatetimeUncertain);

        //Ikke i grunddatamodellen
        //map.put("foedselsraekkefoelge", this.foedselsraekkefoelge);
        return map;
    }
}
