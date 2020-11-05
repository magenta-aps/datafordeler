package dk.magenta.datafordeler.subscribtion.data.subscribtionModel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import javax.persistence.*;
import java.util.ArrayList;
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

    @Id
    @Column(name="id", nullable=false)
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private Long id;



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
    @JoinColumn(name="businessevent_id")
    private Set<BusinessEventSubscription> businessSubscribtion = new HashSet<>();


    @OneToMany(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="dataevent_id")
    private Set<DataEventSubscription> dataSubscribtion;


    @ElementCollection
    @JsonIgnore
    @OneToMany(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="cprlist_id")
    private List<SubscribedCprNumber> cprs = new ArrayList<SubscribedCprNumber>();

    @Column(name="cpr", nullable=false)
    @JsonIgnore
    public List<SubscribedCprNumber> getCpr() {
        return cprs;
    }

    public void setCprs(List<SubscribedCprNumber> cprs) {
        this.cprs = cprs;
    }

    public void addCprs(List<SubscribedCprNumber> cprs) {
        this.cprs.addAll(cprs);
    }

    public void addCprStrings(List<String> cprs) {
        for(String cpr : cprs) {
            this.cprs.add(new SubscribedCprNumber(cpr));
        }
    }

    public void addCprString(String cpr) {
        this.cprs.add(new SubscribedCprNumber(cpr));
    }

    public void removeCpr(String cpr) {
        this.cprs.removeIf(f -> cpr.equals(f.getCprNumber()));
    }
}
