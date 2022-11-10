package dk.magenta.datafordeler.cpr.records.person;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.Mapping;
import dk.magenta.datafordeler.cpr.records.person.data.ChurchDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.ChurchVerificationDataRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Record for Person church relation (type 010).
 */
public class ChurchRecord extends PersonDataRecord {


    public ChurchRecord(String line) throws ParseException {
        this(line, traditionalMapping);
    }

    public ChurchRecord(String line, Mapping mapping) throws ParseException {
        super(line);
        this.obtain(mapping);
    }

    public static final Mapping traditionalMapping = new Mapping();

    static {
        traditionalMapping.add("start_mynkod-folkekirke", 14, 4);
        traditionalMapping.add("fkirk_ts", 18, 12);
        traditionalMapping.add("fkirk", 30, 1);
        traditionalMapping.add("start_dt-folkekirke", 31, 10);
        traditionalMapping.add("start_dt-umrk-folkekirke", 41, 1);
        traditionalMapping.add("dok_mynkod-folkekirke", 42, 4);
        traditionalMapping.add("dok_ts-folkekirke", 46, 12);
        traditionalMapping.add("dok-folkekirke", 58, 3);
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
                this.getInt("start_mynkod-folkekirke", true, 0)
        ).setBitemporality(
                new CprBitemporality(
                        this.getOffsetDateTime("fkirk_ts"),
                        null,
                        this.getOffsetDateTime("start_dt-folkekirke"), this.getBoolean("start_dt-umrk-folkekirke"),
                        null, false
                )
        ));


        if (this.hasAny("dok-folkekirke", "dok_mynkod-folkekirke")) {
            records.add(new ChurchVerificationDataRecord(
                    this.getBoolean("dok-folkekirke")
            ).setAuthority(
                    this.getInt("dok_mynkod-folkekirke")
            ).setBitemporality(
                    new CprBitemporality(
                            this.getOffsetDateTime("dok_ts-folkekirke")
                    )
            ));
        }

        return records;
    }

}
