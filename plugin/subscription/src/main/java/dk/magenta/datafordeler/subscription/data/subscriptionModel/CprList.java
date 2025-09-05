package dk.magenta.datafordeler.subscription.data.subscriptionModel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.exception.ConflictException;
import jakarta.persistence.*;
import org.hibernate.Session;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(
        name = CprList.TABLE_NAME,
        indexes = {}
)
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
    @ManyToOne(fetch = FetchType.LAZY)
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

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "cprList")
    private final Set<BusinessEventSubscription> businessSubscription = new HashSet<>();


    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "cprList")
    private Set<DataEventSubscription> dataSubscription;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "cprList")
    private final Set<SubscribedCprNumber> cprs = new HashSet<SubscribedCprNumber>();

    public static final String DB_FIELD_CPR = "cpr";

    @Column(name = DB_FIELD_CPR, nullable = false)
    @JsonIgnore
    public Set<SubscribedCprNumber> getCpr() {
        return cprs;
    }

    public void addCprStrings(List<String> cprs) throws ConflictException {
        for (String cpr : cprs) {
            this.addCprString(cpr);
        }
    }

    public SubscribedCprNumber addCprString(String cpr) throws ConflictException {
        SubscribedCprNumber n = new SubscribedCprNumber(this, cpr);
        if (!cprs.contains(n)) {
            this.cprs.add(n);
            return n;
        } else {
            throw new ConflictException(cpr+" already exists in list");
        }
    }

    public void removeCprString(String cpr, Session session) {
        SubscribedCprNumber number = this.cprs.stream().filter(f -> cpr.equals(f.getCprNumber())).findFirst().orElse(null);
        if (number != null) {
            this.cprs.remove(number);
            session.remove(number);
        }
    }

    public static final String DB_FIELD_ANY_CPR = "anyCpr";
    @JsonIgnore
    @Column(name = DB_FIELD_ANY_CPR, nullable = false)
    private boolean anyCpr = false;

    @JsonIgnore
    public boolean getAnyCpr() {
        return this.anyCpr;
    }

    public void setAnyCpr(boolean anyCpr) {
        this.anyCpr = anyCpr;
    }
}
