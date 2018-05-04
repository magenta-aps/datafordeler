package dk.magenta.datafordeler.cpr.records.person;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.cpr.data.person.PersonEffect;
import dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData;
import dk.magenta.datafordeler.cpr.records.Bitemporality;
import org.hibernate.Session;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Record for Person base data (type 001).
 */
public class PersonRecord extends PersonDataRecord {

    private Bitemporality statusTemporality;
    private Bitemporality motherTemporality;
    private Bitemporality fatherTemporality;
    private Bitemporality motherVerificationTemporality;
    private Bitemporality fatherVerificationTemporality;
    private Bitemporality positionTemporality;
    private Bitemporality birthTemporality;

    private boolean hasPnrGaeld = false;

    public PersonRecord(String line) throws ParseException {
        super(line);
        this.obtain("pnrgaeld", 14, 10);
        this.obtain("status_ts", 24, 12);
        this.obtain("status", 36, 2);
        this.obtain("statushaenstart", 38, 12);
        this.obtain("statusdto_umrk", 50, 1);
        this.obtain("start_mynkod-person", 51, 4);
        this.obtain("start_ts-person", 55, 12);
        this.obtain("koen", 67, 1);
        this.obtain("foed_dt", 68, 10);
        this.obtain("foed_dt_umrk", 78, 1);
        this.obtain("foed_tm", 79, 8);
        this.obtain("foedsekvens", 87, 4);
        this.obtain("start_dt-person", 91, 10);
        this.obtain("start_dt_umrk-person", 101, 1);
        this.obtain("slut_dt-person", 102, 10);
        this.obtain("slut_dt_umrk-person", 112, 1);
        this.obtain("stilling_mynkod", 113, 4);
        this.obtain("stilling_ts", 117, 12);
        this.obtain("stilling", 129, 34);
        this.obtain("mor_ts", 163, 12);
        this.obtain("mor_mynkod", 175, 4);
        this.obtain("mor_dt", 179, 10);
        this.obtain("mor_dt_umrk", 189, 1);
        this.obtain("pnrmor", 190, 10);
        this.obtain("mor_foed_dt", 200, 10);
        this.obtain("mor_foed_dt_umrk", 210, 1);
        this.obtain("mornvn", 211, 34);
        this.obtain("mornvn_mrk", 245, 1);
        this.obtain("mor_dok_mynkod", 246, 4);
        this.obtain("mor_dok_ts", 250, 12);
        this.obtain("mor_dok", 262, 3);
        this.obtain("far_ts", 265, 12);
        this.obtain("far_mynkod", 277, 4);
        this.obtain("far_dt", 281, 10);
        this.obtain("far_dt_umrk", 291, 1);
        this.obtain("pnrfar", 292, 10);
        this.obtain("far_foed_dt", 302, 10);
        this.obtain("far_foed_dt_umrk", 312, 1);
        this.obtain("farnvn", 313, 34);
        this.obtain("farnvn_mrk", 347, 1);
        this.obtain("far_dok_mynkod", 348, 4);
        this.obtain("far_dok_ts", 352, 12);
        this.obtain("far_dok", 364, 3);

        this.statusTemporality = new Bitemporality(this.getOffsetDateTime("status_ts"), null, this.getOffsetDateTime("statushaenstart"), this.getBoolean("statusdto_umrk"), null, false);
        this.motherTemporality = new Bitemporality(this.getOffsetDateTime("mor_ts"), null, this.getOffsetDateTime("mor_dt"), this.getBoolean("mor_dt_umrk"), null, false);
        this.fatherTemporality = new Bitemporality(this.getOffsetDateTime("far_ts"), null, this.getOffsetDateTime("far_dt"), this.getBoolean("far_dt_umrk"), null, false);
        this.motherVerificationTemporality = new Bitemporality(this.getOffsetDateTime("mor_dok_ts"));
        this.fatherVerificationTemporality = new Bitemporality(this.getOffsetDateTime("far_dok_ts"));
        this.positionTemporality = new Bitemporality(this.getOffsetDateTime("stilling_ts"));
        this.birthTemporality = new Bitemporality(this.getOffsetDateTime("start_ts-person"), null, this.getOffsetDateTime("start_dt-person"), this.getBoolean("start_dt_umrk-person"), this.getOffsetDateTime("slut_dt-person"), this.getBoolean("slut_dt_umrk-person"));

        this.hasPnrGaeld = !this.getString("pnrgaeld", false).trim().isEmpty();
    }

