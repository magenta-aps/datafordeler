package dk.magenta.datafordeler.core.exception;

import org.apache.hc.core5.http.HttpResponse;

import java.net.URI;

/**
 * An exception that basically tells that we got an HTTP status that we didn't expect
 */
public class HttpStatusException extends DataFordelerException {

    private final int statusCode;
    private final String statusLine;
    private final URI uri;

    public HttpStatusException(int statusCode, String statusLine, URI uri) {
        super("Got HTTP error " + statusCode + (uri != null ? (" when accessing " + uri) : ""));
        this.statusCode = statusCode;
        this.statusLine = statusLine;
        this.uri = uri;
    }

    public HttpStatusException(int statusCode, String statusLine) {
        this(statusCode, statusLine, null);
    }

    public HttpStatusException(HttpResponse httpResponse, URI uri) {
        this(httpResponse.getCode(), httpResponse.getReasonPhrase(), uri);
    }

    @Override
    public String getCode() {
        return "datafordeler.import.http_status";
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public String getStatusLine() {
        return this.statusLine;
    }

    public URI getUri() {
        return this.uri;
    }
}
