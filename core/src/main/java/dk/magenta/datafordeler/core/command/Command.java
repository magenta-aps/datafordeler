package dk.magenta.datafordeler.core.command;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.configuration.Configuration;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.migration.MigrateModel;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Command descriptor, holding data about an issued command
 */
@Entity
@Table(name = "command", indexes = {@Index(name = "status", columnList = "status")})
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class Command extends DatabaseEntry implements Configuration, MigrateModel {

    public enum Status {
        QUEUED(0),
        PROCESSING(1),
        SUCCESS(2),
        FAILED(3),
        CANCEL(4),
        CANCELLED(5);

        private final int value;

        Status(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

    @Column(nullable = false)
    @JsonIgnore
    private String issuer;

    @Column(nullable = true, columnDefinition = "datetime2")
    private OffsetDateTime received;

    @Column(nullable = true)
    private OffsetDateTime receivedNew;

    @Column(nullable = true, columnDefinition = "datetime2")
    private OffsetDateTime handled;

    @Column(nullable = true)
    private OffsetDateTime handledNew;

    @Column(nullable = true)
    @JsonIgnore
    private Status status;

    @Column(nullable = true, length = 2048)
    private String errorMessage;

    @Column(nullable = false)
    private String commandName;

    @Column(nullable = true)
    private String commandBody;

    public Command() {
    }

    public Command(String commandName) {
        this.setCommandName(commandName);
    }

    /**
     * Create a Command object from a http request, a user and a command name
     * The request is used to obtain the command body (ie. the request body), to be parsed by a CommandHandler
     * The user object is used to set the command issuer
     * Also sets the received time
     *
     * @param request
     * @param userDetails
     * @param commandName
     * @return
     * @throws IOException
     */
    public static Command fromRequest(HttpServletRequest request, DafoUserDetails userDetails, String commandName) throws IOException {
        if (commandName.startsWith("/")) {
            commandName = commandName.substring(1);
        }
        Command command = new Command(commandName);
        ObjectMapper objectMapper = new ObjectMapper();
        String commandBody = objectMapper.writeValueAsString(
                objectMapper.readTree(
                        request.getInputStream()
                )
        );
        command.setCommandBody(commandBody);
        command.setReceived();
        command.setIssuer(userDetails.getIdentity());
        return command;
    }

    @JsonProperty(value = "id")
    public Long getIdentification() {
        // If this method was named "getId", Jackson would ignore this in favor of superclass getId
        return super.getId();
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public OffsetDateTime getReceived() {
        return received;
    }

    public void setReceived() {
        this.received = this.receivedNew = OffsetDateTime.now();
    }

    public OffsetDateTime getHandled() {
        return handled;
    }

    public void setHandled() {
        this.handled = this.handledNew = OffsetDateTime.now();
    }

    public Status getStatus() {
        return this.status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @JsonIgnore
    public boolean done() {
        return this.status == Command.Status.SUCCESS || this.status == Command.Status.CANCELLED || this.status == Command.Status.FAILED;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public String getCommandBody() {
        return commandBody;
    }

    public void setCommandBody(String commandBody) {
        this.commandBody = commandBody;
    }

    @JsonProperty(value = "status")
    public String getStatusName() {
        if (this.status != null) {
            switch (this.status) {
                case QUEUED:
                    return "queued";
                case PROCESSING:
                    return "running";
                case SUCCESS:
                    return "successful";
                case FAILED:
                    return "failure";
                case CANCELLED:
                    return "cancelled";
            }
        }
        return "running";
    }


    public void updateTimestamp() {
        this.receivedNew = this.getReceived();
        this.handledNew = this.getHandled();
    }

    public static List<String> updateFields() {
        return Arrays.asList("received", "handled");
    }

}
