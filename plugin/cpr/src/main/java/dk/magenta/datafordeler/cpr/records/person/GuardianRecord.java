package dk.magenta.datafordeler.cpr.records.person;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.person.data.GuardianDataRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Record for Person foreign address (type 028).
 */
public class GuardianRecord extends PersonDataRecord {

    private CprBitemporality temporality;

    public GuardianRecord(String line) throws ParseException {
        super(line);
        this.obtain("start_mynkod-umyndig", 14, 4);
        this.obtain("start_dt-umyndig", 18, 10);
        this.obtain("start_dt_umrk-umyndig", 28, 1);
        this.obtain("slet_dt-umyndig", 29, 10);
        this.obtain("umyn_reltyp", 39, 4);
        this.obtain("reltyp-relpnr_pnr", 43, 4);
        this.obtain("start_mynkod-relpnr_pnr", 47, 4);
        this.obtain("relpnr", 51, 10);
        this.obtain("start_dt-relpnr_pnr", 61, 10);
        this.obtain("reltyp-relpnr_txt", 71, 4);
        this.obtain("start_mynkod-relpnr_txt", 75, 4);
        this.obtain("reladrsat_relpnr_txt", 79, 34);
        this.obtain("start_dt-relpnr_txt", 113, 10);
        this.obtain("reltxt1", 123, 34);
        this.obtain("reltxt2", 157, 34);
        this.obtain("reltxt3", 191, 34);
        this.obtain("reltxt4", 225, 34);
        this.obtain("reltxt5", 259, 34);

        this.temporality = new CprBitemporality(
                this.getOffsetDateTime("start_dt-umyndig"),
                null,
                this.getOffsetDateTime("start_dt-umyndig"),
                this.getMarking("start_dt_umrk-umyndig"),
                this.getOffsetDateTime("slet_dt-umyndig"),
                false
        );
    }

    @Override
    public List<CprBitemporalRecord> getBitemporalRecords() {

        ArrayList<CprBitemporalRecord> records = new ArrayList<>();

        records.add(new GuardianDataRecord(
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


    @Override
    public String getRecordType() {
        return RECORDTYPE_GUARDIANSHIP;
    }

}
