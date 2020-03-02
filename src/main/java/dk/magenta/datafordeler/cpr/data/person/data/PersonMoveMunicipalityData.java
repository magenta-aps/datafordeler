package dk.magenta.datafordeler.cpr.data.person.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.cpr.CprPlugin;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Storage for data on a Person's moving between municipalities,
 * referenced by {@link dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData}
 */
@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_person_move_municipality")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PersonMoveMunicipalityData extends AuthorityDetailData {


    public static final String DB_FIELD_OUT_DATETIME = "outDatetime";
    public static final String IO_FIELD_OUT_DATETIME = "fraflytningsdatoKommune";
    @Column(name = DB_FIELD_OUT_DATETIME)
    @JsonProperty(value = IO_FIELD_OUT_DATETIME)
    @XmlElement(name = IO_FIELD_OUT_DATETIME)
    private LocalDateTime outDatetime;

    public LocalDateTime getOutDatetime() {
        return this.outDatetime;
    }

    public void setOutDatetime(LocalDateTime outDatetime) {
        this.outDatetime = outDatetime;
    }



    public static final String DB_FIELD_OUT_DATETIME_UNCERTAIN = "outDatetimeUncertain";
    public static final String IO_FIELD_OUT_DATETIME_UNCERTAIN = "fraflytningsdatoKommuneUsikkerhedsmarkering";
    @Column(name = DB_FIELD_OUT_DATETIME_UNCERTAIN)
    @JsonProperty(value = IO_FIELD_OUT_DATETIME_UNCERTAIN)
    @XmlElement(name = IO_FIELD_OUT_DATETIME_UNCERTAIN)
    private boolean outDatetimeUncertain;

    public boolean isOutDatetimeUncertain() {
        return this.outDatetimeUncertain;
    }

    public void setOutDatetimeUncertain(boolean outDatetimeUncertain) {
        this.outDatetimeUncertain = outDatetimeUncertain;
    }



    public static final String DB_FIELD_OUT_MUNICIPALITY = "outMunicipality";
    public static final String IO_FIELD_OUT_MUNICIPALITY = "fraflytningskommunekode";
    @Column(name = DB_FIELD_OUT_MUNICIPALITY)
    @JsonProperty(value = IO_FIELD_OUT_MUNICIPALITY)
    @XmlElement(name = IO_FIELD_OUT_MUNICIPALITY)
    private int outMunicipality;

    public int getOutMunicipality() {
        return this.outMunicipality;
    }

    public void setOutMunicipality(int outMunicipality) {
        this.outMunicipality = outMunicipality;
    }


    public static final String DB_FIELD_IN_DATETIME = "inDatetime";
    public static final String IO_FIELD_IN_DATETIME = "tilflytningsdatoKommune";
    @Column(name = DB_FIELD_IN_DATETIME)
    @JsonProperty(value = IO_FIELD_IN_DATETIME)
    @XmlElement(name = IO_FIELD_IN_DATETIME)
    private LocalDateTime inDatetime;

    public LocalDateTime getInDatetime() {
        return this.inDatetime;
    }

    public void setInDatetime(LocalDateTime inDatetime) {
        this.inDatetime = inDatetime;
    }



    public static final String DB_FIELD_IN_DATETIME_UNCERTAIN = "inDatetimeUncertain";
    public static final String IO_FIELD_IN_DATETIME_UNCERTAIN = "tilflytningsdatoKommuneUsikkerhedsmarkering";
    @Column(name = DB_FIELD_IN_DATETIME_UNCERTAIN)
    @JsonProperty(value = IO_FIELD_IN_DATETIME_UNCERTAIN)
    @XmlElement(name = IO_FIELD_IN_DATETIME_UNCERTAIN)
    private boolean inDatetimeUncertain;

    public boolean isInDatetimeUncertain() {
        return this.inDatetimeUncertain;
    }

    public void setInDatetimeUncertain(boolean inDatetimeUncertain) {
        this.inDatetimeUncertain = inDatetimeUncertain;
    }



    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>(super.asMap());
        map.put("outDatetime", this.outDatetime);
        map.put("outDatetimeUncertain", this.outDatetimeUncertain);
        map.put("outMunicipality", this.outMunicipality);
        map.put("inDatetime", this.inDatetime);
        map.put("inDatetimeUncertain", this.inDatetimeUncertain);
        return map;
    }

    @Override
    protected PersonMoveMunicipalityData clone() {
        PersonMoveMunicipalityData clone = new PersonMoveMunicipalityData();
        clone.outDatetime = this.outDatetime;
        clone.outDatetimeUncertain = this.outDatetimeUncertain;
        clone.outMunicipality = this.outMunicipality;
        clone.inDatetime = this.inDatetime;
        clone.inDatetimeUncertain = this.inDatetimeUncertain;
        clone.setAuthority(this.getAuthority());
        clone.setDafoUpdated(this.getDafoUpdated());
        return clone;
    }
}
