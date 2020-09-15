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
@Table(name = PnumberList.TABLE_NAME, indexes = {


})
@JsonIgnoreProperties(ignoreUnknown = true)
public class PnumberList extends DatabaseEntry {


    public static final String TABLE_NAME = "pno_list";

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
    public static final String schema = "PnoList";


    public static final String DB_FIELD_ENTITY = "entity";

    public PnumberList() {
    }


    public PnumberList(String listId, String subscriberId) {
        this.listId = listId;
        this.subscriberId = subscriberId;
    }

    @Id
    @Column(name="id", nullable=false)
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private Long id;


    @Column(name = "subscriberId", nullable = false)
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private String subscriberId;

    public String getSubscriberId() {
        return subscriberId;
    }

    public void setSubscriberId(String subscriberId) {
        this.subscriberId = subscriberId;
    }


    @Column(name = "listId", nullable = false)
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private String listId;

    public String getListId() {
        return listId;
    }

    public void setListId(String listId) {
        this.listId = listId;
    }


    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "DS_ID")
    private Set<DataEventSubscribtion> dataSubscribtion;

    @JsonIgnore
    @ElementCollection
    private List<SubscribedPNumber> pNumbers = new ArrayList<SubscribedPNumber>();


    public List<SubscribedPNumber> getPNumbers() {
        return pNumbers;
    }

    public void setPNumbers(List<SubscribedPNumber> pNumbers) {
        this.pNumbers = pNumbers;
    }

    public void setPNumbersStrings(List<String> pnos) {
        for(String pno : pnos) {
            this.pNumbers.add(new SubscribedPNumber(pno));
        }
    }

    public void addPNumbersString(String pno) {
        this.pNumbers.add(new SubscribedPNumber(pno));
    }
}
