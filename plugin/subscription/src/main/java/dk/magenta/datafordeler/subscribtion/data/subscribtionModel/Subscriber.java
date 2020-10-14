package dk.magenta.datafordeler.subscribtion.data.subscribtionModel;

import dk.magenta.datafordeler.core.database.DatabaseEntry;
import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = Subscriber.TABLE_NAME, indexes = {
})
public class Subscriber extends DatabaseEntry {

    public static final String TABLE_NAME = "subscription_subscriber";

    public Subscriber() {
    }

    public Subscriber(String subscriberId) {
        this.subscriberId = subscriberId;
    }

    @Column(name="subscriberId", unique = true, nullable=false)
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private String subscriberId;

    public String getSubscriberId() {
        return subscriberId;
    }

    public void setSubscriberId(String subscriberId) {
        this.subscriberId = subscriberId;
    }



    @OneToMany(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="businesssubscribtion_id")
    Set<BusinessEventSubscription> businessEventSubscription = new HashSet<>();

    public Set<BusinessEventSubscription> getBusinessEventSubscription() {
        return this.businessEventSubscription;
    }

    public void addBusinessEventSubscribtion(BusinessEventSubscription record) {
        this.businessEventSubscription.add(record);
    }


    @OneToMany(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="dataevetsubscribtion_id")
    Set<DataEventSubscription> dataEventSubscription = new HashSet<>();

    public Set<DataEventSubscription> getDataEventSubscription() {
        return this.dataEventSubscription;
    }

    public void addDataEventSubscribtion(DataEventSubscription record) {
        this.dataEventSubscription.add(record);
    }
}
