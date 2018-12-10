package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Objects;

/**
 * Record for Company, CompanyUnit and Participant contact information.
 */
@Entity
@Table(name = ContactRecord.TABLE_NAME, indexes = {
        @Index(name = ContactRecord.TABLE_NAME + "__company", columnList = ContactRecord.DB_FIELD_COMPANY + DatabaseEntry.REF + "," + ContactRecord.DB_FIELD_TYPE),
        @Index(name = ContactRecord.TABLE_NAME + "__companyunit", columnList = ContactRecord.DB_FIELD_COMPANYUNIT + DatabaseEntry.REF + "," + ContactRecord.DB_FIELD_TYPE),
        @Index(name = ContactRecord.TABLE_NAME + "__participant", columnList = ContactRecord.DB_FIELD_PARTICIPANT + DatabaseEntry.REF + "," + ContactRecord.DB_FIELD_TYPE),
        @Index(name = ContactRecord.TABLE_NAME + "__data", columnList = ContactRecord.DB_FIELD_DATA),
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContactRecord extends CvrBitemporalDataRecord {

    public static final String TABLE_NAME = "cvr_record_contact";

    public static final int TYPE_TELEFONNUMMER = 0;
    public static final int TYPE_TELEFAXNUMMER = 1;
    public static final int TYPE_EMAILADRESSE = 2;
    public static final int TYPE_HJEMMESIDE = 3;
    public static final int TYPE_OBLIGATORISK_EMAILADRESSE = 4;


    public static final String DB_FIELD_DATA = "contactInformation";
    public static final String IO_FIELD_DATA = "kontaktoplysning";

    @Column(name = DB_FIELD_DATA)
    @JsonProperty(value = IO_FIELD_DATA)
    protected String contactInformation;

    public String getContactInformation() {
        return this.contactInformation;
    }



    public static final String DB_FIELD_SECRET = "secret";
    public static final String IO_FIELD_SECRET = "hemmelig";

    @Column(name = DB_FIELD_SECRET)
    @JsonProperty(value = IO_FIELD_SECRET)
    protected boolean secret;

    public boolean isSecret() {
        return this.secret;
    }



    public static final String DB_FIELD_SECONDARY = "secondary";

    @Column(name = DB_FIELD_SECONDARY)
    @JsonIgnore
    protected boolean secondary;

    public void setSecondary(boolean secondary) {
        this.secondary = secondary;
    }

    public boolean isSecondary() {
        return this.secondary;
    }



    public static final String DB_FIELD_TYPE = "type";

    @Column(name = DB_FIELD_TYPE)
    @JsonIgnore
    private int type;

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return this.type;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ContactRecord that = (ContactRecord) o;
        return secret == that.secret &&
                secondary == that.secondary &&
                type == that.type &&
                Objects.equals(contactInformation, that.contactInformation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), contactInformation, secret, secondary, type);
    }

    /*@Override
    public boolean equalData(Object o) {
        if (!super.equalData(o)) return false;
        ContactRecord that = (ContactRecord) o;
        return secret == that.secret &&
                secondary == that.secondary &&
                type == that.type &&
                Objects.equals(contactInformation, that.contactInformation);
    }*/
}
