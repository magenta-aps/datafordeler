package dk.magenta.datafordeler.cpr.synchronization;

import dk.magenta.datafordeler.core.io.PluginSourceData;

public class CprSourceData implements PluginSourceData {

    private String schema;
    private String data;
    private String id;

    public CprSourceData(String schema, String data, String id) {
        this.schema = schema;
        this.data = data;
        this.id = id;
    }

    @Override
    public String getSchema() {
        return this.schema;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getData() {
        return this.data;
    }

    @Override
    public String getReference() {
        return null;
    }

}
