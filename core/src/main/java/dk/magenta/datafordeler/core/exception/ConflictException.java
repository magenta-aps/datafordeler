package dk.magenta.datafordeler.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * An exception class that tells the user that the operation could not succeed due to a data conflict
 * such as trying to create something that already exists
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends DataFordelerException {

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getCode() {
        return "datafordeler.conflict";
    }
}
