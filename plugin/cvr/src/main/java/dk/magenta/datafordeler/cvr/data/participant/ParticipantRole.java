package dk.magenta.datafordeler.cvr.data.participant;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * Storage for data on a Participant's role
 * referenced by {@link dk.magenta.datafordeler.cvr.data.participant.ParticipantBaseData}
 */
@Entity
@Table(name = "cvr_participant_role", indexes = {
        @Index(name = "cvr_participant_role_name", columnList = ParticipantRole.DB_FIELD_NAME)
})
public class ParticipantRole extends ParticipantClassification {
}
