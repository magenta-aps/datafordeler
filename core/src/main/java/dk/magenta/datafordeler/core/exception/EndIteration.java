package dk.magenta.datafordeler.core.exception;

public class EndIteration extends Exception {

    private String reason;
    public EndIteration(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
