package dk.magenta.datafordeler.cpr.data.road;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.EntityReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RoadEntityReference extends EntityReference<RoadEntity, RoadRegistrationReference> {
    @Override
    public Class<RoadEntity> getEntityClass() {
        return RoadEntity.class;
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
    public void setRegistrations(List<RoadRegistrationReference> registrations) {
        this.registrationReferences = new ArrayList<RoadRegistrationReference>(registrations);
    }
}
