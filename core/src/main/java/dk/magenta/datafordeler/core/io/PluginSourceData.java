package dk.magenta.datafordeler.core.io;

import java.io.Serializable;

/**
 * A generic container definition for incoming data. Plugins should implement
 * this in a class, and use it to envelop data for the Engine.handleEvent() method,
 * and accept it in their EntityManager.parseData() implementations
 */
public interface PluginSourceData extends Serializable {
    String getData();

    String getReference();

    String getSchema();

    String getId();
}
