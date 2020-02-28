package dk.magenta.datafordeler.core.exception;

/**
 * Exception that should be thrown if the setup of a plugin is incorrect, meaning that the plugin will not function
 */
public class ConfigurationException extends DataFordelerException {

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(Throwable cause) {
        super(cause);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getCode() {
        return "datafordeler.configuration";
    }
}
