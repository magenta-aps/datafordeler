package dk.magenta.datafordeler.cpr.data;

import dk.magenta.datafordeler.core.database.Entity;
import dk.magenta.datafordeler.core.database.Registration;
import dk.magenta.datafordeler.core.util.Equality;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;

import java.time.OffsetDateTime;

public abstract class CprRegistration<E extends Entity<E, R>, R extends CprRegistration<E, R, V>, V extends CprEffect> extends Registration<E, R, V> {

    public V getEffect(OffsetDateTime effectFrom, boolean effectFromUncertain, OffsetDateTime effectTo, boolean effectToUncertain) {
        for (V effect : this.effects) {
            if (
                    Equality.equal(effect.getEffectFrom(), effectFrom) &&
                            Equality.equal(effect.getEffectTo(), effectTo) &&
                            (effect.getEffectFromUncertain() == effectFromUncertain) &&
                            (effect.getEffectToUncertain() == effectToUncertain)
            ) {
                return effect;
            }
        }
        return null;
    }

    public V getEffect(CprBitemporality bitemporality) {
        return this.getEffect(bitemporality.effectFrom, bitemporality.effectFromUncertain, bitemporality.effectTo, bitemporality.effectToUncertain);
    }

    public V createEffect(OffsetDateTime effectFrom, boolean effectFromUncertain, OffsetDateTime effectTo, boolean effectToUncertain) {
        V effect = this.createEffect(effectFrom, effectTo);
        effect.setEffectFromUncertain(effectFromUncertain);
        effect.setEffectToUncertain(effectToUncertain);
        return effect;
    }

    public V createEffect(CprBitemporality bitemporality) {
        return this.createEffect(bitemporality.effectFrom, bitemporality.effectFromUncertain, bitemporality.effectTo, bitemporality.effectToUncertain);
    }

}
