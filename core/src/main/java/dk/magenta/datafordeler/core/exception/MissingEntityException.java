package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.database.Registration;

public class MissingEntityException extends DataFordelerException {

    private final Registration registration;

    public MissingEntityException(Registration registration) {
        this.registration = registration;
    }

    public Registration getRegistration() {
        return registration;
    }

    @Override
    public String getCode() {
        return "datafordeler.import.missing_entity";
    }
}
