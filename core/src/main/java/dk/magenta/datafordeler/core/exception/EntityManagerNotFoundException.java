package dk.magenta.datafordeler.core.exception;

import java.net.URI;

/**
 * Exception to be thrown when attempting to find an EntityManager (e.g. from a schema),
 * and none are found
 */
public class EntityManagerNotFoundException extends DataFordelerException {
    @Override
    public String getCode() {
        return "datafordeler.plugin.entitymanager_not_found";
    }

    private String schema;
    private URI uri;

    public EntityManagerNotFoundException(String schema) {
        this.schema = schema;
    }

    public EntityManagerNotFoundException(URI uri) {
        this.uri = uri;
    }

    @Override
    public String getMessage() {
        if (this.uri != null) {
            return "EntityManager that handles URI " + this.uri + " was not found";
        } else {
            return "EntityManager that handles schema " + this.schema + " was not found";
        }
    }
}
