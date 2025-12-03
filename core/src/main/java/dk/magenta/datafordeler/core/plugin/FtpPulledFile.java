package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.migration.MigrateModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.List;

import static dk.magenta.datafordeler.core.database.Bitemporal.fixOffsetIn;

@Entity
@Table(name = "ftp_pulled_file", indexes = {
        @Index(name = "ftp_pulled_file_filename", columnList = FtpPulledFile.DB_FIELD_FILENAME, unique = true),
        @Index(name = "ftp_pulled_file_type", columnList = FtpPulledFile.DB_FIELD_TYPE),
})
public class FtpPulledFile extends DatabaseEntry implements MigrateModel {

    public static final String DB_FIELD_TYPE = "type";
    public static final String DB_FIELD_FILENAME = "filename";
    public static final String DB_FIELD_TIMESTAMP = "timestamp";

    public FtpPulledFile() {
    }
    public FtpPulledFile(String type, String filename) {
        this.type = type;
        this.filename = filename;
        this.timestamp = fixOffsetIn(OffsetDateTime.now());
        this.timestampNew = OffsetDateTime.now();
    }

    @Column(name = DB_FIELD_TYPE)
    private String type;

    @Column(name = DB_FIELD_FILENAME)
    private String filename;

    @Column(name = DB_FIELD_TIMESTAMP, nullable = false, updatable = false, columnDefinition = "datetime2")
    private OffsetDateTime timestamp;

    @Column(name = DB_FIELD_TIMESTAMP+"_new", nullable = true, updatable = false)
    private OffsetDateTime timestampNew;

    public void updateTimestamp() {
        this.timestampNew = this.timestamp;
    }

    public static List<String> updateFields() {
        return List.of(DB_FIELD_TIMESTAMP);
    }
}
