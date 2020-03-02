package dk.magenta.datafordeler.cpr.data.road;

import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.data.CprRegistration;

import javax.persistence.Index;
import javax.persistence.Table;
import java.time.OffsetDateTime;

/**
 * Representation of registrations in the bitemporal model for roads.
 * @see dk.magenta.datafordeler.core.database.Entity
 */
@javax.persistence.Entity
@Table(name= "cpr_road_registration", indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_road_entity", columnList = "entity_id"),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_road_registration_from", columnList = "registrationFrom"),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_road_registration_to", columnList = "registrationTo")
})
public class RoadRegistration extends CprRegistration<RoadEntity, RoadRegistration, RoadEffect> {

    @Override
    protected RoadEffect createEmptyEffect(OffsetDateTime effectFrom, OffsetDateTime effectTo) {
        return new RoadEffect(this, effectFrom, effectTo);
    }

}
