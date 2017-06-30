package dk.magenta.datafordeler.cpr.data.road.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.cpr.data.DetailData;
import dk.magenta.datafordeler.cpr.data.unversioned.PostCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lars on 29-06-17.
 */
@Entity
@Table(name="cpr_road_city")
public class RoadCityData extends DetailData {

    @Column
    @JsonProperty(value = "husnummerFra")
    @XmlElement(name = "husnummerFra")
    private String houseNumberFrom;

    public String getHouseNumberFrom() {
        return this.houseNumberFrom;
    }

    public void setHouseNumberFrom(String houseNumberFrom) {
        this.houseNumberFrom = houseNumberFrom;
    }

    @Column
    @JsonProperty(value = "husnummerTil")
    @XmlElement(name = "husnummerTil")
    private String houseNumberTo;

    public String getHouseNumberTo() {
        return this.houseNumberTo;
    }

    public void setHouseNumberTo(String houseNumberTo) {
        this.houseNumberTo = houseNumberTo;
    }

    @Column
    @JsonProperty(value = "lige")
    @XmlElement(name = "lige")
    private boolean even;

    public boolean isEven() {
        return this.even;
    }

    public void setEven(boolean even) {
        this.even = even;
    }

    @Column
    @JsonProperty(value = "bynavn")
    @XmlElement(name = "bynavn")
    private String cityName;

    public String getCityName() {
        return this.cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>();

        if (this.houseNumberFrom != null) {
            map.put("houseNumberFrom", this.houseNumberFrom);
        }
        if (this.houseNumberTo != null) {
            map.put("houseNumberTo", this.houseNumberTo);
        }
        map.put("even", this.even);
        if (this.cityName != null) {
            map.put("cityName", this.cityName);
        }
        return map;
    }
}