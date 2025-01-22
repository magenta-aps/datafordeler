package dk.magenta.datafordeler.gladdrreg.data.state;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dk.magenta.datafordeler.core.database.Entity;

import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * Created by lars on 16-05-17.
 */
@Entity
@Table(name = "gladdrreg_state_entity", indexes = {
        @Index(name = "gladdrreg_state_identification", columnList = "identification_id")
})
public class StateEntity extends Entity<StateEntity, StateRegistration> {

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
    public static final String schema = "State";

    @Override
    protected StateRegistration createEmptyRegistration() {
        return new StateRegistration();
    }
}
