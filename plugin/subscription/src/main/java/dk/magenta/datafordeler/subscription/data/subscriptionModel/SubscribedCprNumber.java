package dk.magenta.datafordeler.subscription.data.subscriptionModel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(
        name = SubscribedCprNumber.TABLE_NAME,
        uniqueConstraints = @UniqueConstraint(columnNames = {"cprNumber", "cprlistId"}),
        indexes = {}
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubscribedCprNumber extends DatabaseEntry {

    public static final String TABLE_NAME = "subscription_cpr_number_subscribed";


    public static final String DB_FIELD_ENTITY = "entity";


    @Column(name = "cprNumber", nullable = false)
    private String cprNumber;

    public String getCprNumber() {
        return cprNumber;
    }

    public void setCprNumber(String cprNumber) {
        this.cprNumber = cprNumber;
    }


    public SubscribedCprNumber() {
    }


    public SubscribedCprNumber(CprList cprList, String cprNumber) {
        this.cprList = cprList;
        this.cprNumber = cprNumber;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cprlistId")
    private CprList cprList;

    public CprList getCprList() {
        return this.cprList;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SubscribedCprNumber that = (SubscribedCprNumber) o;
        return Objects.equals(cprNumber, that.cprNumber) && Objects.equals(cprList, that.cprList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cprNumber, cprList);
    }
}
