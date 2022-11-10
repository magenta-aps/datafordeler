package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.database.Registration;

public class SkippedSequenceNumberException extends InvalidDataInputException {

    private final Registration registration;
    private final int highestSequenceNumber;

    public SkippedSequenceNumberException(Registration newRegistration, int highestSequenceNumber) {
        super("Sequencenumber " + newRegistration.getSequenceNumber() + " not matching existing highest sequencenumber " + highestSequenceNumber + "; must be exactly one higher");
        this.registration = newRegistration;
        this.highestSequenceNumber = highestSequenceNumber;
    }

    public Registration getRegistration() {
        return this.registration;
    }

    public int getHighestSequenceNumber() {
        return this.highestSequenceNumber;
    }

    @Override
    public String getCode() {
        return "datafordeler.import.skipped_sequence_number";
    }
}
