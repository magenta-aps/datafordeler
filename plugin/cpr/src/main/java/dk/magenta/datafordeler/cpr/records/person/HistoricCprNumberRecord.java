package dk.magenta.datafordeler.cpr.records.person;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.person.data.PersonNumberDataRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Record for Person historic cpr number (type 065).
 */
public class HistoricCprNumberRecord extends HistoricPersonDataRecord {

    private final CprBitemporality cprTemporality;

    public HistoricCprNumberRecord(String line) throws ParseException {
        super(line);
        this.obtain("start_mynkod-pnrgaeld", 14, 4);
        this.obtain("gammelt_pnr", 18, 10);
        this.obtain("start_dt-person", 28, 10);
        this.obtain("start_dt_umrk-person", 38, 1);
        this.obtain("slut_dt-person", 39, 10);
        this.obtain("slut_dt_umrk-person", 49, 1);
        this.cprTemporality = new CprBitemporality(
                null, null,
                this.getOffsetDateTime("start_dt-person"), this.getBoolean("start_dt_umrk-person"),
                this.getOffsetDateTime("slut_dt-person"), this.getBoolean("slut_dt_umrk-person")
        );
    }

    @Override
    public String getRecordType() {
        return RECORDTYPE_HISTORIC_CPRNUMBER;
    }


    @Override
    public List<CprBitemporalRecord> getBitemporalRecords() {

        ArrayList<CprBitemporalRecord> records = new ArrayList<>();

        records.add(new PersonNumberDataRecord(
                this.getString("gammelt_pnr", false)
        ).setAuthority(
                this.getInt("start_mynkod-pnrgaeld")
        ).setBitemporality(
                this.cprTemporality
        ).setHistoric());

        return records;
    }

}
