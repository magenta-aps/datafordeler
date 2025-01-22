package dk.magenta.datafordeler.subscription.data.subscriptionModel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import org.hibernate.Session;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(
        name = CvrList.TABLE_NAME,
        indexes = {}
)
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


    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "cvrList")
    private Set<DataEventSubscription> dataSubscription;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "cvrList")
    private final Set<SubscribedCvrNumber> cvrs = new HashSet<SubscribedCvrNumber>();

    public static final String DB_FIELD_CVR = "cvr";

    @Column(name = DB_FIELD_CVR, nullable = false)
    @JsonIgnore
    public Set<SubscribedCvrNumber> getCvr() {
        return cvrs;
    }

    public void addCvrStrings(List<String> cvrs) {
        for (String cvr : cvrs) {
            this.cvrs.add(new SubscribedCvrNumber(this, cvr));
        }
    }

    public void addCvrString(String cvr) {
        this.cvrs.add(new SubscribedCvrNumber(this, cvr));
    }

    public void removeCvrString(String cvr, Session session) {
        SubscribedCvrNumber number = this.cvrs.stream().filter(f -> cvr.equals(f.getCvrNumber())).findFirst().orElse(null);
        if (number != null) {
            this.cvrs.remove(number);
            session.delete(number);
        }
    }
}
