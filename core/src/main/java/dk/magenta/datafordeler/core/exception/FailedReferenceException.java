package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.database.RegistrationReference;
import org.apache.http.StatusLine;

/**
 * Exception to be thrown when a reference doesn't lead to a registration
 */
public class FailedReferenceException extends HttpStatusException {

    private final RegistrationReference reference;

    public FailedReferenceException(RegistrationReference reference, StatusLine statusLine) {
        super(statusLine);
        this.reference = reference;
    }

    public FailedReferenceException(RegistrationReference reference, HttpStatusException cause) {
        super(cause.getStatusLine(), cause.getUri());
        this.reference = reference;
        this.initCause(cause);
    }

    public RegistrationReference getReference() {
        return this.reference;
    }

    public String getCode() {
        return "datafordeler.import.reference_failed";
    }
}
