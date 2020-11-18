package dk.magenta.datafordeler.subscription.data.subscriptionModel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dk.magenta.datafordeler.core.database.*;
import javax.persistence.*;
import javax.persistence.Entity;

@Entity
@Table(name = BusinessEventSubscription.TABLE_NAME, indexes = {

})
public class BusinessEventSubscription extends DatabaseEntry  {

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


    @Column(name="businessEventId", unique = true, nullable=false)
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private String businessEventId;

    public String getBusinessEventId() {
        return businessEventId;
    }

    public void setBusinessEventId(String businessEventId) {
        this.businessEventId = businessEventId;
    }

    @Column(name="kodeId", nullable=false)
    private String kodeId;

    public String getKodeId() {
        return kodeId;
    }

    public void setKodeId(String kodeId) {
        this.kodeId = kodeId;
    }


    @JsonIgnore
    @ManyToOne
    @JoinColumn(name="subscriber_id")
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
