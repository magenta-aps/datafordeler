package dk.magenta.datafordeler.cvr.data;

import dk.magenta.datafordeler.core.database.Effect;
import dk.magenta.datafordeler.core.util.Bitemporality;

import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;

public class CvrEffect<R extends CvrRegistration, V extends CvrEffect, D extends CvrData> extends Effect<R, V, D> {

    public CvrEffect() {
    }

    public CvrEffect(R registration, OffsetDateTime effectFrom, OffsetDateTime effectTo) {
        super(registration, effectFrom, effectTo);
    }

    public CvrEffect(R registration, TemporalAccessor effectFrom, TemporalAccessor effectTo) {
        super(registration, effectFrom, effectTo);
    }

    public CvrEffect(R registration, String effectFrom, String effectTo) {
        super(registration, effectFrom, effectTo);
    }

    public boolean compareRange(OffsetDateTime effectFrom, OffsetDateTime effectTo) {
        return (
                (this.getEffectFrom() != null ? this.getEffectFrom().equals(effectFrom) : effectFrom == null) &&
                (this.getEffectTo() != null ? this.getEffectTo().equals(effectTo) : effectTo == null)
        );
    }

    public boolean compareRange(Bitemporality bitemporality) {
        return this.compareRange(bitemporality.effectFrom, bitemporality.effectTo);
    }
}
