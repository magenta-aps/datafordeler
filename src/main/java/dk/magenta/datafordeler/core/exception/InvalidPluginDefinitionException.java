package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.plugin.Plugin;

/**
 * Created by lars on 20-04-17.
 */
public class InvalidPluginDefinitionException extends DataFordelerException {

    private Plugin plugin;

    public InvalidPluginDefinitionException(Plugin plugin, String message) {
        super("Plugin " + plugin.getClass().getCanonicalName() + " is incorrectly defined: " + message);
        this.plugin = plugin;
    }

    @Override
    public String getCode() {
        return "datafordeler.plugin.invalid_plugin_definition";
    }

    public Plugin getPlugin() {
        return this.plugin;
    }
}
