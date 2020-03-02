package dk.magenta.datafordeler.cpr.records.person;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.person.data.ChurchDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.ChurchVerificationDataRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Record for Person church relation (type 010).
 */
public class ChurchRecord extends PersonDataRecord {

    private CprBitemporality churchTemporality;
    private CprBitemporality documentTemporality;

    public ChurchRecord(String line) throws ParseException {
        super(line);
        this.obtain("start_mynkod-folkekirke", 14, 4);
        this.obtain("fkirk_ts", 18, 12);
        this.obtain("fkirk", 30, 1);
        this.obtain("start_dt-folkekirke", 31, 10);
        this.obtain("start_dt-umrk-folkekirke", 41, 1);
        this.obtain("dok_mynkod-folkekirke", 42, 4);
        this.obtain("dok_ts-folkekirke", 46, 12);
        this.obtain("dok-folkekirke", 58, 3);
        this.churchTemporality = new CprBitemporality(this.getOffsetDateTime("fkirk_ts"), null, this.getOffsetDateTime("start_dt-folkekirke"), this.getBoolean("start_dt-umrk-folkekirke"), null, false);
        this.documentTemporality = new CprBitemporality(this.getOffsetDateTime("dok_ts-folkekirke"));
    }

    @Override
    public String getRecordType() {
        return RECORDTYPE_CHURCH;
    }

    @Override
    public List<CprBitemporalRecord> getBitemporalRecords() {

        ArrayList<CprBitemporalRecord> records = new ArrayList<>();

        records.add(new ChurchDataRecord(
                this.getChar("fkirk")
        ).setAuthority(
                this.getInt("start_mynkod-folkekirke", true)
        ).setBitemporality(
                this.churchTemporality
        ));

        records.add(new ChurchVerificationDataRecord(
                this.getBoolean("dok-folkekirke")
        ).setAuthority(
                this.getInt("dok_mynkod-folkekirke")
        ).setBitemporality(
                this.documentTemporality
        ));

        return records;
    }

}
