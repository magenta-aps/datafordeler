package dk.magenta.datafordeler.gladdrreg.data.state;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.EntityReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by lars on 16-05-17.
 */
public class StateEntityReference extends EntityReference<StateEntity, StateRegistrationReference> {
    @Override
    public Class<StateEntity> getEntityClass() {
        return StateEntity.class;
    }

    @JsonProperty
    private String type;

    public String getType() {
        return this.type;
    }

    @JsonProperty("objectID")
    public void setObjectId(UUID objectId) {
        this.objectId = objectId;
    }

    @JsonProperty("registreringer")
    public void setRegistrations(List<StateRegistrationReference> registrations) {
        this.registrationReferences = new ArrayList<StateRegistrationReference>(registrations);
    }
}
