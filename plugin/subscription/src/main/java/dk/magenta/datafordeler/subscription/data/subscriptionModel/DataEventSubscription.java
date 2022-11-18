package dk.magenta.datafordeler.subscription.data.subscriptionModel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dk.magenta.datafordeler.core.database.DatabaseEntry;

import javax.persistence.*;

@Entity
@Table(
        name = DataEventSubscription.TABLE_NAME,
        indexes = {}
)
public class DataEventSubscription extends DatabaseEntry {

    public static final String TABLE_NAME = "subscription_dataevent";

    public static final String DB_FIELD_ENTITY = "entity";


    public DataEventSubscription() {
    }


    public DataEventSubscription(String dataEventId, String kodeId, Subscriber subscriber) {
        this.dataEventId = dataEventId;
        this.kodeId = kodeId;
        this.subscriber = subscriber;
    }


    public static final String DB_FIELD_DATAEVENT_ID = "dataEventId";
    @Column(name = DB_FIELD_DATAEVENT_ID, unique = true, nullable = false)
    private String dataEventId;

    public String getDataEventId() {
        return dataEventId;
    }

    public void setDataEventId(String dataEventId) {
        this.dataEventId = dataEventId;
    }

    public static final String DB_FIELD_KODE_ID = "kodeId";
    @Column(name = DB_FIELD_KODE_ID, nullable = false)
    private String kodeId;

    public String getKodeId() {
        return kodeId;
    }

    public void setKodeId(String kodeId) {
        this.kodeId = kodeId;
    }

    public static final String DB_FIELD_SUBSCRIBER_ID = "subscriber_id";
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = DB_FIELD_SUBSCRIBER_ID)
    private Subscriber subscriber;

    public Subscriber getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(Subscriber subscriber) {
        this.subscriber = subscriber;
    }

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

    public void setCvrList(CvrList cvrList) {
        this.cvrList = cvrList;
    }

}
