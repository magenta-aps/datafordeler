package dk.magenta.datafordeler.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception to be thrown when a resource cannot be found and a HTTP 404 should be sent to
 * the user
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class HttpNotFoundException extends DataFordelerException {

    public HttpNotFoundException(String message) {
        super(message);
    }

    @Override
    public String getCode() {
        return "datafordeler.http.not-found";
    }

    public String reason() {
        return "{\"errorMessage\":\""+super.getMessage()+"\"}";
    }

}
