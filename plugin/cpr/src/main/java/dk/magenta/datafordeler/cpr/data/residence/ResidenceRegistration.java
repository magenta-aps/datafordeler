package dk.magenta.datafordeler.cpr.data.residence;

import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.data.CprRegistration;

import javax.persistence.Index;
import javax.persistence.Table;
import java.time.OffsetDateTime;

/**
 * Representation of registrations in the bitemporal model for residences.
 *
 * @see dk.magenta.datafordeler.core.database.Entity
 */
@javax.persistence.Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_residence_registration", indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_residence_entity", columnList = "entity_id"),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_residence_registration_from", columnList = ResidenceRegistration.DB_FIELD_REGISTRATION_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_residence_registration_to", columnList = ResidenceRegistration.DB_FIELD_REGISTRATION_TO)
})
public class ResidenceRegistration extends CprRegistration<ResidenceEntity, ResidenceRegistration, ResidenceEffect> {

    @Override
    protected ResidenceEffect createEmptyEffect(OffsetDateTime effectFrom, OffsetDateTime effectTo) {
        return new ResidenceEffect(this, effectFrom, effectTo);
    }

}
