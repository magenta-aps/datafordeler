package dk.magenta.datafordeler.cpr.records.person;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.person.data.ProtectionDataRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Record for Person protection (type 015).
 */
public class ProtectionRecord extends PersonDataRecord {

    private CprBitemporality protectionTemporality;

    public ProtectionRecord(String line) throws ParseException {
        super(line);
        this.obtain("beskyttype",14,4);
        this.obtain("start_mynkod-beskyttelse",18,4);
        this.obtain("start_ts-beskyttelse",22,12	);
        this.obtain("start_dt-beskyttelse",34,10	);
        this.obtain("indrap-beskyttelse",44	,3);
        this.obtain("slet_dt-beskyttelse",47,10);

        this.protectionTemporality = new CprBitemporality(this.getOffsetDateTime("start_ts-beskyttelse"), null, this.getOffsetDateTime("start_dt-beskyttelse"), false, this.getOffsetDateTime("slet_dt-beskyttelse"), false);
    }

    @Override
    public String getRecordType() {
        return RECORDTYPE_PROTECTION;
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
        ).setBitemporality(
                new CprBitemporality(
                        this.getOffsetDateTime("start_ts-beskyttelse"),
                        null,
                        this.getOffsetDateTime("start_dt-beskyttelse"), false,
                        this.getOffsetDateTime("slut_dt-beskyttelse"), false
                )
        ));

        return records;
    }

}
