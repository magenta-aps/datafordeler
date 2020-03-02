package dk.magenta.datafordeler.gladdrreg.data.address;

import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.exception.AccessRequiredException;
import dk.magenta.datafordeler.core.fapi.FapiService;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.gladdrreg.GladdrregPlugin;
import dk.magenta.datafordeler.gladdrreg.data.GladdrregOutputWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lars on 19-05-17.
 */
@RestController
@RequestMapping("/gladdrreg/address/1/rest")
public class AddressEntityService extends FapiService<AddressEntity, AddressQuery> {

    public AddressEntityService() {
        this.setOutputWrapper(new AddressOutputWrapper());
    }

    @Autowired
    private GladdrregPlugin gladdregPlugin;

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public String getServiceName() {
        return "address";
    }

    @Override
    protected Class<AddressEntity> getEntityClass() {
        return AddressEntity.class;
    }

    @Override
    public Plugin getPlugin() {
        return this.gladdregPlugin;
    }

    @Override
    protected void checkAccess(DafoUserDetails dafoUserDetails) throws AccessDeniedException, AccessRequiredException {
    }

    @Override
    protected AddressQuery getEmptyQuery() {
        return new AddressQuery();
    }

}
