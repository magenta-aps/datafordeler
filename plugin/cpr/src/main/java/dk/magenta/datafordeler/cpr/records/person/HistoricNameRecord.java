package dk.magenta.datafordeler.cpr.records.person;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.person.data.NameAuthorityTextDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.NameDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.NameVerificationDataRecord;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Record for Person historic name (type 021).
 */
public class HistoricNameRecord extends HistoricPersonDataRecord {

    private final CprBitemporality nameTemporality;
    private final CprBitemporality documentNameTemporality;
    private final CprBitemporality officiaryTemporality;

    public HistoricNameRecord(String line) throws ParseException {
        super(line);
        this.obtain("annkor", 14, 1);
        this.obtain("start_mynkod-navne", 15, 4);
        this.obtain("nvn_ts", 19, 12);
        this.obtain("fornvn", 31, 50);
        this.obtain("fornvn_mrk", 81, 1);
        this.obtain("melnvn", 82, 40);
        this.obtain("melnvn_mrk", 122, 1);
        this.obtain("efternvn", 123, 40);
        this.obtain("efternvn_mrk", 163, 1);
        this.obtain("slægtnvn", 164, 40);
        this.obtain("slægtnvn_mrk", 204, 1);
        this.obtain("nvnhaenstart", 205, 12);
        this.obtain("haenstart_umrk-navne", 217, 1);
        this.obtain("nvnhaenslut", 218, 12);
        this.obtain("haenslut_umrk-navne", 230, 1);
        this.obtain("dok_mynkod-navne", 231, 4);
        this.obtain("dok_ts-navne", 235, 12);
        this.obtain("dok-navne", 247, 3);
        this.obtain("myntxt_mynkod-navne", 250, 4);
        this.obtain("myntxt_ts-navne", 254, 12);
        this.obtain("myntxt-navne", 266, 20);

        OffsetDateTime effectFrom = this.getOffsetDateTime("nvnhaenstart");
        boolean effectFromUncertain = this.getMarking("haenstart_umrk-navne");
        OffsetDateTime effectTo = this.getOffsetDateTime("nvnhaenslut");
        boolean effectToUncertain = this.getMarking("haenslut_umrk-navne");
        this.nameTemporality = new CprBitemporality(this.getOffsetDateTime("nvn_ts"), null, effectFrom, effectFromUncertain, effectTo, effectToUncertain);
        this.documentNameTemporality = new CprBitemporality(this.getOffsetDateTime("dok_ts-navne"), null, effectFrom, effectFromUncertain, effectTo, effectToUncertain);
        this.officiaryTemporality = new CprBitemporality(this.getOffsetDateTime("myntxt_ts-navne"), null, effectFrom, effectFromUncertain, effectTo, effectToUncertain);
    }

    @Override
    public String getRecordType() {
        return RECORDTYPE_HISTORIC_NAME;
    }


    @Override
    public List<CprBitemporalRecord> getBitemporalRecords() {

        ArrayList<CprBitemporalRecord> records = new ArrayList<>();
        Character annkor = this.getChar("annkor");
        records.add(new NameDataRecord(
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
        ).setHistoric(
        ).setAnnKor(annkor));

        if (this.hasAny("dok-navne", "dok_mynkod-navne")) {
            records.add(new NameVerificationDataRecord(
                    this.getBoolean("dok-navne"),
                    null
            ).setAuthority(
                    this.getInt("dok_mynkod-navne")
            ).setBitemporality(
                    this.documentNameTemporality
            ).setHistoric(
            ).setAnnKor(annkor));
        }

        if (this.hasAny("myntxt-navne", "myntxt_mynkod-navne")) {
            records.add(new NameAuthorityTextDataRecord(
                    this.getString("myntxt-navne", true),
                    null
            ).setAuthority(
                    this.getInt("myntxt_mynkod-navne")
            ).setBitemporality(
                    this.officiaryTemporality
            ).setHistoric(
            ).setAnnKor(annkor));
        }

        return records;
    }

}
