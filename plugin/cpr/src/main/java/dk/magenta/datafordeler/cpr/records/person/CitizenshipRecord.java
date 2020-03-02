package dk.magenta.datafordeler.cpr.records.person;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.person.data.CitizenshipDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.CitizenshipVerificationDataRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Record for Person citizenship (type 040).
 */
public class CitizenshipRecord extends PersonDataRecord {

    private CprBitemporality citizenshipTemporality;
    private CprBitemporality documentTemporality;

    public CitizenshipRecord(String line) throws ParseException {
        super(line);
        this.obtain("start_mynkod-statsborgerskab", 14, 4);
        this.obtain("stat_ts", 18, 12);
        this.obtain("landekod", 30, 4);
        this.obtain("haenstart-statsborgerskab", 34, 12);
        this.obtain("haenstart_umrk-statsborgerskab", 46, 1);
        this.obtain("dok_mynkod-statsborgerskab", 47, 4);
        this.obtain("dok_ts-statsborgerskab", 51, 12);
        this.obtain("dok-statsborgerskab", 63, 3);

        this.citizenshipTemporality = new CprBitemporality(this.getOffsetDateTime("stat_ts"), null, this.getOffsetDateTime("haenstart-statsborgerskab"), this.getBoolean("haenstart_umrk-statsborgerskab"), null, false);
        this.documentTemporality = new CprBitemporality(this.getOffsetDateTime("dok_ts-statsborgerskab"));
    }

    @Override
    public String getRecordType() {
        return RECORDTYPE_CITIZENSHIP;
    }

    @Override
    public List<CprBitemporalRecord> getBitemporalRecords() {

        ArrayList<CprBitemporalRecord> records = new ArrayList<>();

        records.add(new CitizenshipDataRecord(
                this.getInt("landekod")
        ).setAuthority(
                this.getInt("start_mynkod-statsborgerskab")
        ).setBitemporality( // TODO: Monotemporal?
                this.citizenshipTemporality
        ));

        records.add(new CitizenshipVerificationDataRecord(
                this.getBoolean("dok-statsborgerskab")
        ).setAuthority(
                this.getInt("dok_mynkod-statsborgerskab")
        ).setBitemporality(
                this.documentTemporality
        ));

        return records;
    }

}
