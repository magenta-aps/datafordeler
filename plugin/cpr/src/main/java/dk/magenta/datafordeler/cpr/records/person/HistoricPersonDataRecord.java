package dk.magenta.datafordeler.cpr.records.person;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import org.hibernate.Session;

/**
 * Superclass for Person records
 */
public abstract class HistoricPersonDataRecord extends PersonDataRecord {

    public HistoricPersonDataRecord(String line) throws ParseException {
        super(line);
    }

    public abstract boolean cleanBaseData(PersonBaseData data, CprBitemporality bitemporality, CprBitemporality outdatedTemporality, Session session);

}
