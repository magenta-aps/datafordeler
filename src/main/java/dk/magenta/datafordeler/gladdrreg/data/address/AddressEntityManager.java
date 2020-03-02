package dk.magenta.datafordeler.gladdrreg.data.address;

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
public class AddressEntityManager extends GladdrregEntityManager {

    @Autowired
    private AddressEntityService addressEntityService;

    public AddressEntityManager() {
        this.managedEntityClass = AddressEntity.class;
        this.managedEntityReferenceClass = AddressEntityReference.class;
        this.managedRegistrationClass = AddressRegistration.class;
        this.managedRegistrationReferenceClass = AddressRegistrationReference.class;
    }

    @Override
    protected String getBaseName() {
        return "address";
    }

    @Override
    public FapiService getEntityService() {
        return this.addressEntityService;
    }

    @Override
    public String getSchema() {
        return AddressEntity.schema;
    }

    @Override
    protected RegistrationReference createRegistrationReference(URI uri) {
        return new AddressRegistrationReference(uri);
    }

}
