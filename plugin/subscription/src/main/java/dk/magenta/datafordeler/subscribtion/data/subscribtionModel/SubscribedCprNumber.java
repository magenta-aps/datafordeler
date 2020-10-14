package dk.magenta.datafordeler.subscribtion.data.subscribtionModel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dk.magenta.datafordeler.core.database.DatabaseEntry;

import javax.persistence.*;

@Entity
@Table(name = SubscribedCprNumber.TABLE_NAME, indexes = {


})
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubscribedCprNumber extends DatabaseEntry {

    public static final String TABLE_NAME = "subscription_cpr_number_subscribed";

    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="type")
    public static final String schema = "CprList";


    public static final String DB_FIELD_ENTITY = "entity";


    @Column(name="cprNumber", nullable=false)
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private String cprNumber;

    public String getCprNumber() {
        return cprNumber;
    }

    public void setCprNumber(String cprNumber) {
        this.cprNumber = cprNumber;
    }


    public SubscribedCprNumber() {
    }


    public SubscribedCprNumber(String cprNumber) {
        this.cprNumber = cprNumber;
    }


    @ManyToOne
    private CprList cprList;

}
