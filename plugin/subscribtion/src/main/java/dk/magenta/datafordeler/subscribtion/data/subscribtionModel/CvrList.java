package dk.magenta.datafordeler.subscribtion.data.subscribtionModel;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import javax.persistence.*;

@Entity
@Table(name = BusinessEventSubscribtion.TABLE_NAME, indexes = {


})
public class CvrList {

    public static final String TABLE_NAME = "cpr_list";

    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="type")
    public static final String schema = "Company";

    public static final String DB_FIELD_ENTITY = "businessevententity";


    public CvrList() {
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


    @OneToOne(mappedBy = "cprList")
    private BusinessEventSubscribtion businessSubscribtion;


    @OneToOne(mappedBy = "cvrList")
    private DataEventSubscribtion dataSubscribtion;

}
