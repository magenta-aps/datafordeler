package dk.magenta.datafordeler.gladdrreg.data.municipality;

import dk.magenta.datafordeler.core.database.RegistrationReference;
import dk.magenta.datafordeler.core.fapi.FapiService;
import dk.magenta.datafordeler.gladdrreg.data.GladdrregEntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;

/**
 * Created by lars on 16-05-17.
 */
@Component
public class MunicipalityEntityManager extends GladdrregEntityManager {

    @Autowired
    private MunicipalityEntityService municipalityEntityService;

    public MunicipalityEntityManager() {
        this.managedEntityClass = MunicipalityEntity.class;
        this.managedEntityReferenceClass = MunicipalityEntityReference.class;
        this.managedRegistrationClass = MunicipalityRegistration.class;
        this.managedRegistrationReferenceClass = MunicipalityRegistrationReference.class;
    }

    @Override
    protected String getBaseName() {
        return "municipality";
    }

    @Override
    public FapiService getEntityService() {
        return this.municipalityEntityService;
    }

    @Override
    public String getSchema() {
        return MunicipalityEntity.schema;
    }

    @Override
    protected RegistrationReference createRegistrationReference(URI uri) {
        return new MunicipalityRegistrationReference(uri);
    }

}
