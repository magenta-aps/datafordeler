package dk.magenta.datafordeler.cvr.data.participant;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.EntityReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ParticipantEntityReference extends EntityReference<ParticipantEntity, ParticipantRegistrationReference> {
    @Override
    public Class<ParticipantEntity> getEntityClass() {
        return ParticipantEntity.class;
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
    public void setRegistrations(List<ParticipantRegistrationReference> registrations) {
        this.registrationReferences = new ArrayList<ParticipantRegistrationReference>(registrations);
    }
}
