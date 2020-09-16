package dk.magenta.datafordeler.subscribtion.data.subscribtionModel;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = Subscriber.TABLE_NAME, indexes = {
})
public class Subscriber extends DatabaseEntry {

    public static final String TABLE_NAME = "subscribtion_subscriber";

    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="type")
    public static final String schema = "Company";


    public Subscriber() {
    }

    public Subscriber(String subscriberId) {
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



    @OneToMany(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="BE_SUBSCRIBTION_ID")
    Set<BusinessEventSubscribtion> businessEventSubscribtion = new HashSet<>();

    public Set<BusinessEventSubscribtion> getBusinessEventSubscribtion() {
        return this.businessEventSubscribtion;
    }

    public void addBusinessEventSubscribtion(BusinessEventSubscribtion record) {
        this.businessEventSubscribtion.add(record);
    }


    @OneToMany(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="DE_SUBSCRIBTION_ID")
    Set<DataEventSubscribtion> dataEventSubscribtion = new HashSet<>();

    public Set<DataEventSubscribtion> getDataEventSubscribtion() {
        return this.dataEventSubscribtion;
    }

    public void addDataEventSubscribtion(DataEventSubscribtion record) {
        this.dataEventSubscribtion.add(record);
    }
}
