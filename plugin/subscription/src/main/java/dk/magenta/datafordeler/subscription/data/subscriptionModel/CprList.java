package dk.magenta.datafordeler.subscription.data.subscriptionModel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dk.magenta.datafordeler.core.database.DatabaseEntry;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = CprList.TABLE_NAME, indexes = {


})
@JsonIgnoreProperties(ignoreUnknown = true)
public class CprList extends DatabaseEntry {


    public static final String TABLE_NAME = "subscription_cpr_list";

    public static final String DB_FIELD_ENTITY = "entity";

    public CprList() {
    }

    public CprList(String listId) {
        this.listId = listId;
    }

    public CprList(String listId, Subscriber subscriber) {
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
    private final Set<BusinessEventSubscription> businessSubscription = new HashSet<>();


    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<DataEventSubscription> dataSubscription;


    @ElementCollection
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<SubscribedCprNumber> cprs = new HashSet<SubscribedCprNumber>();

    public static final String DB_FIELD_CPR = "cpr";
    @Column(name = DB_FIELD_CPR, nullable = false)
    @JsonIgnore
    public Set<SubscribedCprNumber> getCpr() {
        return cprs;
    }

    public void setCprs(Set<SubscribedCprNumber> cprs) {
        this.cprs = cprs;
    }

    public void addCprs(Set<SubscribedCprNumber> cprs) {
        this.cprs.addAll(cprs);
    }

    public void addCprStrings(List<String> cprs) {
        for (String cpr : cprs) {
            this.cprs.add(new SubscribedCprNumber(this, cpr));
        }
    }

    public void addCprString(String cpr) {
        this.cprs.add(new SubscribedCprNumber(this, cpr));
    }

    public void removeCpr(String cpr) {
        this.cprs.removeIf(f -> cpr.equals(f.getCprNumber()));
    }

    public void removeAllCpr() {
        this.cprs = new HashSet<SubscribedCprNumber>();
    }
}
