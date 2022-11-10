package dk.magenta.datafordeler.core.command;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.Engine;
import dk.magenta.datafordeler.core.PluginManager;
import dk.magenta.datafordeler.core.Pull;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.exception.PluginNotFoundException;
import dk.magenta.datafordeler.core.plugin.Plugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * A CommandHandler for executing pulls. The command interface delegates to this
 * when it receives a "pull" command
 */
@Component
public class PullCommandHandler extends CommandHandler {

    public static class PullCommandData extends CommandData {

        public PullCommandData() {
        }

        @JsonProperty(required = true)
        public String plugin;

        private final ObjectNode data = new ObjectMapper().createObjectNode();

        @JsonAnySetter
        public void setData(String key, JsonNode value) {
            this.data.set(key, value);
        }

        public ObjectNode getData() {
            return this.data;
        }

        @Override
        public boolean containsAll(Map<String, Object> data) {
            for (String key : data.keySet()) {
                if (key.equals("plugin") && this.plugin != null && this.plugin.equals(data.get("plugin"))) {
                    // Ok for now
                } else {
                    // This must not happen. It means there is an important difference between the incoming map and this object
                    return false;
                }
            }
            return true;
        }

        @Override
        public Map<String, Object> contents() {
            return Collections.singletonMap("plugin", this.plugin);
        }
    }

    @Autowired
    private Engine engine;

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected String getHandledCommand() {
        return "pull";
    }

    public boolean accept(Command command) {
        return super.accept(command) && this.engine.isPullEnabled();
    }

    @Override
    public Worker doHandleCommand(Command command) throws DataFordelerException {
        if (this.accept(command)) {
            this.getLog().info("Handling command '" + command.getCommandName() + "'");

            PullCommandData commandData = this.getCommandData(command.getCommandBody());
            Plugin plugin = this.getPlugin(commandData);
            this.getLog().info("Pulling with plugin " + plugin.getClass().getCanonicalName());

            Pull pull = new Pull(engine, plugin, commandData.getData());
            pull.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                public void uncaughtException(Thread th, Throwable ex) {
                    PullCommandHandler.this.getLog().error("Pull failed", ex);
                }
            });
            return pull;
        }
        return null;
    }

    public PullCommandData getCommandData(String commandBody)
            throws DataStreamException, InvalidClientInputException {
        try {
            PullCommandData commandData = this.objectMapper.readValue(commandBody, PullCommandData.class);
            this.getLog().info("Command data parsed");
            return commandData;
        } catch (IOException e) {
            InvalidClientInputException ex = new InvalidClientInputException("Unable to parse command data '" + commandBody + "'");
            this.getLog().error(ex);
            throw ex;
        }
    }

    private Plugin getPlugin(PullCommandData commandData) throws PluginNotFoundException {
        Plugin plugin = pluginManager.getPluginByName(commandData.plugin);
        if (plugin == null) {
            this.getLog().error("Couldn't find requested plugin '" + commandData.plugin + "'");
            throw new PluginNotFoundException(commandData.plugin, false);
        }
        return plugin;
    }

    public ObjectNode getCommandStatus(Command command) {
        return objectMapper.valueToTree(command);
    }
}
