package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DataItem;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.cvr.data.company.CompanyBaseData;
import dk.magenta.datafordeler.cvr.data.companyunit.CompanyUnitBaseData;
import dk.magenta.datafordeler.cvr.data.participant.ParticipantBaseData;
import org.hibernate.Session;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A CVR record is the object representation of a node in our input data,
 * holding at least some bitemporality, and (in subclasses) some data that take effect within this bitemporality
 */
@MappedSuperclass
public abstract class CvrRecord extends DatabaseEntry {


    public static final String DB_FIELD_DAFO_UPDATED = "dafoUpdated";
    public static final String IO_FIELD_DAFO_UPDATED = "dafoOpdateret";

    @Column(name = DB_FIELD_DAFO_UPDATED)
    private OffsetDateTime dafoUpdated = null;

    @JsonProperty(value = IO_FIELD_DAFO_UPDATED)
    public OffsetDateTime getDafoUpdated() {
        return this.dafoUpdated;
    }

    public void setDafoUpdated(OffsetDateTime dafoUpdated) {
        this.dafoUpdated = dafoUpdated;
    }

    public CvrRecord() {
    }

    public void populateBaseData(DataItem baseData, Session session, OffsetDateTime timestamp) throws ParseException {
        if (baseData instanceof CompanyBaseData) {
            CompanyBaseData companyBaseData = (CompanyBaseData) baseData;
            this.populateBaseData(companyBaseData, session);
        }
        if (baseData instanceof CompanyUnitBaseData) {
            CompanyUnitBaseData companyUnitBaseData = (CompanyUnitBaseData) baseData;
            this.populateBaseData(companyUnitBaseData, session);
        }
        if (baseData instanceof ParticipantBaseData) {
            ParticipantBaseData participantBaseData = (ParticipantBaseData) baseData;
            this.populateBaseData(participantBaseData, session);
        }
    }

    public void populateBaseData(CompanyBaseData baseData, Session session) throws ParseException {}

    public void populateBaseData(CompanyUnitBaseData baseData, Session session) throws ParseException {}

    public void populateBaseData(ParticipantBaseData baseData, Session session) throws ParseException {}

    public void save(Session session) {
        session.save(this);
    }

    protected static OffsetDateTime roundTime(OffsetDateTime in) {
        if (in != null) {
            //return in.withHour(0).withMinute(0).withSecond(0).withNano(0);
            //return in.withMinute(0).withSecond(0).withNano(0);
            return in.withSecond(0).withNano(0);
        }
        return null;
    }

    public List<CvrRecord> subs() {
        return Collections.emptyList();
    }

    public final List<CvrRecord> fullSubs() {
        ArrayList<CvrRecord> subs = new ArrayList<>();
        this.addSubs(subs);
        return subs;
    }

    private void addSubs(ArrayList<CvrRecord> set) {
        set.add(this);
        for (CvrRecord sub : this.subs()) {
            if (sub != null) {
                sub.addSubs(set);
            }
        }
    }

}
