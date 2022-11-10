package dk.magenta.datafordeler.ger;

import dk.magenta.datafordeler.core.configuration.ConfigurationManager;
import dk.magenta.datafordeler.core.plugin.AreaRestrictionDefinition;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.plugin.RegisterManager;
import dk.magenta.datafordeler.core.plugin.RolesDefinition;
import dk.magenta.datafordeler.ger.configuration.GerConfigurationManager;
import dk.magenta.datafordeler.ger.data.company.CompanyEntityManager;
import dk.magenta.datafordeler.ger.data.responsible.ResponsibleEntityManager;
import dk.magenta.datafordeler.ger.data.unit.UnitEntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Datafordeler Plugin to fetch, parse and serve Ger dk.magenta.datafordeler.ger.data (dk.magenta.datafordeler.ger.data on regions, localities, roads, addresses etc.)
 * As with all plugins, it follows the model laid out in the Datafordeler Core
 * project, so it takes care of where to fetch dk.magenta.datafordeler.ger.data, how to parse it, how to
 * store it (leveraging the Datafordeler bitemporality model), under what path
 * to serve it, and which roles should exist for dk.magenta.datafordeler.ger.data access.
 * The Core and Engine take care of the generic updateRegistrationTo around these, fetching and
 * serving based on the specifics laid out in the plugin.
 */
@Component
public class GerPlugin extends Plugin {

    public static final String DEBUG_TABLE_PREFIX = "";

    @Autowired
    private GerConfigurationManager configurationManager;

    @Autowired
    private GerRegisterManager registerManager;


    @Autowired
    private CompanyEntityManager companyEntityManager;

    @Autowired
    private UnitEntityManager unitEntityManager;

    @Autowired
    private ResponsibleEntityManager responsibleEntityManager;

    private final GerRolesDefinition rolesDefinition = new GerRolesDefinition();

    private final GerAreaRestrictionDefinition areaRestrictionDefinition;

    public GerPlugin() {
        this.areaRestrictionDefinition = new GerAreaRestrictionDefinition(this);
    }

    /**
     * Plugin initialization
     */
    @PostConstruct
    public void init() {
        this.registerManager.addEntityManager(this.companyEntityManager);
        this.registerManager.addEntityManager(this.unitEntityManager);
        this.registerManager.addEntityManager(this.responsibleEntityManager);
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
        return "ger";
    }

    /**
     * Return the plugin’s register manager
     */
    @Override
    public RegisterManager getRegisterManager() {
        return this.registerManager;
    }

    /**
     * Return the plugin’s dk.magenta.datafordeler.ger.dk.magenta.datafordeler.ger.configuration manager
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
