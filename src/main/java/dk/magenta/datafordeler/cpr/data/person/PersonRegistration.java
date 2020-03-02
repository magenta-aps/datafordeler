package dk.magenta.datafordeler.cpr.data.person;

import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.data.CprRegistration;

import javax.persistence.Index;
import javax.persistence.Table;
import java.time.OffsetDateTime;

/**
 * Representation of registrations in the bitemporal model for persons.
 * @see dk.magenta.datafordeler.core.database.Entity
 */
@javax.persistence.Entity
@Table(name= CprPlugin.DEBUG_TABLE_PREFIX + "cpr_person_registration", indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_person_entity", columnList = "entity_id"),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_person_registration_from", columnList = "registrationFrom"),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_person_registration_to", columnList = "registrationTo")
})
public class PersonRegistration extends CprRegistration<PersonEntity, PersonRegistration, PersonEffect> {

    @Override
    protected PersonEffect createEmptyEffect(OffsetDateTime effectFrom, OffsetDateTime effectTo) {
        return new PersonEffect(this, effectFrom, effectTo);
    }

}
