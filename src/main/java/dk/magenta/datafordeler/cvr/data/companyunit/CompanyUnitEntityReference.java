package dk.magenta.datafordeler.cvr.data.companyunit;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.EntityReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CompanyUnitEntityReference extends EntityReference<CompanyUnitEntity, CompanyUnitRegistrationReference> {
    @Override
    public Class<CompanyUnitEntity> getEntityClass() {
        return CompanyUnitEntity.class;
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
    public void setRegistrations(List<CompanyUnitRegistrationReference> registrations) {
        this.registrationReferences = new ArrayList<CompanyUnitRegistrationReference>(registrations);
    }
}
