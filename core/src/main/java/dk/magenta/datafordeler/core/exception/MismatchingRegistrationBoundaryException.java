package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.database.Registration;

import java.time.format.DateTimeFormatter;

public class MismatchingRegistrationBoundaryException extends InvalidDataInputException {

    private final Registration newRegistration;
    private final Registration existingRegistration;

    public MismatchingRegistrationBoundaryException(Registration newRegistration, Registration existingRegistration) {
        super("Mismatching timestamps; incoming registration " + newRegistration.getRegisterChecksum() +
                " with registrationFromBefore " + newRegistration.getRegistrationFrom().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) +
                " does not match existing registration " + existingRegistration.getRegisterChecksum() + " with registrationToBefore " +
                existingRegistration.getRegistrationTo().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        this.newRegistration = newRegistration;
        this.existingRegistration = existingRegistration;
    }

    public Registration getNewRegistration() {
        return newRegistration;
    }

    public Registration getExistingRegistration() {
        return existingRegistration;
    }

    @Override
    public String getCode() {
        return "datafordeler.import.registration_boundary_mismatch";
    }
}
