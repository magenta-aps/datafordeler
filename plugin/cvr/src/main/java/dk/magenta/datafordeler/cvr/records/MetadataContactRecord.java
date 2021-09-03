package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cvr.CvrPlugin;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = CvrPlugin.DEBUG_TABLE_PREFIX + MetadataContactRecord.TABLE_NAME, indexes = {
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + MetadataContactRecord.TABLE_NAME + "__companymetadata", columnList = MetadataContactRecord.DB_FIELD_COMPANY_METADATA + DatabaseEntry.REF),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + MetadataContactRecord.TABLE_NAME + "__unitmetadata", columnList = MetadataContactRecord.DB_FIELD_UNIT_METADATA + DatabaseEntry.REF),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + MetadataContactRecord.TABLE_NAME + "__participantmetadata", columnList = MetadataContactRecord.DB_FIELD_PARTICIPANT_METADATA + DatabaseEntry.REF)
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetadataContactRecord extends CvrNontemporalRecord {

    public static final String TABLE_NAME = "cvr_record_metadata_contact";

    public static final String DB_FIELD_DATA = "data";

    @JsonIgnore
    public String getFieldName() {
        return TABLE_NAME;
    }

    @Column(name = DB_FIELD_DATA)
    private String data;

    public String getData() {
        return this.data;
    }

    public void setData(String data) {
        this.data = data;
    }


    public static final String DB_FIELD_COMPANY_METADATA = "companyMetadataRecord";

    @ManyToOne(targetEntity = CompanyMetadataRecord.class, fetch = FetchType.LAZY)
    @JoinColumn(name = DB_FIELD_COMPANY_METADATA + DatabaseEntry.REF)
    @JsonIgnore
    private MetadataRecord companyMetadataRecord;

    public void setCompanyMetadataRecord(CompanyMetadataRecord companyMetadataRecord) {
        this.companyMetadataRecord = companyMetadataRecord;
    }



    public static final String DB_FIELD_UNIT_METADATA = "unitMetadataRecord";

    @ManyToOne(targetEntity = CompanyUnitMetadataRecord.class, fetch = FetchType.LAZY)
    @JoinColumn(name = DB_FIELD_UNIT_METADATA + DatabaseEntry.REF)
    @JsonIgnore
    private MetadataRecord unitMetadataRecord;

    public void setUnitMetadataRecord(CompanyUnitMetadataRecord unitMetadataRecord) {
        this.unitMetadataRecord = unitMetadataRecord;
    }



    public static final String DB_FIELD_PARTICIPANT_METADATA = "participantMetadataRecord";

    @ManyToOne(targetEntity = ParticipantMetadataRecord.class, fetch = FetchType.LAZY)
    @JoinColumn(name = DB_FIELD_PARTICIPANT_METADATA + DatabaseEntry.REF)
    @JsonIgnore
    private ParticipantMetadataRecord participantMetadataRecord;

    public void setParticipantMetadataRecord(ParticipantMetadataRecord participantMetadataRecord) {
        this.participantMetadataRecord = participantMetadataRecord;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetadataContactRecord that = (MetadataContactRecord) o;
        return Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }
}
