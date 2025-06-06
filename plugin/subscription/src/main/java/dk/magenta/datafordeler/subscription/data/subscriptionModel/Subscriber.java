package dk.magenta.datafordeler.subscription.data.subscriptionModel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = Subscriber.TABLE_NAME,
        indexes = {}
)
public class Subscriber extends DatabaseEntry {

    public static final String TABLE_NAME = "subscription_subscriber";


    public static final String JOIN_BUSINESS_COLUMN = "businesssubscription_id";
    public static final String JOIN_DATA_COLUMN = "dataevetsubscription_id";
    public static final String DB_FIELD_SUBSCRIBER_ID = "subscriberId";

    public Subscriber() {
    }

    public Subscriber(String subscriberId) {
        this.subscriberId = subscriberId;
    }

    @Column(name = DB_FIELD_SUBSCRIBER_ID, unique = true, nullable = false)
    private String subscriberId;

    public String getSubscriberId() {
        return subscriberId;
    }

    public void setSubscriberId(String subscriberId) {
        this.subscriberId = subscriberId;
    }


    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "cprList_id")
    @JsonProperty
    @JsonInclude(JsonInclude.Include.ALWAYS)
    Set<CprList> cprLists = new HashSet<>();

    public Set<CprList> getCprLists() {
        return this.cprLists;
    }

    public void addCprList(CprList cprList) {
        this.cprLists.add(cprList);
    }

    public void removeCprList(CprList cprList) {
        this.cprLists.remove(cprList);
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "cvrList_id")
    @JsonProperty
    @JsonInclude(JsonInclude.Include.ALWAYS)
    Set<CvrList> cvrLists = new HashSet<>();

    public Set<CvrList> getCvrLists() {
        return this.cvrLists;
    }

    public void addCvrList(CvrList cvrList) {
        this.cvrLists.add(cvrList);
    }

    public void removeCvrList(CvrList cvrList) {
        this.cvrLists.remove(cvrList);
    }


    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "subscriber")
    Set<BusinessEventSubscription> businessEventSubscription = new HashSet<>();

    public Set<BusinessEventSubscription> getBusinessEventSubscription() {
        return this.businessEventSubscription;
    }

    public void addBusinessEventSubscription(BusinessEventSubscription record) {
        this.businessEventSubscription.add(record);
        record.setSubscriber(this);
    }

    public void removeBusinessEventSubscription(BusinessEventSubscription record) {
        this.businessEventSubscription.remove(record);
    }


    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "subscriber")
    Set<DataEventSubscription> dataEventSubscription = new HashSet<>();

    public Set<DataEventSubscription> getDataEventSubscription() {
        return this.dataEventSubscription;
    }

    public void addDataEventSubscription(DataEventSubscription record) {
        this.dataEventSubscription.add(record);
        record.setSubscriber(this);
    }

    public void removeDataEventSubscription(DataEventSubscription record) {
        this.dataEventSubscription.remove(record);
    }
}
