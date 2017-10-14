package dk.magenta.datafordeler.cpr.parsers;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.cpr.records.residence.ResidenceRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;


/**
 * Created by lars on 04-11-14.
 */
@Component
public class ResidenceParser extends CprSubParser<ResidenceRecord> {

    public ResidenceParser() {
    }

    private Logger log = LogManager.getLogger(ResidenceParser.class);

    @Override
    public Logger getLog() {
        return this.log;
    }

    @Override
    public ResidenceRecord parseLine(String recordType, String line) {
        this.logType(recordType);
        try {
            switch (recordType) {
               case ResidenceRecord.RECORDTYPE_RESIDENCE:
                    return new ResidenceRecord(line);
                // TODO: Add one of these for each type...
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

}
