package dk.magenta.datafordeler.core.exception;

import org.apache.http.StatusLine;

import java.net.URI;

/**
 * An exception that basically tells that we got an HTTP status that we didn't expect
 *
 */
public class HttpStatusException extends DataFordelerException {

    private StatusLine statusLine;
    private URI uri;

    public HttpStatusException(StatusLine statusLine, URI uri) {
        this.statusLine = statusLine;
        this.uri = uri;
    }

    public HttpStatusException(StatusLine statusLine) {
        this(statusLine, null);
    }

    @Override
    public String getCode() {
        return "datafordeler.import.http_status";
    }

    public StatusLine getStatusLine() {
        return this.statusLine;
    }

    public URI getUri() {
        return this.uri;
    }
}
