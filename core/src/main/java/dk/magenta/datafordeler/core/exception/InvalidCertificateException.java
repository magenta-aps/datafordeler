package dk.magenta.datafordeler.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * An exception class that represents an error in processing an incoming SAML token.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidCertificateException extends DataFordelerException {

    public InvalidCertificateException(String message) {
        super(message);
    }

    public InvalidCertificateException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getCode() {
        return "datafordeler.authorization.invalid_certificate";
    }
}
