package dk.magenta.datafordeler.cpr.records.person;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.person.data.CivilStatusAuthorityTextDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.CivilStatusDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.CivilStatusVerificationDataRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Record for Person civil status (type 035).
 */
public class CivilStatusRecord extends PersonDataRecord {

    private CprBitemporality civilTemporality;
    private CprBitemporality documentTemporality;
    private CprBitemporality officiaryTemporality;

    public CivilStatusRecord(String line) throws ParseException {
        super(line);
        this.obtain("start_mynkod-civilstand", 14, 4);
        this.obtain("civ_ts", 18, 12);
        this.obtain("civst", 30, 1);
        this.obtain("aegtepnr", 31, 10);
        this.obtain("aegtefoed_dt", 41, 10);
        this.obtain("aegtefoeddt_umrk", 51, 1);
        this.obtain("aegtenvn", 52, 34);
        this.obtain("aegtenvn_mrk", 86, 1);
        this.obtain("haenstart-civilstand", 87, 12);
        this.obtain("haenstart_umrk-civilstand", 99, 1);
        this.obtain("dok_mynkod-civilstand", 100, 4);
        this.obtain("dok_ts-civilstand", 104, 12);
        this.obtain("dok-civilstand", 116, 3);
        this.obtain("myntxt_mynkod-civilstand", 119, 4);
        this.obtain("myntxt_ts-civilstand", 123, 12);
        this.obtain("myntxt-civilstand", 135, 20);
        this.obtain("sep_henvis_ts", 155, 12);

        this.civilTemporality = new CprBitemporality(this.getOffsetDateTime("civ_ts"), null, this.getOffsetDateTime("haenstart-civilstand"), this.getBoolean("haenstart_umrk-civilstand"), null, false);
        this.documentTemporality = new CprBitemporality(this.getOffsetDateTime("dok_ts-civilstand"));
        this.officiaryTemporality = new CprBitemporality(this.getOffsetDateTime("myntxt_ts-civilstand"));
    }

    @Override
    public String getRecordType() {
        return RECORDTYPE_CIVILSTATUS;
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
        ));

        records.add(new CivilStatusVerificationDataRecord(
                this.getBoolean("dok-civilstand"),
                null
        ).setAuthority(
                this.getInt("dok_mynkod-civilstand")
        ).setBitemporality(
                this.documentTemporality
        ));

        records.add(new CivilStatusAuthorityTextDataRecord(
                this.getString("myntxt-civilstand", true),
                null
        ).setAuthority(
                this.getInt("myntxt_mynkod-civilstand")
        ).setBitemporality(
                this.officiaryTemporality
        ));

        return records;
    }

}
