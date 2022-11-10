package dk.magenta.datafordeler.cpr.records.person;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.Mapping;
import dk.magenta.datafordeler.cpr.records.person.data.CitizenshipDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.CitizenshipVerificationDataRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Record for Person citizenship (type 040).
 */
public class CitizenshipRecord extends PersonDataRecord {

    private final CprBitemporality citizenshipTemporality;
    private final CprBitemporality documentTemporality;

    public CitizenshipRecord(String line) throws ParseException {
        this(line, traditionalMapping);
    }

    public CitizenshipRecord(String line, Mapping mapping) throws ParseException {
        super(line);
        this.obtain(mapping);

        this.citizenshipTemporality = new CprBitemporality(this.getOffsetDateTime("stat_ts"), null, this.getOffsetDateTime("haenstart-statsborgerskab"), this.getBoolean("haenstart_umrk-statsborgerskab"), null, false);
        this.documentTemporality = new CprBitemporality(this.getOffsetDateTime("dok_ts-statsborgerskab"));
    }

    public static final Mapping traditionalMapping = new Mapping();

    static {
        traditionalMapping.add("stat_ts", 18, 12);
        traditionalMapping.add("landekod", 30, 4);
        traditionalMapping.add("haenstart-statsborgerskab", 34, 12);
        traditionalMapping.add("haenstart_umrk-statsborgerskab", 46, 1);
        traditionalMapping.add("dok_mynkod-statsborgerskab", 47, 4);
        traditionalMapping.add("dok_ts-statsborgerskab", 51, 12);
        traditionalMapping.add("dok-statsborgerskab", 63, 3);
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
                this.getInt("start_mynkod-statsborgerskab", 0)
        ).setBitemporality( // TODO: Monotemporal?
                this.citizenshipTemporality
        ));

        if (this.hasAny("dok-statsborgerskab", "dok_mynkod-statsborgerskab")) {
            if (this.has("dok-statsborgerskab")) {
                records.add(new CitizenshipVerificationDataRecord(
                        this.getBoolean("dok-statsborgerskab")
                ).setAuthority(
                        this.getInt("dok_mynkod-statsborgerskab")
                ).setBitemporality(
                        this.documentTemporality
                ));
            }
        }

        return records;
    }

}
