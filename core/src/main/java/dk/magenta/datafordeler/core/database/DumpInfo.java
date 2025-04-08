package dk.magenta.datafordeler.core.database;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dk.magenta.datafordeler.core.dump.DumpConfiguration;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import org.apache.commons.io.Charsets;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.time.OffsetDateTime;


@Entity
@Table(name = "dump_info")
public final class DumpInfo extends DatabaseEntry implements
        Comparable<DumpInfo> {

    @Column(nullable = false)
    private String name, requestPath, charset, format;

    @Column(nullable = false, columnDefinition = "datetime2")
    private OffsetDateTime timestamp;

    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true, optional = true,
            cascade = CascadeType.ALL)
    @Lob
    private DumpData data;

    public DumpInfo() {
    }

    public DumpInfo(DumpConfiguration config,
                    OffsetDateTime timestamp,
                    byte[] data) {
        this.name = config.getName();
        this.requestPath = config.getRequestPath();
        this.format = config.getFormat().name();
        this.charset = config.getCharset().name();
        this.timestamp = timestamp;
        this.data = data != null ? new DumpData(data) : null;
    }

    @Override
    public int compareTo(DumpInfo o) {
        return Long.compare(this.getId(), o.getId());
    }

    @JsonIgnore
    public String getName() {
        return this.name;
    }

    @JsonIgnore
    public String getRequestPath() {
        return this.requestPath;
    }

    @JsonIgnore
    public byte[] getData() {
        try {
            return this.data.getData();
        } catch (NullPointerException e) {
            return null;
        }
    }

    @JsonIgnore
    public String getStringData() {
        try {
            return new String(this.data.getData(), this.charset);
        } catch (NullPointerException | UnsupportedEncodingException e) {
            return null;
        }
    }

    @JsonIgnore
    public DumpConfiguration.Format getFormat() {
        return DumpConfiguration.Format.valueOf(format);
    }

    @JsonIgnore
    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public String toString() {
        return String.format(
                "DumpInfo(%s, %s, %s, %s, %s)",
                name, requestPath, format, timestamp, data
        );
    }

    public Charset getCharset() {
        return Charsets.toCharset(charset);
    }
}

