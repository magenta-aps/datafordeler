package dk.magenta.datafordeler.subscribtion.data.subscribtionModel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
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


    public static final String TABLE_NAME = "cpr_list";

    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="type")
    public static final String schema = "CprList";


    public static final String DB_FIELD_ENTITY = "entity";

    public CprList() {
    }


    public CprList(String listId, String subscriberId) {
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
    @JoinColumn(name="BS_ID")
    private Set<BusinessEventSubscribtion> businessSubscribtion = new HashSet<>();


    @OneToMany(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="DS_ID")
    private Set<DataEventSubscribtion> dataSubscribtion;


    @ElementCollection
    @JsonIgnore
    @OneToMany(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="CPR_ID")
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
