package dk.magenta.datafordeler.cpr.data;

import dk.magenta.datafordeler.core.database.Entity;
import dk.magenta.datafordeler.core.database.Identification;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class CprEntity<E extends Entity<E, R>, R extends CprRegistration> extends Entity<E, R> {

    public CprEntity() {
    }

    public CprEntity(Identification identification) {
        super(identification);
    }

    public CprEntity(UUID uuid, String domain) {
        super(uuid, domain);
    }

    public Set<R> getRegistrationsAt(OffsetDateTime time) {
        HashSet<R> registrations = new HashSet<>();
        for (R registration : this.registrations) {
            OffsetDateTime from = registration.getRegistrationFrom();
            OffsetDateTime to = registration.getRegistrationTo();
            if ((from == null || from.isBefore(time) || from.isEqual(time)) && (to == null || to.isAfter(time) || to.isEqual(time))) {
                registrations.add(registration);
            }
        }
        return registrations;
    }
}
