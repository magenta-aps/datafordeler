package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.HttpStatusException;

import java.io.InputStream;
import java.net.URI;

public interface Communicator {

    InputStream fetch(URI uri) throws HttpStatusException, DataStreamException;

}
