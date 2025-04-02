package dk.magenta.datafordeler.subscription.data.subscriptionModel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import jakarta.persistence.*;

@Entity
@Table(
        name = SubscribedCvrNumber.TABLE_NAME,
        uniqueConstraints = @UniqueConstraint(columnNames = {"cvrNumber", "cvrlistId"}),
        indexes = {}
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubscribedCvrNumber extends DatabaseEntry {

    public static final String TABLE_NAME = "subscription_cvr_number_subscribed";

    public static final String DB_FIELD_ENTITY = "entity";


    @Column(name = "cvrNumber", nullable = false)
    private String cvrNumber;

    public String getCvrNumber() {
        return cvrNumber;
    }

    public void setCvrNumber(String cvrNumber) {
        this.cvrNumber = cvrNumber;
    }


    public SubscribedCvrNumber() {
    }


    public SubscribedCvrNumber(CvrList cvrList, String cvrNumber) {
        this.cvrList = cvrList;
        this.cvrNumber = cvrNumber;
    }


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cvrlistId")
    private CvrList cvrList;

    public CvrList getCvrList() {
        return this.cvrList;
    }
}
