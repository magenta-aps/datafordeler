package dk.magenta.datafordeler.cpr.records.person;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.Mapping;
import dk.magenta.datafordeler.cpr.records.person.data.AddressNameDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.NameAuthorityTextDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.NameDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.NameVerificationDataRecord;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Record for Person name (type 020).
 */
public class NameRecord extends PersonDataRecord {

    private final CprBitemporality nameTemporality;
    private final CprBitemporality addressNameTemporality;
    private final CprBitemporality documentNameTemporality;
    private final CprBitemporality officiaryTemporality;

    public NameRecord(String line) throws ParseException {
        this(line, traditionalMapping);
    }

    public NameRecord(String line, Mapping mapping) throws ParseException {
        super(line);
        this.obtain(mapping);

        OffsetDateTime effectFrom = this.getOffsetDateTime("nvnhaenstart");
        boolean effectFromUncertain = this.getMarking("haenstart_umrk-navne");
        this.nameTemporality = new CprBitemporality(this.getOffsetDateTime("nvn_ts"), null, effectFrom, effectFromUncertain, null, false);
        this.addressNameTemporality = new CprBitemporality(this.getOffsetDateTime("adrnvn_ts"), null, effectFrom, effectFromUncertain, null, false);
        this.documentNameTemporality = new CprBitemporality(this.getOffsetDateTime("dok_ts-navne"), null, effectFrom, effectFromUncertain, null, false);
        this.officiaryTemporality = new CprBitemporality(this.getOffsetDateTime("myntxt_ts-navne"), null, effectFrom, effectFromUncertain, null, false);
    }

    public static final Mapping traditionalMapping = new Mapping();

    static {
        traditionalMapping.add("start_mynkod-navne", 14, 4);
        traditionalMapping.add("nvn_ts", 18, 12);
        traditionalMapping.add("fornvn", 30, 50);
        traditionalMapping.add("fornvn_mrk", 80, 1);
        traditionalMapping.add("melnvn", 81, 40);
        traditionalMapping.add("melnvn_mrk", 121, 1);
        traditionalMapping.add("efternvn", 122, 40);
        traditionalMapping.add("efternvn_mrk", 162, 1);
        traditionalMapping.add("slægtnvn", 163, 40);
        traditionalMapping.add("slægtnvn_mrk", 203, 1);
        traditionalMapping.add("nvnhaenstart", 204, 12);
        traditionalMapping.add("haenstart_umrk-navne", 216, 1);
        traditionalMapping.add("adrnvn_mynkod", 217, 4);
        traditionalMapping.add("adrnvn_ts", 221, 12);
        traditionalMapping.add("adrnvn", 233, 34);
        traditionalMapping.add("indrap-navne", 267, 3);
        traditionalMapping.add("dok_mynkod-navne", 270, 4);
        traditionalMapping.add("dok_ts-navne", 274, 12);
        traditionalMapping.add("dok-navne", 286, 3);
        traditionalMapping.add("myntxt_mynkod-navne", 289, 4);
        traditionalMapping.add("myntxt_ts-navne", 293, 12);
        traditionalMapping.add("myntxt-navne", 305, 20);
    }

    @Override
    public String getRecordType() {
        return RECORDTYPE_CURRENT_NAME;
    }

    @Override
    public List<CprBitemporalRecord> getBitemporalRecords() {

        ArrayList<CprBitemporalRecord> records = new ArrayList<>();

        records.add(new NameDataRecord(
                // String adresseringsnavn,
                null,
                this.getString("fornvn", true),
                this.getMarking("fornvn_mrk"),
                this.getString("melnvn", true),
                this.getMarking("melnvn_mrk"),
                this.getString("efternvn", true),
                this.getMarking("efternvn_mrk"),
                this.getString("slægtsnvn", true),
                this.getMarking("slægtsnvn_mrk")
        ).setAuthority(
                this.getInt("start_mynkod-navne")
        ).setBitemporality(
                this.nameTemporality
        ));

        if (this.hasAny("adrnvn", "adrnvn_mynkod")) {
            records.add(new AddressNameDataRecord(
                    this.getString("adrnvn", true),
                    this.getBoolean("indrap-navne", false)
            ).setAuthority(
                    this.getInt("adrnvn_mynkod")
            ).setBitemporality(
                    this.addressNameTemporality
            ));
        }

        if (this.hasAny("dok-navne", "dok_mynkod-navne")) {
            records.add(new NameVerificationDataRecord(
                    this.getBoolean("dok-navne"),
                    null
            ).setAuthority(
                    this.getInt("dok_mynkod-navne")
            ).setBitemporality(
                    this.documentNameTemporality
            ));
        }

        if (this.hasAny("myntxt-navne", "myntxt_mynkod-navne")) {
            records.add(new NameAuthorityTextDataRecord(
                    this.getString("myntxt-navne", true),
                    null
            ).setAuthority(
                    this.getInt("myntxt_mynkod-navne")
            ).setBitemporality(
                    this.officiaryTemporality
            ));
        }

        return records;
    }

}
