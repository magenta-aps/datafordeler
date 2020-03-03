package dk.magenta.datafordeler.gladdrreg.data.bnumber;

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
public class BNumberEntityManager extends GladdrregEntityManager {

    @Autowired
    private BNumberEntityService bNumberEntityService;

    public BNumberEntityManager() {
        this.managedEntityClass = BNumberEntity.class;
        this.managedEntityReferenceClass = BNumberEntityReference.class;
        this.managedRegistrationClass = BNumberRegistration.class;
        this.managedRegistrationReferenceClass = BNumberRegistrationReference.class;
    }

    @Override
    protected String getBaseName() {
        return "bnumber";
    }

    @Override
    public FapiService getEntityService() {
        return this.bNumberEntityService;
    }

    @Override
    public String getSchema() {
        return BNumberEntity.schema;
    }

    @Override
    protected RegistrationReference createRegistrationReference(URI uri) {
        return new BNumberRegistrationReference(uri);
    }

}
