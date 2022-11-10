package dk.magenta.datafordeler.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class MissingParameterException extends DataFordelerException {

    public MissingParameterException(String parameterName) {
        super("Missing required parameter '" + parameterName + "'");
    }

    @Override
    public String getCode() {
        return "datafordeler.http.missing-parameter";
    }

}
