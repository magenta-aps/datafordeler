package dk.magenta.datafordeler.cpr.records.person;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.cpr.data.person.PersonEffect;
import dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.person.data.ChurchDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.ChurchVerificationDataRecord;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Record for Person historic church relation (type 011).
 */
public class HistoricChurchRecord extends HistoricPersonDataRecord {

    private CprBitemporality churchTemporality;
    private CprBitemporality documentTemporality;

    public HistoricChurchRecord(String line) throws ParseException {
        super(line);
        this.obtain("start_mynkod-folkekirke", 14, 4);
        this.obtain("fkirk_ts", 18, 12);
        this.obtain("fkirk", 30, 1);
        this.obtain("start_dt-folkekirke", 31, 10);
        this.obtain("start_dt-umrk-folkekirke", 41, 1);
        this.obtain("slut_dt-folkekirke", 42, 10);
        this.obtain("slut_dt-umrk-folkekirke", 52, 1);
        this.obtain("dok_mynkod-folkekirke", 53, 4);
        this.obtain("dok_ts-folkekirke", 57, 12);
        this.obtain("dok-folkekirke", 69, 3);

        this.churchTemporality = new CprBitemporality(
                this.getOffsetDateTime("fkirk_ts"), null,
                this.getOffsetDateTime("start_dt-folkekirke"), this.getBoolean("start_dt-umrk-folkekirke"),
                this.getOffsetDateTime("slut_dt-folkekirke"), this.getBoolean("slut_dt-umrk-folkekirke")
        );
        this.documentTemporality = new CprBitemporality(this.getOffsetDateTime("dok_ts-folkekirke"));
    }

    @Override
    public String getRecordType() {
        return RECORDTYPE_HISTORIC_CHURCH;
    }

    @Override
    public boolean populateBaseData(PersonBaseData data, CprBitemporality bitemporality, Session session, ImportMetadata importMetadata) {
        boolean updated = false;
        if (bitemporality.equals(this.churchTemporality)) {
            data.setChurch(
                    this.getInt("start_mynkod-folkekirke"),
                    this.getChar("fkirk"),
                    importMetadata.getImportTime()
            );
            updated = true;
        }
        if (bitemporality.equals(this.documentTemporality)) {
            data.setChurchVerification(
                    this.getInt("dok_mynkod-folkekirke"),
                    this.getBoolean("dok-folkekirke"),
                    importMetadata.getImportTime()
            );
            updated = true;
        }
        return updated;
    }

    @Override
    public boolean cleanBaseData(PersonBaseData data, CprBitemporality bitemporality, CprBitemporality outdatedTemporality, Session session) {
        boolean updated = false;
        if (bitemporality.equals(this.churchTemporality) && outdatedTemporality.equals(this.churchTemporality, CprBitemporality.EXCLUDE_EFFECT_TO)) {
            data.clearChurch(session);
            updated = true;
        }
        if (bitemporality.equals(this.documentTemporality) && outdatedTemporality.equals(this.documentTemporality, CprBitemporality.EXCLUDE_EFFECT_TO)) {
            data.clearChurchVerification(session);
            updated = true;
        }
        return updated;
    }

    @Override
    public List<CprBitemporalRecord> getBitemporalRecords() {

        ArrayList<CprBitemporalRecord> records = new ArrayList<>();

        records.add(new ChurchDataRecord(
                this.getChar("fkirk")
        ).setAuthority(
                this.getInt("start_mynkod-folkekirke", true)
        ).setBitemporality( // TODO: Monotemporal?
                this.churchTemporality // TODO: mangler registrationTo
        ).setHistoric());

        records.add(new ChurchVerificationDataRecord(
                this.getBoolean("dok-folkekirke")
        ).setAuthority(
                this.getInt("dok_mynkod-folkekirke")
        ).setBitemporality(
                this.documentTemporality
        ).setHistoric());

        return records;
    }

    @Override
    public List<CprBitemporality> getBitemporality() {
        ArrayList<CprBitemporality> bitemporalities = new ArrayList<>();
        bitemporalities.add(this.churchTemporality);
        bitemporalities.add(this.documentTemporality);
        return bitemporalities;
    }

    @Override
    public Set<PersonEffect> getEffects() {
        HashSet<PersonEffect> effects = new HashSet<>();
        effects.add(new PersonEffect(null, this.getOffsetDateTime("start_dt-folkekirke"), this.getBoolean("start_dt-umrk-folkekirke"), this.getOffsetDateTime("slut_dt-folkekirke"), this.getBoolean("slut_dt-umrk-folkekirke")));
        effects.add(new PersonEffect(null, null, false, null, false));
        return effects;
    }
}
