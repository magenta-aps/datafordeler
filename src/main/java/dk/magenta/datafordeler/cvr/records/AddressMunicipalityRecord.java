package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.Bitemporal;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cvr.records.unversioned.Municipality;
import org.hibernate.Session;

import javax.persistence.*;

/**
 * Record for Company form.
 */
@Entity
@Table(name = AddressMunicipalityRecord.TABLE_NAME, indexes = {
        @Index(name = AddressMunicipalityRecord.TABLE_NAME + "__municipality", columnList = AddressMunicipalityRecord.DB_FIELD_MUNICIPALITY + DatabaseEntry.REF)
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddressMunicipalityRecord extends CvrBitemporalRecord {

    public static final String TABLE_NAME = "cvr_record_address_municipality";

    public static final String IO_FIELD_MUNICIPALITY_CODE = "kommuneKode";

    @Transient
    @JsonProperty(value = IO_FIELD_MUNICIPALITY_CODE)
    private int municipalityCode;

    public int getMunicipalityCode() {
        if (this.municipality != null) {
            return this.municipality.getCode();
        }
        return this.municipalityCode;
    }

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



}
