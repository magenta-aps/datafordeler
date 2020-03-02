package dk.magenta.datafordeler.cvr.data.participant;

import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cvr.data.shared.SingleData;

import javax.persistence.*;

@Entity
@Table(name = "cvr_participant_name", indexes = {
        @Index(name = "cvr_participant_name_data", columnList = ParticipantName.DB_FIELD_VALUE),
        @Index(name = "cvr_participant_name_base", columnList = ParticipantName.DB_FIELD_BASEDATA + DatabaseEntry.REF)
})
public class ParticipantName extends SingleData<String> {

    public static final String DB_FIELD_BASEDATA = "participantBaseData";

    @ManyToOne(targetEntity = ParticipantBaseData.class, optional = false)
    @JoinColumn(name = DB_FIELD_BASEDATA + DatabaseEntry.REF)
    private ParticipantBaseData participantBaseData;

    public void setParticipantBaseData(ParticipantBaseData participantBaseData) {
        this.participantBaseData = participantBaseData;
    }

}
