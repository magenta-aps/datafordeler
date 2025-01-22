package dk.magenta.datafordeler.gladdrreg.data.state;

import dk.magenta.datafordeler.core.database.Effect;

import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/**
 * Created by lars on 16-05-17.
 */
@javax.persistence.Entity
@Table(name = "gladdrreg_state_effect")
public class StateEffect extends Effect<StateRegistration, StateEffect, StateData> {
    public StateEffect() {
    }

    public StateEffect(StateRegistration registration, OffsetDateTime effectFrom, OffsetDateTime effectTo) {
        super(registration, effectFrom, effectTo);
    }
}
