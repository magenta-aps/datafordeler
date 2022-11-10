package dk.magenta.datafordeler.cpr.parsers;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.cpr.records.road.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;


/**
 * Parser for the Road line format.
 */
@Component
public class RoadParser extends CprSubParser<RoadDataRecord> {

    public RoadParser() {
    }

    private final Logger log = LogManager.getLogger(RoadParser.class.getCanonicalName());

    @Override
    public Logger getLog() {
        return this.log;
    }

    @Override
    public RoadDataRecord parseLine(String recordType, String line) {
        this.logType(recordType);
        try {
            switch (recordType) {
                case RoadDataRecord.RECORDTYPE_ROAD:
                    return new RoadNameRecord(line);
                case RoadDataRecord.RECORDTYPE_ROADMEMO:
                    return new RoadMemoRecord(line);
                case RoadDataRecord.RECORDTYPE_ROADPOSTCODE:
                    return new RoadPostcodeRecord(line);
                case RoadDataRecord.RECORDTYPE_ROADCITY:
                    return new RoadCityRecord(line);
                // TODO: Add one of these for each type...
            }
        } catch (ParseException e) {
            log.error("ParseException", e);
        }
        return null;
    }

}
