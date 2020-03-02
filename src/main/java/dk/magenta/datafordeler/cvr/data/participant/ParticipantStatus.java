package dk.magenta.datafordeler.cvr.data.participant;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * Storage for data on a Participant's status
 * referenced by {@link dk.magenta.datafordeler.cvr.data.participant.ParticipantBaseData}
 */
@Entity
@Table(name = "cvr_participant_status", indexes = {
        @Index(name = "cvr_participant_status_name", columnList = ParticipantStatus.DB_FIELD_NAME)
})
public class ParticipantStatus extends ParticipantClassification {
}
