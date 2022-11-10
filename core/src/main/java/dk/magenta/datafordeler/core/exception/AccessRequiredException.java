package dk.magenta.datafordeler.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AccessRequiredException extends DataFordelerException {

    public AccessRequiredException(String message) {
        super(message);
    }

    @Override
    public String getCode() {
        return "datafordeler.accessrequired";
    }

}
