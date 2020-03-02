package dk.magenta.datafordeler.core.exception;

public class InvalidReferenceException extends DataFordelerException {

    private String reference;

    public InvalidReferenceException(String reference) {
        this.reference = reference;
    }

    @Override
    public String getCode() {
        return "datafordeler.import.reference_invalid";
    }

    public String getReference() {
        return this.reference;
    }
}
