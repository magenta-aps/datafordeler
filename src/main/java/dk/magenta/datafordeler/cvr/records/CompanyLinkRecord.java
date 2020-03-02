package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cvr.data.companyunit.CompanyUnitBaseData;
import org.hibernate.Session;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * Record for CompanyUnit associated CVR number.
 */
@Entity
@Table(name = CompanyLinkRecord.TABLE_NAME, indexes = {
        @Index(name = CompanyLinkRecord.TABLE_NAME + "__companyunit", columnList = CompanyLinkRecord.DB_FIELD_COMPANYUNIT + DatabaseEntry.REF),
        @Index(name = CompanyLinkRecord.TABLE_NAME + "__cvrnumber", columnList = CompanyLinkRecord.DB_FIELD_CVRNUMBER),
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompanyLinkRecord extends CvrBitemporalDataRecord {

    public static final String TABLE_NAME = "cvr_record_unit_link_company";

    public static final String DB_FIELD_CVRNUMBER = "cvrNumber";
    public static final String IO_FIELD_CVRNUMBER = "cvrNummer";

    @Column(name = DB_FIELD_CVRNUMBER)
    @JsonProperty(value = IO_FIELD_CVRNUMBER)
    private int cvrNumber;

    @Override
    public void populateBaseData(CompanyUnitBaseData baseData, Session session) {
        baseData.addAssociatedCvrNumber(this.cvrNumber);
    }

}
