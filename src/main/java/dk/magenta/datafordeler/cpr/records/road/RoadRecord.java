package dk.magenta.datafordeler.cpr.records.road;

import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.cpr.data.road.RoadEffect;
import dk.magenta.datafordeler.cpr.data.road.data.RoadBaseData;
import org.hibernate.Session;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by lars on 28-06-17.
 */
public class RoadRecord extends RoadDataRecord {

    public RoadRecord(String line) throws ParseException {
        super(line);
        this.obtain("timestamp", 12, 12);
        this.obtain("tilkomkod", 24, 4);
        this.obtain("tilvejkod", 28, 4);
        this.obtain("frakomkod", 32, 4);
        this.obtain("fravejkod", 36, 4);
        this.obtain("haenstart", 40, 12);
        this.obtain("vejadrnvn", 52, 20);
        this.obtain("vejnvn", 72, 40);
    }



    @Override
    protected RoadBaseData createEmptyBaseData() {
        return new RoadBaseData();
    }

    @Override
    public String getRecordType() {
        return RECORDTYPE_ROAD;
    }

    @Override
    public void populateBaseData(RoadBaseData data, RoadEffect effect, OffsetDateTime registrationTime, QueryManager queryManager, Session session) {
        if (registrationTime.equals(this.getOffsetDateTime("timestamp"))) {
            data.setCore(
                    this.getInt("tilkomkod"),
                    this.getInt("tilvejkod"),
                    this.getInt("frakomkod"),
                    this.getInt("fravejkod"),
                    this.get("vejadrnvn"),
                    this.get("vejnvn")
            );
        }
    }

    @Override
    public HashSet<OffsetDateTime> getRegistrationTimestamps() {
        HashSet<OffsetDateTime> timestamps = super.getRegistrationTimestamps();
        timestamps.add(this.getOffsetDateTime("timestamp"));
        return timestamps;
    }

    @Override
    public Set<RoadEffect> getEffects() {
        HashSet<RoadEffect> effects = new HashSet<>();
        effects.add(new RoadEffect(null, this.getOffsetDateTime("haenstart"), false, null, false));
        return effects;
    }

}