package dk.magenta.datafordeler.core.configuration;

/**
 * Configuration base
 * Implementing classes should be annotated with javax.persistence.Entity, and define their configuration fields, optionally with defaults
 * There should only be one instance of a specific subclass, which holds all the data configuring the owning plugin
 */
public interface Configuration {

}
