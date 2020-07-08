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
                this.getOffsetDateTime("start_ts"),
                null,
                this.getOffsetDateTime("start_dt-umyndig"),
                this.getMarking("start_dt_umrk-umyndig"),
                this.getOffsetDateTime("slet_dt-umyndig"),
                false
        );
    }

    public static final Mapping traditionalMapping = new Mapping();
    static {
        traditionalMapping.add("start_ts", 14, 12);
        traditionalMapping.add("umyn_reltyp", 26, 4);//ok
        traditionalMapping.add("start_mynkod-umyndig", 30, 4);//ok
        traditionalMapping.add("start_dt-umyndig", 34, 10);//ok
        traditionalMapping.add("start_dt_umrk-umyndig", 44, 1);//ok
        traditionalMapping.add("slet_dt-umyndig", 45, 10);//ok
        traditionalMapping.add("reltyp-relpnr_pnr", 55, 4);//ok
        traditionalMapping.add("start_mynkod-relpnr_pnr", 59, 4);//ok
        traditionalMapping.add("relpnr", 63, 10);//ok
        traditionalMapping.add("start_dt-relpnr_pnr", 73, 10);//ok
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
