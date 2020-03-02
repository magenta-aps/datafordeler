package dk.magenta.datafordeler.cpr.records.person;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.cpr.data.person.PersonEffect;
import dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.person.data.CitizenshipDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.CitizenshipVerificationDataRecord;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Record for Person historic citizenship (type 041).
 */
public class HistoricCitizenshipRecord extends HistoricPersonDataRecord {

    private CprBitemporality citizenshipTemporality;
    private CprBitemporality documentTemporality;

    public HistoricCitizenshipRecord(String line) throws ParseException {
        super(line);
        this.obtain("annkor", 14, 1);
        this.obtain("start_mynkod-statsborgerskab", 15, 4);
        this.obtain("stat_ts", 19, 12);
        this.obtain("landekod", 31, 4);
        this.obtain("haenstart-statsborgerskab", 35, 12);
        this.obtain("haenstart_umrk-statsborgerskab", 47, 1);
        this.obtain("haenslut-statsborgerskab", 48, 12);
        this.obtain("haenslut_umrk-statsborgerskab", 60, 1);
        this.obtain("dok_mynkod-statsborgerskab", 61, 4);
        this.obtain("dok_ts-statsborgerskab", 65, 12);
        this.obtain("dok-statsborgerskab", 77, 3);

        this.citizenshipTemporality = new CprBitemporality(this.getOffsetDateTime("stat_ts"), null, this.getOffsetDateTime("haenstart-statsborgerskab"), this.getBoolean("haenstart_umrk-statsborgerskab"), this.getOffsetDateTime("haenslut-statsborgerskab"), this.getBoolean("haenslut_umrk-statsborgerskab"));
        this.documentTemporality = new CprBitemporality(this.getOffsetDateTime("dok_ts-statsborgerskab"));
    }

    @Override
    public String getRecordType() {
        return RECORDTYPE_HISTORIC_CITIZENSHIP;
    }

    @Override
    public boolean populateBaseData(PersonBaseData data, CprBitemporality bitemporality, Session session, ImportMetadata importMetadata) {
        boolean updated = false;
        if (bitemporality.equals(this.citizenshipTemporality)) {
            data.setCitizenship(
                    this.getInt("start_mynkod-statsborgerskab"),
                    this.getInt("landekod"),
                    importMetadata.getImportTime()
            );
            updated = true;
        }
        if (bitemporality.equals(this.documentTemporality)) {
            data.setCitizenshipVerification(
                    this.getInt("dok_mynkod-statsborgerskab"),
                    this.getBoolean("dok-statsborgerskab"),
                    importMetadata.getImportTime()
            );
            updated = true;
        }
        return updated;
    }

    @Override
    public boolean cleanBaseData(PersonBaseData data, CprBitemporality bitemporality, CprBitemporality outdatedTemporality, Session session) {
        boolean updated = false;
        if (bitemporality.equals(this.citizenshipTemporality) && outdatedTemporality.equals(this.citizenshipTemporality, CprBitemporality.EXCLUDE_EFFECT_TO)) {
            data.clearCitizenship(session);
            updated = true;
        }
        if (bitemporality.equals(this.documentTemporality) && outdatedTemporality.equals(this.documentTemporality, CprBitemporality.EXCLUDE_EFFECT_TO)) {
            data.clearCitizenshipVerification(session);
            updated = true;
        }
        return updated;
    }

    @Override
    public List<CprBitemporalRecord> getBitemporalRecords() {

        ArrayList<CprBitemporalRecord> records = new ArrayList<>();

        records.add(new CitizenshipDataRecord(
                this.getInt("landekod")
        ).setAuthority(
                this.getInt("start_mynkod-statsborgerskab")
        ).setBitemporality( // TODO: Monotemporal?
                this.citizenshipTemporality // TODO: mangler registrationTo
        ).setHistoric());

        records.add(new CitizenshipVerificationDataRecord(
                this.getBoolean("dok-statsborgerskab")
        ).setAuthority(
                this.getInt("dok_mynkod-statsborgerskab")
        ).setBitemporality(
                this.documentTemporality
        ).setHistoric());

        return records;
    }

    @Override
    public List<CprBitemporality> getBitemporality() {
        ArrayList<CprBitemporality> bitemporalities = new ArrayList<>();
        bitemporalities.add(this.citizenshipTemporality);
        if (this.documentTemporality != null) {
            bitemporalities.add(this.documentTemporality);
        }
        return bitemporalities;
    }

    @Override
    public Set<PersonEffect> getEffects() {
        HashSet<PersonEffect> effects = new HashSet<>();
        effects.add(new PersonEffect(null, this.getOffsetDateTime("haenstart-statsborgerskab"), this.getBoolean("haenstart_umrk-statsborgerskab"), this.getOffsetDateTime("haenslut-statsborgerskab"), this.getBoolean("haenslut_umrk-statsborgerskab")));
        effects.add(new PersonEffect(null, null, false, null, false));
        return effects;
    }
}
