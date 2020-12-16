package dk.magenta.datafordeler.subscription.data.subscriptionModel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import javax.persistence.*;
import java.util.*;

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


    @Column(name="listId", unique = true, nullable=false)
    private String listId;

    public String getListId() {
        return listId;
    }

    public void setListId(String listId) {
        this.listId = listId;
    }

    @OneToMany(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<BusinessEventSubscription> businessSubscription = new HashSet<>();


    @OneToMany(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<DataEventSubscription> dataSubscription;


    @ElementCollection
    @JsonIgnore
    @OneToMany(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<SubscribedCprNumber> cprs = new HashSet<SubscribedCprNumber>();

    @Column(name="cpr", nullable=false)
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
        for(String cpr : cprs) {
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
