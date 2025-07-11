package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cvr.CvrPlugin;
import dk.magenta.datafordeler.cvr.records.unversioned.Municipality;
import jakarta.persistence.*;
import org.hibernate.Session;

/**
 * Record for Company form.
 */
@Entity
@Table(name = CvrPlugin.DEBUG_TABLE_PREFIX + AddressMunicipalityRecord.TABLE_NAME, indexes = {
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + AddressMunicipalityRecord.TABLE_NAME + "__municipality", columnList = AddressMunicipalityRecord.DB_FIELD_MUNICIPALITY + DatabaseEntry.REF),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + AddressMunicipalityRecord.TABLE_NAME + "__" + CvrBitemporalRecord.DB_FIELD_LAST_UPDATED, columnList = CvrBitemporalRecord.DB_FIELD_LAST_UPDATED),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + AddressMunicipalityRecord.TABLE_NAME + "__" + CvrRecordPeriod.DB_FIELD_VALID_FROM, columnList = CvrRecordPeriod.DB_FIELD_VALID_FROM),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + AddressMunicipalityRecord.TABLE_NAME + "__" + CvrRecordPeriod.DB_FIELD_VALID_TO, columnList = CvrRecordPeriod.DB_FIELD_VALID_TO)
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddressMunicipalityRecord extends CvrBitemporalRecord implements Cloneable {

    public static final String TABLE_NAME = "cvr_record_address_municipality";

    public static final String IO_FIELD_MUNICIPALITY_CODE = "kommuneKode";

    @JsonIgnore
    public String getFieldName() {
        return TABLE_NAME;
    }

    @Transient
    private int municipalityCode;

    @JsonProperty(value = IO_FIELD_MUNICIPALITY_CODE)
    public int getMunicipalityCode() {
        if (this.municipality != null) {
            return this.municipality.getCode();
        }
        return this.municipalityCode;
    }

    @JsonProperty(value = IO_FIELD_MUNICIPALITY_CODE)
    public void setMunicipalityCode(int municipalityCode) {
        this.municipalityCode = municipalityCode;
    }

    public static final String IO_FIELD_MUNICIPALITY_NAME = "kommuneNavn";

    @Transient
    @JsonProperty(value = IO_FIELD_MUNICIPALITY_NAME)
    private String municipalityName;

    public String getMunicipalityName() {
        if (this.municipality != null) {
            return this.municipality.getName();
        }
        return this.municipalityName;
    }

    public void setMunicipalityName(String municipalityName) {
        this.municipalityName = municipalityName;
    }

    public static final String DB_FIELD_MUNICIPALITY = "municipality";

    @ManyToOne(targetEntity = Municipality.class)
    @JoinColumn(name = DB_FIELD_MUNICIPALITY + DatabaseEntry.REF)
    @JsonIgnore
    private Municipality municipality;

    public Municipality getMunicipality() {
        return this.municipality;
    }

    public void wire(Session session) {
        if (this.municipalityCode != 0 && (this.municipality == null || this.municipality.getCode() != this.municipalityCode)) {
            this.municipality = Municipality.getMunicipality(this.municipalityCode, this.municipalityName, session);
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        AddressMunicipalityRecord clone = (AddressMunicipalityRecord) super.clone();
        clone.setMunicipalityCode(this.getMunicipalityCode());
        clone.setMunicipalityName(this.getMunicipalityName());
        clone.municipality = this.getMunicipality();
        return clone;
    }

}
