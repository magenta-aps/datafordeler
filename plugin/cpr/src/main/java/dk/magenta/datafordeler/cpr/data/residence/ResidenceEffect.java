package dk.magenta.datafordeler.cpr.data.residence;

import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.data.CprEffect;
import dk.magenta.datafordeler.cpr.data.residence.data.ResidenceBaseData;

import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;

/**
 * Representation of effects in the bitemporal model for residences.
 *
 * @see dk.magenta.datafordeler.core.database.Entity
 */
@javax.persistence.Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_residence_effect", indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_residence_effect_registration", columnList = "registration_id"),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_residence_effect_from", columnList = "effectFrom"),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_residence_effect_to", columnList = "effectTo")
})
public class ResidenceEffect extends CprEffect<ResidenceRegistration, ResidenceEffect, ResidenceBaseData> {

    public ResidenceEffect() {
    }

    public ResidenceEffect(ResidenceRegistration registration, OffsetDateTime effectFrom, OffsetDateTime effectTo) {
        super(registration, effectFrom, effectTo);
    }

    public ResidenceEffect(ResidenceRegistration registration, TemporalAccessor effectFrom, TemporalAccessor effectTo) {
        super(registration, effectFrom, effectTo);
    }

    public ResidenceEffect(ResidenceRegistration registration, String effectFrom, String effectTo) {
        super(registration, effectFrom, effectTo);
    }


    public ResidenceEffect(ResidenceRegistration registration, OffsetDateTime effectFrom, boolean effectFromUncertain, OffsetDateTime effectTo, boolean effectToUncertain) {
        this(registration, effectFrom, effectTo);
        this.setEffectFromUncertain(effectFromUncertain);
        this.setEffectToUncertain(effectToUncertain);
    }


}
