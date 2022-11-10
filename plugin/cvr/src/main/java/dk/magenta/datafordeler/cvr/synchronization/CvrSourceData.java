package dk.magenta.datafordeler.cvr.synchronization;

import dk.magenta.datafordeler.core.io.PluginSourceData;

/**
 * Specific PluginSourceData implementation, holding a chunk of data from the
 * CVR source, along with an id and a schema
 */
public class CvrSourceData implements PluginSourceData {

    private final String schema;
    private final String data;
    private final String id;

    public CvrSourceData(String schema, String data, String id) {
        this.schema = schema;
        this.data = data;
        this.id = id;
    }

    @Override
    public String getSchema() {
        return this.schema;
    }

    @Override
    public String getData() {
        return this.data;
    }

    @Override
    public String getReference() {
        return null;
    }

    @Override
    public String getId() {
        return this.id;
    }
}
