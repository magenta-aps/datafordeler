package dk.magenta.datafordeler.cpr.parsers;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.cpr.records.CprRecord;
import dk.magenta.datafordeler.cpr.records.person.*;
import dk.magenta.datafordeler.cpr.records.residence.ResidenceRecord;
import dk.magenta.datafordeler.cpr.records.road.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;


/**
 * Created by lars on 04-11-14.
 */
@Component
public class RoadParser extends CprSubParser<RoadDataRecord> {

    public RoadParser() {
    }

    private Logger log = LogManager.getLogger(RoadParser.class);

    @Override
    public Logger getLog() {
        return this.log;
    }

    @Override
    protected RoadDataRecord parseLine(String recordType, String line) {
        this.logType(recordType);
        try {
            switch (recordType) {
                case RoadDataRecord.RECORDTYPE_ROAD:
                    return new RoadRecord(line);
                case RoadDataRecord.RECORDTYPE_ROADMEMO:
                    return new RoadMemoRecord(line);
                case RoadDataRecord.RECORDTYPE_ROADPOSTCODE:
                    return new RoadPostcodeRecord(line);
                case RoadDataRecord.RECORDTYPE_ROADCITY:
                    return new RoadCityRecord(line);
                // TODO: Add one of these for each type...
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

}