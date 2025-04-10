package dk.magenta.datafordeler.core.database;

import dk.magenta.datafordeler.core.util.Bitemporality;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAccessor;

public interface Bitemporal extends Monotemporal {

    String FILTER_EFFECTFROM_AFTER = "effectFromAfterFilter";
    String FILTER_EFFECTFROM_BEFORE = "effectFromBeforeFilter";
    String FILTER_EFFECTTO_AFTER = "effectToAfterFilter";
    String FILTER_EFFECTTO_BEFORE = "effectToBeforeFilter";

    String FILTERPARAM_EFFECTFROM_AFTER = "effectFromAfterDate";
    String FILTERPARAM_EFFECTFROM_BEFORE = "effectFromBeforeDate";
    String FILTERPARAM_EFFECTTO_AFTER = "effectToAfterDate";
    String FILTERPARAM_EFFECTTO_BEFORE = "effectToBeforeDate";

    // True if the sought registration begins after our query
    String FILTERLOGIC_EFFECTFROM_AFTER = "(" + Bitemporal.DB_FIELD_EFFECT_FROM + " >= :" + Bitemporal.FILTERPARAM_EFFECTFROM_AFTER + ")";
    // True if the sought effect begins before our query
    String FILTERLOGIC_EFFECTFROM_BEFORE = "(" + Bitemporal.DB_FIELD_EFFECT_FROM + " < :" + Bitemporal.FILTERPARAM_EFFECTFROM_BEFORE + " OR " + Bitemporal.DB_FIELD_EFFECT_FROM + " is null)";

    // True if the sought effect ends after our query
    String FILTERLOGIC_EFFECTTO_AFTER = "(" + Bitemporal.DB_FIELD_EFFECT_TO + " >= :" + Bitemporal.FILTERPARAM_EFFECTTO_AFTER + " OR " + Bitemporal.DB_FIELD_EFFECT_TO + " is null)";
    // True if the sought effect ends before our query
    String FILTERLOGIC_EFFECTTO_BEFORE = "(" + Bitemporal.DB_FIELD_EFFECT_TO + " < :" + Bitemporal.FILTERPARAM_EFFECTTO_BEFORE + ")";


    String DB_FIELD_EFFECT_FROM = "effectFrom";
    String IO_FIELD_EFFECT_FROM = "virkningFra";

    OffsetDateTime getEffectFrom();

    void setEffectFrom(OffsetDateTime effectFrom);

    String DB_FIELD_EFFECT_TO = "effectTo";
    String IO_FIELD_EFFECT_TO = "virkningTil";

    OffsetDateTime getEffectTo();

    void setEffectTo(OffsetDateTime effectTo);

    default Bitemporal setBitemporality(String registrationFrom, String registrationTo, String effectFrom, String effectTo) {
        return this.setBitemporality(
                registrationFrom != null ? OffsetDateTime.parse(registrationFrom) : null,
                registrationTo != null ? OffsetDateTime.parse(registrationTo) : null,
                effectFrom != null ? OffsetDateTime.parse(effectFrom) : null,
                effectTo != null ? OffsetDateTime.parse(effectTo) : null
        );
    }

    default Bitemporal setBitemporality(OffsetDateTime registrationFrom, OffsetDateTime registrationTo, OffsetDateTime effectFrom, OffsetDateTime effectTo) {
        this.setRegistrationFrom(registrationFrom);
        this.setRegistrationTo(registrationTo);
        this.setEffectFrom(effectFrom);
        this.setEffectTo(effectTo);
        return this;
    }

    default Bitemporal setBitemporality(OffsetDateTime registrationFrom, OffsetDateTime registrationTo, TemporalAccessor effectFrom, TemporalAccessor effectTo) {
        this.setRegistrationFrom(registrationFrom);
        this.setRegistrationTo(registrationTo);
        this.setEffectFrom(OffsetDateTime.from(effectFrom));
        this.setEffectTo(OffsetDateTime.from(effectTo));
        return this;
    }

    default Bitemporal setBitemporality(Bitemporality bitemporality) {
        this.setBitemporality(bitemporality.registrationFrom, bitemporality.registrationTo, bitemporality.effectFrom, bitemporality.effectTo);
        return this;
    }

    Bitemporality getBitemporality();
    static void copy(Bitemporal from, Bitemporal to) {
        Monotemporal.copy(from, to);
        to.setEffectFrom(from.getEffectFrom());
        to.setEffectTo(from.getEffectTo());
    }


    ZoneId zoneId = ZoneId.of("Europe/Copenhagen");
    static OffsetDateTime fixOffsetOut(OffsetDateTime date) {
        if (date != null) {
            return date.atZoneSameInstant(zoneId).toOffsetDateTime();
        }
        return null;
    }
    static OffsetDateTime fixOffsetIn(OffsetDateTime date) {
        if (date != null) {
            return date.atZoneSameInstant(ZoneOffset.UTC).toOffsetDateTime();
        }
        return null;
    }
}
