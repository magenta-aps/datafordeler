package dk.magenta.datafordeler.cvr.data.participant;

import dk.magenta.datafordeler.core.database.QueryManager;
import org.hibernate.Session;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Collections;

/**
 * Storage for data on a Participant's type
 * referenced by {@link dk.magenta.datafordeler.cvr.data.participant.ParticipantBaseData}
 */
@Entity
@Table(name = "cvr_participant_type", indexes = {
        @Index(name = "cvr_participant_type_name", columnList = ParticipantType.DB_FIELD_NAME)
})
public class ParticipantType extends ParticipantClassification {

    public static ParticipantType getType(String navn, Session session) {
        if(navn == null) {
            return null;
        }
        ParticipantType type = QueryManager.getItem(session, ParticipantType.class, Collections.singletonMap("name", navn));
        if (type == null) {
            type = new ParticipantType();
            type.setName(navn);
            session.save(type);
        }
        return type;
    }
}
