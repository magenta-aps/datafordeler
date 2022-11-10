package dk.magenta.datafordeler.core.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAccessor;
import java.util.Objects;

public class Monotemporality {
    public OffsetDateTime registrationFrom;
    public OffsetDateTime registrationTo;

    public Monotemporality(OffsetDateTime registrationFrom, OffsetDateTime registrationTo) {
        this.registrationFrom = registrationFrom;
        this.registrationTo = registrationTo;
    }

    public Monotemporality(OffsetDateTime registrationFrom) {
        this.registrationFrom = registrationFrom;
    }

    public static final char COMPARE_REGISTRATION_FROM = 0x01;
    public static final char COMPARE_REGISTRATION_TO = 0x02;
    public static final char COMPARE_REGISTRATION = COMPARE_REGISTRATION_FROM | COMPARE_REGISTRATION_TO;
    public static final char COMPARE_ALL = COMPARE_REGISTRATION;

    public static final char EXCLUDE_REGISTRATION_FROM = COMPARE_ALL ^ COMPARE_REGISTRATION_FROM;
    public static final char EXCLUDE_REGISTRATION_TO = COMPARE_ALL ^ COMPARE_REGISTRATION_TO;

    @Override
    public boolean equals(Object o) {
        return this.equals(o, COMPARE_ALL);
    }

    public boolean equals(Object o, char compare) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Monotemporality that = (Monotemporality) o;

        if (((compare & COMPARE_REGISTRATION_FROM) != 0) && (registrationFrom != null ? !registrationFrom.equals(that.registrationFrom) : that.registrationFrom != null))
            return false;
        return ((compare & COMPARE_REGISTRATION_TO) == 0) || (registrationTo != null ? registrationTo.equals(that.registrationTo) : that.registrationTo == null);
    }

    public boolean equalRegistration(Monotemporality o) {
        return o != null && Objects.equals(this.registrationFrom, o.registrationFrom) && Objects.equals(this.registrationTo, o.registrationTo);
    }

    @Override
    public int hashCode() {
        int result = registrationFrom != null ? registrationFrom.hashCode() : 0;
        return result;
    }

    public boolean matches(OffsetDateTime registrationTime) {
        return Equality.equal(this.registrationFrom, registrationTime);
    }

    public boolean overlapsRegistration(OffsetDateTime rangeStart, OffsetDateTime rangeEnd) {
        return (this.registrationFrom == null || rangeEnd == null || !rangeEnd.isBefore(this.registrationFrom)) && (this.registrationTo == null || rangeStart == null || !rangeStart.isAfter(this.registrationTo));
    }

    public boolean overlaps(Monotemporality other) {
        return this.overlapsRegistration(other.registrationFrom, other.registrationTo);
    }

    public boolean containsRegistration(OffsetDateTime rangeStart, OffsetDateTime rangeEnd) {
        return (this.registrationFrom == null || (rangeStart != null && !rangeStart.isBefore(this.registrationFrom))) && (this.registrationTo == null || (rangeEnd != null && !rangeEnd.isAfter(this.registrationTo)));
    }

    public boolean contains(Monotemporality other) {
        return this.containsRegistration(other.registrationFrom, other.registrationTo);
    }

    public String toString() {
        return this.registrationFrom + "|" + this.registrationTo;
    }

    public static OffsetDateTime convertTime(LocalDate time) {
        return time != null ? OffsetDateTime.of(time, LocalTime.MIDNIGHT, ZoneOffset.UTC) : null;
    }

    public static OffsetDateTime convertTime(TemporalAccessor time) {
        return time != null ? OffsetDateTime.from(time) : null;
    }

    public Bitemporality asBitemporality() {
        return new Bitemporality(this.registrationFrom, this.registrationTo);
    }
}
