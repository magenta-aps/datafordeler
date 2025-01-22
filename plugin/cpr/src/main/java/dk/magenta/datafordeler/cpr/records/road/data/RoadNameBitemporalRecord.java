package dk.magenta.datafordeler.cpr.records.road.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cpr.CprPlugin;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import javax.xml.bind.annotation.XmlElement;
import java.util.Objects;

@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + RoadNameBitemporalRecord.TABLE_NAME, indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + RoadNameBitemporalRecord.TABLE_NAME + RoadNameBitemporalRecord.DB_FIELD_ENTITY, columnList = CprBitemporalRoadRecord.DB_FIELD_ENTITY + DatabaseEntry.REF),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + RoadNameBitemporalRecord.TABLE_NAME + RoadNameBitemporalRecord.DB_FIELD_ROADNAME, columnList = RoadNameBitemporalRecord.DB_FIELD_ROADNAME),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + RoadNameBitemporalRecord.TABLE_NAME + RoadNameBitemporalRecord.DB_FIELD_REPLACED_BY, columnList = CprBitemporalRoadRecord.DB_FIELD_REPLACED_BY + DatabaseEntry.REF),
})
public class RoadNameBitemporalRecord extends CprBitemporalRoadRecord<RoadNameBitemporalRecord> {

    public static final String TABLE_NAME = "cpr_road_name_record";

    public RoadNameBitemporalRecord() {
    }

    public RoadNameBitemporalRecord(int toMunicipalityCode, int toRoadCode, int fromMunicipalityCode, int fromRoadCode, String roadAdddressName, String roadName) {
        this.toMunicipalityCode = toMunicipalityCode;
        this.toRoadCode = toRoadCode;
        this.fromMunicipalityCode = fromMunicipalityCode;
        this.fromRoadCode = fromRoadCode;
        this.roadAdddressName = roadAdddressName;
        this.roadName = roadName;
    }


    // Til kommunekode
    public static final String DB_FIELD_TO_MUNIPALITY_CODE = "toMunicipalityCode";
    public static final String IO_FIELD_TO_MUNIPALITY_CODE = "tilKommuneKode";
    @Column(name = DB_FIELD_TO_MUNIPALITY_CODE)
    @JsonProperty(value = IO_FIELD_TO_MUNIPALITY_CODE)
    @XmlElement(name = IO_FIELD_TO_MUNIPALITY_CODE)
    private int toMunicipalityCode;

    public int getToMunicipalityCode() {
        return this.toMunicipalityCode;
    }

    public void setToMunicipalityCode(int toMunicipalityCode) {
        this.toMunicipalityCode = toMunicipalityCode;
    }

    // Til vejkode
    public static final String DB_FIELD_TO_ROAD_CODE = "toRoadCode";
    public static final String IO_FIELD_TO_ROAD_CODE = "tilVejKode";
    @Column(name = DB_FIELD_TO_ROAD_CODE)
    @JsonProperty(value = IO_FIELD_TO_ROAD_CODE)
    @XmlElement(name = IO_FIELD_TO_ROAD_CODE)
    private int toRoadCode;

    public int getToRoadCode() {
        return toRoadCode;
    }

    public void setToRoadCode(int toRoadCode) {
        this.toRoadCode = toRoadCode;
    }


    // Fra kommunekode
    public static final String DB_FIELD_FROM_MUNIPALITY_CODE = "fromMunicipalityCode";
    public static final String IO_FIELD_FROM_MUNIPALITY_CODE = "fraKommuneKode";
    @Column(name = DB_FIELD_FROM_MUNIPALITY_CODE)
    @JsonProperty(value = IO_FIELD_FROM_MUNIPALITY_CODE)
    @XmlElement(name = IO_FIELD_FROM_MUNIPALITY_CODE)
    private int fromMunicipalityCode;

    public int getFromMunicipalityCode() {
        return fromMunicipalityCode;
    }

    public void setFromMunicipalityCode(int fromMunicipalityCode) {
        this.fromMunicipalityCode = fromMunicipalityCode;
    }

    // Fra vejkode
    public static final String DB_FIELD_FROM_ROAD_CODE = "fromRoadCode";
    public static final String IO_FIELD_FROM_ROAD_CODE = "fraVejKode";
    @Column(name = DB_FIELD_FROM_ROAD_CODE)
    @JsonProperty(value = IO_FIELD_FROM_ROAD_CODE)
    @XmlElement(name = IO_FIELD_FROM_ROAD_CODE)
    private int fromRoadCode;

    public int getFromRoadCode() {
        return fromRoadCode;
    }

    public void setFromRoadCode(int fromRoadCode) {
        this.fromRoadCode = fromRoadCode;
    }


    // Vejens adressenavn
    public static final String DB_FIELD_ADDRESS_NAME = "roadAdddressName";
    public static final String IO_FIELD_ADDRESS_NAME = "addressenavn";
    @Column(name = DB_FIELD_ADDRESS_NAME)
    @JsonProperty(value = IO_FIELD_ADDRESS_NAME)
    @XmlElement(name = IO_FIELD_ADDRESS_NAME)
    private String roadAdddressName;

    public String getRoadAdddressName() {
        return roadAdddressName;
    }

    public void setRoadAdddressName(String roadAdddressName) {
        this.roadAdddressName = roadAdddressName;
    }

    // Vejens navn
    public static final String DB_FIELD_ROADNAME = "roadName";
    public static final String IO_FIELD_ROADNAME = "vejnavn";
    @Column(name = DB_FIELD_ROADNAME)
    @JsonProperty(value = IO_FIELD_ROADNAME)
    @XmlElement(name = IO_FIELD_ROADNAME)
    private String roadName;

    public String getRoadName() {
        return roadName;
    }

    public void setRoadName(String roadName) {
        this.roadName = roadName;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoadNameBitemporalRecord)) return false;
        if (!super.equals(o)) return false;
        RoadNameBitemporalRecord that = (RoadNameBitemporalRecord) o;
        return Objects.equals(toMunicipalityCode, that.toMunicipalityCode) &&
                Objects.equals(toRoadCode, that.toRoadCode) &&
                Objects.equals(fromMunicipalityCode, that.fromMunicipalityCode) &&
                Objects.equals(fromRoadCode, that.fromRoadCode) &&
                Objects.equals(roadAdddressName, that.roadAdddressName) &&
                Objects.equals(roadName, that.roadName);
    }

    @Override
    public boolean hasData() {
        return this.toMunicipalityCode != 0 || this.toRoadCode != 0 ||
                this.fromMunicipalityCode != 0 || this.fromRoadCode != 0 ||
                stringNonEmpty(this.roadAdddressName) || stringNonEmpty(this.roadName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), toMunicipalityCode, toRoadCode, fromMunicipalityCode, fromRoadCode, roadAdddressName, roadName);
    }

    @Override
    public RoadNameBitemporalRecord clone() {
        RoadNameBitemporalRecord clone = new RoadNameBitemporalRecord();
        clone.toMunicipalityCode = this.toMunicipalityCode;
        clone.toRoadCode = this.toRoadCode;
        clone.fromMunicipalityCode = this.fromMunicipalityCode;
        clone.fromRoadCode = this.fromRoadCode;
        clone.roadAdddressName = this.roadAdddressName;
        clone.roadName = this.roadName;
        RoadNameBitemporalRecord.copy(this, clone);
        return clone;
    }
}
