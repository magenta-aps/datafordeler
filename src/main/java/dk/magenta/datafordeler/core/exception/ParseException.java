package dk.magenta.datafordeler.core.exception;

public class ParseException extends InvalidDataInputException {

    public ParseException(String msg) {
        super(msg);
    }

    public ParseException(String msg, Throwable cause) {
        super(msg);
        this.initCause(cause);
    }

    @Override
    public String getCode() {
        return "datafordeler.import.parse_error";
    }
}
