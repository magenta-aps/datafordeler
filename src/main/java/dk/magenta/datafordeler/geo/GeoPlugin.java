package dk.magenta.datafordeler.geo;

import dk.magenta.datafordeler.core.configuration.ConfigurationManager;
import dk.magenta.datafordeler.core.plugin.AreaRestrictionDefinition;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.plugin.RegisterManager;
import dk.magenta.datafordeler.core.plugin.RolesDefinition;
import dk.magenta.datafordeler.geo.configuration.GeoConfigurationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Datafordeler Plugin to fetch, parse and serve Geo data (data on regions, localities, roads, addresses etc.)
 * As with all plugins, it follows the model laid out in the Datafordeler Core
 * project, so it takes care of where to fetch data, how to parse it, how to 
 * store it (leveraging the Datafordeler bitemporality model), under what path 
 * to serve it, and which roles should exist for data access.
 * The Core and Engine take care of the generic updateRegistrationTo around these, fetching and
 * serving based on the specifics laid out in the plugin.
 */
@Component
public class GeoPlugin extends Plugin {

    public static final String DEBUG_TABLE_PREFIX = "";

    @Autowired
    private GeoConfigurationManager configurationManager;

    @Autowired
    private GeoRegisterManager registerManager;

    private GeoRolesDefinition rolesDefinition = new GeoRolesDefinition();

    private GeoAreaRestrictionDefinition areaRestrictionDefinition;

    public GeoPlugin() {
        this.areaRestrictionDefinition = new GeoAreaRestrictionDefinition(this);
    }

    /**
     * Plugin initialization
     */
    @PostConstruct
    public void init() {
    }

    /**
     * Return the name for the plugin, used to identify it when issuing commands
     */
    @Override
    public long getVersion() {
        return 1;
    }

    @Override
    public String getName() {
        return "geo";
    }

    /**
     * Return the plugin’s register manager
     */
    @Override
    public RegisterManager getRegisterManager() {
        return this.registerManager;
    }

    /**
     * Return the plugin’s dk.magenta.datafordeler.geo.configuration manager
     */
    @Override
    public ConfigurationManager getConfigurationManager() {
        return this.configurationManager;
    }

    /**
     * Get a definition of user roles
     */
    @Override
    public RolesDefinition getRolesDefinition() {
        return this.rolesDefinition;
    }

    @Override
    public AreaRestrictionDefinition getAreaRestrictionDefinition() {
        return this.areaRestrictionDefinition;
    }
}
