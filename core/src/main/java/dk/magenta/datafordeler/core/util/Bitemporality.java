package dk.magenta.datafordeler.core.util;

import dk.magenta.datafordeler.core.database.Effect;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Bitemporality implements Comparable<Bitemporality> {
    public OffsetDateTime registrationFrom;
    public OffsetDateTime registrationTo;
    public OffsetDateTime effectFrom;
    public OffsetDateTime effectTo;

    private Bitemporality() {
    }

    public Bitemporality(OffsetDateTime registrationFrom, OffsetDateTime registrationTo, OffsetDateTime effectFrom, OffsetDateTime effectTo) {
        this.registrationFrom = registrationFrom;
        this.registrationTo = registrationTo;
        this.effectFrom = effectFrom;
        this.effectTo = effectTo;
    }

    public static Bitemporality pureRegistration(OffsetDateTime registrationFrom, OffsetDateTime registrationTo) {
        Bitemporality bitemporality = new Bitemporality();
        bitemporality.registrationFrom = registrationFrom;
        bitemporality.registrationTo = registrationTo;
        return bitemporality;
    }

    public Bitemporality(OffsetDateTime registrationFrom) {
        this.registrationFrom = registrationFrom;

    }

    public Bitemporality(OffsetDateTime registrationFrom, OffsetDateTime registrationTo) {
        this.registrationFrom = registrationFrom;
        this.registrationTo = registrationTo;
    }

    public Bitemporality(OffsetDateTime registrationFrom, OffsetDateTime registrationTo, LocalDate effectFrom, LocalDate effectTo) {
        this(registrationFrom, registrationTo, convertTime(effectFrom), convertTime(effectTo));
    }

    public Bitemporality(OffsetDateTime registrationFrom, OffsetDateTime registrationTo, TemporalAccessor effectFrom, TemporalAccessor effectTo) {
        this(registrationFrom, registrationTo, convertTime(effectFrom), convertTime(effectTo));
    }

    public Bitemporality withEffect(Effect effect) {
        return new Bitemporality(this.registrationFrom, this.registrationTo, effect.getEffectFrom(), effect.getEffectTo());
    }

    public static final char COMPARE_REGISTRATION_FROM = 0x01;
    public static final char COMPARE_REGISTRATION_TO = 0x02;
    public static final char COMPARE_REGISTRATION = COMPARE_REGISTRATION_FROM | COMPARE_REGISTRATION_TO;
    public static final char COMPARE_EFFECT_FROM = 0x04;
    public static final char COMPARE_EFFECT_TO = 0x08;
    public static final char COMPARE_EFFECT = COMPARE_EFFECT_FROM | COMPARE_EFFECT_TO;
    public static final char COMPARE_ALL = COMPARE_REGISTRATION | COMPARE_EFFECT;

    public static final char EXCLUDE_REGISTRATION_FROM = COMPARE_ALL ^ COMPARE_REGISTRATION_FROM;
    public static final char EXCLUDE_REGISTRATION_TO = COMPARE_ALL ^ COMPARE_REGISTRATION_TO;
    public static final char EXCLUDE_EFFECT_FROM = COMPARE_ALL ^ COMPARE_EFFECT_FROM;
    public static final char EXCLUDE_EFFECT_TO = COMPARE_ALL ^ COMPARE_EFFECT_TO;

    @Override
    public boolean equals(Object o) {
        return this.equals(o, COMPARE_ALL);
    }

    public boolean equals(Object o, char compare) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Bitemporality that = (Bitemporality) o;

        if (((compare & COMPARE_REGISTRATION_FROM) != 0) && (registrationFrom != null ? !Equality.equal(registrationFrom, that.registrationFrom) : that.registrationFrom != null))
            return false;
        if (((compare & COMPARE_REGISTRATION_TO) != 0) && (registrationTo != null ? !Equality.equal(registrationTo, that.registrationTo) : that.registrationTo != null))
            return false;
        if (((compare & COMPARE_EFFECT_FROM) != 0) && (effectFrom != null ? !Equality.equal(effectFrom, that.effectFrom) : that.effectFrom != null))
            return false;
        return ((compare & COMPARE_EFFECT_TO) == 0) || (effectTo != null ? Equality.equal(effectTo, that.effectTo) : that.effectTo == null);
    }

    public boolean equalRegistration(Bitemporality o) {
        return o != null && Equality.equal(this.registrationFrom, o.registrationFrom) && Equality.equal(this.registrationTo, o.registrationTo);
    }

    public boolean equalEffect(Bitemporality o) {
        return o != null && Equality.equal(this.effectFrom, o.effectFrom) && Equality.equal(this.effectTo, o.effectTo);
    }

    @Override
    public int hashCode() {
        int result = registrationFrom != null ? registrationFrom.hashCode() : 0;
        result = 31 * result + (effectFrom != null ? effectFrom.hashCode() : 0);
        result = 31 * result + (effectTo != null ? effectTo.hashCode() : 0);
        return result;
    }

    public boolean matches(OffsetDateTime registrationTime, Effect effect) {
        return Equality.equal(this.registrationFrom, registrationTime) && effect.compareRange(this);
    }

    public boolean overlapsRegistration(OffsetDateTime rangeStart, OffsetDateTime rangeEnd) {
        return (this.registrationFrom == null || rangeEnd == null || !rangeEnd.isBefore(this.registrationFrom)) && (this.registrationTo == null || rangeStart == null || !rangeStart.isAfter(this.registrationTo));
    }

    public boolean overlapsEffect(OffsetDateTime rangeStart, OffsetDateTime rangeEnd) {
        return (this.effectFrom == null || rangeEnd == null || !rangeEnd.isBefore(this.effectFrom)) && (this.effectTo == null || rangeStart == null || !rangeStart.isAfter(this.effectTo));
    }

    public boolean overlaps(Bitemporality other) {
        return this.overlapsRegistration(other.registrationFrom, other.registrationTo) && this.overlapsEffect(other.effectFrom, other.effectTo);
    }

    public boolean containsRegistration(OffsetDateTime rangeStart, OffsetDateTime rangeEnd) {
        return (this.registrationFrom == null || (rangeStart != null && !rangeStart.isBefore(this.registrationFrom))) && (this.registrationTo == null || (rangeEnd != null && !rangeEnd.isAfter(this.registrationTo)));
    }

    public boolean containsRegistration(Bitemporality other) {
        return this.containsRegistration(other.registrationFrom, other.registrationTo);
    }

    public boolean containsEffect(OffsetDateTime rangeStart, OffsetDateTime rangeEnd) {
        return (this.effectFrom == null || (rangeStart != null && !rangeStart.isBefore(this.effectFrom))) && (this.effectTo == null || (rangeEnd != null && !rangeEnd.isAfter(this.effectTo)));
    }

    public boolean containsEffect(Bitemporality other) {
        return this.containsEffect(other.effectFrom, other.effectTo);
    }

    public boolean containsEffectTime(OffsetDateTime time) {
        return this.containsEffect(time, time);
    }

    public boolean containsEffectNow() {
        return this.containsEffectTime(OffsetDateTime.now());
    }

    public boolean contains(Bitemporality other) {
        return this.containsRegistration(other.registrationFrom, other.registrationTo) && this.containsEffect(other.effectFrom, other.effectTo);
    }

    public static OffsetDateTime min(OffsetDateTime a, OffsetDateTime b, boolean nullIsMin) {
        if (a == null) {
            return nullIsMin ? null : b;
        }
        if (b == null) {
            return nullIsMin ? null : a;
        }
        return a.isBefore(b) ? a : b;
    }

    public static OffsetDateTime max(OffsetDateTime a, OffsetDateTime b, boolean nullIsMax) {
        if (a == null) {
            return nullIsMax ? null : b;
        }
        if (b == null) {
            return nullIsMax ? null : a;
        }
        return a.isAfter(b) ? a : b;
    }

    public String toString() {
        return String.format("%25s - %-25s  |  %25s - %-25s", this.registrationFrom, this.registrationTo, this.effectFrom, this.effectTo);
    }

    public static OffsetDateTime convertTime(LocalDate time) {
        return time != null ? OffsetDateTime.of(time, LocalTime.MIDNIGHT, ZoneOffset.UTC) : null;
    }

    public static OffsetDateTime convertTime(TemporalAccessor time) {
        return time != null ? OffsetDateTime.from(time) : null;
    }

    public boolean emptyEffect() {
        return (this.effectFrom != null && this.effectTo != null && this.effectFrom.isEqual(this.effectTo));
    }

    @Override
    public int compareTo(Bitemporality o) {
        return BitemporalityComparator.all(this.getClass()).compare(this, o);
    }

    public static List<Bitemporality> unionOnEffect(Collection<Bitemporality> items) {
        // Finder foreningsmængen af bitemporaliteter udfra effekt, dvs. reducerer overlappende bitemporaliteter til én
        ArrayList<Bitemporality> result = new ArrayList<>();
        Bitemporality current = null;
        ArrayList<Bitemporality> sorted = new ArrayList<>(items);
        sorted.sort(BitemporalityComparator.EFFECT);
        for (Bitemporality item : sorted) {
            if (current == null || (item.effectFrom != null && current.effectTo != null && item.effectFrom.isAfter(current.effectTo))) {
                // Ingen current
                current = new Bitemporality();
                current.effectFrom = item.effectFrom;
                current.effectTo = item.effectTo;
                result.add(current);
            } else if (current.effectTo != null && (item.effectTo == null || item.effectTo.isAfter(current.effectTo))) {
                // Overlap
                current.effectTo = item.effectTo;
            }
        }
        return result;
    }

}
