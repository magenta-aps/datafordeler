package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.database.Registration;

/**
 * Exception to be thrown when two registrations on the same entity has the same sequence number
 */
public class DuplicateSequenceNumberException extends InvalidDataInputException {

    private final Registration existingRegistration;
    private final Registration newRegistration;

    public DuplicateSequenceNumberException(Registration newRegistration, Registration existingRegistration) {
        super("Duplicate sequencenumber " + newRegistration.getSequenceNumber() + ", shared between existing registration " + existingRegistration.getRegisterChecksum() + " and new registration " + newRegistration.getRegisterChecksum());
        this.newRegistration = newRegistration;
        this.existingRegistration = existingRegistration;
    }

    public Registration getExistingRegistration() {
        return this.existingRegistration;
    }

    public Registration getNewRegistration() {
        return this.newRegistration;
    }

    @Override
    public String getCode() {
        return "datafordeler.import.duplicate_sequence_number";
    }
}
