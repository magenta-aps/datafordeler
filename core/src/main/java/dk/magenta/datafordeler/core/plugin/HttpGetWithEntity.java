package dk.magenta.datafordeler.core.plugin;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;

import java.net.URI;

public class HttpGetWithEntity extends HttpUriRequestBase {

    public static final String METHOD_NAME = "GET";

    public HttpGetWithEntity(URI uri) {
        super(METHOD_NAME, uri);
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }
}
