package dk.magenta.datafordeler.subscribtion.data.subscribtionModel;

import dk.magenta.datafordeler.core.database.DatabaseEntry;
import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = Subscriber.TABLE_NAME, indexes = {
})
public class Subscriber extends DatabaseEntry {

    public static final String TABLE_NAME = "subscription_subscriber";


    public static final String JOIN_BUSINESS_COLUMN = "businesssubscribtion_id";
    public static final String JOIN_DATA_COLUMN = "dataevetsubscribtion_id";
    public static final String SUBSCRIBER_COLUMN = "subscriberId";

    public Subscriber() {
    }

    public Subscriber(String subscriberId) {
        this.subscriberId = subscriberId;
    }

    @Column(name=SUBSCRIBER_COLUMN, unique = true, nullable=false)
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private String subscriberId;

    public String getSubscriberId() {
        return subscriberId;
    }

    public void setSubscriberId(String subscriberId) {
        this.subscriberId = subscriberId;
    }


    @OneToMany(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="cprList_id")
    Set<CprList> cprLists = new HashSet<>();

    public Set<CprList> getCprLists() {
        return this.cprLists;
    }

    public void addCvrList(CprList cprList) {
        this.cprLists.add(cprList);
    }

    public void removeCprList(CprList cprList) {
        this.cprLists.remove(cprList);
    }

    @OneToMany(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="cvrList_id")
    Set<CvrList> cvrLists = new HashSet<>();

    public Set<CvrList> getCvrLists() {
        return this.cvrLists;
    }

    public void addCvrList(CvrList cvrList) {
        this.cvrLists.add(cvrList);
    }

    public void removeCprList(CvrList cvrList) {
        this.cvrLists.remove(cvrList);
    }



    @OneToMany(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name=JOIN_BUSINESS_COLUMN)
    Set<BusinessEventSubscription> businessEventSubscription = new HashSet<>();

    public Set<BusinessEventSubscription> getBusinessEventSubscription() {
        return this.businessEventSubscription;
    }

    public void addBusinessEventSubscribtion(BusinessEventSubscription record) {
        this.businessEventSubscription.add(record);
    }

    public void removeBusinessEventSubscribtion(BusinessEventSubscription record) {
        this.businessEventSubscription.remove(record);
    }


    @OneToMany(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name=JOIN_DATA_COLUMN)
    Set<DataEventSubscription> dataEventSubscription = new HashSet<>();

    public Set<DataEventSubscription> getDataEventSubscription() {
        return this.dataEventSubscription;
    }

    public void addDataEventSubscribtion(DataEventSubscription record) {
        this.dataEventSubscription.add(record);
    }

    public void removeDataEventSubscribtion(DataEventSubscription record) {
        this.dataEventSubscription.remove(record);
    }
}
