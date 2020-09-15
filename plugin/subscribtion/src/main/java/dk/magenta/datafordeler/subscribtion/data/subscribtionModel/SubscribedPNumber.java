package dk.magenta.datafordeler.subscribtion.data.subscribtionModel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dk.magenta.datafordeler.core.database.DatabaseEntry;

import javax.persistence.*;

@Entity
@Table(name = SubscribedPNumber.TABLE_NAME, indexes = {


})
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubscribedPNumber extends DatabaseEntry {

    public static final String TABLE_NAME = "p_number_subscribed";

    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="type")
    public static final String schema = "PList";


    public static final String DB_FIELD_ENTITY = "entity";


    @Column(name="pNumber", nullable=false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String pNumber;

    public String getPNumber() {
        return pNumber;
    }

    public void setPNumber(String cprNumber) {
        this.pNumber = pNumber;
    }


    public SubscribedPNumber() {
    }


    public SubscribedPNumber(String pNumber) {
        this.pNumber = pNumber;
    }


    @ManyToOne
    private PnumberList pnoList;

}
