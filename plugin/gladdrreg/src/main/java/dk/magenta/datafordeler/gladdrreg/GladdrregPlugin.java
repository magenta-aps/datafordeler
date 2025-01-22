package dk.magenta.datafordeler.gladdrreg;

import dk.magenta.datafordeler.core.configuration.ConfigurationManager;
import dk.magenta.datafordeler.core.plugin.AreaRestrictionDefinition;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.plugin.RegisterManager;
import dk.magenta.datafordeler.core.plugin.RolesDefinition;
import dk.magenta.datafordeler.gladdrreg.configuration.GladdregConfigurationManager;
import dk.magenta.datafordeler.gladdrreg.data.address.AddressEntityManager;
import dk.magenta.datafordeler.gladdrreg.data.bnumber.BNumberEntityManager;
import dk.magenta.datafordeler.gladdrreg.data.district.DistrictEntityManager;
import dk.magenta.datafordeler.gladdrreg.data.locality.LocalityEntityManager;
import dk.magenta.datafordeler.gladdrreg.data.municipality.MunicipalityEntityManager;
import dk.magenta.datafordeler.gladdrreg.data.postalcode.PostalCodeEntityManager;
import dk.magenta.datafordeler.gladdrreg.data.road.RoadEntityManager;
import dk.magenta.datafordeler.gladdrreg.data.state.StateEntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Created by lars on 16-05-17.
 */
@Component
public class GladdrregPlugin extends Plugin {

    @Autowired
    private GladdregConfigurationManager configurationManager;

    @Autowired
    private GladdrregRegisterManager registerManager;

    @Autowired
    private MunicipalityEntityManager municipalityEntityManager;

    @Autowired
    private DistrictEntityManager districtEntityManager;

    @Autowired
    private PostalCodeEntityManager postalCodeEntityManager;

    @Autowired
    private LocalityEntityManager localityEntityManager;

    @Autowired
    private BNumberEntityManager bNumberEntityManager;

    @Autowired
    private RoadEntityManager roadEntityManager;

    @Autowired
    private AddressEntityManager addressEntityManager;

    @Autowired
    private StateEntityManager stateEntityManager;

    private GladdrregRolesDefinition rolesDefinition;

    private AreaRestrictionDefinition areaRestrictionDefinition;

    public GladdrregPlugin() {
        this.rolesDefinition = new GladdrregRolesDefinition();
        this.areaRestrictionDefinition = new GladdrregAreaRestrictionDefinition(this);
    }

    @PostConstruct
    public void init() {
        this.registerManager.addEntityManager(this.municipalityEntityManager);
        this.registerManager.addEntityManager(this.districtEntityManager);
        this.registerManager.addEntityManager(this.postalCodeEntityManager);
        this.registerManager.addEntityManager(this.localityEntityManager);
        this.registerManager.addEntityManager(this.bNumberEntityManager);
        this.registerManager.addEntityManager(this.roadEntityManager);
        this.registerManager.addEntityManager(this.addressEntityManager);
        this.registerManager.addEntityManager(this.stateEntityManager);
    }

    @Override
    public String getName() {
        return "gladdrreg";
    }

    @Override
    public RegisterManager getRegisterManager() {
        return this.registerManager;
    }

    @Override
    public ConfigurationManager getConfigurationManager() {
        return this.configurationManager;
    }

    @Override
    public RolesDefinition getRolesDefinition() {
        return this.rolesDefinition;
    }

    @Override
    public AreaRestrictionDefinition getAreaRestrictionDefinition() {
        return this.areaRestrictionDefinition;
    }
}
