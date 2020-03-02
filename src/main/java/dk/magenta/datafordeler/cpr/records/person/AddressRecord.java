package dk.magenta.datafordeler.cpr.records.person;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.person.data.AddressConameDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.AddressDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.MoveMunicipalityDataRecord;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Record for Person address (type 025).
 */
public class AddressRecord extends PersonDataRecord {

    private CprBitemporality addressTemporality;
    private CprBitemporality conameTemporality;
    private CprBitemporality municipalityTemporality;

    public AddressRecord(String line) throws ParseException {
        super(line);
        this.obtain("start_mynkod-personbolig", 14, 4);
        this.obtain("adr_ts", 18, 12);
        this.obtain("komkod", 30, 4);
        this.obtain("vejkod", 34, 4);
        this.obtain("husnr", 38, 4);
        this.obtain("etage", 42, 2);
        this.obtain("sidedoer", 44, 4);
        this.obtain("bnr", 48, 4);
        this.obtain("convn", 52, 34);
        this.obtain("convn_ts", 86, 12);
        this.obtain("tilflydto", 98, 12);
        this.obtain("tilflydto_umrk", 110, 1);
        this.obtain("tilfra_mynkod", 111, 4);
        this.obtain("tilfra_ts", 115, 12);
        this.obtain("tilflykomdto", 127, 12);
        this.obtain("tilflykomdt_umrk", 139, 1);
        this.obtain("fraflykomkod", 140, 4);
        this.obtain("fraflykomdto", 144, 12);
        this.obtain("fraflykomdt_umrk", 156, 1);
        this.obtain("adrtxttype", 157, 4);
        this.obtain("start_mynkod-adrtxt", 161, 4);
        this.obtain("adr1-supladr", 165, 34);
        this.obtain("adr2-supladr", 199, 34);
        this.obtain("adr3-supladr", 233, 34);
        this.obtain("adr4-supladr", 267, 34);
        this.obtain("adr5-supladr", 301, 34);
        this.obtain("start_dt-adrtxt", 335, 10);
        this.obtain("slet_dt-adrtxt", 345, 10);

        this.addressTemporality = new CprBitemporality(this.getOffsetDateTime("adr_ts"), null, this.getOffsetDateTime("tilflydto"), this.getBoolean("tilflydto_umrk"), null, false);
        this.conameTemporality = new CprBitemporality(this.getOffsetDateTime("convn_ts"));
        this.municipalityTemporality = new CprBitemporality(this.getOffsetDateTime("tilfra_ts"));
    }

    @Override
    public String getRecordType() {
        return RECORDTYPE_DOMESTIC_ADDRESS;
    }

    @Override
    public List<CprBitemporalRecord> getBitemporalRecords() {
        ArrayList<CprBitemporalRecord> records = new ArrayList<>();

        records.add(new AddressDataRecord(
                this.getInt("komkod", false),
                this.getInt("vejkod", false),
                this.getString("bnr", true),
                this.getString("husnr", true),
                this.getString("etage", false),
                this.getString("sidedoer", true),
                this.getString("adr1-supladr", false),
                this.getString("adr2-supladr", false),
                this.getString("adr3-supladr", false),
                this.getString("adr4-supladr", false),
                this.getString("adr5-supladr", false),
                this.getInt("adrtxttype"),
                this.getInt("start_mynkod-adrtxt")
        ).setAuthority(
                this.getInt("start_mynkod-personbolig")
        ).setBitemporality(
                this.getOffsetDateTime("adr_ts"),
                null,
                this.getOffsetDateTime("tilflydto"),
                this.getBoolean("tilflydto_umrk"),
                null,
                false
        ));


        OffsetDateTime convnTs = this.getOffsetDateTime("convn_ts");
        if (convnTs == null) {
           convnTs = this.getOffsetDateTime("adr_ts");
        }
        records.add(new AddressConameDataRecord(
                this.getString("convn", false)
        ).setAuthority(
                this.getInt("start_mynkod-personbolig")
        ).setBitemporality(
                convnTs,
                null,
                this.getOffsetDateTime("tilflydto"),
                this.getBoolean("tilflydto_umrk"),
                null,
                false
        ));

        records.add(new MoveMunicipalityDataRecord(
                this.getDateTime("fraflykomdto"),
                this.getBoolean("fraflykomdt_umrk"),
                this.getInt("fraflykomkod"),
                this.getDateTime("tilflykomdto"),
                this.getBoolean("tilflykomdt_umrk")
        ).setAuthority(
                this.getInt("tilfra_mynkod")
        ).setBitemporality(
                this.getOffsetDateTime("tilfra_ts"),
                null,
                this.getOffsetDateTime("tilflydto"),
                this.getBoolean("tilflydto_umrk"),
                null,
                false
        ));

        /*for (CprBitemporalRecord p : records) {
            p.line = this.getLine();
        }*/
        return records;
    }

    public int getMunicipalityCode() {
        return this.getInt("komkod");
    }
}
