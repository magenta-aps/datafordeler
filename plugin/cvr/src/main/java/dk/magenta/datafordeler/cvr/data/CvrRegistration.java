package dk.magenta.datafordeler.cvr.data;

import dk.magenta.datafordeler.core.database.Registration;
import dk.magenta.datafordeler.core.util.Bitemporality;
import dk.magenta.datafordeler.core.util.Equality;

import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;

public abstract class CvrRegistration<E extends CvrEntity<E, R>, R extends CvrRegistration<E, R, V>, V extends CvrEffect> extends Registration<E, R, V> {

    public CvrRegistration() {
        super();
    }

    public CvrRegistration(OffsetDateTime registrationFrom, OffsetDateTime registrationTo, int sequenceNumber) {
        super(registrationFrom, registrationTo, sequenceNumber);
    }

    public CvrRegistration(TemporalAccessor registrationFrom, TemporalAccessor registrationTo, int sequenceNumber) {
        super(registrationFrom, registrationTo, sequenceNumber);
    }

    public CvrRegistration(String registrationFrom, String registrationTo, int sequenceNumber) {
        super(registrationFrom, registrationTo, sequenceNumber);
    }

    public V getEffect(OffsetDateTime effectFrom, boolean effectFromUncertain, OffsetDateTime effectTo, boolean effectToUncertain) {
        for (V effect : this.effects) {
            if (
                    Equality.equal(effect.getEffectFrom(), effectFrom) &&
                    Equality.equal(effect.getEffectTo(), effectTo)
                    ) {
                return effect;
            }
        }
        return null;
    }

    public V getEffect(Bitemporality bitemporality) {
        return this.getEffect(bitemporality.effectFrom, bitemporality.effectTo);
    }

    public V createEffect(Bitemporality bitemporality) {
        return this.createEffect(bitemporality.effectFrom, bitemporality.effectTo);
    }
}
