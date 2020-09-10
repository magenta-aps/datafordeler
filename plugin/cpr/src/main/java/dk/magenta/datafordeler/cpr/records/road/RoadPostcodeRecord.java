package dk.magenta.datafordeler.cpr.records.road;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.road.data.RoadPostalcodeBitemporalRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Record for Road postcodes (type 004).
 */
public class RoadPostcodeRecord extends RoadDataRecord {

    private CprBitemporality postcodeTemporality;

    public RoadPostcodeRecord(String line) throws ParseException {
        super(line);
        this.obtain("husnrfra", 12, 4);
        this.obtain("husnrtil", 16, 4);
        this.obtain("ligeulige", 20, 1);
        this.obtain("timestamp", 21, 12);
        this.obtain("postnr", 33, 4);
        this.obtain("postdisttxt", 37, 20);

        this.postcodeTemporality = new CprBitemporality(this.getOffsetDateTime("timestamp"));
    }

    @Override
    public String getRecordType() {
        return RECORDTYPE_ROADPOSTCODE;
    }


    private boolean getEven(String key) {
        String value = this.getString(key, true);
        return "L".equalsIgnoreCase(value);
    }

    @Override
    public List<CprBitemporalRecord> getBitemporalRecords() {
        List<CprBitemporalRecord> records = new ArrayList<>();
        records.add(
                new RoadPostalcodeBitemporalRecord(
                        this.getString("husnrtil", false),
                        this.getString("husnrfra", false),
                        this.getEven("ligeulige"),
                        this.getInt("postnr"),
                        this.getString("postdisttxt", true)
                ).setBitemporality(
                        this.postcodeTemporality
                )
        );
        return records;
    }
}
