package dk.magenta.datafordeler.cpr.records.person;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.Mapping;
import dk.magenta.datafordeler.cpr.records.person.data.CustodyDataRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Record for Person foreign address (type 046).
 */
public class CustodyRecord extends PersonDataRecord {

    private CprBitemporality temporality;

    public CustodyRecord(String line) throws ParseException {
        this(line, traditionalMapping);
    }

     public CustodyRecord(String line, Mapping mapping) throws ParseException {
        super(line);
        this.obtain(mapping);

        this.temporality = new CprBitemporality(
                this.getOffsetDateTime("start_dt-umyndig"),
                null,
                this.getOffsetDateTime("start_dt-umyndig"),
                this.getMarking("start_dt_umrk-umyndig"),
                this.getOffsetDateTime("slet_dt-umyndig"),
                false
        );
    }

    public static final Mapping traditionalMapping = new Mapping();
    static {
        traditionalMapping.add("start_mynkod-umyndig", 14, 4);
        traditionalMapping.add("start_dt-umyndig", 18, 10);
        traditionalMapping.add("start_dt_umrk-umyndig", 28, 1);
        traditionalMapping.add("slet_dt-umyndig", 29, 10);
        traditionalMapping.add("umyn_reltyp", 39, 4);
        traditionalMapping.add("reltyp-relpnr_pnr", 43, 4);
        traditionalMapping.add("start_mynkod-relpnr_pnr", 47, 4);
        traditionalMapping.add("relpnr", 51, 10);
        traditionalMapping.add("start_dt-relpnr_pnr", 61, 10);
        traditionalMapping.add("reltyp-relpnr_txt", 71, 4);
        traditionalMapping.add("start_mynkod-relpnr_txt", 75, 4);
        traditionalMapping.add("reladrsat_relpnr_txt", 79, 34);
        traditionalMapping.add("start_dt-relpnr_txt", 113, 10);
        traditionalMapping.add("reltxt1", 123, 34);
        traditionalMapping.add("reltxt2", 157, 34);
        traditionalMapping.add("reltxt3", 191, 34);
        traditionalMapping.add("reltxt4", 225, 34);
        traditionalMapping.add("reltxt5", 259, 34);
    }

    @Override
    public String getRecordType() {
        return RECORDTYPE_GUARDIANSHIP;
    }

    @Override
    public List<CprBitemporalRecord> getBitemporalRecords() {

        ArrayList<CprBitemporalRecord> records = new ArrayList<>();

        records.add(new CustodyDataRecord(
            this.getInt("umyn_reltyp"),
            this.getInt("reltyp-relpnr_pnr"),
            this.getInt("start_mynkod-relpnr_pnr"),
            this.getString("relpnr", false),
            this.getDate("start_dt-relpnr_pnr"),
            this.getInt("reltyp-relpnr_txt"),
            this.getInt("start_mynkod-relpnr_txt"),
            this.getString("reladrsat_relpnr_txt", false),
            this.getDate("start_dt-relpnr_txt"),
            this.getString("reltxt1", false),
            this.getString("reltxt2", false),
            this.getString("reltxt3", false),
            this.getString("reltxt4", false),
            this.getString("reltxt5", false)
        ).setAuthority(
            this.getInt("start_mynkod-umyndig")
        ).setBitemporality(
            this.temporality
        ));

        return records;
    }

}
