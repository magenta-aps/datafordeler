package dk.magenta.datafordeler.cpr.records.person;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.person.data.ProtectionDataRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Record for Historic Person protection (type 016).
 */
public class HistoricProtectionRecord extends HistoricPersonDataRecord {

    private final CprBitemporality protectionTemporality;

    public HistoricProtectionRecord(String line) throws ParseException {
        super(line);
        this.obtain("beskyttype", 14, 4);
        this.obtain("start_mynkod-beskyttelse", 18, 4);
        this.obtain("start_ts-beskyttelse", 22, 12);
        this.obtain("start_dt-beskyttelse", 34, 10);
        this.obtain("indrap-beskyttelse", 44, 3);
        this.obtain("slet_dt-beskyttelse", 47, 10);

        this.obtain("slut_mynkod-beskyttelse", 57, 4);
        this.obtain("slut_ts-beskyttelse", 61, 12);
        this.obtain("slut_dt-beskyttelse", 73, 10);

        //this.protectionTemporality = new Bitemporality(this.getOffsetDateTime("start_ts-beskyttelse"), this.getOffsetDateTime("slut_ts-beskyttelse"), this.getOffsetDateTime("start_dt-beskyttelse"), false, this.getOffsetDateTime("slut_dt-beskyttelse"), false);
        this.protectionTemporality = new CprBitemporality(this.getOffsetDateTime("start_ts-beskyttelse"), this.getOffsetDateTime("slut_ts-beskyttelse"), this.getOffsetDateTime("start_dt-beskyttelse"), false, this.getOffsetDateTime("slut_dt-beskyttelse"), false);
    }

    @Override
    public String getRecordType() {
        return RECORDTYPE_HISTORIC_PROTECTION;
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

}
