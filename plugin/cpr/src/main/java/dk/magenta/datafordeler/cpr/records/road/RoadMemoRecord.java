package dk.magenta.datafordeler.cpr.records.road;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.road.data.RoadMemoBitemporalRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Record for Road memos (type 005).
 */
public class RoadMemoRecord extends RoadDataRecord {

    private final CprBitemporality memoTemporality;

    public RoadMemoRecord(String line) throws ParseException {
        super(line);
        this.obtain("timestamp", 54, 12);
        this.obtain("notatnr", 12, 2);
        this.obtain("notatlinie", 14, 40);
        this.obtain("haenstart", 66, 12);

        this.memoTemporality = new CprBitemporality(this.getOffsetDateTime("timestamp"), null, this.getOffsetDateTime("haenstart"), false, null, false);
    }

    @Override
    public String getRecordType() {
        return RECORDTYPE_ROADMEMO;
    }

    @Override
    public List<CprBitemporalRecord> getBitemporalRecords() {
        List<CprBitemporalRecord> records = new ArrayList<>();
        records.add(
                new RoadMemoBitemporalRecord(
                        this.getInt("notatnr"),
                        this.getString("notatlinie", false)
                ).setBitemporality(
                        this.memoTemporality
                )
        );

        return records;
    }

}
