package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cvr.data.company.CompanyBaseData;
import dk.magenta.datafordeler.cvr.data.companyunit.CompanyUnitBaseData;
import dk.magenta.datafordeler.cvr.data.participant.ParticipantBaseData;
import org.hibernate.Session;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Objects;

/**
 * Record for Company, CompanyUnit or Participant name.
 */
@Entity
@Table(name = SecNameRecord.TABLE_NAME, indexes = {
        @Index(name = SecNameRecord.TABLE_NAME + "__company", columnList = SecNameRecord.DB_FIELD_COMPANY + DatabaseEntry.REF),
        @Index(name = SecNameRecord.TABLE_NAME + "__companyunit", columnList = SecNameRecord.DB_FIELD_COMPANYUNIT + DatabaseEntry.REF),
        @Index(name = SecNameRecord.TABLE_NAME + "__participant", columnList = SecNameRecord.DB_FIELD_PARTICIPANT + DatabaseEntry.REF),
        @Index(name = SecNameRecord.TABLE_NAME + "__data", columnList = SecNameRecord.DB_FIELD_NAME),
})
public class SecNameRecord extends CvrBitemporalDataRecord {

    public static final String TABLE_NAME = "cvr_record_name2";

    public static final String DB_FIELD_NAME = "name";
    public static final String IO_FIELD_NAME = "navn";

    @Column(name = DB_FIELD_NAME, length = 900)
    @JsonProperty(value = IO_FIELD_NAME)
    private String name;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        if (name != null && name.length() > 900) {
            name = name.substring(0, 900);
        }
        this.name = name;
    }

    public static final String DB_FIELD_SECONDARY = "secondary";

    @Column(name = DB_FIELD_SECONDARY)
    @JsonIgnore
    private boolean secondary;

    public void setSecondary(boolean secondary) {
        this.secondary = secondary;
    }



    @Override
    public void populateBaseData(CompanyBaseData baseData, Session session) {
        baseData.setCompanyName(this.name);
    }

    @Override
    public void populateBaseData(CompanyUnitBaseData baseData, Session session) {
        baseData.setName(this.name);
    }

    @Override
    public void populateBaseData(ParticipantBaseData baseData, Session session) {
        baseData.addName(this.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SecNameRecord that = (SecNameRecord) o;
        return secondary == that.secondary &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, secondary);
    }
}
