package dk.magenta.datafordeler.cpr.records.person;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
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

        OffsetDateTime statusEffectTime = this.getOffsetDateTime("statushaenstart");
        if (statusEffectTime == null) {
            statusEffectTime = this.getOffsetDateTime("status_ts");
        }
        this.statusTemporality = new CprBitemporality(this.getOffsetDateTime("status_ts"), null, statusEffectTime, this.getBoolean("statusdto_umrk"), null, false);
        this.motherTemporality = new CprBitemporality(this.getOffsetDateTime("mor_ts"), null, this.getOffsetDateTime("mor_dt"), this.getBoolean("mor_dt_umrk"), null, false);
        this.fatherTemporality = new CprBitemporality(this.getOffsetDateTime("far_ts"), null, this.getOffsetDateTime("far_dt"), this.getBoolean("far_dt_umrk"), null, false);
        this.motherVerificationTemporality = new CprBitemporality(this.getOffsetDateTime("mor_dok_ts"));
        this.fatherVerificationTemporality = new CprBitemporality(this.getOffsetDateTime("far_dok_ts"));
        this.positionTemporality = new CprBitemporality(this.getOffsetDateTime("stilling_ts"));
        this.birthTemporality = new CprBitemporality(this.getOffsetDateTime("start_ts-person"), null, this.getOffsetDateTime("start_dt-person"), this.getBoolean("start_dt_umrk-person"), this.getOffsetDateTime("slut_dt-person"), this.getBoolean("slut_dt_umrk-person"));

        this.hasPnrGaeld = !this.getString("pnrgaeld", false).trim().isEmpty();
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

        records.add(new PersonStatusDataRecord(
                this.getInt("status", true)
        ).setBitemporality(
                this.statusTemporality
        ));

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

        records.add(new ParentVerificationDataRecord(
                this.getBoolean("mor_dok"),
                true
        ).setAuthority(
                this.getInt("mor_dok_mynkod")
        ).setBitemporality(
                this.motherVerificationTemporality
        ));

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

        records.add(new ParentVerificationDataRecord(
                this.getBoolean("far_dok"),
                false
        ).setAuthority(
                this.getInt("far_dok_mynkod")
        ).setBitemporality(
                this.fatherVerificationTemporality
        ));

        records.add(new PersonPositionDataRecord(
                this.getString("stilling", true)
        ).setAuthority(
                this.getInt("stilling_mynkod")
        ).setBitemporality(
                this.positionTemporality
        ));

        records.add(new BirthTimeDataRecord(
                this.getDateTime("foed_dt", "foed_tm"),
                this.getBoolean("foed_dt_umrk"),
                this.getInt("foedsekvens")
        ).setAuthority(
                this.getInt("start_mynkod-person")
        ).setBitemporality(
                this.birthTemporality
        ));

        records.add(new PersonCoreDataRecord(
                this.getString("koen", true)
        ).setAuthority(
                this.getInt("start_mynkod-person")
        ).setBitemporality(
                this.birthTemporality
        ));

        return records;
    }

}
