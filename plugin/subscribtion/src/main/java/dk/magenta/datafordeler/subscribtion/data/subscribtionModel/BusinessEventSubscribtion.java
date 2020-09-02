package dk.magenta.datafordeler.subscribtion.data.subscribtionModel;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dk.magenta.datafordeler.core.database.*;
import javax.persistence.*;
import javax.persistence.Entity;

@Entity
@Table(name = BusinessEventSubscribtion.TABLE_NAME, indexes = {


})
public class BusinessEventSubscribtion extends DatabaseEntry  {


    public static final String TABLE_NAME = "subscribtion_businessevent";

    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="type")
    public static final String schema = "Company";


    public static final String DB_FIELD_ENTITY = "entity";


    public BusinessEventSubscribtion() {
    }



    public BusinessEventSubscribtion(String businessEventId) {
        this.businessEventId = businessEventId;
    }


    @Column(name="businessEventId", nullable=false)
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private String businessEventId;

    public String getBusinessEventId() {
        return businessEventId;
    }

    public void setBusinessEventId(String businessEventId) {
        this.businessEventId = businessEventId;
    }


    @ManyToOne
    private Subscriber subscriber;



    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "cprList", referencedColumnName = "listId")
    private CprList cprList;

    public CprList getCprList() {
        return cprList;
    }

    public void setCprList(CprList cprList) {
        this.cprList = cprList;
    }

}
