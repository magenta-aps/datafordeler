package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.Bitemporal;
import dk.magenta.datafordeler.core.database.Monotemporal;
import dk.magenta.datafordeler.core.util.Bitemporality;
import dk.magenta.datafordeler.core.util.Equality;
import dk.magenta.datafordeler.core.util.ListHashMap;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.MappedSuperclass;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@MappedSuperclass
public abstract class CvrBitemporalRecord extends CvrNontemporalRecord implements Comparable<CvrBitemporalRecord> {

    public static final String FILTERLOGIC_REGISTRATIONFROM_AFTER = "(" + CvrBitemporalRecord.DB_FIELD_LAST_UPDATED + " >= :" + Monotemporal.FILTERPARAM_REGISTRATIONFROM_AFTER + ")";
    public static final String FILTERLOGIC_REGISTRATIONFROM_BEFORE = "(" + CvrBitemporalRecord.DB_FIELD_LAST_UPDATED + " < :" + Monotemporal.FILTERPARAM_REGISTRATIONFROM_BEFORE + " OR " + CvrBitemporalRecord.DB_FIELD_LAST_UPDATED + " is null)";
    public static final String FILTERLOGIC_REGISTRATIONTO_AFTER = "(" + CvrBitemporalRecord.DB_FIELD_REGISTRATION_TO + " >= :" + Monotemporal.FILTERPARAM_REGISTRATIONTO_AFTER + " OR " + CvrBitemporalRecord.DB_FIELD_REGISTRATION_TO + " is null)";
    public static final String FILTERLOGIC_REGISTRATIONTO_BEFORE = "(" + CvrBitemporalRecord.DB_FIELD_REGISTRATION_TO + " < :" + Monotemporal.FILTERPARAM_REGISTRATIONTO_BEFORE + ")";


    public static final String FILTERLOGIC_EFFECTFROM_AFTER = "(" + CvrRecordPeriod.DB_FIELD_VALID_FROM + " >= :" + Bitemporal.FILTERPARAM_EFFECTFROM_AFTER + ")";
    public static final String FILTERLOGIC_EFFECTFROM_BEFORE = "(" + CvrRecordPeriod.DB_FIELD_VALID_FROM + " < :" + Bitemporal.FILTERPARAM_EFFECTFROM_BEFORE + " OR " + CvrRecordPeriod.DB_FIELD_VALID_FROM + " is null)";
    public static final String FILTERLOGIC_EFFECTTO_AFTER = "(" + CvrRecordPeriod.DB_FIELD_VALID_TO + " >= :" + Bitemporal.FILTERPARAM_EFFECTTO_AFTER + " OR " + CvrRecordPeriod.DB_FIELD_VALID_TO + " is null)";
    public static final String FILTERLOGIC_EFFECTTO_BEFORE = "(" + CvrRecordPeriod.DB_FIELD_VALID_TO + " < :" + Bitemporal.FILTERPARAM_EFFECTTO_BEFORE + ")";


    public static final String FILTERPARAMTYPE_REGISTRATIONFROM = "java.time.OffsetDateTime";
    public static final String FILTERPARAMTYPE_REGISTRATIONTO = "java.time.OffsetDateTime";

    public static final String FILTERPARAMTYPE_EFFECTFROM = "java.time.OffsetDateTime";
    public static final String FILTERPARAMTYPE_EFFECTTO = "java.time.OffsetDateTime";

    public static final String FILTERPARAMTYPE_LASTUPDATED = "java.time.OffsetDateTime";

    /**
     * We do not want to use any timestamps of type LocalDate
     *
     * @param input
     * @param filter
     * @return
     */
    @Deprecated
    public static Object castFilterParam(Object input, String filter) {
        switch (filter) {
            case Bitemporal.FILTER_EFFECTFROM_AFTER:
            case Bitemporal.FILTER_EFFECTFROM_BEFORE:
            case Bitemporal.FILTER_EFFECTTO_AFTER:
            case Bitemporal.FILTER_EFFECTTO_BEFORE:
                return ((OffsetDateTime) input).toLocalDate();
        }
        return null;
    }


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

    public void setRegistrationFrom(OffsetDateTime offsetDateTime) {
        this.lastUpdated = offsetDateTime;
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
        if (this.validity != null) {
            return this.validity.getValidTo();
        } else {
            return null;
        }
    }

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

    public static final String DB_FIELD_REGISTRATION_TO = Monotemporal.DB_FIELD_REGISTRATION_TO;
    public static final String IO_FIELD_REGISTRATION_TO = Monotemporal.IO_FIELD_REGISTRATION_TO;

    @JsonProperty(IO_FIELD_REGISTRATION_TO)
    @Column(name = DB_FIELD_REGISTRATION_TO)
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
    public OffsetDateTime getEffectFrom() {
        if (this.validity != null) {
            return Bitemporality.convertTime(this.validity.getValidFrom());
        }
        return null;
    }

    public void setEffectFrom(OffsetDateTime offsetDateTime) {
        if (this.validity == null) {
            this.validity = new CvrRecordPeriod();
        }
        this.validity.setValidFrom(convertTime(offsetDateTime));
    }

    @JsonIgnore
    public OffsetDateTime getEffectTo() {
        if (this.validity != null) {
            return Bitemporality.convertTime(this.validity.getValidTo());
        }
        return null;
    }

    public void setEffectTo(OffsetDateTime offsetDateTime) {
        if (this.validity == null) {
            this.validity = new CvrRecordPeriod();
        }
        this.validity.setValidTo(convertTime(offsetDateTime));
    }

    @JsonIgnore
    public Bitemporality getBitemporality() {
        return new Bitemporality(this.getRegistrationFrom(), this.getRegistrationTo(), this.getValidFrom(), this.getValidTo());
    }

    public static LocalDate convertTime(OffsetDateTime time) {
        return time != null ? time.atZoneSameInstant(ZoneOffset.UTC).toLocalDate() : null;
    }

    public static void updateRegistrations(Set<? extends CvrBitemporalRecord> records, boolean onlyOneOpenRegistration) {
        System.out.println("There are "+records.size()+" records");
        if (records != null && records.size() > 1) {
            OffsetDateTime nextUpdateTime = null;
            Stream<? extends CvrBitemporalRecord> recordStream = records.stream();
            if (!onlyOneOpenRegistration) {
                recordStream = recordStream.filter(r -> r.getEffectTo() == null);
            }
            recordStream = recordStream.sorted(Comparator.comparing(CvrBitemporalRecord::getLastUpdated, Comparator.nullsFirst(Comparator.naturalOrder())).reversed());
            List<CvrBitemporalRecord> recordList = recordStream.collect(Collectors.toList());
            for (CvrBitemporalRecord record : recordList) {
                record.setRegistrationTo(nextUpdateTime);
                System.out.println(record.getId()+" "+record.getRegistrationFrom()+" "+record.getRegistrationTo());
                nextUpdateTime = record.getRegistrationFrom();
            }
        }
    }
}
