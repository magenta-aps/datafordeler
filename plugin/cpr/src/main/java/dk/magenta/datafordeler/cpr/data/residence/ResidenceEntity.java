package dk.magenta.datafordeler.cpr.data.residence;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.data.CprEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * An Entity representing a residence. Bitemporal data is structured as described
 * in {@link dk.magenta.datafordeler.core.database.Entity}
 */
@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_residence_entity", indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_residence_identification", columnList = "identification_id", unique = true)
})
public class ResidenceEntity extends CprEntity<ResidenceEntity, ResidenceRegistration> {

    public ResidenceEntity() {
    }

    public ResidenceEntity(Identification identification) {
        super(identification);
    }

    public ResidenceEntity(UUID uuid, String domain) {
        super(uuid, domain);
    }

    @Override
    protected ResidenceRegistration createEmptyRegistration() {
        return new ResidenceRegistration();
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
    public static final String schema = "Bolig";


    public static UUID generateUUID(int komkod, int vejkod, String husnr, String etage, String sidedoer) {
        String uuidInput = "residence:" + komkod + ":" + vejkod + ":" + husnr + ":" + etage + ":" + sidedoer;
        return UUID.nameUUIDFromBytes(uuidInput.getBytes());
    }

}
