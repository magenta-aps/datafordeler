package dk.magenta.datafordeler.gladdreg.data.bnumber;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.RegistrationReference;

import java.net.URI;

/**
 * Created by lars on 16-05-17.
 */
public class BNumberRegistrationReference implements RegistrationReference {

    @JsonProperty("checksum")
    public String checksum;

    private URI uri;

    public BNumberRegistrationReference(String checksum) {
        this.checksum = checksum;
    }

    public BNumberRegistrationReference(URI uri) {
        this.uri = uri;
    }

    @Override
    public String getChecksum() {
        return this.checksum;
    }


    @Override
    public URI getURI() {
        return this.uri;
    }
}
