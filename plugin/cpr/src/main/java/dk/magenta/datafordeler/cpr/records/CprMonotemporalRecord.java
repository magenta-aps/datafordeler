package dk.magenta.datafordeler.cpr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.Monotemporal;
import dk.magenta.datafordeler.cpr.data.CprRecordEntity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static dk.magenta.datafordeler.core.database.Bitemporal.fixOffsetIn;

@MappedSuperclass
public abstract class CprMonotemporalRecord<E extends CprRecordEntity, S extends CprMonotemporalRecord<E, S>> extends CprNontemporalRecord<E, S> implements Monotemporal {

    public static final Class<?> FILTERPARAMTYPE_REGISTRATIONFROM = OffsetDateTime.class;
    public static final Class<?> FILTERPARAMTYPE_REGISTRATIONTO = OffsetDateTime.class;

    public static final String DB_FIELD_ENTITY = CprNontemporalRecord.DB_FIELD_ENTITY;

    // For storing the calculated endRegistration time, ie. when the next registration "overrides" us
    public static final String DB_FIELD_REGISTRATION_FROM = Monotemporal.DB_FIELD_REGISTRATION_FROM;
    public static final String IO_FIELD_REGISTRATION_FROM = Monotemporal.IO_FIELD_REGISTRATION_FROM;


    @Column(name = DB_FIELD_REGISTRATION_FROM, columnDefinition = "datetime2")
    protected OffsetDateTime registrationFrom;

    @JsonIgnore
    @Column(name = DB_FIELD_REGISTRATION_FROM+"_new")
    protected OffsetDateTime registrationFromNew;

    @JsonProperty(value = IO_FIELD_REGISTRATION_FROM)
    public OffsetDateTime getRegistrationFrom() {
        return this.registrationFromNew;
    }

    @JsonProperty(value = IO_FIELD_REGISTRATION_FROM)
    public void setRegistrationFrom(OffsetDateTime registrationFrom) {
        this.registrationFrom = fixOffsetIn(registrationFrom);
        this.registrationFromNew = registrationFrom;
    }


    // For storing the calculated endRegistration time, ie. when the next registration "overrides" us
    public static final String DB_FIELD_REGISTRATION_TO = Monotemporal.DB_FIELD_REGISTRATION_TO;
    public static final String IO_FIELD_REGISTRATION_TO = Monotemporal.IO_FIELD_REGISTRATION_TO;
    @Column(name = DB_FIELD_REGISTRATION_TO, columnDefinition = "datetime2")
    protected OffsetDateTime registrationTo;

    @JsonIgnore
    @Column(name = DB_FIELD_REGISTRATION_TO+"_new")
    protected OffsetDateTime registrationToNew;

    @JsonProperty(value = IO_FIELD_REGISTRATION_TO)
    public OffsetDateTime getRegistrationTo() {
        return this.registrationToNew;
    }

    @JsonProperty(value = IO_FIELD_REGISTRATION_TO)
    public void setRegistrationTo(OffsetDateTime registrationTo) {
        this.registrationTo = fixOffsetIn(registrationTo);
        this.registrationToNew = registrationTo;
    }


    public CprMonotemporalRecord setBitemporality(OffsetDateTime registrationFrom, OffsetDateTime registrationTo) {
        this.setRegistrationFrom(registrationFrom);
        this.setRegistrationTo(registrationTo);
        return this;
    }

    public CprMonotemporalRecord setAuthority(int authority) {
        super.setAuthority(authority);
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
        to.registrationFromNew = from.registrationFromNew;
        to.registrationToNew = from.registrationToNew;
    }

    public void updateTimestamp() {
        super.updateTimestamp();
        this.registrationFromNew = this.getRegistrationFrom();
        this.registrationToNew = this.getRegistrationTo();
        System.out.println("Updated registrationFromNew for "+this.getId()+" to "+this.registrationFromNew);
        System.out.println("Updated registrationToNew for "+this.getId()+" to "+this.registrationToNew);
    }

    public static List<String> updateFields() {
        ArrayList<String> list = new ArrayList<>();
        list.add(DB_FIELD_REGISTRATION_FROM);
        list.add(DB_FIELD_REGISTRATION_TO);
        list.addAll(CprNontemporalRecord.updateFields());
        return list;
    }
}
