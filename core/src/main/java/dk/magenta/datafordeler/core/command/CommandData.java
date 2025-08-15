package dk.magenta.datafordeler.core.command;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.StringJoiner;

/**
 * Superclass for parsed command bodies
 * A POST request to the command service should include a body, which a CommandHandler will parse into an object of this type
 */
public abstract class CommandData {

    @JsonProperty
    private String targetServer;

    public String getTargetServer() {
        return this.targetServer;
    }

    /**
     * Should determine if an incoming request matches what is needed for a CommandRole
     * The data object specifies what the CommandRole requires, and we must return whether we adhere to that
     *
     * @param data
     * @return
     */
    public abstract boolean containsAll(Map<String, Object> data);

    /**
     * Should return the contents of this object, for printing purposes
     *
     * @return
     */
    protected abstract Map<String, Object> contents();

    @Override
    public String toString() {
        StringJoiner contentJoiner = new StringJoiner(", ");
        Map<String, Object> contents = this.contents();
        for (String key : contents.keySet()) {
            contentJoiner.add(key + ": \"" + contents.get(key).toString() + "\"");
        }
        return this.getClass().getSimpleName() + " [" + contentJoiner + "]";
    }
}
