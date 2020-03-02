package dk.magenta.datafordeler.cvr.data.participant;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.RegistrationReference;

import java.net.URI;

public class ParticipantRegistrationReference implements RegistrationReference {

    @JsonProperty("checksum")
    public String checksum;

    private URI uri;

    public ParticipantRegistrationReference(String checksum) {
        this.checksum = checksum;
    }

    public ParticipantRegistrationReference(URI uri) {
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
