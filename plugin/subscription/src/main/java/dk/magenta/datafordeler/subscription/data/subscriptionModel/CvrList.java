package dk.magenta.datafordeler.subscription.data.subscriptionModel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dk.magenta.datafordeler.core.database.DatabaseEntry;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = CvrList.TABLE_NAME, indexes = {


})
public class CvrList extends DatabaseEntry {

    public static final String TABLE_NAME = "subscription_cvr_list";

    public static final String DB_FIELD_ENTITY = "businessevententity";


    public CvrList() {
    }

    public CvrList(String listId) {
        this.listId = listId;
    }

    public CvrList(String listId, Subscriber subscriber) {
        this.listId = listId;
        this.subscriber = subscriber;
    }


    public static final String DB_FIELD_SUBSCRIBER = "subscriber_id";
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = DB_FIELD_SUBSCRIBER)
    private Subscriber subscriber;

    public Subscriber getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(Subscriber subscriber) {
        this.subscriber = subscriber;
    }

    public static final String DB_FIELD_LIST_ID = "listId";
    @Column(name = DB_FIELD_LIST_ID, unique = true, nullable = false)
    private String listId;

    public String getListId() {
        return listId;
    }

    public void setListId(String listId) {
        this.listId = listId;
    }


    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<DataEventSubscription> dataSubscription;

    @ElementCollection
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<SubscribedCvrNumber> cvrs = new HashSet<SubscribedCvrNumber>();

    public static final String DB_FIELD_CVR = "cvr";
    @Column(name = DB_FIELD_CVR, nullable = false)
    @JsonIgnore
    public Set<SubscribedCvrNumber> getCvr() {
        return cvrs;
    }

    public void setCvrs(Set<SubscribedCvrNumber> cvrs) {
        this.cvrs = cvrs;
    }

    public void addCvrs(Set<SubscribedCvrNumber> cvrs) {
        this.cvrs.addAll(cvrs);
    }

    public void addCvrStrings(List<String> cvrs) {
        for (String cvr : cvrs) {
            this.cvrs.add(new SubscribedCvrNumber(cvr));
        }
    }

    public void addCvrString(String cpr) {
        this.cvrs.add(new SubscribedCvrNumber(cpr));
    }

    public void removeCvr(String cvr) {
        this.cvrs.removeIf(f -> cvr.equals(f.getCvrNumber()));
    }

    public void removeAllCvr() {
        this.cvrs = new HashSet<SubscribedCvrNumber>();
    }
}
