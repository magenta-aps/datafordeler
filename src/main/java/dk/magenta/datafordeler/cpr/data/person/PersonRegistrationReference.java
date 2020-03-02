package dk.magenta.datafordeler.cpr.data.person;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.RegistrationReference;

import java.net.URI;

public class PersonRegistrationReference implements RegistrationReference {

    @JsonProperty("checksum")
    public String checksum;

    private URI uri;

    public PersonRegistrationReference(String checksum) {
        this.checksum = checksum;
    }

    public PersonRegistrationReference(URI uri) {
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
