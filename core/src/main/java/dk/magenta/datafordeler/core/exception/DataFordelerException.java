package dk.magenta.datafordeler.core.exception;

/**
 * Superclass for all custom exceptions in DAFO, specifying that they must implement a getCode method
 */
public abstract class DataFordelerException extends Exception {

    public DataFordelerException() {
        super();
    }

    public DataFordelerException(String message) {
        super(message);
    }

    public DataFordelerException(Throwable cause) {
        super(cause);
    }

    public DataFordelerException(String message, Throwable cause) {
        super(message, cause);
    }

    public abstract String getCode();
}
