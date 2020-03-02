package dk.magenta.datafordeler.gladdrreg.data.bnumber;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.EntityReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by lars on 16-05-17.
 */
public class BNumberEntityReference extends EntityReference<BNumberEntity, BNumberRegistrationReference> {
    @Override
    public Class<BNumberEntity> getEntityClass() {
        return BNumberEntity.class;
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
    public void setRegistrations(List<BNumberRegistrationReference> registrations) {
        this.registrationReferences = new ArrayList<BNumberRegistrationReference>(registrations);
    }
}
