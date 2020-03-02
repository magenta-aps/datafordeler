package dk.magenta.datafordeler.cpr.records;

import dk.magenta.datafordeler.core.util.Bitemporality;
import dk.magenta.datafordeler.core.util.BitemporalityComparator;
import dk.magenta.datafordeler.cpr.data.CprEffect;

import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Objects;

public class CprBitemporality extends Bitemporality {
    public boolean effectFromUncertain;
    public boolean effectToUncertain;

    public CprBitemporality(OffsetDateTime registrationFrom, OffsetDateTime registrationTo, OffsetDateTime effectFrom, boolean effectFromUncertain, OffsetDateTime effectTo, boolean effectToUncertain) {
        super(registrationFrom, registrationTo, effectFrom, effectTo);
        this.effectFromUncertain = effectFromUncertain;
        this.effectToUncertain = effectToUncertain;
    }

    public CprBitemporality(OffsetDateTime registrationFrom) {
        this(registrationFrom, null, null, false, null, false);
    }

    public CprBitemporality(OffsetDateTime registrationFrom, OffsetDateTime registrationTo) {
        this(registrationFrom, registrationTo, null, false, null, false);
    }

    public CprBitemporality(OffsetDateTime registrationFrom, OffsetDateTime registrationTo, OffsetDateTime effectFrom, OffsetDateTime effectTo) {
        this(registrationFrom, registrationTo, effectFrom, false, effectTo, false);
    }

    public CprBitemporality(OffsetDateTime registrationFrom, OffsetDateTime registrationTo, TemporalAccessor effectFrom, TemporalAccessor effectTo) {
        this(registrationFrom, registrationTo, convertTime(effectFrom), false, convertTime(effectTo), false);
    }

    public CprBitemporality(OffsetDateTime registrationFrom, OffsetDateTime registrationTo, TemporalAccessor effectFrom, boolean effectFromUncertain, TemporalAccessor effectTo, boolean effectToUncertain) {
        this(registrationFrom, registrationTo, convertTime(effectFrom), effectFromUncertain, convertTime(effectTo), effectToUncertain);
    }

    public CprBitemporality withEffect(CprEffect effect) {
        return new CprBitemporality(this.registrationFrom, this.registrationTo, effect.getEffectFrom(), effect.getEffectFromUncertain(), effect.getEffectTo(), effect.getEffectToUncertain());
    }

    public static final BitemporalityComparator<CprBitemporality> effectComparator = BitemporalityComparator.effect(CprBitemporality.class);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CprBitemporality that = (CprBitemporality) o;
        return effectFromUncertain == that.effectFromUncertain &&
                effectToUncertain == that.effectToUncertain;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), effectFromUncertain, effectToUncertain);
    }

    public int compareTo(CprBitemporality o) {
        return super.compareTo(o);
    }
}
