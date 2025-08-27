package dk.magenta.datafordeler.plugindemo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name="demo_data_record")
public class DemoDataRecord extends DemoBitemporalRecord {


    public static final String DB_FIELD_NAME = "bynavn";
    @Column(name = DB_FIELD_NAME)
    @JsonProperty("bynavn")
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
