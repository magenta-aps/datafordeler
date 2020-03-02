package dk.magenta.datafordeler.cvr.data.participant;

import dk.magenta.datafordeler.cvr.data.CvrRegistration;

import javax.persistence.Index;
import javax.persistence.Table;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;

/**
 * Representation of registrations in the bitemporal model for participants.
 * @see dk.magenta.datafordeler.core.database.Entity
 */
@javax.persistence.Entity
@Table(name="cvr_participant_registration", indexes = {
        @Index(name = "cvr_participant_entity", columnList = "entity_id"),
        @Index(name = "cvr_participant_registration_from", columnList = "registrationFrom"),
        @Index(name = "cvr_participant_registration_to", columnList = "registrationTo")
})
public class ParticipantRegistration extends CvrRegistration<ParticipantEntity, ParticipantRegistration, ParticipantEffect> {
    public ParticipantRegistration() {
    }

    public ParticipantRegistration(OffsetDateTime registrationFrom, OffsetDateTime registrationTo, int sequenceNumber) {
        super(registrationFrom, registrationTo, sequenceNumber);
    }

    public ParticipantRegistration(TemporalAccessor registrationFrom, TemporalAccessor registrationTo, int sequenceNumber) {
        super(registrationFrom, registrationTo, sequenceNumber);
    }

    public ParticipantRegistration(String registrationFrom, String registrationTo, int sequenceNumber) {
        super(registrationFrom, registrationTo, sequenceNumber);
    }

    @Override
    protected ParticipantEffect createEmptyEffect(OffsetDateTime effectFrom, OffsetDateTime effectTo) {
        return new ParticipantEffect(this, effectFrom, effectTo);
    }
}
