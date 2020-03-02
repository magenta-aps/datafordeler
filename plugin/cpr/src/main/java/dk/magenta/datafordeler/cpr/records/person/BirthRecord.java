package dk.magenta.datafordeler.cpr.records.person;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.cpr.data.person.PersonEffect;
import dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.person.data.BirthPlaceDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.BirthPlaceVerificationDataRecord;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Record for Person birth (type 025).
 */
public class BirthRecord extends PersonDataRecord {

    private CprBitemporality birthTemporality;
    private CprBitemporality documentTemporality;

    public BirthRecord(String line) throws ParseException {
        super(line);
        this.obtain("start_mynkod-fødested", 14, 4);
        this.obtain("fødested_ts", 18, 12);
        this.obtain("myntxt_mynkod-fødested", 30, 4);
        this.obtain("myntxt_ts-fødested", 34, 12);
        this.obtain("myntxt-fødested", 46, 20);
        this.obtain("dok_mynkod-fødested", 66, 4);
        this.obtain("dok_ts-fødested", 70, 12);
        this.obtain("dok-fødested", 82, 3);

        this.birthTemporality = new CprBitemporality(this.getOffsetDateTime("fødested_ts"));
        this.documentTemporality = new CprBitemporality(this.getOffsetDateTime("dok_ts-fødested"));
    }

    @Override
    public String getRecordType() {
        return RECORDTYPE_BIRTH;
    }

    @Override
    public boolean populateBaseData(PersonBaseData data, CprBitemporality bitemporality, Session session, ImportMetadata importMetadata) {
        boolean updated = false;
        if (bitemporality.equals(this.birthTemporality)) {
            data.setBirth(
                    this.getInt("start_mynkod-fødested"),
                    this.getInt("myntxt_mynkod-fødested", true),
                    this.getString("myntxt-fødested", true),
                    importMetadata.getImportTime()
            );
            updated = true;
        }
        if (bitemporality.equals(this.documentTemporality)) {
            data.setBirthVerification(
                    this.getInt("dok_mynkod-fødested"),
                    this.getBoolean("dok-fødested"),
                    importMetadata.getImportTime()
            );
            updated = true;
        }
        return updated;
    }

    @Override
    public List<CprBitemporalRecord> getBitemporalRecords() {

        ArrayList<CprBitemporalRecord> records = new ArrayList<>();

        records.add(new BirthPlaceDataRecord(
                this.getInt("myntxt_mynkod-fødested", true),
                this.getString("myntxt-fødested", true)
        ).setAuthority(
                this.getInt("start_mynkod-fødested")
        ).setBitemporality( // TODO: Monotemporal?
                this.birthTemporality
        ));

        records.add(new BirthPlaceVerificationDataRecord(
                this.getBoolean("dok-fødested")
                //importMetadata.getImportTime()
        ).setAuthority(
                this.getInt("dok_mynkod-fødested")
        ).setBitemporality(
                this.documentTemporality
        ));

        return records;
    }

    @Override
    public List<CprBitemporality> getBitemporality() {
        ArrayList<CprBitemporality> bitemporalities = new ArrayList<>();
        bitemporalities.add(this.birthTemporality);
        if (this.documentTemporality != null) {
            bitemporalities.add(this.documentTemporality);
        }
        return bitemporalities;
    }

    @Override
    public Set<PersonEffect> getEffects() {
        HashSet<PersonEffect> effects = new HashSet<>();
        effects.add(new PersonEffect(null, null, false, null, false));
        return effects;
    }
}
