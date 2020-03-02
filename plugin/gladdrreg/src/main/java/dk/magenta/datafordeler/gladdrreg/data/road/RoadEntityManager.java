package dk.magenta.datafordeler.gladdrreg.data.road;

import dk.magenta.datafordeler.core.database.RegistrationReference;
import dk.magenta.datafordeler.core.fapi.FapiService;
import dk.magenta.datafordeler.gladdrreg.data.GladdrregEntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;

/**
 * Created by lars on 16-05-17.
 */
@Component("gladdrregRoadEntityManager")
public class RoadEntityManager extends GladdrregEntityManager {

    @Autowired
    private RoadEntityService roadEntityService;

    public RoadEntityManager() {
        this.managedEntityClass = RoadEntity.class;
        this.managedEntityReferenceClass = RoadEntityReference.class;
        this.managedRegistrationClass = RoadRegistration.class;
        this.managedRegistrationReferenceClass = RoadRegistrationReference.class;
    }

    @Override
    protected String getBaseName() {
        return "road";
    }

    @Override
    public FapiService getEntityService() {
        return this.roadEntityService;
    }

    @Override
    public String getSchema() {
        return RoadEntity.schema;
    }

    @Override
    protected RegistrationReference createRegistrationReference(URI uri) {
        return new RoadRegistrationReference(uri);
    }


}
