package dk.magenta.datafordeler.geo.data.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.Monotemporal;
import dk.magenta.datafordeler.core.util.Monotemporality;
import dk.magenta.datafordeler.geo.data.GeoEntity;
import dk.magenta.datafordeler.geo.data.WireCache;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.Session;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

import static dk.magenta.datafordeler.core.database.Bitemporal.fixOffsetIn;
import static dk.magenta.datafordeler.core.database.Bitemporal.fixOffsetOut;

@MappedSuperclass
public class GeoMonotemporalRecord<E extends GeoEntity> extends GeoNontemporalRecord<E> implements Monotemporal {

    public static final String DB_FIELD_ENTITY = GeoNontemporalRecord.DB_FIELD_ENTITY;

    // For storing the calculated endRegistration time, ie. when the next registration "overrides" us
    public static final String DB_FIELD_REGISTRATION_FROM = Monotemporal.DB_FIELD_REGISTRATION_FROM;
    public static final String IO_FIELD_REGISTRATION_FROM = Monotemporal.IO_FIELD_REGISTRATION_FROM;
    @Column(name = DB_FIELD_REGISTRATION_FROM, columnDefinition = "datetime2")
    @JsonProperty(value = IO_FIELD_REGISTRATION_FROM)
    private OffsetDateTime registrationFrom;

    public OffsetDateTime getRegistrationFrom() {
        return fixOffsetOut(this.registrationFrom);
    }

    public void setRegistrationFrom(OffsetDateTime registrationFrom) {
        this.registrationFrom = fixOffsetIn(registrationFrom);
    }

    public void setRegistrationFrom(long registrationFrom) {
        this.setRegistrationFrom(
                Instant.ofEpochMilli(registrationFrom).atOffset(ZoneOffset.UTC)
        );
    }


    // For storing the calculated endRegistration time, ie. when the next registration "overrides" us
    public static final String DB_FIELD_REGISTRATION_TO = Monotemporal.DB_FIELD_REGISTRATION_TO;
    public static final String IO_FIELD_REGISTRATION_TO = Monotemporal.IO_FIELD_REGISTRATION_TO;
    @Column(name = DB_FIELD_REGISTRATION_TO, columnDefinition = "datetime2")
    @JsonProperty(value = IO_FIELD_REGISTRATION_TO)
    private OffsetDateTime registrationTo;

    public OffsetDateTime getRegistrationTo() {
        return fixOffsetOut(this.registrationTo);
    }

    public void setRegistrationTo(OffsetDateTime registrationTo) {
        this.registrationTo = fixOffsetIn(registrationTo);
    }

    public void setRegistrationTo(long registrationTo) {
        this.setRegistrationTo(
                Instant.ofEpochMilli(registrationTo).atOffset(ZoneOffset.UTC)
        );
    }


    public static final String DB_FIELD_EDITOR = "editor";
    public static final String IO_FIELD_EDITOR = "ændretAf";
    @Column(name = DB_FIELD_EDITOR)
    @JsonProperty(value = IO_FIELD_EDITOR)
    private String editor;

    public String getEditor() {
        return this.editor;
    }

    public GeoMonotemporalRecord setEditor(String editor) {
        this.editor = editor;
        return this;
    }


    public static <T extends GeoMonotemporalRecord> T newestRecord(Collection<T> set) {
        if (set.isEmpty()) {
            return null;
        } else {
            ArrayList<T> list = new ArrayList<>(set);
            list.sort(Comparator.comparing(GeoMonotemporalRecord::getRegistrationFrom));
            return list.get(list.size() - 1);
        }
    }

    public static <T extends GeoMonotemporalRecord> void updateRegistrationTo(Collection<T> set) {
        ArrayList<T> list = new ArrayList<>(set);
        list.sort(Comparator.comparing(GeoMonotemporalRecord::getRegistrationFrom));
        T previous = null;
        for (T record : list) {
            if (previous != null && previous.getRegistrationTo() == null) {
                previous.setRegistrationTo(record.getRegistrationFrom());
            }
            previous = record;
        }
    }

    public void setDafoUpdated(OffsetDateTime updateTime) {
        super.setDafoUpdated(updateTime);
    }


    /**
     * For sorting purposes; we implement the Comparable interface, so we should
     * provide a comparison method. Here, we sort CvrRecord objects by registrationFrom, with nulls first
     */
    public int compareTo(GeoMonotemporalRecord o) {
        OffsetDateTime oUpdated = o == null ? null : o.getRegistrationFrom();
        if (this.getRegistrationFrom() == null && oUpdated == null) return 0;
        if (this.getRegistrationFrom() == null) return -1;
        return this.getRegistrationFrom().compareTo(oUpdated);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeoMonotemporalRecord that = (GeoMonotemporalRecord) o;
        if (!this.equalData(that)) return false;
        return Objects.equals(registrationFrom, that.registrationFrom) &&
                Objects.equals(registrationTo, that.registrationTo) &&
                Objects.equals(dafoUpdated, that.dafoUpdated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.registrationFrom, this.registrationTo);
    }

    protected static void copy(GeoMonotemporalRecord from, GeoMonotemporalRecord to) {
        GeoNontemporalRecord.copy(from, to);
        to.registrationFrom = from.registrationFrom;
        to.registrationTo = from.registrationTo;
    }

    public void wire(Session session, WireCache wireCache) {
        // Implement in subclasses as needed
    }

    @JsonIgnore
    public Monotemporality getMonotemporality() {
        return new Monotemporality(this.registrationFrom, this.registrationTo);
    }
}
