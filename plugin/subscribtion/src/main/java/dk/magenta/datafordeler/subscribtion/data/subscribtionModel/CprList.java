package dk.magenta.datafordeler.subscribtion.data.subscribtionModel;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = CprList.TABLE_NAME, indexes = {


})
public class CprList extends DatabaseEntry {


    public static final String TABLE_NAME = "cpr_list";

    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="type")
    public static final String schema = "CprList";


    public static final String DB_FIELD_ENTITY = "entity";

    public CprList() {
    }


    public CprList(String listId) {
        this.listId = listId;
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

    @OneToOne
    private BusinessEventSubscribtion businessSubscribtion;


    @OneToOne
    private DataEventSubscribtion dataSubscribtion;


    @ElementCollection
    private List<String> nrps = new ArrayList<String>();


    public List<String> getNrps() {
        return nrps;
    }

    public void setNrps(List<String> nrps) {
        this.nrps = nrps;
    }
}
