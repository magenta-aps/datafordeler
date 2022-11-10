package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.PluginManager;
import dk.magenta.datafordeler.core.configuration.ConfigurationManager;
import dk.magenta.datafordeler.core.fapi.BaseQuery;

import java.net.URI;
import java.util.*;

/**
 * Base class for a plugin. Defines which methods must be implemented.
 * A Plugin to DAFO is a separate JAR, drawing on and subclassing Core classes.
 * Upon compilation, the jar is stored in a special folder (configured by dafo.plugins.folder)
 * and included in the classpath by the Application class.
 */
public abstract class Plugin {

    protected long version = 1L;

    private PluginManager pluginManager;

    public Plugin() {
    }

    public long getVersion() {
        return version;
    }

    /**
     * Get the unique name for the plugin
     *
     * @return
     */
    public abstract String getName();

    /**
     * Get the plugin's RegisterManager implementation
     *
     * @return
     */
    public abstract RegisterManager getRegisterManager();

    /**
     * Returns whether this plugin can handle input of the given schema
     *
     * @param schema
     * @return
     */
    public boolean handlesSchema(String schema) {
        RegisterManager registerManager = this.getRegisterManager();
        return registerManager != null && registerManager.handlesSchema(schema);
    }

    /**
     * Get the plugin's EntityManager implementation for the given schema
     *
     * @return
     */
    public final EntityManager getEntityManager(String schema) {
        RegisterManager registerManager = this.getRegisterManager();
        return registerManager != null ? registerManager.getEntityManager(schema) : null;
    }

    /**
     * Get the plugin's EntityManager implementation for the given URI
     *
     * @return
     */
    public final EntityManager getEntityManager(URI uri) {
        RegisterManager registerManager = this.getRegisterManager();
        return registerManager != null ? registerManager.getEntityManager(uri) : null;
    }

    /**
     * Get the plugin's ConfigurationManager implementation
     *
     * @return
     */
    public abstract ConfigurationManager getConfigurationManager();

    public final Collection<String> getHandledURISubstrings() {
        RegisterManager registerManager = this.getRegisterManager();
        return registerManager != null ? registerManager.getHandledURISubstrings() : Collections.emptyList();
    }

    public abstract RolesDefinition getRolesDefinition();

    public abstract AreaRestrictionDefinition getAreaRestrictionDefinition();

    public boolean isDemo() {
        return false;
    }


    public final String getServiceOwner() {
        return this.getName().toLowerCase();
    }

    public final void setPluginManager(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    public final PluginManager getPluginManager() {
        return this.pluginManager;
    }

    public String getJoinString(Map<String, String> handles) {
        return "";
    }

    public LinkedHashMap<String, Class> getJoinClassAliases(Collection<String> handles) {
        return new LinkedHashMap<>();
    }

    public List<BaseQuery> getQueries(Map<String, String> values) {
        return Collections.emptyList();
    }


}
