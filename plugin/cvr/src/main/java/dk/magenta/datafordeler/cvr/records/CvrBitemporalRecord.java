package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.Effect;
import dk.magenta.datafordeler.core.database.Registration;
import dk.magenta.datafordeler.core.util.Bitemporality;
import dk.magenta.datafordeler.core.util.Equality;
import dk.magenta.datafordeler.core.util.ListHashMap;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.MappedSuperclass;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Objects;

@MappedSuperclass
public class CvrBitemporalRecord extends CvrRecord implements Comparable<CvrBitemporalRecord> {

    public static final String FILTER_LAST_UPDATED = "(" + CvrBitemporalRecord.DB_FIELD_LAST_UPDATED + " < :" + Registration.FILTERPARAM_REGISTRATION_TO + ")";
    public static final String FILTER_EFFECT_FROM = "(" + CvrRecordPeriod.DB_FIELD_VALID_TO + " >= :" + Effect.FILTERPARAM_EFFECT_FROM + " OR " + CvrRecordPeriod.DB_FIELD_VALID_TO + " is null)";
    public static final String FILTER_EFFECT_TO = "(" + CvrRecordPeriod.DB_FIELD_VALID_FROM + " < :" + Effect.FILTERPARAM_EFFECT_TO + " OR " + CvrRecordPeriod.DB_FIELD_VALID_FROM + " is null)";



    public static final String DB_FIELD_LAST_UPDATED = "lastUpdated";
    public static final String IO_FIELD_LAST_UPDATED = "sidstOpdateret";

    @Column(name = DB_FIELD_LAST_UPDATED)
    @JsonProperty(value = IO_FIELD_LAST_UPDATED)
    private OffsetDateTime lastUpdated;

    @JsonIgnore
    public OffsetDateTime getLastUpdated() {
        return this.lastUpdated;
    }

    public void setLastUpdated(OffsetDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }





    public static final String DB_FIELD_LAST_LOADED = "lastLoaded";
    public static final String IO_FIELD_LAST_LOADED = "sidstIndlaest";

    @Column(name = DB_FIELD_LAST_LOADED)
    @JsonProperty(value = IO_FIELD_LAST_LOADED)
    private OffsetDateTime lastLoaded;

    @JsonIgnore
    public OffsetDateTime getLastLoaded() {
        return this.lastLoaded;
    }

    @JsonIgnore
    public OffsetDateTime getRegistrationFrom() {
        return (this.lastUpdated != null) ? this.lastUpdated : this.lastLoaded;
    }



    public static final String IO_FIELD_PERIOD = "periode";

    @Embedded
    @JsonProperty(value = IO_FIELD_PERIOD)
    private CvrRecordPeriod validity;

    public CvrRecordPeriod getValidity() {
        return this.validity;
    }

    public void setValidity(CvrRecordPeriod validity) {
        this.validity = validity;
    }



    @JsonIgnore
    public LocalDate getValidFrom() {
        if (this.validity != null) {
            return this.validity.getValidFrom();
        } else {
            return null;
        }
    }

    @JsonIgnore
    public LocalDate getValidTo() {
        if (this.validity != null){
            return this.validity.getValidTo();
        } else {
            return null;
        }
    }


    /*public void merge(CvrBitemporalRecord other) {
        if (other != null && other.getClass() == this.getClass()) {

        }
    }*/

    /**
     * For sorting purposes; we implement the Comparable interface, so we should
     * provide a comparison method. Here, we sort CvrRecord objects by registrationFrom, with nulls first
     */
    @Override
    public int compareTo(CvrBitemporalRecord o) {
        OffsetDateTime oUpdated = o == null ? null : o.getRegistrationFrom();
        if (this.getRegistrationFrom() == null && oUpdated == null) return 0;
        if (this.getRegistrationFrom() == null) return -1;
        return this.getRegistrationFrom().compareTo(oUpdated);
    }

    // For storing the calculated endRegistration time, ie. when the next registration "overrides" us
    @JsonIgnore
    private OffsetDateTime registrationTo;

    public OffsetDateTime getRegistrationTo() {
        return this.registrationTo;
    }

    public void setRegistrationTo(OffsetDateTime registrationTo) {
        this.registrationTo = registrationTo;
    }

    /**
     * Given a Collection of CvrRecord objects, group them into buckets that share
     * bitemporality. That way, we can treat all records in a bucket the same way,
     * thus we won’t have to look up the appropriate Registration/Effect more than once
     */
    public static <T extends CvrBitemporalRecord> ListHashMap<String, T> sortIntoGroups(Collection<T> records) {
        // Sort the records into groups that share bitemporality
        ListHashMap<String, T> recordGroups = new ListHashMap<>();
        for (T record : records) {
            // Find the appropriate registration object
            OffsetDateTime registrationFrom = record.getRegistrationFrom();
            OffsetDateTime registrationTo = record.getRegistrationTo();
            LocalDate effectFrom = record.getValidFrom();
            LocalDate effectTo = record.getValidTo();
            String groupKey = registrationFrom + "|" + registrationTo + "|" + effectFrom + "|" + effectTo;
            recordGroups.add(groupKey, record);
        }
        return recordGroups;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CvrBitemporalRecord that = (CvrBitemporalRecord) o;
        return Equality.equal(lastUpdated, that.lastUpdated) &&
                Equality.equal(lastLoaded, that.lastLoaded) &&
                Objects.equals(validity, that.validity) &&
                Equality.equal(registrationTo, that.registrationTo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lastUpdated, lastLoaded, validity, registrationTo);
    }

    @JsonIgnore
    public Bitemporality getBitemporality() {
        return new Bitemporality(this.getRegistrationFrom(), this.getRegistrationTo(), this.getValidFrom(), this.getValidTo());
    }
}
