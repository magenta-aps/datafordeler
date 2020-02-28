package dk.magenta.datafordeler.core.gapi;

import dk.magenta.datafordeler.core.Engine;
import dk.magenta.datafordeler.core.io.Event;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.*;
import org.apache.olingo.server.api.deserializer.DeserializerResult;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.uri.UriInfo;

import java.util.Locale;


/**
 * @author Lars Peter Thomsen
 * @version 0.1
 *
 * Processor for the GAPI interface.
 * The OData request data should be extracted here and sent to the Datafordeler Engine
 */
public class GapiProcessor implements EntityProcessor {

    private OData oData;
    private Edm edm;
    private Engine engine;
    private static Logger log = LogManager.getLogger(GapiProcessor.class.getCanonicalName());

    public GapiProcessor(Edm edm, Engine engine) {
        this.edm = edm;
        this.engine = engine;
    }

    @Override
    public void init(OData oData, ServiceMetadata serviceMetadata) {
        this.oData = oData;
    }

    /**
     * Receive GET requests (throw error because we don't support it)
     */
    @Override
    public void readEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType contentType) throws ODataApplicationException, ODataLibraryException {
        throw new ODataApplicationException("Reading events is not supported", HttpStatusCode.METHOD_NOT_ALLOWED.getStatusCode(), Locale.ENGLISH);
    }

    /**
     * Receive POST requests, delegating to the odata API for parsing, and handing the result over to the Datafordeler Engine
     */
    @Override
    public void createEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        try {
            this.log.info("handle createEntity");
            boolean success;
            ImportMetadata importMetadata = new ImportMetadata();
            try {
                ODataDeserializer deserializer = this.oData.createDeserializer(requestFormat);
                DeserializerResult deserializerResult = deserializer.entity(request.getBody(), this.edm.getEntityType(GapiEdmProvider.ET_EVENT));
                Entity entity = deserializerResult.getEntity();
                Event event = GapiEdmProvider.convertEvent(entity);
                success = this.engine.handleEvent(event, importMetadata);
            } catch (ODataLibraryException e) {
                e.printStackTrace();
                throw e;
            }
            HttpStatusCode statusCode = success ? HttpStatusCode.CREATED : HttpStatusCode.INTERNAL_SERVER_ERROR;
            this.log.info("createEntity handled, responding with HTTP "+statusCode.getStatusCode());
            response.setStatusCode(statusCode.getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Receive PUT requests (throw error because we don't support it)
     */
    @Override
    public void updateEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        throw new ODataApplicationException("Updating events is not supported", HttpStatusCode.METHOD_NOT_ALLOWED.getStatusCode(), Locale.ENGLISH);
    }

    /**
     * Receive DELETE requests (throw error because we don't support it)
     */
    @Override
    public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo) throws ODataApplicationException, ODataLibraryException {
        throw new ODataApplicationException("Deleting events is not supported", HttpStatusCode.METHOD_NOT_ALLOWED.getStatusCode(), Locale.ENGLISH);
    }

}
