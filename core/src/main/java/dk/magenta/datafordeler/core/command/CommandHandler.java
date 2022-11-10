package dk.magenta.datafordeler.core.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.Engine;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Superclass for command handlers.
 */
@Component
public abstract class CommandHandler {

    @Autowired
    private Engine engine;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Logger log = LogManager.getLogger(CommandHandler.class.getCanonicalName());

    /**
     * Return the command name that this handler answers to. Must be unique among CommandHandlers; no two CommandHandler classes may return the same string
     *
     * @return
     */
    protected abstract String getHandledCommand();

    /**
     * Returns the command names that this handler answers to. Default implementation is to return getHandledCommand() wrapped in a list.
     * Subclasses will only need to implement this if they really have more than one command name
     *
     * @return
     */
    public List<String> getHandledCommands() {
        return Collections.singletonList(this.getHandledCommand());
    }

    public ObjectNode getCommandBodyAsJson(Command command) throws IOException {
        String commandBody = command.getCommandBody();
        if (commandBody != null) {
            JsonNode jsonNode = this.objectMapper.readTree(commandBody);
            if (jsonNode instanceof ObjectNode) {
                return (ObjectNode) jsonNode;
            } else {
                throw new IOException("CommandBody is not an object");
            }
        }
        return null;
    }

    public boolean accept(Command command) {
        try {
            ObjectNode commandBody = this.getCommandBodyAsJson(command);
            if (commandBody != null) {
                JsonNode targetServerNode = commandBody.get("targetServer");
                if (targetServerNode != null) {
                    String requestedServerName = targetServerNode.textValue();
                    if (requestedServerName != null) {
                        return requestedServerName.equals(this.engine.getServerName());
                    }
                }
            }
        } catch (IOException | NullPointerException e) {
            return false;
        }
        return true;
    }

    /**
     * Return a Worker subclass object that will handle the given Command. This worker runs as a thread started by CommandWatcher
     *
     * @param command
     * @return
     * @throws DataFordelerException
     */
    public abstract Worker doHandleCommand(Command command) throws DataFordelerException;

    protected Logger getLog() {
        return log;
    }

    /**
     * Given a Command object, write the output for GET and POST requests
     *
     * @param command
     * @return
     */
    public abstract JsonNode getCommandStatus(Command command);


    /**
     * Parses a command body (body of a POST request) into a CommandData subclass object.
     * Implementors must themselves define their CommandData subclass returned by this method
     *
     * @param commandBody
     * @return
     * @throws DataStreamException
     * @throws InvalidClientInputException
     */
    public abstract CommandData getCommandData(String commandBody)
            throws DataStreamException, InvalidClientInputException;
}
