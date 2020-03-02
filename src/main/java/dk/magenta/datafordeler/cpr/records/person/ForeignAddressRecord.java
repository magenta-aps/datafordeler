package dk.magenta.datafordeler.cpr.records.person;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.cpr.data.person.PersonEffect;
import dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.person.data.ForeignAddressDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.ForeignAddressEmigrationDataRecord;
import org.hibernate.Session;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Record for Person foreign address (type 028).
 */
public class ForeignAddressRecord extends PersonDataRecord {

    private CprBitemporality emigrationTemporality;
    private CprBitemporality foreignAddressTemporality;

    public ForeignAddressRecord(String line) throws ParseException {
        super(line);
        this.obtain("start_mynkod-udrindrejse", 14, 4);
        this.obtain("udr_ts", 18, 12);
        this.obtain("udr_landekod", 30, 4);
        this.obtain("udrdto", 34, 12);
        this.obtain("udrdto_umrk", 46, 1);
        this.obtain("udlandadr_mynkod", 47, 4);
        this.obtain("udlandadr_ts", 51, 12);
        this.obtain("udlandadr1", 63, 34);
        this.obtain("udlandadr2", 97, 34);
        this.obtain("udlandadr3", 131, 34);
        this.obtain("udlandadr4", 165, 34);
        this.obtain("udlandadr5", 199, 34);

        OffsetDateTime effectFrom = this.getOffsetDateTime("udrdto");
        boolean effectFromUncertain = this.getMarking("udrdto_umrk");
        this.emigrationTemporality = new CprBitemporality(this.getOffsetDateTime("udr_ts"), null, effectFrom, effectFromUncertain, null, false);
        this.foreignAddressTemporality = new CprBitemporality(this.getOffsetDateTime("udlandadr_ts"), null, effectFrom, effectFromUncertain, null, false);
    }

    @Override
    public boolean populateBaseData(PersonBaseData data, CprBitemporality bitemporality, Session session, ImportMetadata importMetadata) {
        boolean updated = false;
        if (bitemporality.equals(this.emigrationTemporality)) {
            data.setEmigration(
                    this.getInt("start_mynkod-udrindrejs"),
                    this.getInt("udr_landekod"),
                    importMetadata.getImportTime()
            );
            updated = true;
        }
        if (bitemporality.equals(this.foreignAddressTemporality)) {
            data.setForeignAddress(
                    this.getInt("udlandadr_mynkod"),
                    this.get("udlandadr1"),
                    this.get("udlandadr2"),
                    this.get("udlandadr3"),
                    this.get("udlandadr4"),
                    this.get("udlandadr5"),
                    importMetadata.getImportTime()
            );
            updated = true;
        }
        return updated;
    }

    @Override
    public List<CprBitemporalRecord> getBitemporalRecords() {

        ArrayList<CprBitemporalRecord> records = new ArrayList<>();

        records.add(new ForeignAddressDataRecord(
                this.get("udlandadr1"),
                this.get("udlandadr2"),
                this.get("udlandadr3"),
                this.get("udlandadr4"),
                this.get("udlandadr5")
        ).setAuthority(
                this.getInt("udlandadr_mynkod")
        ).setBitemporality(
                this.foreignAddressTemporality
        ));

        records.add(new ForeignAddressEmigrationDataRecord(
                this.getInt("udr_landekod")
        ).setAuthority(
                this.getInt("start_mynkod-udrindrejs")
        ).setBitemporality(
                this.emigrationTemporality
        ));

        return records;
    }


    @Override
    public String getRecordType() {
        return RECORDTYPE_FOREIGN_ADDRESS;
    }

    @Override
    public List<CprBitemporality> getBitemporality() {
        ArrayList<CprBitemporality> bitemporalities = new ArrayList<>();
        if (this.has("udr_ts") || this.has("udr_landekod")) {
            bitemporalities.add(this.emigrationTemporality);
        }
        if (this.has("udlandadr_mynkod") || this.has("udlandadr1")) {
            bitemporalities.add(this.foreignAddressTemporality);
        }
        return bitemporalities;
    }

    @Override
    public Set<PersonEffect> getEffects() {
        HashSet<PersonEffect> effects = new HashSet<>();
        effects.add(new PersonEffect(null, this.getOffsetDateTime("udrdto"), this.getMarking("udrdto_umrk"), null, false));
        return effects;
    }
}
