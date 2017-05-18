package dk.magenta.datafordeler.gladdreg.municipality;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.EntityReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by lars on 16-05-17.
 */
public class MunicipalityEntityReference extends EntityReference<MunicipalityEntity, MunicipalityRegistrationReference> {
    @Override
    public Class<MunicipalityEntity> getEntityClass() {
        return MunicipalityEntity.class;
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
    public void setRegistrations(List<MunicipalityRegistrationReference> registrations) {
        this.registrationReferences = new ArrayList<MunicipalityRegistrationReference>(registrations);
    }
}
