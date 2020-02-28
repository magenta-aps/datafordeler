package dk.magenta.datafordeler.core.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class DataOutputException extends WebApplicationException {
    public DataOutputException(Throwable cause) {
        super(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(cause).type(MediaType.TEXT_PLAIN).build());
    }
}
