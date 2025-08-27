package dk.magenta.datafordeler.cpr.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import dk.magenta.datafordeler.core.database.Effect;
import dk.magenta.datafordeler.core.database.Registration;
import dk.magenta.datafordeler.core.util.Equality;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;

@MappedSuperclass
@JsonPropertyOrder({"effectFrom", "effectFromUncertain", "effectTo", "effectToUncertain", "dataItems"})
public abstract class CprEffect<R extends Registration, V extends CprEffect, D extends CprData> extends Effect<R, V, D> {

    @Column
    private boolean effectFromUncertain;

    @Column
    private boolean effectToUncertain;

    public CprEffect() {
    }

    public CprEffect(R registration, OffsetDateTime effectFrom, OffsetDateTime effectTo) {
        super(registration, effectFrom, effectTo);
    }

    public CprEffect(R registration, TemporalAccessor effectFrom, TemporalAccessor effectTo) {
        super(registration, effectFrom, effectTo);
    }

    public CprEffect(R registration, String effectFrom, String effectTo) {
        super(registration, effectFrom, effectTo);
    }

    @JsonProperty("virkningFraUsikkerhedsmarkering")
    public boolean getEffectFromUncertain() {
        return this.effectFromUncertain;
    }

    public void setEffectFromUncertain(boolean effectFromUncertain) {
        this.effectFromUncertain = effectFromUncertain;
    }

    @JsonProperty("virkningTilUsikkerhedsmarkering")
    public boolean getEffectToUncertain() {
        return this.effectToUncertain;
    }

    public void setEffectToUncertain(boolean effectToUncertain) {
        this.effectToUncertain = effectToUncertain;
    }

    public void setRegistration(R registrering) {
        super.setRegistration(registrering);
    }

    public boolean compareRange(V other) {
        return (other != null && this.compareRange(other.getEffectFrom(), other.getEffectFromUncertain(), other.getEffectTo(), other.getEffectToUncertain()));
    }

    public boolean compareRange(OffsetDateTime effectFrom, boolean effectFromUncertain, OffsetDateTime effectTo, boolean effectToUncertain) {
        return (
                Equality.equal(this.getEffectFrom(), effectFrom) &&
                        Equality.equal(this.getEffectTo(), effectTo) &&
                        (this.getEffectFromUncertain() == effectFromUncertain) &&
                        (this.getEffectToUncertain() == effectToUncertain)
        );
    }

    public boolean compareRange(CprBitemporality bitemporality) {
        return this.compareRange(bitemporality.effectFrom, bitemporality.effectFromUncertain, bitemporality.effectTo, bitemporality.effectToUncertain);
    }

    public V createClone() {
        V other = (V) this.registration.createEffect(this.getEffectFrom(), this.getEffectTo());
        other.setEffectFromUncertain(this.effectFromUncertain);
        other.setEffectToUncertain(this.effectToUncertain);
        for (D data : this.dataItems) {
            D dataClone = (D) data.clone();
            dataClone.addEffect(other);
        }
        return other;
    }

    public String toString() {
        return this.registration.getRegistrationFrom() + "|" + this.registration.getRegistrationTo() + "|" + this.getEffectFrom() + "|" + this.getEffectFromUncertain() + "|" + this.getEffectTo() + "|" + this.getEffectToUncertain();
    }
}
