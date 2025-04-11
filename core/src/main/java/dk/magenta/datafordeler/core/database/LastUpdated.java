package dk.magenta.datafordeler.core.database;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

import static dk.magenta.datafordeler.core.database.Bitemporal.fixOffsetIn;
import static dk.magenta.datafordeler.core.database.Bitemporal.fixOffsetOut;

/**
 * A database entry for storing the timestamp of the last successful import
 * of an EntityManager. This is done so the next import can request the source
 * for changes happened after this timestamp
 */
@Entity
@Table(name = "last_updated")
public class LastUpdated extends DatabaseEntry {

    public static final String DB_FIELD_PLUGIN = "plugin";
    @Column(name = DB_FIELD_PLUGIN)
    private String plugin;

    public static final String DB_FIELD_SCHEMA_NAME = "schemaName";
    @Column(name = DB_FIELD_SCHEMA_NAME)
    private String schemaName;

    public static final String DB_FIELD_TIMESTAMP = "timestamp";
    @Column(name = DB_FIELD_TIMESTAMP, columnDefinition = "datetime2")
    private OffsetDateTime timestamp;


    public String getPlugin() {
        return this.plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    public String getSchemaName() {
        return this.schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public OffsetDateTime getTimestamp() {
        return fixOffsetOut(this.timestamp);
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = fixOffsetIn(timestamp);
    }
}
