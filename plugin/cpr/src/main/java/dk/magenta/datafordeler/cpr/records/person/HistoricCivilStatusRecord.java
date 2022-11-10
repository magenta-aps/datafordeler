package dk.magenta.datafordeler.cpr.records.person;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.person.data.CivilStatusAuthorityTextDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.CivilStatusDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.CivilStatusVerificationDataRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Record for Person historic civil status (type 036).
 */
public class HistoricCivilStatusRecord extends HistoricPersonDataRecord {

    private final CprBitemporality civilTemporality;
    private final CprBitemporality documentTemporality;
    private final CprBitemporality officiaryTemporality;

    public HistoricCivilStatusRecord(String line) throws ParseException {
        super(line);
        this.obtain("annkor", 14, 1);
        this.obtain("start_mynkod-civilstand", 15, 4);
        this.obtain("civ_ts", 19, 12);
        this.obtain("civst", 31, 1);
        this.obtain("aegtepnr", 32, 10);
        this.obtain("aegtefoed_dt", 42, 10);
        this.obtain("aegtefoeddt_umrk", 52, 1);
        this.obtain("aegtenvn", 53, 34);
        this.obtain("aegtenvn_mrk", 87, 1);
        this.obtain("haenstart-civilstand", 88, 12);
        this.obtain("haenstart_umrk-civilstand", 100, 1);
        this.obtain("haenslut-civilstand", 101, 12);
        this.obtain("haenslut_umrk-civilstand", 113, 1);
        this.obtain("dok_mynkod-civilstand", 114, 4);
        this.obtain("dok_ts-civilstand", 118, 12);
        this.obtain("dok-civilstand", 130, 3);
        this.obtain("myntxt_mynkod-civilstand", 133, 4);
        this.obtain("myntxt_ts-civilstand", 137, 12);
        this.obtain("myntxt-civilstand", 149, 20);
        this.obtain("sep_henvis_ts", 169, 12);

        this.civilTemporality = new CprBitemporality(this.getOffsetDateTime("civ_ts"), null, this.getOffsetDateTime("haenstart-civilstand"), this.getBoolean("haenstart_umrk-civilstand"), this.getOffsetDateTime("haenslut-civilstand"), this.getBoolean("haenslut_umrk-civilstand"));
        this.documentTemporality = new CprBitemporality(this.getOffsetDateTime("dok_ts-civilstand"));
        this.officiaryTemporality = new CprBitemporality(this.getOffsetDateTime("myntxt_ts-civilstand"));
    }

    @Override
    public List<CprBitemporalRecord> getBitemporalRecords() {

        ArrayList<CprBitemporalRecord> records = new ArrayList<>();
        Character annkor = this.getChar("annkor");
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
        ).setHistoric(
        ).setAnnKor(annkor));

        if (this.hasAny("dok-civilstand", "dok_mynkod-civilstand")) {
            records.add(new CivilStatusVerificationDataRecord(
                    this.getBoolean("dok-civilstand"),
                    null
            ).setAuthority(
                    this.getInt("dok_mynkod-civilstand")
            ).setBitemporality(
                    this.documentTemporality
            ).setHistoric(
            ).setAnnKor(annkor));
        }

        if (this.hasAny("myntxt-civilstand", "myntxt_mynkod-civilstand")) {
            records.add(new CivilStatusAuthorityTextDataRecord(
                    this.getString("myntxt-civilstand", true),
                    null
            ).setAuthority(
                    this.getInt("myntxt_mynkod-civilstand")
            ).setBitemporality(
                    this.officiaryTemporality
            ).setHistoric(
            ).setAnnKor(annkor));
        }

        return records;
    }

    @Override
    public String getRecordType() {
        return RECORDTYPE_HISTORIC_CIVILSTATUS;
    }

}
