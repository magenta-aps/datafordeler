package dk.magenta.datafordeler.core.io;

import dk.magenta.datafordeler.core.exception.DataFordelerException;

import java.time.OffsetDateTime;

public class Receipt {

    public enum Status {
        ok,
        failed
    }

    protected String eventID;
    protected Status status;
    protected OffsetDateTime received;
    protected String errorCode;
    protected String errorMessage;

    public Receipt(String eventID, OffsetDateTime received) {
        this.eventID = eventID;
        this.status = Status.ok;
        this.received = received;
    }

    public Receipt(String eventID, OffsetDateTime received, DataFordelerException e) {
        this(eventID, received);
        this.status = Status.failed;
        this.errorCode = e.getCode();
        this.errorMessage = e.getMessage();
    }

    public String getEventID() {
        return eventID;
    }

    public Status getStatus() {
        return status;
    }

    public OffsetDateTime getReceived() {
        return received;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