    /**
     * Create a set of populated PersonBaseData objects, each with its own unique effect period
     *
     * @param bitemporality
     * @param importMetadata
     * @return
     */
    @Override
    public boolean populateBaseData(PersonBaseData data, Bitemporality bitemporality, Session session, ImportMetadata importMetadata) {
        boolean updated = true;


        if (this.hasPnrGaeld) {
            data.setPersonnummer(
                    this.getString("pnrgaeld", false),
                    importMetadata.getImportTime()
            );
        }


        if (bitemporality.equals(this.statusTemporality)) {
            data.setStatus(
                    this.getInt("status", true),
                    importMetadata.getImportTime()
            );
            updated = true;
        }

        if (bitemporality.equals(this.motherTemporality)) {
                data.setMother(
                    // String name,
                    this.get("mornvn"),
                    // boolean nameMarking,
                    this.getBoolean("mornvn_mrk"),
                    // String cprNumber,
                    this.getString("pnrmor", false),
                    // LocalDate birthDate,
                    this.getDate("mor_foed_dt"),
                    // boolean birthDateUncertain,
                    this.getBoolean("mor_foed_dt_umrk"),
                    // int authorityCode
                    this.getInt("mor_mynkod"),
                    importMetadata.getImportTime()
            );
            updated = true;
        }

        if (bitemporality.equals(this.fatherTemporality)) {
            data.setFather(
                    // String name,
                    this.get("farnvn"),
                    // boolean nameMarking,
                    this.getBoolean("farnvn_mrk"),
                    // String cprNumber,
                    this.getString("pnrfar", false),
                    // LocalDate birthDate,
                    this.getDate("far_foed_dt"),
                    // boolean birthDateUncertain,
                    this.getBoolean("far_foed_dt_umrk"),
                    // int authorityCode
                    this.getInt("far_mynkod"),
                    importMetadata.getImportTime()
            );
            updated = true;
        }

        if (bitemporality.equals(this.motherVerificationTemporality)) {
            data.setMotherVerification(
                    // int authorityCode,
                    this.getInt("mor_dok_mynkod"),
                    // boolean verified
                    this.getBoolean("mor_dok"),
                    importMetadata.getImportTime()
            );
            updated = true;
        }

        if (bitemporality.equals(this.fatherVerificationTemporality)) {
            data.setFatherVerification(
                    // int authorityCode,
                    this.getInt("far_dok_mynkod"),
                    // boolean verified
                    this.getBoolean("far_dok"),
                    importMetadata.getImportTime()
            );
            updated = true;
        }

        if (bitemporality.equals(this.positionTemporality)) {
            data.setPosition(
                    // int authorityCode,
                    this.getInt("stilling_mynkod"),
                    // String position
                    this.get("stilling"),
                    importMetadata.getImportTime()
            );
            updated = true;
        }

        if (bitemporality.equals(this.birthTemporality)) {
            data.setBirth(
                    // LocalDateTime foedselsdato,
                    this.getDateTime("foed_dt", "foed_tm"),
                    // boolean foedselsdatoUsikkerhedsmarkering,
                    this.getBoolean("foed_dt_umrk"),
                    // int foedselsraekkefoelge
                    this.getInt("foedsekvens"),
                    importMetadata.getImportTime()
            );
            data.setKoen(
                    this.get("koen"),
                    importMetadata.getImportTime()
            );
            data.setStartAuthority(
                    this.getInt("start_mynkod-person"),
                    importMetadata.getImportTime()
            );
            updated = true;
        }
        return updated;
    }

    @Override
    protected PersonBaseData createEmptyBaseData() {
        return new PersonBaseData();
    }

    @Override
    public String getRecordType() {
        return RECORDTYPE_PERSON;
    }

    @Override
    public HashSet<OffsetDateTime> getRegistrationTimestamps() {
        HashSet<OffsetDateTime> timestamps = super.getRegistrationTimestamps();
        timestamps.add(this.statusTemporality.registrationFrom);
        timestamps.add(this.motherTemporality.registrationFrom);
        timestamps.add(this.fatherTemporality.registrationFrom);
        timestamps.add(this.motherVerificationTemporality.registrationFrom);
        timestamps.add(this.fatherVerificationTemporality.registrationFrom);
        timestamps.add(this.positionTemporality.registrationFrom);
        timestamps.add(this.birthTemporality.registrationFrom);
        return timestamps;
    }

    @Override
    public List<Bitemporality> getBitemporality() {
        return Arrays.asList(
                this.statusTemporality,
                this.motherTemporality,
                this.fatherTemporality,
                this.motherVerificationTemporality,
                this.fatherVerificationTemporality,
                this.positionTemporality,
                this.birthTemporality
        );
    }


    @Override
    public Set<PersonEffect> getEffects() {
        HashSet<PersonEffect> effects = new HashSet<>();
        effects.add(new PersonEffect(null, this.getOffsetDateTime("statushaenstart"), this.getBoolean("statusdto_umrk"), null, false));
        effects.add(new PersonEffect(null, this.getOffsetDateTime("mor_dt"), this.getBoolean("mor_dt_umrk"), null, false));
        effects.add(new PersonEffect(null, this.getOffsetDateTime("far_dt"), this.getBoolean("far_dt_umrk"), null, false));
        effects.add(new PersonEffect(null, this.getOffsetDateTime("start_dt-person"), this.getBoolean("start_dt_umrk-person"), this.getOffsetDateTime("slut_dt-person"), this.getBoolean("slut_dt_umrk-person")));
        effects.add(new PersonEffect(null, null, false, null, false));
        return effects;
    }
}
