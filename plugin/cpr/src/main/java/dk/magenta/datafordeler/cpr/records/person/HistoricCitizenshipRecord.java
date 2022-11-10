package dk.magenta.datafordeler.cpr.records.person;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.person.data.CitizenshipDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.CitizenshipVerificationDataRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Record for Person historic citizenship (type 041).
 */
public class HistoricCitizenshipRecord extends HistoricPersonDataRecord {

    private final CprBitemporality citizenshipTemporality;
    private final CprBitemporality documentTemporality;

    public HistoricCitizenshipRecord(String line) throws ParseException {
        super(line);
        this.obtain("annkor", 14, 1);
        this.obtain("start_mynkod-statsborgerskab", 15, 4);
        this.obtain("stat_ts", 19, 12);
        this.obtain("landekod", 31, 4);
        this.obtain("haenstart-statsborgerskab", 35, 12);
        this.obtain("haenstart_umrk-statsborgerskab", 47, 1);
        this.obtain("haenslut-statsborgerskab", 48, 12);
        this.obtain("haenslut_umrk-statsborgerskab", 60, 1);
        this.obtain("dok_mynkod-statsborgerskab", 61, 4);
        this.obtain("dok_ts-statsborgerskab", 65, 12);
        this.obtain("dok-statsborgerskab", 77, 3);

        this.citizenshipTemporality = new CprBitemporality(this.getOffsetDateTime("stat_ts"), null, this.getOffsetDateTime("haenstart-statsborgerskab"), this.getBoolean("haenstart_umrk-statsborgerskab"), this.getOffsetDateTime("haenslut-statsborgerskab"), this.getBoolean("haenslut_umrk-statsborgerskab"));
        this.documentTemporality = new CprBitemporality(this.getOffsetDateTime("dok_ts-statsborgerskab"));
    }

    @Override
    public String getRecordType() {
        return RECORDTYPE_HISTORIC_CITIZENSHIP;
    }

    @Override
    public List<CprBitemporalRecord> getBitemporalRecords() {

        ArrayList<CprBitemporalRecord> records = new ArrayList<>();
        Character annkor = this.getChar("annkor");
        records.add(new CitizenshipDataRecord(
                this.getInt("landekod")
        ).setAuthority(
                this.getInt("start_mynkod-statsborgerskab")
        ).setBitemporality( // TODO: Monotemporal?
                this.citizenshipTemporality // TODO: mangler registrationTo
        ).setHistoric(
        ).setAnnKor(annkor));

        if (this.hasAny("dok-statsborgerskab", "dok_mynkod-statsborgerskab")) {
            records.add(new CitizenshipVerificationDataRecord(
                    this.getBoolean("dok-statsborgerskab")
            ).setAuthority(
                    this.getInt("dok_mynkod-statsborgerskab")
            ).setBitemporality(
                    this.documentTemporality
            ).setHistoric(
            ).setAnnKor(annkor));
        }

        return records;
    }

}
