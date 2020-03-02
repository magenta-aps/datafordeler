package dk.magenta.datafordeler.cpr.records.road.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.cpr.CprPlugin;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import java.time.OffsetDateTime;
import java.util.Objects;

@javax.persistence.Entity
@Table(name=CprPlugin.DEBUG_TABLE_PREFIX + RoadCityBitemporalRecord.TABLE_NAME)
public class RoadCityBitemporalRecord extends CprBitemporalRoadRecord<RoadCityBitemporalRecord> {

    public static final String TABLE_NAME = "cpr_road_city_record";

    public RoadCityBitemporalRecord() {
    }

    public RoadCityBitemporalRecord(String toHousenumber, String fromHousenumber, boolean equalUnequal, String cityName) {
        this.toHousenumber = toHousenumber;
        this.fromHousenumber = fromHousenumber;
        this.equalUnequal = equalUnequal;
        this.cityName = cityName;
    }


    // Til husnummer
    public static final String DB_FIELD_TO_HOUSENUMBER = "toHousenumber";
    public static final String IO_FIELD_TO_HOUSENUMBER = "tilHusnummer";
    @Column(name = DB_FIELD_TO_HOUSENUMBER)
    @JsonProperty(value = IO_FIELD_TO_HOUSENUMBER)
    @XmlElement(name = IO_FIELD_TO_HOUSENUMBER)
    private String toHousenumber;
    public String getToHousenumber() {
        return toHousenumber;
    }

    public void setToHousenumber(String toHousenumber) {
        this.toHousenumber = toHousenumber;
    }

    // Fra husnummer
    public static final String DB_FIELD_FROM_HOUSENUMBER = "fromHousenumber";
    public static final String IO_FIELD_FROM_HOUSENUMBER = "fraHusnummer";
    @Column(name = DB_FIELD_FROM_HOUSENUMBER)
    @JsonProperty(value = IO_FIELD_FROM_HOUSENUMBER)
    @XmlElement(name = IO_FIELD_FROM_HOUSENUMBER)
    private String fromHousenumber;
    public String getFromHousenumber() {
        return fromHousenumber;
    }

    public void setFromHousenumber(String fromHousenumber) {
        this.fromHousenumber = fromHousenumber;
    }

    // Lige/ulige indikator
    public static final String DB_FIELD_EQUAL_UNEQUAL = "equalUnequal";
    public static final String IO_FIELD_EQUAL_UNEQUAL = "ligeUlige";
    @Column(name = DB_FIELD_EQUAL_UNEQUAL)
    @JsonProperty(value = IO_FIELD_EQUAL_UNEQUAL)
    @XmlElement(name = IO_FIELD_EQUAL_UNEQUAL)
    private boolean equalUnequal;
    public boolean getEqualUnequal() {
        return equalUnequal;
    }

    public void setEqualUnequal(boolean equalUnequal) {
        this.equalUnequal = equalUnequal;
    }

    // Byens navn
    public static final String DB_FIELD_CITY_NAME = "cityName";
    public static final String IO_FIELD_CITY_NAME = "byNavn";
    @Column(name = DB_FIELD_CITY_NAME)
    @JsonProperty(value = IO_FIELD_CITY_NAME)
    @XmlElement(name = IO_FIELD_CITY_NAME)
    private String cityName;
    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoadCityBitemporalRecord)) return false;
        if (!super.equals(o)) return false;
        RoadCityBitemporalRecord that = (RoadCityBitemporalRecord) o;
        return Objects.equals(toHousenumber, that.toHousenumber) &&
                Objects.equals(fromHousenumber, that.fromHousenumber) &&
                Objects.equals(equalUnequal, that.equalUnequal) &&
                Objects.equals(cityName, that.cityName);
    }

    @Override
    public boolean hasData() {
        return stringNonEmpty(this.toHousenumber) || stringNonEmpty(this.fromHousenumber) ||
                stringNonEmpty(this.cityName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), toHousenumber, fromHousenumber, equalUnequal, cityName);
    }

    @Override
    public RoadCityBitemporalRecord clone() {
        RoadCityBitemporalRecord clone = new RoadCityBitemporalRecord();
        clone.toHousenumber = this.toHousenumber;
        clone.fromHousenumber = this.fromHousenumber;
        clone.equalUnequal = this.equalUnequal;
        clone.cityName = this.cityName;
        RoadCityBitemporalRecord.copy(this, clone);
        return clone;
    }
}