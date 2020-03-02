package dk.magenta.datafordeler.cvr.data.companyunit;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.RegistrationReference;

import java.net.URI;

public class CompanyUnitRegistrationReference implements RegistrationReference {

    @JsonProperty("checksum")
    public String checksum;

    private URI uri;

    public CompanyUnitRegistrationReference(String checksum) {
        this.checksum = checksum;
    }

    public CompanyUnitRegistrationReference(URI uri) {
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
