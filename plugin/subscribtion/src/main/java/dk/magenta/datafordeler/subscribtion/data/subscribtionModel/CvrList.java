package dk.magenta.datafordeler.subscribtion.data.subscribtionModel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = CvrList.TABLE_NAME, indexes = {


})
public class CvrList {

    public static final String TABLE_NAME = "cvr_list";

    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="type")
    public static final String schema = "Company";

    public static final String DB_FIELD_ENTITY = "businessevententity";


    public CvrList() {
    }

    public CvrList(String listId, String subscriberId) {
        this.listId = listId;
        this.subscriberId = subscriberId;
    }


    @Column(name="subscriberId", nullable=false)
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private String subscriberId;

    public String getSubscriberId() {
        return subscriberId;
    }

    public void setSubscriberId(String subscriberId) {
        this.subscriberId = subscriberId;
    }




    @Id
    @Column(name="userId", nullable=false)
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private long userId;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        userId = userId;
    }


    @Column(name="listId", nullable=false)
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private String listId;

    public String getListId() {
        return listId;
    }

    public void setListId(String listId) {
        this.listId = listId;
    }


    @OneToMany(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="DS_ID")
    private Set<DataEventSubscribtion> dataSubscribtion;

    @ElementCollection
    @JsonIgnore
    private List<SubscribedCvrNumber> cvrs = new ArrayList<SubscribedCvrNumber>();

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

    public void setCvrsStrings(List<String> cprs) {
        for(String cpr : cprs) {
            this.cvrs.add(new SubscribedCvrNumber(cpr));
        }
    }

    public void addCvrsString(String cpr) {
        this.cvrs.add(new SubscribedCvrNumber(cpr));
    }
}
