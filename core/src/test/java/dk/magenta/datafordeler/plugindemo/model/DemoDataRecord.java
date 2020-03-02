package dk.magenta.datafordeler.plugindemo.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@javax.persistence.Entity
@Table(name="demo_data_record")
@XmlRootElement
public class DemoDataRecord extends DemoBitemporalRecord {


    public static final String DB_FIELD_NAME = "bynavn";
    @Column(name = DB_FIELD_NAME)
    @JsonProperty("bynavn")
    @XmlElement(name="bynavn")
    private String bynavn;

    public DemoDataRecord() {}

    public DemoDataRecord(String bynavn) {
        this.bynavn = bynavn;
    }

    public String getBynavn() {
        return bynavn;
    }

    public boolean equalData(Object o) {
        return false;
    }
}
