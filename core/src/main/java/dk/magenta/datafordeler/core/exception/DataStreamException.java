package dk.magenta.datafordeler.core.exception;

/**
 * Exception to be thrown in case of an error in handling data transfer
 */
public class DataStreamException extends DataFordelerException {

    public DataStreamException(Exception cause) {
        super(cause);
    }

    public DataStreamException(String message) {
        super(message);
    }

    public DataStreamException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getCode() {
        return "datafordeler.ioexception";
    }
}
