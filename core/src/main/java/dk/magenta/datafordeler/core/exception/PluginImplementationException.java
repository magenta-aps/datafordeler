package dk.magenta.datafordeler.core.exception;

public class PluginImplementationException extends DataFordelerException {

    public PluginImplementationException(String message) {
        super(message);
    }

    public PluginImplementationException(String messsage, Throwable cause) {
        super(messsage, cause);
    }

    @Override
    public String getCode() {
        return "datafordeler.plugin.implementation_error";
    }
}
