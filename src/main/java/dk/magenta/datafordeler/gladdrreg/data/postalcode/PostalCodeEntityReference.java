package dk.magenta.datafordeler.gladdrreg.data.postalcode;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.EntityReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by lars on 16-05-17.
 */
public class PostalCodeEntityReference extends EntityReference<PostalCodeEntity, PostalCodeRegistrationReference> {
    @Override
    public Class<PostalCodeEntity> getEntityClass() {
        return PostalCodeEntity.class;
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
    public void setRegistrations(List<PostalCodeRegistrationReference> registrations) {
        this.registrationReferences = new ArrayList<PostalCodeRegistrationReference>(registrations);
    }
}
