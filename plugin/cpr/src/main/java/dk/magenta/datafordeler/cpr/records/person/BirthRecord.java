package dk.magenta.datafordeler.cpr.records.person;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.Mapping;
import dk.magenta.datafordeler.cpr.records.person.data.BirthPlaceDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.BirthPlaceVerificationDataRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Record for Person birth (type 025).
 */
public class BirthRecord extends PersonDataRecord {

    private final CprBitemporality birthTemporality;
    private final CprBitemporality documentTemporality;

    public BirthRecord(String line) throws ParseException {
        this(line, traditionalMapping);
    }

    public BirthRecord(String line, Mapping mapping) throws ParseException {
        super(line);
        this.obtain(mapping);

        this.birthTemporality = new CprBitemporality(this.getOffsetDateTime("fødested_ts"));
        this.documentTemporality = new CprBitemporality(this.getOffsetDateTime("dok_ts-fødested"));
    }

    public static final Mapping traditionalMapping = new Mapping();

    static {
        traditionalMapping.add("start_mynkod-fødested", 14, 4);
        traditionalMapping.add("fødested_ts", 18, 12);
        traditionalMapping.add("myntxt_mynkod-fødested", 30, 4);
        traditionalMapping.add("myntxt_ts-fødested", 34, 12);
        traditionalMapping.add("myntxt-fødested", 46, 20);
        traditionalMapping.add("dok_mynkod-fødested", 66, 4);
        traditionalMapping.add("dok_ts-fødested", 70, 12);
        traditionalMapping.add("dok-fødested", 82, 3);
    }

    @Override
    public String getRecordType() {
        return RECORDTYPE_BIRTH;
    }

    @Override
    public List<CprBitemporalRecord> getBitemporalRecords() {

        ArrayList<CprBitemporalRecord> records = new ArrayList<>();

        records.add(new BirthPlaceDataRecord(
                this.getInt("myntxt_mynkod-fødested", true, 0),
                this.getString("myntxt-fødested", true)
        ).setAuthority(
                this.getInt("start_mynkod-fødested")
        ).setBitemporality( // TODO: Monotemporal?
                this.birthTemporality
        ));

        if (this.has("dok-fødested")) {
            records.add(new BirthPlaceVerificationDataRecord(
                    this.getBoolean("dok-fødested")
                    //importMetadata.getImportTime()
            ).setAuthority(
                    this.getInt("dok_mynkod-fødested")
            ).setBitemporality(
                    this.documentTemporality
            ));
        }

        return records;
    }
}
