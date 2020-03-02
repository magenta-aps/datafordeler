package dk.magenta.datafordeler.cpr.data.residence;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.EntityReference;
import dk.magenta.datafordeler.cpr.data.person.PersonRegistrationReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ResidenceEntityReference extends EntityReference<ResidenceEntity, PersonRegistrationReference> {
    @Override
    public Class<ResidenceEntity> getEntityClass() {
        return ResidenceEntity.class;
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
    public void setRegistrations(List<PersonRegistrationReference> registrations) {
        this.registrationReferences = new ArrayList<PersonRegistrationReference>(registrations);
    }
}
