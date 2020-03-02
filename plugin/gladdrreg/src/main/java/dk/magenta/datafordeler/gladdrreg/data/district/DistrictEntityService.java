package dk.magenta.datafordeler.gladdrreg.data.district;

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
@RequestMapping("/gladdrreg/district/1/rest")
public class DistrictEntityService extends FapiService<DistrictEntity, DistrictQuery> {

    public DistrictEntityService() {
        this.setOutputWrapper(new DistrictOutputWrapper());
    }

    @Autowired
    private GladdrregPlugin gladdregPlugin;

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public String getServiceName() {
        return "district";
    }

    @Override
    protected Class<DistrictEntity> getEntityClass() {
        return DistrictEntity.class;
    }

    @Override
    public Plugin getPlugin() {
        return this.gladdregPlugin;
    }

    @Override
    protected void checkAccess(DafoUserDetails dafoUserDetails) throws AccessDeniedException, AccessRequiredException {
    }

    @Override
    protected DistrictQuery getEmptyQuery() {
        return new DistrictQuery();
    }

}
