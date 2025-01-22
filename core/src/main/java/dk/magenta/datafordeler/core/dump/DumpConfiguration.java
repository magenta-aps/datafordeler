package dk.magenta.datafordeler.core.dump;

import dk.magenta.datafordeler.core.configuration.Configuration;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import org.springframework.http.MediaType;

import jakarta.persistence.*;
import java.nio.charset.Charset;
import java.util.logging.Logger;

@Entity
@Table(
        name = "dump_config",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {
                        "request_path", "format", "charset", "destination_uri",
                }),
        })
public class DumpConfiguration extends DatabaseEntry implements Configuration {

    public static final MediaType TEXT_CSV = new MediaType("text", "csv");
    public static final MediaType TEXT_TSV = new MediaType("text", "tsv");

    private static final Logger log =
            Logger.getLogger(DumpConfiguration.class.getCanonicalName());

    public enum Format {
        xml(MediaType.APPLICATION_XML),
        json(MediaType.APPLICATION_JSON),
        csv(TEXT_CSV);

        private final MediaType mediaType;

        Format(MediaType mediaType) {
            this.mediaType = mediaType;
        }

        public MediaType getMediaType() {
            return mediaType;
        }

        public static Format forMediaType(String mediaType) {
            return forMediaType(MediaType.parseMediaType(mediaType));
        }

        public static Format forMediaType(MediaType mediaType) {
            for (Format format : values()) {
                if (format.mediaType.includes(mediaType)) {
                    return format;
                }
            }

            throw new IllegalArgumentException(mediaType.toString());
        }
    }

    @OrderBy
    @Column(nullable = false, length = 20, unique = true)
    private String name;

    @Column(name = "request_path", nullable = false)
    private String requestPath;

    @Column(nullable = false, length = 20)
    private String format;

    @Column(nullable = false, length = 20)
    private String charset;

    @Column(nullable = false, length = 100)
    private String schedule;

    @Column(nullable = false, length = 1000)
    private String notes;

    @Column(name = "destination_uri", nullable = true)
    private String destinationURI;

    public DumpConfiguration() {
    }

    public DumpConfiguration(
            String name, String requestPath, Format format, Charset charset,
            String schedule, String notes, String destinationURI
    ) {
        this.name = name;
        this.requestPath = requestPath;
        this.format = format != null ? format.mediaType.toString() : null;
        this.charset = charset != null ? charset.toString() : null;
        this.schedule = schedule;
        this.notes = notes;
        this.destinationURI = destinationURI;
    }

    public String getName() {
        return name;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public Format getFormat() {
        return Format.forMediaType(format);
    }

    public Charset getCharset() {
        return Charset.forName(this.charset);
    }

    public String getSchedule() {
        return this.schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public String getDestinationURI() {
        return destinationURI;
    }

}
