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
 * Record for Person historic address (type 026).
 */
public class HistoricAddressRecord extends HistoricPersonDataRecord {

    private final CprBitemporality addressTemporality;
    private final CprBitemporality conameTemporality;
    private final CprBitemporality municipalityTemporality;

    public HistoricAddressRecord(String line) throws ParseException {
        super(line);
        this.obtain("annkor", 14, 1);
        this.obtain("start_mynkod-personbolig", 15, 4);
        this.obtain("adr_ts", 19, 12);
        this.obtain("komkod", 31, 4);
        this.obtain("vejkod", 35, 4);
        this.obtain("husnr", 39, 4);
        this.obtain("etage", 43, 2);
        this.obtain("sidedoer", 45, 4);
        this.obtain("bnr", 49, 4);
        this.obtain("convn", 53, 34);
        this.obtain("convn_ts", 87, 12);
        this.obtain("tilflydto", 99, 12);
        this.obtain("tilflydto_umrk", 111, 1);
        this.obtain("fraflydto", 112, 12);
        this.obtain("fraflydto_umrk", 124, 1);
        this.obtain("tilfra_mynkod", 125, 4);
        this.obtain("tilfra_ts", 129, 12);
        this.obtain("tilflykomdto", 141, 12);
        this.obtain("tilflykomdt_umrk", 153, 1);
        this.obtain("fraflykomkod", 154, 4);
        this.obtain("fraflykomdto", 158, 12);
        this.obtain("fraflykomdt_umrk", 170, 1);

        this.addressTemporality = new CprBitemporality(this.getOffsetDateTime("adr_ts"), null, this.getOffsetDateTime("tilflydto"), this.getBoolean("tilflydto_umrk"), this.getOffsetDateTime("fraflydto"), this.getBoolean("fraflydto_umrk"));

        this.conameTemporality = new CprBitemporality(this.getOffsetDateTime("convn_ts"));
        this.municipalityTemporality = new CprBitemporality(this.getOffsetDateTime("tilfra_ts"));
    }

    @Override
    public String getRecordType() {
        return RECORDTYPE_HISTORIC_DOMESTIC_ADDRESS;
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
                "",
                "",
                "",
                "",
                "",
                this.getInt("adrtxttype"),
                this.getInt("start_mynkod-adrtxt")
        ).setAuthority(
                this.getInt("start_mynkod-personbolig")
        ).setBitemporality(
                this.addressTemporality
        ));

        OffsetDateTime convnTs = this.getOffsetDateTime("convn_ts");
        if (convnTs == null) {
            convnTs = this.getOffsetDateTime("adr_ts");
        }
        records.add(new AddressConameDataRecord(
                this.getString("convn", true)
        ).setAuthority(
                this.getInt("start_mynkod-personbolig")
        ).setBitemporality(
                convnTs,
                null,
                this.getOffsetDateTime("tilflydto"),
                this.getBoolean("tilflydto_umrk"),
                this.getOffsetDateTime("fraflydto"),
                this.getBoolean("fraflydto_umrk")
        ));

        if (this.hasAny("fraflykomdto", "fraflykomdt_umrk", "fraflykomkod", "tilflykomdto")) {
            if (this.getInt("tilfra_mynkod") != 0) {
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
                        this.getOffsetDateTime("fraflydto"),
                        this.getBoolean("fraflydto_umrk")
                ));
            }
        }

        Character annkor = this.getChar("annkor");
        for (CprBitemporalRecord p : records) {
            //p.line = this.getLine();
            p.setHistoric();
            p.setAnnKor(annkor);
        }

        return records;
    }

    public int getMunicipalityCode() {
        return this.getInt("komkod");
    }
}
