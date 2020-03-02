package dk.magenta.datafordeler.cvr.data;

import dk.magenta.datafordeler.core.database.Entity;
import dk.magenta.datafordeler.core.database.Identification;

import java.util.UUID;

public abstract class CvrEntity<E extends CvrEntity, R extends CvrRegistration> extends Entity<E, R> {
    public CvrEntity() {
    }

    public CvrEntity(Identification identification) {
        super(identification);
    }

    public CvrEntity(UUID uuid, String domain) {
        super(uuid, domain);
    }
}
