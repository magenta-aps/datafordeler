package dk.magenta.datafordeler.subscribtion.data.subscribtionModel;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import javax.persistence.*;

@Entity
@Table(name = DataEventSubscription.TABLE_NAME, indexes = {


})
public class DataEventSubscription extends DatabaseEntry {

    public static final String TABLE_NAME = "subscription_dataevent";

    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="type")
    public static final String schema = "Company";


    public static final String DB_FIELD_ENTITY = "entity";


    public DataEventSubscription() {
    }



    public DataEventSubscription(String dataEventId, String kodeId) {
        this.dataEventId = dataEventId;
        this.kodeId = kodeId;
    }


    @Column(name="dataEventId", nullable=false)
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private String dataEventId;

    public String getDataEventId() {
        return dataEventId;
    }

    public void setDataEventId(String dataEventId) {
        this.dataEventId = dataEventId;
    }

    @Column(name="kodeId", nullable=false)
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private String kodeId;

    public String getKodeId() {
        return kodeId;
    }

    public void setKodeId(String kodeId) {
        this.kodeId = kodeId;
    }

    @ManyToOne
    private Subscriber subscriber;


    @ManyToOne
    private CprList cprList;

    public CprList getCprList() {
        return cprList;
    }

    public void setCprList(CprList cprList) {
        this.cprList = cprList;
    }

    @ManyToOne
    private CvrList cvrList;
    public CvrList getCvrList() {
        return cvrList;
    }

    public void setCvrList(CvrList cprList) {
        this.cvrList = cvrList;
    }

}
