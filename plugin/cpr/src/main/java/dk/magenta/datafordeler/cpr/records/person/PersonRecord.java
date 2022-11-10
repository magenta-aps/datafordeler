package dk.magenta.datafordeler.cpr.records.person;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.Mapping;
import dk.magenta.datafordeler.cpr.records.person.data.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Record for Person base data (type 001).
 */
public class PersonRecord extends PersonDataRecord {

    private CprBitemporality statusTemporality;
    private CprBitemporality motherTemporality;
    private CprBitemporality fatherTemporality;
    private CprBitemporality motherVerificationTemporality;
    private CprBitemporality fatherVerificationTemporality;
    private CprBitemporality positionTemporality;
    private CprBitemporality birthTemporality;

    private boolean hasPnrGaeld = false;

    public PersonRecord(String line) throws ParseException {
        this(line, traditionalMapping);
    }

    public PersonRecord(String line, Mapping mapping) throws ParseException {
        super(line);
        this.obtain(mapping);

        OffsetDateTime statusEffectTime = this.getOffsetDateTime("statushaenstart");
        if (statusEffectTime == null) {
            statusEffectTime = this.getOffsetDateTime("status_ts");
        }
        if (this.has("status_ts")) {
            this.statusTemporality = new CprBitemporality(this.getOffsetDateTime("status_ts"), null, statusEffectTime, this.getBoolean("statusdto_umrk"), null, false);
        }
        if (this.has("pnrmor")) {
            this.motherTemporality = new CprBitemporality(this.getOffsetDateTime("mor_ts"), null, this.getOffsetDateTime("mor_dt"), this.getBoolean("mor_dt_umrk"), null, false);
            this.motherVerificationTemporality = new CprBitemporality(this.getOffsetDateTime("mor_dok_ts"));
        }
        if (this.has("pnrfar")) {
            this.fatherTemporality = new CprBitemporality(this.getOffsetDateTime("far_ts"), null, this.getOffsetDateTime("far_dt"), this.getBoolean("far_dt_umrk"), null, false);
            this.fatherVerificationTemporality = new CprBitemporality(this.getOffsetDateTime("far_dok_ts"));
        }
        if (this.has("stilling")) {
            this.positionTemporality = new CprBitemporality(this.getOffsetDateTime("stilling_ts"));
        }
        if (this.has("start_ts-person")) {
            this.birthTemporality = new CprBitemporality(this.getOffsetDateTime("start_ts-person"), null, this.getOffsetDateTime("start_dt-person"), this.getBoolean("start_dt_umrk-person"), this.getOffsetDateTime("slut_dt-person"), this.getBoolean("slut_dt_umrk-person"));
        }
        if (this.has("pnrgaeld")) {
            this.hasPnrGaeld = !this.getString("pnrgaeld", false).trim().isEmpty();
        }
    }

    public static final Mapping traditionalMapping = new Mapping();

    static {
        traditionalMapping.add("pnrgaeld", 14, 10);
        traditionalMapping.add("status_ts", 24, 12);
        traditionalMapping.add("status", 36, 2);
        traditionalMapping.add("statushaenstart", 38, 12);
        traditionalMapping.add("statusdto_umrk", 50, 1);
        traditionalMapping.add("start_mynkod-person", 51, 4);
        traditionalMapping.add("start_ts-person", 55, 12);
        traditionalMapping.add("koen", 67, 1);
        traditionalMapping.add("foed_dt", 68, 10);
        traditionalMapping.add("foed_dt_umrk", 78, 1);
        traditionalMapping.add("foed_tm", 79, 8);
        traditionalMapping.add("foedsekvens", 87, 4);
        traditionalMapping.add("start_dt-person", 91, 10);
        traditionalMapping.add("start_dt_umrk-person", 101, 1);
        traditionalMapping.add("slut_dt-person", 102, 10);
        traditionalMapping.add("slut_dt_umrk-person", 112, 1);
        traditionalMapping.add("stilling_mynkod", 113, 4);
        traditionalMapping.add("stilling_ts", 117, 12);
        traditionalMapping.add("stilling", 129, 34);
        traditionalMapping.add("mor_ts", 163, 12);
        traditionalMapping.add("mor_mynkod", 175, 4);
        traditionalMapping.add("mor_dt", 179, 10);
        traditionalMapping.add("mor_dt_umrk", 189, 1);
        traditionalMapping.add("pnrmor", 190, 10);
        traditionalMapping.add("mor_foed_dt", 200, 10);
        traditionalMapping.add("mor_foed_dt_umrk", 210, 1);
        traditionalMapping.add("mornvn", 211, 34);
        traditionalMapping.add("mornvn_mrk", 245, 1);
        traditionalMapping.add("mor_dok_mynkod", 246, 4);
        traditionalMapping.add("mor_dok_ts", 250, 12);
        traditionalMapping.add("mor_dok", 262, 3);
        traditionalMapping.add("far_ts", 265, 12);
        traditionalMapping.add("far_mynkod", 277, 4);
        traditionalMapping.add("far_dt", 281, 10);
        traditionalMapping.add("far_dt_umrk", 291, 1);
        traditionalMapping.add("pnrfar", 292, 10);
        traditionalMapping.add("far_foed_dt", 302, 10);
        traditionalMapping.add("far_foed_dt_umrk", 312, 1);
        traditionalMapping.add("farnvn", 313, 34);
        traditionalMapping.add("farnvn_mrk", 347, 1);
        traditionalMapping.add("far_dok_mynkod", 348, 4);
        traditionalMapping.add("far_dok_ts", 352, 12);
        traditionalMapping.add("far_dok", 364, 3);
    }

