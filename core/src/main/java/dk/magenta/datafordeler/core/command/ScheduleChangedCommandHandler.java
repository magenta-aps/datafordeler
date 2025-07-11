package dk.magenta.datafordeler.core.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.Engine;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A CommandHandler for executing pulls. The command interface delegates
 * to this when it receives a "pull" command
 */
@Component
public class ScheduleChangedCommandHandler extends CommandHandler {

    private enum ScheduleType {
        DUMP,
        PULL;

        static ScheduleType forData(ScheduleChangedCommandData data) {
            switch (data.table) {
                case "dump_config":
                    return DUMP;

                case "cpr_config":
                case "cvr_config":
                    return PULL;

                default:
                    return null;
            }
        }
    }

    public static class ScheduleChangedCommandData extends CommandData {

        public ScheduleChangedCommandData() {
        }

        @JsonProperty(required = true)
        public String table;

        @JsonProperty(required = true)
        public String id;

        @JsonProperty(required = true)
        public List<String> fields;

        @Override
        public boolean containsAll(Map<String, Object> data) {
            return data.keySet().containsAll(Arrays.asList(
                    "table", "id", "fields"
            ));
        }

        @Override
        public Map<String, Object> contents() {
            Map<String, Object> map = new HashMap<>();
            map.put("table", this.table);
            map.put("id", this.id);
            map.put("fields", this.fields);
            return map;
        }
    }

    @Autowired
    private Engine engine;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected String getHandledCommand() {
        return "schedule-changed";
    }

    public boolean accept(Command command) {
        if (command != null && command.getCommandBody() != null) {
            try {
                ScheduleType scheduleType = ScheduleType.forData(this.getCommandData(command.getCommandBody()));
                if (scheduleType != null) {
                    switch (scheduleType) {
                        case DUMP:
                            return engine.isDumpEnabled();

                        case PULL:
                            return engine.isPullEnabled();
                    }
                }
            } catch (InvalidClientInputException e) {
                getLog().warn("failed to determine acceptance", e);
            }
        }
        return false;
    }

    @Override
    public Worker doHandleCommand(Command command)
            throws DataFordelerException {
        this.getLog()
                .info("Handling command '" + command.getCommandName() + "'");

        final ScheduleChangedCommandData data =
                this.getCommandData(command.getCommandBody());

        Worker worker = new Worker() {
            @Override
            public void run() {
                // TODO: be more granular rather than reloading everything
                switch (ScheduleType.forData(data)) {
                    case DUMP:
                        engine.setupDumpSchedules();
                        return;

                    case PULL:
                        engine.setupPullSchedules();
                        return;
                }
            }
        };
        worker.setUncaughtExceptionHandler(
                (th, exc) -> this.getLog().error("Reschedule failed", exc)
        );

        return worker;
    }

    public ScheduleChangedCommandData getCommandData(String commandBody)
            throws InvalidClientInputException {
        try {
            return this.objectMapper.readValue(
                    commandBody, ScheduleChangedCommandData.class
            );
        } catch (IOException e) {
            InvalidClientInputException wrapped =
                    new InvalidClientInputException(
                            "Unable to parse command data '" + commandBody + "'", e
                    );
            this.getLog().error(e);
            throw wrapped;
        }
    }

    public ObjectNode getCommandStatus(Command command) {
        return objectMapper.valueToTree(command);
    }
}
