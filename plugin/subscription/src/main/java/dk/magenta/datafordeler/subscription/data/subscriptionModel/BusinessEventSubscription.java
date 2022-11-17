package dk.magenta.datafordeler.subscription.data.subscriptionModel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dk.magenta.datafordeler.core.database.DatabaseEntry;

import javax.persistence.*;

@Entity
@Table(
        name = BusinessEventSubscription.TABLE_NAME,
        indexes = {}
)
public class BusinessEventSubscription extends DatabaseEntry {

    public static final String TABLE_NAME = "subscription_businessevent";

    public BusinessEventSubscription() {
    }


    public BusinessEventSubscription(String businessEventId) {
        this.businessEventId = businessEventId;
    }

    public BusinessEventSubscription(String businessEventId, String kodeId) {
        this.businessEventId = businessEventId;
        this.kodeId = kodeId;
    }


    public static final String DB_FIELD_BUSINESS_EVENT_ID = "businessEventId";

    @Column(name = DB_FIELD_BUSINESS_EVENT_ID, unique = true, nullable = false)
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private String businessEventId;

    public String getBusinessEventId() {
        return businessEventId;
    }

    public void setBusinessEventId(String businessEventId) {
        this.businessEventId = businessEventId;
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

}
