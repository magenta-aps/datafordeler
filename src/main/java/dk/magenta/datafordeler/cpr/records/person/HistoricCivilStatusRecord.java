package dk.magenta.datafordeler.cpr.records.person;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.cpr.data.person.PersonEffect;
import dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.person.data.CivilStatusAuthorityTextDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.CivilStatusDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.CivilStatusVerificationDataRecord;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Record for Person historic civil status (type 036).
 */
public class HistoricCivilStatusRecord extends HistoricPersonDataRecord {

    private CprBitemporality civilTemporality;
    private CprBitemporality documentTemporality;
    private CprBitemporality officiaryTemporality;

    public HistoricCivilStatusRecord(String line) throws ParseException {
        super(line);
        this.obtain("annkor", 14, 1);
        this.obtain("start_mynkod-civilstand", 15, 4);
        this.obtain("civ_ts", 19, 12);
        this.obtain("civst", 31, 1);
        this.obtain("aegtepnr", 32, 10);
        this.obtain("aegtefoed_dt", 42, 10);
        this.obtain("aegtefoeddt_umrk", 52, 1);
        this.obtain("aegtenvn", 53, 34);
        this.obtain("aegtenvn_mrk", 87, 1);
        this.obtain("haenstart-civilstand", 88, 12);
        this.obtain("haenstart_umrk-civilstand", 100, 1);
        this.obtain("haenslut-civilstand", 101, 12);
        this.obtain("haenslut_umrk-civilstand", 113, 1);
        this.obtain("dok_mynkod-civilstand", 114, 4);
        this.obtain("dok_ts-civilstand", 118, 12);
        this.obtain("dok-civilstand", 130, 3);
        this.obtain("myntxt_mynkod-civilstand", 133, 4);
        this.obtain("myntxt_ts-civilstand", 137, 12);
        this.obtain("myntxt-civilstand", 149, 20);
        this.obtain("sep_henvis_ts", 169, 12);

        this.civilTemporality = new CprBitemporality(this.getOffsetDateTime("civ_ts"), null, this.getOffsetDateTime("haenstart-civilstand"), this.getBoolean("haenstart_umrk-civilstand"), this.getOffsetDateTime("haenslut-civilstand"), this.getBoolean("haenslut_umrk-civilstand"));
        this.documentTemporality = new CprBitemporality(this.getOffsetDateTime("dok_ts-civilstand"));
        this.officiaryTemporality = new CprBitemporality(this.getOffsetDateTime("myntxt_ts-civilstand"));
    }

    @Override
    public boolean populateBaseData(PersonBaseData data, CprBitemporality bitemporality, Session session, ImportMetadata importMetadata) {
        boolean updated = false;
        if (bitemporality.equals(this.civilTemporality)) {
            data.setCivilStatus(
                    // int authority,
                    this.getInt("start_mynkod-civilstand"),
                    // String civilStatus,
                    this.getString("civst", true),
                    // String spouseCpr,
                    this.getString("aegtepnr", true),
                    // LocalDate spouseBirthdate,
                    this.getDate("aegtefoed_dt"),
                    // boolean spouseBirthdateUncertain,
                    this.getBoolean("aegtefoeddt_umrk"),
                    // String spouseName,
                    this.getString("aegtenvn", true),
                    // boolean spouseNameMarking
                    this.getMarking("aegtenvn_mrk"),
                    // String correctionMarking
                    this.getString("annkor", true),
                    importMetadata.getImportTime()
            );
            updated = true;
        }
        if (bitemporality.equals(this.documentTemporality)) {
            data.setCivilStatusVerification(
                    this.getInt("dok_mynkod-civilstand"),
                    this.getBoolean("dok-civilstand"),
                    this.getString("annkor", true),
                    importMetadata.getImportTime()
            );
        }
        if (bitemporality.equals(this.officiaryTemporality)) {
            data.setCivilStatusAuthorityText(
                    this.getInt("myntxt_mynkod-civilstand"),
                    this.getString("myntxt-civilstand", true),
                    this.getString("annkor", true),
                    importMetadata.getImportTime()
            );
        }
        return updated;
    }

    @Override
    public boolean cleanBaseData(PersonBaseData data, CprBitemporality bitemporality, CprBitemporality outdatedTemporality, Session session) {
        boolean updated = false;
        if (bitemporality.equals(this.civilTemporality) && outdatedTemporality.equals(this.civilTemporality, CprBitemporality.EXCLUDE_EFFECT_TO)) {
            data.clearCivilStatus(session);
            updated = true;
        }
        if (bitemporality.equals(this.documentTemporality) && outdatedTemporality.equals(this.documentTemporality, CprBitemporality.EXCLUDE_EFFECT_TO)) {
            data.clearCitizenshipVerification(session);
            updated = true;
        }
        if (bitemporality.equals(this.officiaryTemporality) && outdatedTemporality.equals(this.officiaryTemporality, CprBitemporality.EXCLUDE_EFFECT_TO)) {
            data.clearCivilStatusAuthorityText(session);
            updated = true;
        }
        return updated;
    }

    @Override
    public List<CprBitemporalRecord> getBitemporalRecords() {

        ArrayList<CprBitemporalRecord> records = new ArrayList<>();

        records.add(new CivilStatusDataRecord(
                null,
                this.getString("civst", true),
                this.getString("aegtepnr", false),
                this.getDate("aegtefoed_dt"),
                this.getBoolean("aegtefoeddt_umrk"),
                this.getString("aegtenvn", true),
                this.getMarking("aegtenvn_mrk")
        ).setAuthority(
                this.getInt("start_mynkod-civilstand")
        ).setBitemporality(
                this.civilTemporality
        ).setHistoric());

        records.add(new CivilStatusVerificationDataRecord(
                this.getBoolean("dok-civilstand"),
                null
        ).setAuthority(
                this.getInt("dok_mynkod-civilstand")
        ).setBitemporality(
                this.documentTemporality
        ).setHistoric());

        records.add(new CivilStatusAuthorityTextDataRecord(
                this.getString("myntxt-civilstand", true),
                null
        ).setAuthority(
                this.getInt("myntxt_mynkod-civilstand")
        ).setBitemporality(
                this.officiaryTemporality
        ).setHistoric());

        return records;
    }

    @Override
    public String getRecordType() {
        return RECORDTYPE_HISTORIC_CIVILSTATUS;
    }

    @Override
    public List<CprBitemporality> getBitemporality() {
        ArrayList<CprBitemporality> bitemporalities = new ArrayList<>();
        if (this.has("civst") || this.has("aegtepnr")) {
            bitemporalities.add(this.civilTemporality);
        }
        if (this.has("dok_mynkod-civilstand")) {
            bitemporalities.add(this.documentTemporality);
        }
        if (this.has("myntxt_mynkod-civilstand")) {
            bitemporalities.add(this.officiaryTemporality);
        }
        return bitemporalities;
    }

    @Override
    public Set<PersonEffect> getEffects() {
        HashSet<PersonEffect> effects = new HashSet<>();
        effects.add(new PersonEffect(null, this.getOffsetDateTime("haenstart-civilstand"), this.getMarking("haenstart_umrk-civilstand"), null, false));
        return effects;
    }
}
