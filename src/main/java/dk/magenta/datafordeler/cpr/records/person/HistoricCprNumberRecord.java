package dk.magenta.datafordeler.cpr.records.person;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.cpr.data.person.PersonEffect;
import dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.person.data.PersonNumberDataRecord;
import org.hibernate.Session;

import java.time.OffsetDateTime;
import java.util.*;

/**
 * Record for Person historic cpr number (type 065).
 */
public class HistoricCprNumberRecord extends HistoricPersonDataRecord {

    private CprBitemporality cprTemporality;

    public HistoricCprNumberRecord(String line) throws ParseException {
        super(line);
        this.obtain("start_mynkod-pnrgaeld", 14, 4);
        this.obtain("gammelt_pnr", 18, 10);
        this.obtain("start_dt-person", 28, 10);
        this.obtain("start_dt_umrk-person", 38, 1);
        this.obtain("slut_dt-person", 39, 10);
        this.obtain("slut_dt_umrk-person", 49, 1);
        this.cprTemporality = new CprBitemporality(
                null, null,
                this.getOffsetDateTime("start_dt-person"), this.getBoolean("start_dt_umrk-person"),
                this.getOffsetDateTime("slut_dt-person"), this.getBoolean("slut_dt_umrk-person")
        );
    }

    @Override
    public boolean populateBaseData(PersonBaseData data, CprBitemporality bitemporality, Session session, ImportMetadata importMetadata) {
        boolean updated = false;
        if (bitemporality.equals(this.cprTemporality)) {
            data.setCprNumber(
                    this.getInt("start_mynkod-pnrgaeld"),
                    this.getString("gammelt_pnr", false),
                    importMetadata.getImportTime()
            );
            updated = true;
        }
        return updated;
    }

    @Override
    public boolean cleanBaseData(PersonBaseData data, CprBitemporality bitemporality, CprBitemporality outdatedTemporality, Session session) {
        boolean updated = false;
        if (bitemporality.equals(this.cprTemporality) && outdatedTemporality.equals(this.cprTemporality, CprBitemporality.EXCLUDE_EFFECT_TO)) {
            data.clearCprNumber(session);
            updated = true;
        }
        return updated;
    }

    @Override
    public String getRecordType() {
        return RECORDTYPE_HISTORIC_CPRNUMBER;
    }


    @Override
    public List<CprBitemporalRecord> getBitemporalRecords() {

        ArrayList<CprBitemporalRecord> records = new ArrayList<>();

        records.add(new PersonNumberDataRecord(
                this.getString("gammelt_pnr", false)
        ).setAuthority(
                this.getInt("start_mynkod-pnrgaeld")
        ).setBitemporality(
                this.cprTemporality
        ).setHistoric());

        return records;
    }

    @Override
    public HashSet<OffsetDateTime> getRegistrationTimestamps() {
        HashSet<OffsetDateTime> timestamps = super.getRegistrationTimestamps();
        timestamps.add(this.cprTemporality.registrationFrom);
        return timestamps;
    }

    @Override
    public List<CprBitemporality> getBitemporality() {
        return Collections.singletonList(this.cprTemporality);
    }

    @Override
    public Set<PersonEffect> getEffects() {
        HashSet<PersonEffect> effects = new HashSet<>();
        effects.add(new PersonEffect(null, this.getOffsetDateTime("start_dt-person"), this.getBoolean("start_dt_umrk-person"), this.getOffsetDateTime("slut_dt-person"), this.getBoolean("slut_dt_umrk-person")));
        return effects;
    }
}
