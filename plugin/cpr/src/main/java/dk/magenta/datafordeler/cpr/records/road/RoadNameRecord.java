package dk.magenta.datafordeler.cpr.records.road;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.road.data.RoadNameBitemporalRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Record for Road names (type 001).
 */
public class RoadNameRecord extends RoadDataRecord {

    private final CprBitemporality roadTemporality;

    public RoadNameRecord(String line) throws ParseException {
        super(line);
        this.obtain("timestamp", 12, 12);
        this.obtain("tilkomkod", 24, 4);
        this.obtain("tilvejkod", 28, 4);
        this.obtain("frakomkod", 32, 4);
        this.obtain("fravejkod", 36, 4);
        this.obtain("haenstart", 40, 12);
        this.obtain("vejadrnvn", 52, 20);
        this.obtain("vejnvn", 72, 40);

        this.roadTemporality = new CprBitemporality(this.getOffsetDateTime("timestamp"), null, this.getOffsetDateTime("haenstart"), false, null, false);
    }


    @Override
    public String getRecordType() {
        return RECORDTYPE_ROAD;
    }

    @Override
    public List<CprBitemporalRecord> getBitemporalRecords() {
        List<CprBitemporalRecord> records = new ArrayList<>();
        records.add(
                new RoadNameBitemporalRecord(
                        this.getInt("tilkomkod"),
                        this.getInt("tilvejkod"),
                        this.getInt("frakomkod"),
                        this.getInt("fravejkod"),
                        this.getString("vejadrnvn", true),
                        this.getString("vejnvn", true)
                ).setBitemporality(
                        this.roadTemporality
                )
        );
        return records;
    }

}
