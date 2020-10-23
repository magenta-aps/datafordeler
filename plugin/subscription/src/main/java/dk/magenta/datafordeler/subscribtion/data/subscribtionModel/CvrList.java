package dk.magenta.datafordeler.subscribtion.data.subscribtionModel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = CvrList.TABLE_NAME, indexes = {


})
public class CvrList {

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

    @Id
    @Column(name="id", unique = true, nullable=false)
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
    @JoinColumn(name="DS_ID")
    private Set<DataEventSubscription> dataSubscribtion;

    @ElementCollection
    @JsonIgnore
    @OneToMany(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="CPR_ID")
    private List<SubscribedCvrNumber> cvrs = new ArrayList<SubscribedCvrNumber>();

    @Column(name="cpr", nullable=false)
    @JsonIgnore
    public List<SubscribedCvrNumber> getCvr() {
        return cvrs;
    }

    public void setCvrs(List<SubscribedCvrNumber> cvrs) {
        this.cvrs = cvrs;
    }

    public void addCvrs(List<SubscribedCvrNumber> cvrs) {
        this.cvrs.addAll(cvrs);
    }

    public void addCvrsStrings(List<String> cvrs) {
        for(String cvr : cvrs) {
            this.cvrs.add(new SubscribedCvrNumber(cvr));
        }
    }

    public void addCvrsString(String cpr) {
        this.cvrs.add(new SubscribedCvrNumber(cpr));
    }
}
