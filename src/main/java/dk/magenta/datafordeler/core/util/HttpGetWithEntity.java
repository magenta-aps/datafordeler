package dk.magenta.datafordeler.core.util;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import java.net.URI;

public class HttpGetWithEntity extends HttpEntityEnclosingRequestBase {
    public final static String METHOD_NAME = "GET";

    public HttpGetWithEntity() {
    }

    public HttpGetWithEntity(URI uri) {
        super.setURI(uri);
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }
}
