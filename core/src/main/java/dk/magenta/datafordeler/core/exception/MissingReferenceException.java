package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.io.PluginSourceData;

public class MissingReferenceException extends DataFordelerException {

    private final PluginSourceData event;

    public MissingReferenceException(PluginSourceData event) {
        super();
        this.event = event;
    }

    public PluginSourceData getEvent() {
        return event;
    }

    @Override
    public String getCode() {
        return "datafordeler.import.missing_reference";
    }
}
