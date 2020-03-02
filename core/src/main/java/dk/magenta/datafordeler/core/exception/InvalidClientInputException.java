package dk.magenta.datafordeler.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidClientInputException extends DataFordelerException {
    public InvalidClientInputException(String message) {
        super(message);
    }

    public InvalidClientInputException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getCode() {
        return "datafordeler.http.invalid-client-input";
    }
}
