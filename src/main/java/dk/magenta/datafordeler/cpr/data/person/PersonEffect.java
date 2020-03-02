package dk.magenta.datafordeler.cpr.data.person;

import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.data.CprEffect;
import dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData;

import javax.persistence.Index;
import javax.persistence.Table;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;

/**
 * Representation of effects in the bitemporal model for persons.
 * @see dk.magenta.datafordeler.core.database.Entity
 */
@javax.persistence.Entity
@Table(name= CprPlugin.DEBUG_TABLE_PREFIX + "cpr_person_effect", indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_person_effect_registration", columnList = "registration_id"),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_person_effect_from", columnList = "effectFrom"),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_person_effect_to", columnList = "effectTo")
})
public class PersonEffect extends CprEffect<PersonRegistration, PersonEffect, PersonBaseData> {

    public PersonEffect() {
    }

    public PersonEffect(PersonRegistration registration, OffsetDateTime effectFrom, OffsetDateTime effectTo) {
        super(registration, effectFrom, effectTo);
    }

    public PersonEffect(PersonRegistration registration, TemporalAccessor effectFrom, TemporalAccessor effectTo) {
        super(registration, effectFrom, effectTo);
    }

    public PersonEffect(PersonRegistration registration, String effectFrom, String effectTo) {
        super(registration, effectFrom, effectTo);
    }


    public PersonEffect(PersonRegistration registration, OffsetDateTime effectFrom, boolean effectFromUncertain, OffsetDateTime effectTo, boolean effectToUncertain) {
        this(registration, effectFrom, effectTo);
        this.setEffectFromUncertain(effectFromUncertain);
        this.setEffectToUncertain(effectToUncertain);
    }

}
