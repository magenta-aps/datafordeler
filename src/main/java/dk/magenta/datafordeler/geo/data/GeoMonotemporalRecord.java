package dk.magenta.datafordeler.geo.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.Registration;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlElement;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

@MappedSuperclass
public class GeoMonotemporalRecord extends GeoNontemporalRecord {


    // For storing the calculated endRegistration time, ie. when the next registration "overrides" us
    public static final String DB_FIELD_REGISTRATION_FROM = "registrationFrom";
    public static final String IO_FIELD_REGISTRATION_FROM = "registreringFra";
    @Column(name = DB_FIELD_REGISTRATION_FROM)
    @JsonProperty(value = IO_FIELD_REGISTRATION_FROM)
    @XmlElement(name = IO_FIELD_REGISTRATION_FROM)
    private OffsetDateTime registrationFrom;

    public OffsetDateTime getRegistrationFrom() {
        return this.registrationFrom;
    }

    public GeoMonotemporalRecord setRegistrationFrom(OffsetDateTime registrationFrom) {
        this.registrationFrom = registrationFrom;
        return this;
    }

    public GeoMonotemporalRecord setRegistrationFrom(long registrationFrom) {
        return this.setRegistrationFrom(
                Instant.ofEpochMilli(registrationFrom).atOffset(ZoneOffset.UTC)
        );
    }




    // For storing the calculated endRegistration time, ie. when the next registration "overrides" us
    public static final String DB_FIELD_REGISTRATION_TO = "registrationTo";
    public static final String IO_FIELD_REGISTRATION_TO = "registreringTil";
    @Column(name = DB_FIELD_REGISTRATION_TO)
    @JsonProperty(value = IO_FIELD_REGISTRATION_TO)
    @XmlElement(name = IO_FIELD_REGISTRATION_TO)
    private OffsetDateTime registrationTo;

    public OffsetDateTime getRegistrationTo() {
        return this.registrationTo;
    }

    public GeoMonotemporalRecord setRegistrationTo(OffsetDateTime registrationTo) {
        this.registrationTo = registrationTo;
        return this;
    }

    public GeoMonotemporalRecord setRegistrationTo(long registrationTo) {
        return this.setRegistrationTo(
                Instant.ofEpochMilli(registrationTo).atOffset(ZoneOffset.UTC)
        );
    }



    public static final String DB_FIELD_EDITOR = "editor";
    public static final String IO_FIELD_EDITOR = "ændretAf";
    @Column(name = DB_FIELD_EDITOR)
    @JsonProperty(value = IO_FIELD_EDITOR)
    @XmlElement(name = IO_FIELD_EDITOR)
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



    public GeoMonotemporalRecord setDafoUpdated(OffsetDateTime updateTime) {
        super.setDafoUpdated(updateTime);
        return this;
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
                Objects.equals(registrationTo, that.registrationTo);
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
}
