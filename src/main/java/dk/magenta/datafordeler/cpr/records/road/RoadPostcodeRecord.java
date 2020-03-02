package dk.magenta.datafordeler.cpr.records.road;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.cpr.data.road.RoadEffect;
import dk.magenta.datafordeler.cpr.data.road.data.RoadBaseData;
import dk.magenta.datafordeler.cpr.data.unversioned.PostCode;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import org.hibernate.Session;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Override
    public boolean populateBaseData(RoadBaseData data, CprBitemporality bitemporality, Session session, ImportMetadata importMetadata) {
        if (bitemporality.equals(this.postcodeTemporality)) {
            data.addPostcode(
                    this.getString("husnrfra", false),
                    this.getString("husnrtil", false),
                    this.getEven("ligeulige"),
                    PostCode.getPostcode(this.getInt("postnr"), this.getString("postdisttxt", true), session),
                    importMetadata.getImportTime()
            );
            return true;
        }
        return false;
    }

    @Override
    public HashSet<OffsetDateTime> getRegistrationTimestamps() {
        HashSet<OffsetDateTime> timestamps = super.getRegistrationTimestamps();
        timestamps.add(this.postcodeTemporality.registrationFrom);
        return timestamps;
    }

    @Override
    public List<CprBitemporality> getBitemporality() {
        return Collections.singletonList(this.postcodeTemporality);
    }

    private boolean getEven(String key) {
        String value = this.get(key);
        return "L".equalsIgnoreCase(value);
    }

    @Override
    public Set<RoadEffect> getEffects() {
        HashSet<RoadEffect> effects = new HashSet<>();
        effects.add(new RoadEffect(null, null, false, null, false));
        return effects;
    }
}
