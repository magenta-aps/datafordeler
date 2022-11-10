package dk.magenta.datafordeler.gladdrreg.data.state;

import dk.magenta.datafordeler.core.database.Registration;

import javax.persistence.Index;
import javax.persistence.Table;
import java.time.OffsetDateTime;

/**
 * Created by lars on 16-05-17.
 */
@javax.persistence.Entity
@Table(name = "gladdrreg_state_registration", indexes = {
        @Index(name = "gladdrreg_state_entity", columnList = "entity_id"),
        @Index(name = "gladdrreg_state_registration_from", columnList = "registrationFrom"),
        @Index(name = "gladdrreg_state_registration_to", columnList = "registrationTo")
})
public class StateRegistration extends Registration<StateEntity, StateRegistration, StateEffect> {
    @Override
    protected StateEffect createEmptyEffect(OffsetDateTime effectFrom, OffsetDateTime effectTo) {
        return new StateEffect(this, effectFrom, effectTo);
    }
}
