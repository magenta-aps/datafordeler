package dk.magenta.datafordeler.cpr.records.person;

import dk.magenta.datafordeler.core.exception.ParseException;

/**
 * Superclass for Person records
 */
public abstract class HistoricPersonDataRecord extends PersonDataRecord {

    public HistoricPersonDataRecord(String line) throws ParseException {
        super(line);
    }

}
