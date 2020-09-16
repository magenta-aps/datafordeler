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

    public BusinessEventSubscribtion(String businessEventId, String kodeId) {
        this.businessEventId = businessEventId;
        this.kodeId = kodeId;
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

    @Column(name="kodeId", nullable=false)
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private String kodeId;

    public String getKodeId() {
        return kodeId;
    }

    public void setKodeId(String kodeId) {
        this.kodeId = kodeId;
    }


    @ManyToOne
    private Subscriber subscriber;

    public Subscriber getSubscriber() {
        return subscriber;
    }



    @ManyToOne
    private CprList cprList;

    public CprList getCprList() {
        return cprList;
    }

    public void setCprList(CprList cprList) {
        this.cprList = cprList;
    }

}
