package dk.magenta.datafordeler.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidParameterException extends DataFordelerException {

    public InvalidParameterException(String parameterName) {
        super("Invalid parameter '" + parameterName + "'");
    }

    @Override
    public String getCode() {
        return "datafordeler.http.invalid-parameter";
    }

}
