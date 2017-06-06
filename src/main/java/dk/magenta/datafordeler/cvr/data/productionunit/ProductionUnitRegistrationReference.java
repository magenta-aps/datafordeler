package dk.magenta.datafordeler.cvr.data.productionunit;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.RegistrationReference;

import java.net.URI;

/**
 * Created by lars on 16-05-17.
 */
public class ProductionUnitRegistrationReference implements RegistrationReference {

    @JsonProperty("checksum")
    public String checksum;

    private URI uri;

    public ProductionUnitRegistrationReference(String checksum) {
        this.checksum = checksum;
    }

    public ProductionUnitRegistrationReference(URI uri) {
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
