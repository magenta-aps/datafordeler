package dk.magenta.datafordeler.core.exception;

import java.net.URI;

public class PluginNotFoundException extends DataFordelerException {
    @Override
    public String getCode() {
        return "datafordeler.plugin.plugin_not_found";
    }

    private String schema;
    private String name;
    private URI uri;

    public PluginNotFoundException(String identifier, boolean isSchema) {
        if (isSchema) {
            this.schema = identifier;
        } else {
            this.name = identifier;
        }
    }

    public PluginNotFoundException(URI uri) {
        this.uri = uri;
    }

    @Override
    public String getMessage() {
        if (this.name != null) {
            return "Plugin named " + this.name + " was not found";
        } else if (this.schema != null) {
            return "Plugin that handles schema " + this.schema + " was not found";
        } else if (this.uri != null) {
            return "Plugin that handles URI " + this.uri + " was not found";
        } else {
            return "Plugin lookup on null";
        }
    }
}
