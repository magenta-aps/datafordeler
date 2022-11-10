package dk.magenta.datafordeler.cpr.records.person;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.person.data.ForeignAddressDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.ForeignAddressEmigrationDataRecord;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Record for Person foreign address (type 028).
 */
public class HistoricForeignAddressRecord extends HistoricPersonDataRecord {

    private final CprBitemporality emigrationTemporality;
    private final CprBitemporality immigrationTemporality;
    private final CprBitemporality foreignAddressTemporality;

    public HistoricForeignAddressRecord(String line) throws ParseException {
        super(line);
        this.obtain("annkor", 14, 1);
        this.obtain("start_mynkod-udrindrejse", 16, 4);
        this.obtain("udr_ts", 19, 12);
        this.obtain("udr_landekod", 31, 4);
        this.obtain("udrdto", 35, 12);
        this.obtain("udrdto_umrk", 47, 1);
        this.obtain("indr_ts", 48, 12);
        this.obtain("indr_landekod", 60, 4);
        this.obtain("indrdto", 64, 12);
        this.obtain("indrdto_umrk", 76, 1);
        this.obtain("udlandadr_mynkod", 77, 4);
        this.obtain("udlandadr_ts", 81, 12);
        this.obtain("udlandadr1", 93, 34);
        this.obtain("udlandadr2", 127, 34);
        this.obtain("udlandadr3", 161, 34);
        this.obtain("udlandadr4", 195, 34);
        this.obtain("udlandadr5", 229, 34);

        OffsetDateTime effectFrom = this.getOffsetDateTime("udrdto");
        boolean effectFromUncertain = this.getMarking("udrdto_umrk");
        OffsetDateTime effectTo = this.getOffsetDateTime("indrdto");
        boolean effectToUncertain = this.getMarking("indrdto_umrk");
        this.emigrationTemporality = new CprBitemporality(this.getOffsetDateTime("udr_ts"), null, effectFrom, effectFromUncertain, effectTo, effectToUncertain);
        this.immigrationTemporality = new CprBitemporality(this.getOffsetDateTime("indr_ts"), null, effectFrom, effectFromUncertain, effectTo, effectToUncertain);
        this.foreignAddressTemporality = new CprBitemporality(this.getOffsetDateTime("udlandadr_ts"), null, effectFrom, effectFromUncertain, effectTo, effectToUncertain);
    }

    @Override
    public String getRecordType() {
        return RECORDTYPE_HISTORIC_FOREIGN_ADDRESS;
    }

    @Override
    public List<CprBitemporalRecord> getBitemporalRecords() {

        ArrayList<CprBitemporalRecord> records = new ArrayList<>();
        Character annkor = this.getChar("annkor");
        records.add(new ForeignAddressDataRecord(
                this.getString("udlandadr1", false),
                this.getString("udlandadr2", false),
                this.getString("udlandadr3", false),
                this.getString("udlandadr4", false),
                this.getString("udlandadr5", false)
        ).setAuthority(
                this.getInt("udlandadr_mynkod")
        ).setBitemporality(
                this.foreignAddressTemporality
        ).setHistoric(
        ).setAnnKor(annkor));


        records.add(new ForeignAddressEmigrationDataRecord(
                this.getInt("indr_landekod"),
                this.getInt("udr_landekod"),
                this.getOffsetDateTime("udr_ts"),
                this.getOffsetDateTime("indr_ts")
        ).setAuthority(
                this.getInt("start_mynkod-udrindrejse")
        ).setBitemporality(
                new CprBitemporality(
                        firstSet(
                                this.getOffsetDateTime("udr_ts"),
                                this.getOffsetDateTime("indr_ts")
                        ),
                        null,
                        this.getOffsetDateTime("udrdto"), this.getMarking("udrdto_umrk"),
                        this.getOffsetDateTime("indrdto"), this.getMarking("indrdto_umrk")
                )
        ).setHistoric(
        ).setAnnKor(annkor));

        return records;
    }

}
