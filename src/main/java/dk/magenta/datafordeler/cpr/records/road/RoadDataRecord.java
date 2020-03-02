package dk.magenta.datafordeler.cpr.records.road;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.cpr.data.road.RoadEffect;
import dk.magenta.datafordeler.cpr.data.road.data.RoadBaseData;
import dk.magenta.datafordeler.cpr.records.CprDataRecord;

import java.time.OffsetDateTime;
import java.util.HashSet;

/**
 * Superclass for Road records
 */
public abstract class RoadDataRecord extends CprDataRecord<RoadEffect, RoadBaseData> {

    public static final String RECORDTYPE_ROAD = "001";
    public static final String RECORDTYPE_ROADCITY = "003";
    public static final String RECORDTYPE_ROADPOSTCODE = "004";
    public static final String RECORDTYPE_ROADMEMO = "005";
    // TODO: Add one for each data type

    public RoadDataRecord(String line) throws ParseException {
        super(line);
        this.obtain("komkod", 4, 4);
        this.obtain("vejkod", 8, 4);
    }

    public int getMunicipalityCode() {
        return this.getInt("komkod");
    }

    public int getRoadCode() {
        return this.getInt("vejkod");
    }

    public HashSet<OffsetDateTime> getRegistrationTimestamps() {
        return new HashSet<>();
    }

    protected RoadBaseData createEmptyBaseData() {
        return new RoadBaseData();
    }

    @Override
    protected RoadEffect createEffect(OffsetDateTime effectFrom, boolean effectFromUncertain, OffsetDateTime effectTo, boolean effectToUncertain) {
        RoadEffect effect = new RoadEffect(null, effectFrom, effectTo);
        effect.setEffectFromUncertain(effectFromUncertain);
        effect.setEffectToUncertain(effectToUncertain);
        return effect;
    }

}
