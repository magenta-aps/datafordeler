package dk.magenta.datafordeler.cpr.records.person.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.person.CprBitemporalPersonRecord;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlElement;
import java.util.Objects;

/**
 * Storage for data on a Person's church verification,
 * referenced by {@link PersonBaseData}
 */
@MappedSuperclass
public abstract class VerificationDataRecord extends CprBitemporalPersonRecord {

    public VerificationDataRecord() {
    }

    public VerificationDataRecord(boolean verified) {
        this.verified = verified;
    }

    public static final String DB_FIELD_VERIFIED = "verified";
    public static final String IO_FIELD_VERIFIED = "verificeret";
    @Column(name = DB_FIELD_VERIFIED)
    @JsonProperty(value = IO_FIELD_VERIFIED)
    @XmlElement(name = IO_FIELD_VERIFIED)
    private boolean verified;

    public boolean isVerified() {
        return this.verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    @Override
    public boolean equalData(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equalData(o)) return false;
        VerificationDataRecord that = (VerificationDataRecord) o;
        return verified == that.verified;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), verified);
    }

    protected static void copy(VerificationDataRecord from, VerificationDataRecord to) {
        CprBitemporalRecord.copy(from, to);
        to.verified = from.verified;
    }
}
