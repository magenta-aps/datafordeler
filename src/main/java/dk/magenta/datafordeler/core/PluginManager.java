package dk.magenta.datafordeler.core;

import dk.magenta.datafordeler.core.plugin.Plugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by lars on 12-01-17.
 */

@Component()
public class PluginManager {

    @Autowired(required = false)
    private List<Plugin> plugins;

    private HashMap<String, Plugin> pluginsByURISubstring = new HashMap<>();

    private HashMap<String, Plugin> pluginsByName= new HashMap<>();

    private Logger log = LogManager.getLogger(PluginManager.class);

    private List<PluginManagerCallbackHandler> postConstructCallbackHandlers = new ArrayList<>();

    private boolean afterPostContruct = false;

    public PluginManager() {
    }

    /**
     * Run bean initialization
     * Populate pluginsByName for lookup by plugin name
     * Populate pluginsByURISubstring for lookup by URI substring
     */
    @PostConstruct
    public void init() {
        if (this.plugins == null) {
            this.plugins = new ArrayList<>();
        }
        for (Plugin plugin : this.plugins) {
            this.log.info("Found plugin " + plugin.getName());
            this.pluginsByName.put(plugin.getName(), plugin);
            for (String domain : plugin.getHandledURISubstrings()) {
                this.pluginsByURISubstring.put(domain, plugin);
            }
            plugin.setPluginManager(this);
        }
        for (PluginManagerCallbackHandler handler : postConstructCallbackHandlers) {
            handler.executePluginManagerCallback(this);
        }
        afterPostContruct = true;
    }

    public void addPostConstructCallBackHandler(PluginManagerCallbackHandler handler) {
        postConstructCallbackHandlers.add(handler);
        // If this gets called after @PostConstruct handler has been called, just execute
        // the handler straight away.
        if (afterPostContruct) {
            handler.executePluginManagerCallback(this);
        }
    }

    /**
     * Get all detected plugins (Subclasses of Plugin that are Beans and present in the classpath)
     * @return List of Plugins
     */
    public List<Plugin> getPlugins() {
        return plugins;
    }

    /**
     * Find the plugin that handles the defined schema
     * @param schema Schema name to search by
     * @return Found Plugin, or null if none found
     */
    public Plugin getPluginForSchema(String schema) {
        for (Plugin plugin : this.plugins) {
            if (plugin.handlesSchema(schema)) {
                return plugin;
            }
        }
        return null;
    }

    /**
     * Find the plugin that handles the defined URI
     * @param uri URI to search by. A plugin handles this URI if the uri begins with one of the plugin's handledUriSubstrings
     * @return Found Plugin, or null if none found
     */
    public Plugin getPluginForURI(URI uri) {
        for (String substring : this.pluginsByURISubstring.keySet()) {
            if (uri.toString().startsWith(substring)) {
                return this.pluginsByURISubstring.get(substring);
            }
        }
        return null;
    }

    /**
     * Find a plugin by name
     * @param name Plugin name to search for. Must be an exact match
     * @return Found Plugin, or null if none found
     */
    public Plugin getPluginByName(String name) {
        return this.pluginsByName.get(name);
    }

    public void addPluginURISubstring(Plugin plugin, String substring) {
        this.pluginsByURISubstring.put(substring, plugin);
    }
    public void removePluginURISubstring(Plugin plugin, String substring) {
        this.pluginsByURISubstring.remove(substring, plugin);
    }
}
