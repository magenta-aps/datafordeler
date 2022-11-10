package dk.magenta.datafordeler.cpr.records.person;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.Mapping;
import dk.magenta.datafordeler.cpr.records.person.data.ProtectionDataRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Record for Person protection (type 015).
 */
public class ProtectionRecord extends PersonDataRecord {

    private final CprBitemporality protectionTemporality;

    public ProtectionRecord(String line) throws ParseException {
        this(line, traditionalMapping);
    }

    public ProtectionRecord(String line, Mapping mapping) throws ParseException {
        super(line);
        this.obtain(mapping);

        this.protectionTemporality = new CprBitemporality(this.getOffsetDateTime("start_ts-beskyttelse"), null, this.getOffsetDateTime("start_dt-beskyttelse"), false, this.getOffsetDateTime("slet_dt-beskyttelse"), false);
    }

    public static final Mapping traditionalMapping = new Mapping();

    static {
        traditionalMapping.add("beskyttype", 14, 4);
        traditionalMapping.add("start_mynkod-beskyttelse", 18, 4);
        traditionalMapping.add("start_ts-beskyttelse", 22, 12);
        traditionalMapping.add("start_dt-beskyttelse", 34, 10);
        traditionalMapping.add("indrap-beskyttelse", 44, 3);
        traditionalMapping.add("slet_dt-beskyttelse", 47, 10);
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
                this.getBoolean("indrap-beskyttelse", false),
                this.getDate("slet_dt-beskyttelse")
        ).setAuthority(
                this.getInt("start_mynkod-beskyttelse")
        ).setBitemporality(
                new CprBitemporality(
                        this.getOffsetDateTime("start_ts-beskyttelse"),
                        null,
                        this.getOffsetDateTime("start_dt-beskyttelse"), false,
                        this.getOffsetDateTime("slet_dt-beskyttelse"), false
                )
        ));

        return records;
    }

}
