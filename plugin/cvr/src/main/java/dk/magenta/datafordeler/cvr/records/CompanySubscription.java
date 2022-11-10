package dk.magenta.datafordeler.cvr.records;

import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cpr.CprPlugin;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + "cvr_company_subscription")
public class CompanySubscription extends DatabaseEntry {

    public static final String DB_FIELD_CVR_NUMBER = "cvrNumber";

    public CompanySubscription() {
    }

    public CompanySubscription(Integer cvrNumber) {
        this.cvrNumber = cvrNumber;
    }


    @Column(name = DB_FIELD_CVR_NUMBER, nullable = false, unique = true)
    private Integer cvrNumber;

    public Integer getCvrNumber() {
        return this.cvrNumber;
    }

    public void setCvrNumber(Integer cvrNumber) {
        this.cvrNumber = cvrNumber;
    }
}
