package dk.magenta.datafordeler.cpr.records.road;

import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.cpr.data.road.RoadEffect;
import dk.magenta.datafordeler.cpr.data.road.data.RoadBaseData;
import dk.magenta.datafordeler.cpr.records.Bitemporality;
import org.hibernate.Session;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by lars on 28-06-17.
 */
public class RoadMemoRecord extends RoadDataRecord {

    private Bitemporality memoTemporality;

    public RoadMemoRecord(String line) throws ParseException {
        super(line);
        this.obtain("timestamp", 54, 12);
        this.obtain("notatnr", 12, 2);
        this.obtain("notatlinie", 14, 40);
        this.obtain("haenstart", 66, 12);

        this.memoTemporality = new Bitemporality(this.getOffsetDateTime("timestamp"), null, this.getOffsetDateTime("haenstart"), false, null, false);
    }

    @Override
    public String getRecordType() {
        return RECORDTYPE_ROADMEMO;
    }

    @Override
    public boolean populateBaseData(RoadBaseData data, RoadEffect effect, OffsetDateTime registrationTime, QueryManager queryManager, Session session) {
        if (this.memoTemporality.matches(registrationTime, effect)) {
            data.addMemo(
                    this.getInt("notatnr"),
                    this.get("notatlinie")
            );
            return true;
        }
        return false;
    }

    @Override
    public Set<RoadEffect> getEffects() {
            HashSet<RoadEffect> effects = new HashSet<>();
            effects.add(new RoadEffect(null, null, false, null, false));
            return effects;
    }

    @Override
    public HashSet<OffsetDateTime> getRegistrationTimestamps() {
        HashSet<OffsetDateTime> timestamps = super.getRegistrationTimestamps();
        timestamps.add(this.memoTemporality.registrationFrom);
        return timestamps;
    }

    @Override
    public List<Bitemporality> getBitemporality() {
        return Collections.singletonList(this.memoTemporality);
    }

}