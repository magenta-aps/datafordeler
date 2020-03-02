package dk.magenta.datafordeler.gladdrreg.data.state;

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
public class StateEntityManager extends GladdrregEntityManager {

    @Autowired
    private StateEntityService stateEntityService;

    public StateEntityManager() {
        super();
        this.managedEntityClass = StateEntity.class;
        this.managedEntityReferenceClass = StateEntityReference.class;
        this.managedRegistrationClass = StateRegistration.class;
        this.managedRegistrationReferenceClass = StateRegistrationReference.class;
    }

    @Override
    protected String getBaseName() {
        return "state";
    }

    @Override
    public FapiService getEntityService() {
        return this.stateEntityService;
    }

    @Override
    public String getSchema() {
        return StateEntity.schema;
    }

    @Override
    public RegistrationReference createRegistrationReference(URI uri) {
        return new StateRegistrationReference(uri);
    }

}
