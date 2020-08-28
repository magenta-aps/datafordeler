package dk.magenta.datafordeler.subscribtion.data.subscribtionModel;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = Subscriber.TABLE_NAME, indexes = {
        /*@Index(
                name = Subscriber.TABLE_NAME + Subscriber.DB_FIELD_USERID,
                columnList = Subscriber.DB_FIELD_USERID
        )*/

})
public class Subscriber extends DatabaseEntry {

    public static final String TABLE_NAME = "subscribtion_subscriber";

    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="type")
    public static final String schema = "Company";


    public Subscriber() {
    }

    public Subscriber(String userId) {
        this.userId = userId;
    }

    @Column(name="userId", nullable=false)
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }



    @OneToMany(cascade=CascadeType.ALL)
    @JoinColumn(name="EMPLOYEE_ID")
    Set<BusinessEventSubscribtion> businessEventSubscribtion = new HashSet<>();

    public Set<BusinessEventSubscribtion> getBusinessEventSubscribtion() {
        return this.businessEventSubscribtion;
    }

    public void addBusinessEventSubscribtion(BusinessEventSubscribtion record) {
        this.businessEventSubscribtion.add(record);
    }


    @OneToMany(cascade=CascadeType.ALL)
    @JoinColumn(name="EMPLOYEE_ID")
    Set<DataEventSubscribtion> dataEventSubscribtion = new HashSet<>();

    public Set<DataEventSubscribtion> getDataEventSubscribtion() {
        return this.dataEventSubscribtion;
    }

    public void addDataEventSubscribtion(DataEventSubscribtion record) {
        this.dataEventSubscribtion.add(record);
    }
}
