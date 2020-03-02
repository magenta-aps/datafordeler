package dk.magenta.datafordeler.gladdrreg.data.district;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.EntityReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by lars on 16-05-17.
 */
public class DistrictEntityReference extends EntityReference<DistrictEntity, DistrictRegistrationReference> {
    @Override
    public Class<DistrictEntity> getEntityClass() {
        return DistrictEntity.class;
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
    public void setRegistrations(List<DistrictRegistrationReference> registrations) {
        this.registrationReferences = new ArrayList<DistrictRegistrationReference>(registrations);
    }
}
