package dk.magenta.datafordeler.cvr;

import dk.magenta.datafordeler.core.configuration.ConfigurationManager;
import dk.magenta.datafordeler.core.plugin.AreaRestrictionDefinition;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.plugin.RegisterManager;
import dk.magenta.datafordeler.core.plugin.RolesDefinition;
import dk.magenta.datafordeler.cvr.access.CvrAreaRestrictionDefinition;
import dk.magenta.datafordeler.cvr.access.CvrRolesDefinition;
import dk.magenta.datafordeler.cvr.configuration.CvrConfigurationManager;
import dk.magenta.datafordeler.cvr.entitymanager.CompanyEntityManager;
import dk.magenta.datafordeler.cvr.entitymanager.CompanyUnitEntityManager;
import dk.magenta.datafordeler.cvr.entitymanager.ParticipantEntityManager;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Datafordeler Plugin to fetch, parse and serve CVR data (data on companies).
 * As with all plugins, it follows the model laid out in the Datafordeler Core
 * project, so it takes care of where to fetch data, how to parse it, how to
 * store it (leveraging the Datafordeler bitemporality model), under what path
 * to serve it, and which roles should exist for data access.
 * The Core and Engine take care of the generic glue around these, fetching and
 * serving based on the specifics laid out in the plugin.
 */
@Component
public class CvrPlugin extends Plugin {

    public static final String DEBUG_TABLE_PREFIX = "";

    @Autowired
    private CvrConfigurationManager configurationManager;

    private CvrRegisterManager registerManager;

    @Autowired
    private CompanyEntityManager companyEntityManager;

    @Autowired
    private CompanyUnitEntityManager companyUnitEntityManager;

    @Autowired
    private ParticipantEntityManager participantEntityManager;

    private final CvrRolesDefinition rolesDefinition;

    private final CvrAreaRestrictionDefinition areaRestrictionDefinition;

    @Autowired
    public void setRegisterManager(CvrRegisterManager registerManager) {
        System.out.println("Setting registermanager in plugin");
        this.registerManager = registerManager;
    }

//    @Autowired
//    public CvrPlugin(@Lazy CvrRegisterManager cvrRegisterManager, @Lazy CompanyEntityManager companyEntityManager) {
//        this.rolesDefinition = new CvrRolesDefinition();
//        this.areaRestrictionDefinition = new CvrAreaRestrictionDefinition(this);
//        this.registerManager = cvrRegisterManager;
//        this.companyEntityManager = companyEntityManager;
//    }

    public CvrPlugin() {
        System.out.println("Initializing CVR plugin");
        this.rolesDefinition = new CvrRolesDefinition();
        this.areaRestrictionDefinition = new CvrAreaRestrictionDefinition(this);

    }

    /**
     * Plugin initialization
     */
    @PostConstruct
    public void init() throws IOException {
        this.registerManager.setPlugin(this);
        this.registerManager.addEntityManager(this.companyEntityManager);
        this.registerManager.addEntityManager(this.companyUnitEntityManager);
        this.registerManager.addEntityManager(this.participantEntityManager);
    }

    /**
     * Return the name for the plugin, used to identify it when issuing commands
     */
    @Override
    public String getName() {
        return "cvr";
    }

    /**
     * Return the domain for the plugin, used in Identification objects under CVR
     */
    public static String getDomain() {
        return "cvr";
    }

    /**
     * Return the plugin’s register manager
     */
    @Override
    public RegisterManager getRegisterManager() {
        return this.registerManager;
    }


    /**
     * Return the plugin’s configuration manager
     */
    @Override
    public ConfigurationManager getConfigurationManager() {
        return this.configurationManager;
    }

    /**
     * Get a defintion of user roles
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
