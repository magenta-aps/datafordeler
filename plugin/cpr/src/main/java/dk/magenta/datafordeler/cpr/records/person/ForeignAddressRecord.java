package dk.magenta.datafordeler.cpr.records.person;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.Mapping;
import dk.magenta.datafordeler.cpr.records.person.data.ForeignAddressDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.ForeignAddressEmigrationDataRecord;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Record for Person foreign address (type 028).
 */
public class ForeignAddressRecord extends PersonDataRecord {

    private final CprBitemporality emigrationTemporality;
    private final CprBitemporality foreignAddressTemporality;

    public ForeignAddressRecord(String line) throws ParseException {
        this(line, traditionalMapping);
    }

    public ForeignAddressRecord(String line, Mapping mapping) throws ParseException {
        super(line);
        this.obtain(mapping);

        OffsetDateTime effectFrom = this.getOffsetDateTime("udrdto");
        boolean effectFromUncertain = this.getMarking("udrdto_umrk");
        this.emigrationTemporality = new CprBitemporality(this.getOffsetDateTime("udr_ts"), null, effectFrom, effectFromUncertain, null, false);
        this.foreignAddressTemporality = new CprBitemporality(this.getOffsetDateTime("udlandadr_ts"), null, effectFrom, effectFromUncertain, null, false);
    }

    public static final Mapping traditionalMapping = new Mapping();

    static {
        traditionalMapping.add("start_mynkod-udrindrejse", 14, 4);
        traditionalMapping.add("udr_ts", 18, 12);
        traditionalMapping.add("udr_landekod", 30, 4);
        traditionalMapping.add("udrdto", 34, 12);
        traditionalMapping.add("udrdto_umrk", 46, 1);
        traditionalMapping.add("udlandadr_mynkod", 47, 4);
        traditionalMapping.add("udlandadr_ts", 51, 12);
        traditionalMapping.add("udlandadr1", 63, 34);
        traditionalMapping.add("udlandadr2", 97, 34);
        traditionalMapping.add("udlandadr3", 131, 34);
        traditionalMapping.add("udlandadr4", 165, 34);
        traditionalMapping.add("udlandadr5", 199, 34);
    }

    @Override
    public List<CprBitemporalRecord> getBitemporalRecords() {

        ArrayList<CprBitemporalRecord> records = new ArrayList<>();

        records.add(new ForeignAddressDataRecord(
                this.getString("udlandadr1", false),
                this.getString("udlandadr2", false),
                this.getString("udlandadr3", false),
                this.getString("udlandadr4", false),
                this.getString("udlandadr5", false)
        ).setAuthority(
                this.getInt("udlandadr_mynkod")
        ).setBitemporality(
                this.foreignAddressTemporality
        ));

        records.add(new ForeignAddressEmigrationDataRecord(
                this.getInt("udr_landekod")
        ).setAuthority(
                this.getInt("start_mynkod-udrindrejs")
        ).setBitemporality(
                this.emigrationTemporality
        ));

        return records;
    }


    @Override
    public String getRecordType() {
        return RECORDTYPE_FOREIGN_ADDRESS;
    }

}
