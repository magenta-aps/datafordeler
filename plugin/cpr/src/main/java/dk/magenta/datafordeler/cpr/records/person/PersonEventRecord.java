package dk.magenta.datafordeler.cpr.records.person;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.person.data.PersonEventDataRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Record for Personevents (record 900)
 */
public class PersonEventRecord extends HistoricPersonDataRecord {

    public PersonEventRecord(String line) throws ParseException {
        super(line);
        this.obtain("timestamp", 14, 12);
        this.obtain("event", 26, 3);
        this.obtain("derived", 29, 2);
    }

    @Override
    public String getRecordType() {
        return RECORDTYPE_HISTORIC_CPRNUMBER;
    }

    /**
     * Get a list of events
     *
     * @return
     */
    public List<PersonEventDataRecord> getPersonEvents() {
        List<PersonEventDataRecord> records = new ArrayList<>();
        records.add(new PersonEventDataRecord(this.getOffsetDateTime("timestamp")
                , this.getString("event", false)
                , this.getString("derived", false)));
        return records;
    }

    /**
     * Eventinformation does not result in any bitemporal records
     *
     * @return
     */
    @Override
    public List<CprBitemporalRecord> getBitemporalRecords() {
        return new ArrayList<CprBitemporalRecord>();
    }

}
