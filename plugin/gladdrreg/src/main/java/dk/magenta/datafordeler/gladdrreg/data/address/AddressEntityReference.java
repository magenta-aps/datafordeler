package dk.magenta.datafordeler.gladdrreg.data.address;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.EntityReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by lars on 16-05-17.
 */
public class AddressEntityReference extends EntityReference<AddressEntity, AddressRegistrationReference> {
    @Override
    public Class<AddressEntity> getEntityClass() {
        return AddressEntity.class;
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
    public void setRegistrations(List<AddressRegistrationReference> registrations) {
        this.registrationReferences = new ArrayList<AddressRegistrationReference>(registrations);
    }
}
