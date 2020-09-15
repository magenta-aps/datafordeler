package dk.magenta.datafordeler.subscribtion.data.subscribtionModel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dk.magenta.datafordeler.core.database.DatabaseEntry;

import javax.persistence.*;

@Entity
@Table(name = SubscribedCvrNumber.TABLE_NAME, indexes = {


})
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubscribedCvrNumber extends DatabaseEntry {

    public static final String TABLE_NAME = "cvr_number_subscribed";

    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="type")
    public static final String schema = "CvrList";


    public static final String DB_FIELD_ENTITY = "entity";


    @Column(name="cvrNumber", nullable=false)
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private String cvrNumber;

    public String getCvrNumber() {
        return cvrNumber;
    }

    public void setCprNumber(String cvrNumber) {
        this.cvrNumber = cvrNumber;
    }


    public SubscribedCvrNumber() {
    }


    public SubscribedCvrNumber(String cvrNumber) {
        this.cvrNumber = cvrNumber;
    }


    @ManyToOne
    private CvrList cvrList;

}
