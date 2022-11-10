package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.HttpStatusException;
import org.apache.http.StatusLine;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public interface Communicator {

    InputStream fetch(URI uri) throws HttpStatusException, DataStreamException;

    StatusLine send(URI endpoint, String payload) throws IOException, DataFordelerException;

}
