package dk.magenta.datafordeler.cpr.records.person.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.person.CprBitemporalPersonRecord;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Storage for data on a Person's moving between municipalities,
 * referenced by {@link dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData}
 */
@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + MoveMunicipalityDataRecord.TABLE_NAME, indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + MoveMunicipalityDataRecord.TABLE_NAME + CprBitemporalPersonRecord.DB_FIELD_ENTITY, columnList = CprBitemporalPersonRecord.DB_FIELD_ENTITY + DatabaseEntry.REF),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + MoveMunicipalityDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + MoveMunicipalityDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_TO, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_TO),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + MoveMunicipalityDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_FROM, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + MoveMunicipalityDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_TO, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_TO),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + MoveMunicipalityDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_CORRECTION_OF, columnList = CprBitemporalRecord.DB_FIELD_CORRECTION_OF + DatabaseEntry.REF),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + MoveMunicipalityDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REPLACED_BY, columnList = CprBitemporalRecord.DB_FIELD_REPLACED_BY + DatabaseEntry.REF)
})
public class MoveMunicipalityDataRecord extends CprBitemporalPersonRecord<MoveMunicipalityDataRecord> {

    public static final String TABLE_NAME = "cpr_person_movemunicipality_record";

    public MoveMunicipalityDataRecord() {
    }

    public MoveMunicipalityDataRecord(LocalDateTime outDatetime, boolean outDatetimeUncertain, int outMunicipality, LocalDateTime inDatetime, boolean inDatetimeUncertain) {
        this.outDatetime = outDatetime;
        this.outDatetimeUncertain = outDatetimeUncertain;
        this.outMunicipality = outMunicipality;
        this.inDatetime = inDatetime;
        this.inDatetimeUncertain = inDatetimeUncertain;
    }

    @JsonIgnore
    public String getFieldName() {
        return TABLE_NAME;
    }

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
    public boolean equalData(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equalData(o)) return false;
        MoveMunicipalityDataRecord that = (MoveMunicipalityDataRecord) o;
        return outDatetimeUncertain == that.outDatetimeUncertain &&
                outMunicipality == that.outMunicipality &&
                inDatetimeUncertain == that.inDatetimeUncertain &&
                Objects.equals(outDatetime, that.outDatetime) &&
                Objects.equals(inDatetime, that.inDatetime);
    }

    @Override
    public boolean hasData() {
        return this.outDatetimeUncertain || this.outMunicipality != 0 || this.inDatetimeUncertain || this.outDatetime != null || this.inDatetime != null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), outDatetime, outDatetimeUncertain, outMunicipality, inDatetime, inDatetimeUncertain);
    }

    @Override
    public MoveMunicipalityDataRecord clone() {
        MoveMunicipalityDataRecord clone = new MoveMunicipalityDataRecord();
        clone.outDatetime = this.outDatetime;
        clone.outDatetimeUncertain = this.outDatetimeUncertain;
        clone.outMunicipality = this.outMunicipality;
        clone.inDatetime = this.inDatetime;
        clone.inDatetimeUncertain = this.inDatetimeUncertain;
        CprBitemporalRecord.copy(this, clone);
        return clone;
    }
}
