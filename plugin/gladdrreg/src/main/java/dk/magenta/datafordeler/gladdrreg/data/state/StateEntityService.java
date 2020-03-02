package dk.magenta.datafordeler.gladdrreg.data.state;

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
@RequestMapping("/gladdrreg/state/1/rest")
public class StateEntityService extends FapiService<StateEntity, StateQuery> {

    public StateEntityService() {
        this.setOutputWrapper(new StateOutputWrapper());
    }

    @Autowired
    private GladdrregPlugin gladdregPlugin;

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public String getServiceName() {
        return "state";
    }

    @Override
    protected Class<StateEntity> getEntityClass() {
        return StateEntity.class;
    }

    @Override
    public Plugin getPlugin() {
        return this.gladdregPlugin;
    }

    @Override
    protected void checkAccess(DafoUserDetails dafoUserDetails) throws AccessDeniedException, AccessRequiredException {
    }

    @Override
    protected StateQuery getEmptyQuery() {
        return new StateQuery();
    }

}
