package dk.magenta.datafordeler.cpr.data;

import dk.magenta.datafordeler.core.database.Entity;
import dk.magenta.datafordeler.core.database.Registration;
import dk.magenta.datafordeler.cpr.records.Bitemporality;

import java.time.OffsetDateTime;

public abstract class CprRegistration<E extends Entity<E, R>, R extends CprRegistration<E, R, V>, V extends CprEffect> extends Registration<E, R, V> {

    public V getEffect(OffsetDateTime effectFrom, boolean effectFromUncertain, OffsetDateTime effectTo, boolean effectToUncertain) {
        for (V effect : this.effects) {
            if (
                    Registration.equalOffsetDateTime(effect.getEffectFrom(), effectFrom) &&
                    Registration.equalOffsetDateTime(effect.getEffectTo(), effectTo) &&
                            (effect.isUncertainFrom() == effectFromUncertain) &&
                            (effect.isUncertainTo() == effectToUncertain)
                    ) {
                return effect;
            }
        }
        return null;
    }

    public V getEffect(Bitemporality bitemporality) {
        return this.getEffect(bitemporality.effectFrom, bitemporality.effectFromUncertain, bitemporality.effectTo, bitemporality.effectToUncertain);
    }

    public V createEffect(OffsetDateTime effectFrom, boolean effectFromUncertain, OffsetDateTime effectTo, boolean effectToUncertain) {
        V effect = this.createEffect(effectFrom, effectTo);
        effect.setUncertainFrom(effectFromUncertain);
        effect.setUncertainTo(effectToUncertain);
        return effect;
    }

    public V createEffect(Bitemporality bitemporality) {
        return this.createEffect(bitemporality.effectFrom, bitemporality.effectFromUncertain, bitemporality.effectTo, bitemporality.effectToUncertain);
    }

}