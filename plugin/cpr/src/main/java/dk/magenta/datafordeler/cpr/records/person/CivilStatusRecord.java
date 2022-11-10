package dk.magenta.datafordeler.cpr.records.person;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.Mapping;
import dk.magenta.datafordeler.cpr.records.person.data.CivilStatusAuthorityTextDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.CivilStatusDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.CivilStatusVerificationDataRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Record for Person civil status (type 035).
 */
public class CivilStatusRecord extends PersonDataRecord {

    private final CprBitemporality civilTemporality;
    private final CprBitemporality documentTemporality;
    private final CprBitemporality officiaryTemporality;

    public CivilStatusRecord(String line) throws ParseException {
        this(line, traditionalMapping);
    }

    public CivilStatusRecord(String line, Mapping mapping) throws ParseException {
        super(line);
        this.obtain(mapping);

        this.civilTemporality = new CprBitemporality(this.getOffsetDateTime("civ_ts"), null, this.getOffsetDateTime("haenstart-civilstand"), this.getBoolean("haenstart_umrk-civilstand"), null, false);
        this.documentTemporality = new CprBitemporality(this.getOffsetDateTime("dok_ts-civilstand"));
        this.officiaryTemporality = new CprBitemporality(this.getOffsetDateTime("myntxt_ts-civilstand"));
    }

    public static final Mapping traditionalMapping = new Mapping();

    static {
        traditionalMapping.add("start_mynkod-civilstand", 14, 4);
        traditionalMapping.add("civ_ts", 18, 12);
        traditionalMapping.add("civst", 30, 1);
        traditionalMapping.add("aegtepnr", 31, 10);
        traditionalMapping.add("aegtefoed_dt", 41, 10);
        traditionalMapping.add("aegtefoeddt_umrk", 51, 1);
        traditionalMapping.add("aegtenvn", 52, 34);
        traditionalMapping.add("aegtenvn_mrk", 86, 1);
        traditionalMapping.add("haenstart-civilstand", 87, 12);
        traditionalMapping.add("haenstart_umrk-civilstand", 99, 1);
        traditionalMapping.add("dok_mynkod-civilstand", 100, 4);
        traditionalMapping.add("dok_ts-civilstand", 104, 12);
        traditionalMapping.add("dok-civilstand", 116, 3);
        traditionalMapping.add("myntxt_mynkod-civilstand", 119, 4);
        traditionalMapping.add("myntxt_ts-civilstand", 123, 12);
        traditionalMapping.add("myntxt-civilstand", 135, 20);
        traditionalMapping.add("sep_henvis_ts", 155, 12);
    }

    @Override
    public String getRecordType() {
        return RECORDTYPE_CIVILSTATUS;
    }

    @Override
    public List<CprBitemporalRecord> getBitemporalRecords() {

        ArrayList<CprBitemporalRecord> records = new ArrayList<>();

        records.add(new CivilStatusDataRecord(
                null,
                this.getString("civst", true),
                this.getString("aegtepnr", false),
                this.getDate("aegtefoed_dt"),
                this.getBoolean("aegtefoeddt_umrk"),
                this.getString("aegtenvn", true),
                this.getMarking("aegtenvn_mrk")
        ).setAuthority(
                this.getInt("start_mynkod-civilstand")
        ).setBitemporality(
                this.civilTemporality
        ));

        if (this.hasAny("dok-civilstand", "dok_mynkod-civilstand")) {
            if (this.has("dok-civilstand")) {
                records.add(new CivilStatusVerificationDataRecord(
                        this.getBoolean("dok-civilstand"),
                        null
                ).setAuthority(
                        this.getInt("dok_mynkod-civilstand")
                ).setBitemporality(
                        this.documentTemporality
                ));
            }
        }

        if (this.hasAny("myntxt-civilstand", "myntxt_mynkod-civilstand")) {
            if (this.has("myntxt-civilstand")) {
                records.add(new CivilStatusAuthorityTextDataRecord(
                        this.getString("myntxt-civilstand", true),
                        null
                ).setAuthority(
                        this.getInt("myntxt_mynkod-civilstand")
                ).setBitemporality(
                        this.officiaryTemporality
                ));
            }
        }

        return records;
    }

}
