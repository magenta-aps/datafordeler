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
 * Record for Company, CompanyUnit and Participant contact information.
 */
@Entity
@Table(name = "cvr_record_contact", indexes = {
        @Index(name = "cvr_record_contact_company", columnList = ContactRecord.DB_FIELD_COMPANY + DatabaseEntry.REF + "," + ContactRecord.DB_FIELD_TYPE),
        @Index(name = "cvr_record_contact_companyunit", columnList = ContactRecord.DB_FIELD_COMPANYUNIT + DatabaseEntry.REF + "," + ContactRecord.DB_FIELD_TYPE),
        @Index(name = "cvr_record_contact_participant", columnList = ContactRecord.DB_FIELD_PARTICIPANT + DatabaseEntry.REF + "," + ContactRecord.DB_FIELD_TYPE),
        @Index(name = "cvr_record_contact_data", columnList = ContactRecord.DB_FIELD_DATA),
})
public class ContactRecord extends CvrBitemporalDataRecord {

    public static final int TYPE_TELEFONNUMMER = 0;
    public static final int TYPE_TELEFAXNUMMER = 1;
    public static final int TYPE_EMAILADRESSE = 2;
    public static final int TYPE_HJEMMESIDE = 3;
    public static final int TYPE_OBLIGATORISK_EMAILADRESSE = 4;


    public static final String DB_FIELD_DATA = "contactInformation";
    public static final String IO_FIELD_DATA = "kontaktoplysning";

    @Column(name = DB_FIELD_DATA)
    @JsonProperty(value = "kontaktoplysning")
    protected String contactInformation;



    public static final String DB_FIELD_SECRET = "secret";
    public static final String IO_FIELD_SECRET = "hemmelig";

    @Column(name = DB_FIELD_SECRET)
    @JsonProperty(value = "hemmelig")
    protected boolean secret;



    public static final String DB_FIELD_SECONDARY = "secondary";

    @Column(name = DB_FIELD_SECONDARY)
    @JsonIgnore
    protected boolean secondary;

    public void setSecondary(boolean secondary) {
        this.secondary = secondary;
    }



    public static final String DB_FIELD_TYPE = "type";

    @Column(name = DB_FIELD_TYPE)
    @JsonIgnore
    private int type;

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public void populateBaseData(CompanyBaseData baseData, Session session) {
        switch (this.type) {
            case TYPE_TELEFONNUMMER:
                baseData.setPhoneNumber(this.contactInformation, this.secret);
                break;
            case TYPE_TELEFAXNUMMER:
                baseData.setFaxNumber(this.contactInformation, this.secret);
                break;
            case TYPE_EMAILADRESSE:
                baseData.setEmailAddress(this.contactInformation, this.secret);
                break;
            case TYPE_HJEMMESIDE:
                baseData.setHomepage(this.contactInformation, this.secret);
                break;
            case TYPE_OBLIGATORISK_EMAILADRESSE:
                baseData.setMandatoryEmail(this.contactInformation, this.secret);
                break;
        }
    }

    @Override
    public void populateBaseData(CompanyUnitBaseData baseData, Session session) {
        switch (this.type) {
            case TYPE_TELEFONNUMMER:
                baseData.setPhoneNumber(this.contactInformation, this.secret);
                break;
            case TYPE_TELEFAXNUMMER:
                baseData.setFaxNumber(this.contactInformation, this.secret);
                break;
            case TYPE_EMAILADRESSE:
                baseData.setEmailAddress(this.contactInformation, this.secret);
                break;
        }
    }

    @Override
    public void populateBaseData(ParticipantBaseData baseData, Session session) {
        switch (this.type) {
            case TYPE_TELEFONNUMMER:
                baseData.setPhoneNumber(this.contactInformation, this.secret);
                break;
            case TYPE_TELEFAXNUMMER:
                baseData.setFaxNumber(this.contactInformation, this.secret);
                break;
            case TYPE_EMAILADRESSE:
                baseData.setEmailAddress(this.contactInformation, this.secret);
                break;
        }
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
}
