package dk.magenta.datafordeler.cpr.records;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.Monotemporal;
import dk.magenta.datafordeler.cpr.data.CprEntity;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.FilterDefs;
import org.hibernate.annotations.ParamDef;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlElement;
import java.time.OffsetDateTime;
import java.util.Objects;

@MappedSuperclass
@FilterDefs({
        @FilterDef(name = Monotemporal.FILTER_REGISTRATION_AFTER, parameters = @ParamDef(name = Monotemporal.FILTERPARAM_REGISTRATION_AFTER, type = "java.time.OffsetDateTime")),
        @FilterDef(name = Monotemporal.FILTER_REGISTRATION_BEFORE, parameters = @ParamDef(name = Monotemporal.FILTERPARAM_REGISTRATION_BEFORE, type = "java.time.OffsetDateTime"))
})
public abstract class CprMonotemporalRecord<E extends CprEntity> extends CprNontemporalRecord<E> implements Monotemporal<E> {

    public static final String DB_FIELD_ENTITY = CprNontemporalRecord.DB_FIELD_ENTITY;

    // For storing the calculated endRegistration time, ie. when the next registration "overrides" us
    public static final String DB_FIELD_REGISTRATION_FROM = Monotemporal.DB_FIELD_REGISTRATION_FROM;
    public static final String IO_FIELD_REGISTRATION_FROM = Monotemporal.IO_FIELD_REGISTRATION_FROM;
    @Column(name = DB_FIELD_REGISTRATION_FROM)
    @JsonProperty(value = IO_FIELD_REGISTRATION_FROM)
    @XmlElement(name = IO_FIELD_REGISTRATION_FROM)
    private OffsetDateTime registrationFrom;

    public OffsetDateTime getRegistrationFrom() {
        return this.registrationFrom;
    }

    public CprMonotemporalRecord setRegistrationFrom(OffsetDateTime registrationFrom) {
        this.registrationFrom = registrationFrom;
        return this;
    }


    // For storing the calculated endRegistration time, ie. when the next registration "overrides" us
    public static final String DB_FIELD_REGISTRATION_TO = Monotemporal.DB_FIELD_REGISTRATION_TO;
    public static final String IO_FIELD_REGISTRATION_TO = Monotemporal.IO_FIELD_REGISTRATION_TO;
    @Column(name = DB_FIELD_REGISTRATION_TO)
    @JsonProperty(value = IO_FIELD_REGISTRATION_TO)
    @XmlElement(name = IO_FIELD_REGISTRATION_TO)
    private OffsetDateTime registrationTo;

    public OffsetDateTime getRegistrationTo() {
        return this.registrationTo;
    }

    public CprMonotemporalRecord setRegistrationTo(OffsetDateTime registrationTo) {
        this.registrationTo = registrationTo;
        return this;
    }


    public CprMonotemporalRecord setBitemporality(OffsetDateTime registrationFrom, OffsetDateTime registrationTo) {
        this.registrationFrom = registrationFrom;
        this.registrationTo = registrationTo;
        return this;
    }

    public CprMonotemporalRecord setAuthority(int authority) {
        super.setAuthority(authority);
        return this;
    }

    public CprMonotemporalRecord setDafoUpdated(OffsetDateTime updateTime) {
        super.setDafoUpdated(updateTime);
        return this;
    }


    /**
     * For sorting purposes; we implement the Comparable interface, so we should
     * provide a comparison method. Here, we sort CvrRecord objects by registrationFrom, with nulls first
     */
    public int compareTo(CprMonotemporalRecord o) {
        OffsetDateTime oUpdated = o == null ? null : o.getRegistrationFrom();
        if (this.getRegistrationFrom() == null && oUpdated == null) return 0;
        if (this.getRegistrationFrom() == null) return -1;
        return this.getRegistrationFrom().compareTo(oUpdated);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CprMonotemporalRecord that = (CprMonotemporalRecord) o;
        if (!this.equalData(that)) return false;
        return Objects.equals(registrationFrom, that.registrationFrom) &&
                Objects.equals(registrationTo, that.registrationTo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.registrationFrom, this.registrationTo);
    }

    protected static void copy(CprMonotemporalRecord from, CprMonotemporalRecord to) {
        CprNontemporalRecord.copy(from, to);
        to.registrationFrom = from.registrationFrom;
        to.registrationTo = from.registrationTo;
    }
}