    @Override
    public String getRecordType() {
        return RECORDTYPE_PERSON;
    }

    @Override
    public List<CprBitemporalRecord> getBitemporalRecords() {

        ArrayList<CprBitemporalRecord> records = new ArrayList<>();

        if (this.hasPnrGaeld) {
            records.add(new PersonNumberDataRecord(
                    this.getString("pnrgaeld", false)
            ));
        }

        if (this.has("status")) {
            records.add(new PersonStatusDataRecord(
                    this.getInt("status", true)
            ).setBitemporality(
                    this.statusTemporality
            ));
        }

        if (this.has("pnrmor")) {
            records.add(new ParentDataRecord(
                    true,
                    this.getString("pnrmor", false),
                    this.getDate("mor_foed_dt"),
                    this.getBoolean("mor_foed_dt_umrk"),
                    this.getString("mornvn", true),
                    this.getBoolean("mornvn_mrk")
            ).setAuthority(
                    this.getInt("mor_mynkod")
            ).setBitemporality(
                    this.motherTemporality
            ));
        }

        if (this.has("mor_dok")) {
            records.add(new ParentVerificationDataRecord(
                    this.getBoolean("mor_dok"),
                    true
            ).setAuthority(
                    this.getInt("mor_dok_mynkod")
            ).setBitemporality(
                    this.motherVerificationTemporality
            ));
        }

        if (this.has("pnrfar")) {
            records.add(new ParentDataRecord(
                    false,
                    this.getString("pnrfar", false),
                    this.getDate("far_foed_dt"),
                    this.getBoolean("far_foed_dt_umrk"),
                    this.getString("farnvn", false),
                    this.getBoolean("farnvn_mrk")
            ).setAuthority(
                    this.getInt("far_mynkod")
            ).setBitemporality(
                    this.fatherTemporality
            ));
        }

        if (this.has("far_dok")) {
            records.add(new ParentVerificationDataRecord(
                    this.getBoolean("far_dok"),
                    false
            ).setAuthority(
                    this.getInt("far_dok_mynkod")
            ).setBitemporality(
                    this.fatherVerificationTemporality
            ));
        }

        if (this.has("stilling")) {
            records.add(new PersonPositionDataRecord(
                    this.getString("stilling", true)
            ).setAuthority(
                    this.getInt("stilling_mynkod")
            ).setBitemporality(
                    this.positionTemporality
            ));
        }

        if (this.has("foed_dt")) {
            records.add(new BirthTimeDataRecord(
                    this.getDateTime("foed_dt", "foed_tm"),
                    this.getBoolean("foed_dt_umrk"),
                    this.getInt("foedsekvens")
            ).setAuthority(
                    this.getInt("start_mynkod-person")
            ).setBitemporality(
                    this.birthTemporality
            ));
        }

        if (this.has("koen")) {
            records.add(new PersonCoreDataRecord(
                    this.getString("koen", true)
            ).setAuthority(
                    this.getInt("start_mynkod-person")
            ).setBitemporality(
                    this.birthTemporality
            ));
        }

        return records;
    }

}
