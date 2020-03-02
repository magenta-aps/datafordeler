package dk.magenta.datafordeler.cpr.records.person;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.cpr.data.person.PersonEffect;
import dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.person.data.ProtectionDataRecord;
import org.hibernate.Session;

import java.util.*;

/**
 * Record for Historic Person protection (type 016).
 */
public class HistoricProtectionRecord extends HistoricPersonDataRecord {

    private CprBitemporality protectionTemporality;

    public HistoricProtectionRecord(String line) throws ParseException {
        super(line);
        this.obtain("beskyttype",14,4);
        this.obtain("start_mynkod-beskyttelse",18,4);
        this.obtain("start_ts-beskyttelse",22,12);
        this.obtain("start_dt-beskyttelse",34,10);
        this.obtain("indrap-beskyttelse",44	,3);
        this.obtain("slet_dt-beskyttelse",47,10);

        this.obtain("slut_mynkod-beskyttelse",57,4);
        this.obtain("slut_ts-beskyttelse",61,12);
        this.obtain("slut_dt-beskyttelse",73,10);

        //this.protectionTemporality = new Bitemporality(this.getOffsetDateTime("start_ts-beskyttelse"), this.getOffsetDateTime("slut_ts-beskyttelse"), this.getOffsetDateTime("start_dt-beskyttelse"), false, this.getOffsetDateTime("slut_dt-beskyttelse"), false);
        this.protectionTemporality = new CprBitemporality(this.getOffsetDateTime("start_ts-beskyttelse"), this.getOffsetDateTime("slut_ts-beskyttelse"), this.getOffsetDateTime("start_dt-beskyttelse"), false, this.getOffsetDateTime("slut_dt-beskyttelse"), false);
    }

    @Override
    public boolean cleanBaseData(PersonBaseData data, CprBitemporality bitemporality, CprBitemporality outdatedTemporality, Session session) {
        return false;
    }

    @Override
    public String getRecordType() {
        return RECORDTYPE_HISTORIC_PROTECTION;
    }

    @Override
    public boolean populateBaseData(PersonBaseData data, CprBitemporality bitemporality, Session session, ImportMetadata importMetadata) {
        /*
        Keep this off for now
        if (bitemporality.equals(this.protectionTemporality)) {
            data.addProtection(
                    // int authority,
                    this.getInt("start_mynkod-beskyttelse"),
                    // int beskyttelsestype,
                    this.getInt("beskyttype"),
                    // boolean reportMarking
                    this.getBoolean("indrap-beskyttelse"),
                    importMetadata.getImportTime()
            );
            return true;
        }*/
        return false;
    }

    @Override
    public List<CprBitemporalRecord> getBitemporalRecords() {

        ArrayList<CprBitemporalRecord> records = new ArrayList<>();

        records.add(new ProtectionDataRecord(
                this.getInt("beskyttype"),
                this.getBoolean("indrap-beskyttelse"),
                this.getDate("slet_dt-beskyttelse")
        ).setAuthority(
                this.getInt("start_mynkod-beskyttelse")
        ).setEndAuthority(
                this.getInt("slut_mynkod-beskyttelse")
        ).setBitemporality(
                this.protectionTemporality
        ).setHistoric());

        return records;
    }

    @Override
    public List<CprBitemporality> getBitemporality() {
        //return Collections.singletonList(this.protectionTemporality);
        return Collections.emptyList();
    }

    @Override
    public Set<PersonEffect> getEffects() {
        HashSet<PersonEffect> effects = new HashSet<>();
        //effects.add(new PersonEffect(null, this.getOffsetDateTime("start_dt-beskyttelse"), false, this.getOffsetDateTime("slut_dt-beskyttelse"), false));
        return effects;
    }
}
