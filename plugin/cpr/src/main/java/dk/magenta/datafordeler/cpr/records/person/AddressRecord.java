package dk.magenta.datafordeler.cpr.records.person;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.Mapping;
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

    private final CprBitemporality addressTemporality;
    private final CprBitemporality conameTemporality;
    private final CprBitemporality municipalityTemporality;

    public AddressRecord(String line) throws ParseException {
        this(line, traditionalMapping);
    }

    public AddressRecord(String line, Mapping mapping) throws ParseException {
        super(line);
        this.obtain(mapping);

        this.addressTemporality = new CprBitemporality(this.getOffsetDateTime("adr_ts"), null, this.getOffsetDateTime("tilflydto"), this.getBoolean("tilflydto_umrk"), null, false);
        this.conameTemporality = new CprBitemporality(this.getOffsetDateTime("convn_ts"));
        this.municipalityTemporality = new CprBitemporality(this.getOffsetDateTime("tilfra_ts"));
    }

    public static final Mapping traditionalMapping = new Mapping();

    static {
        traditionalMapping.add("start_mynkod-personbolig", 14, 4);
        traditionalMapping.add("adr_ts", 18, 12);
        traditionalMapping.add("komkod", 30, 4);
        traditionalMapping.add("vejkod", 34, 4);
        traditionalMapping.add("husnr", 38, 4);
        traditionalMapping.add("etage", 42, 2);
        traditionalMapping.add("sidedoer", 44, 4);
        traditionalMapping.add("bnr", 48, 4);
        traditionalMapping.add("convn", 52, 34);
        traditionalMapping.add("convn_ts", 86, 12);
        traditionalMapping.add("tilflydto", 98, 12);
        traditionalMapping.add("tilflydto_umrk", 110, 1);
        traditionalMapping.add("tilfra_mynkod", 111, 4);
        traditionalMapping.add("tilfra_ts", 115, 12);
        traditionalMapping.add("tilflykomdto", 127, 12);
        traditionalMapping.add("tilflykomdt_umrk", 139, 1);
        traditionalMapping.add("fraflykomkod", 140, 4);
        traditionalMapping.add("fraflykomdto", 144, 12);
        traditionalMapping.add("fraflykomdt_umrk", 156, 1);
        traditionalMapping.add("adrtxttype", 157, 4);
        traditionalMapping.add("start_mynkod-adrtxt", 161, 4);
        traditionalMapping.add("adr1-supladr", 165, 34);
        traditionalMapping.add("adr2-supladr", 199, 34);
        traditionalMapping.add("adr3-supladr", 233, 34);
        traditionalMapping.add("adr4-supladr", 267, 34);
        traditionalMapping.add("adr5-supladr", 301, 34);
        traditionalMapping.add("start_dt-adrtxt", 335, 10);
        traditionalMapping.add("slet_dt-adrtxt", 345, 10);
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

        if (this.hasAny("fraflykomdto", "fraflykomdt_umrk", "fraflykomkod", "tilflykomdto")) {
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
        }

        return records;
    }

    public int getMunicipalityCode() {
        return this.getInt("komkod");
    }
}
