package dk.magenta.datafordeler.subscribtion.data.subscribtionModel;

import dk.magenta.datafordeler.core.database.DatabaseEntry;
import javax.persistence.*;

@Entity
@Table(name = CprList.TABLE_NAME, indexes = {


})
public class CprList extends DatabaseEntry {


    public static final String TABLE_NAME = "cpr_list";

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


    @OneToOne(mappedBy = "cprList")
    private BusinessEventSubscribtion businessSubscribtion;


    @OneToOne(mappedBy = "cvrList")
    private DataEventSubscribtion dataSubscribtion;
}
