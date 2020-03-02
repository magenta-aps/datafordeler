package dk.magenta.datafordeler.cpr.records.person;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.cpr.data.person.PersonEffect;
import dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.person.data.AddressNameDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.NameAuthorityTextDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.NameDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.NameVerificationDataRecord;
import org.hibernate.Session;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Record for Person name (type 020).
 */
public class NameRecord extends PersonDataRecord {

    private CprBitemporality nameTemporality;
    private CprBitemporality addressNameTemporality;
    private CprBitemporality documentNameTemporality;
    private CprBitemporality officiaryTemporality;

    public NameRecord(String line) throws ParseException {
        super(line);
        this.obtain("start_mynkod-navne", 14, 4);
        this.obtain("nvn_ts", 18, 12);
        this.obtain("fornvn", 30, 50);
        this.obtain("fornvn_mrk", 80, 1);
        this.obtain("melnvn", 81, 40);
        this.obtain("melnvn_mrk", 121, 1);
        this.obtain("efternvn", 122, 40);
        this.obtain("efternvn_mrk", 162, 1);
        this.obtain("slægtnvn", 163, 40);
        this.obtain("slægtnvn_mrk", 203, 1);
        this.obtain("nvnhaenstart", 204, 12);
        this.obtain("haenstart_umrk-navne", 216, 1);
        this.obtain("adrnvn_mynkod", 217, 4);
        this.obtain("adrnvn_ts", 221, 12);
        this.obtain("adrnvn", 233, 34);
        this.obtain("indrap-navne", 267, 3);
        this.obtain("dok_mynkod-navne", 270, 4);
        this.obtain("dok_ts-navne", 274, 12);
        this.obtain("dok-navne", 286, 3);
        this.obtain("myntxt_mynkod-navne", 289, 4);
        this.obtain("myntxt_ts-navne", 293, 12);
        this.obtain("myntxt-navne", 305, 20);

        OffsetDateTime effectFrom = this.getOffsetDateTime("nvnhaenstart");
        boolean effectFromUncertain = this.getMarking("haenstart_umrk-navne");
        this.nameTemporality = new CprBitemporality(this.getOffsetDateTime("nvn_ts"), null, effectFrom, effectFromUncertain, null, false);
        this.addressNameTemporality = new CprBitemporality(this.getOffsetDateTime("adrnvn_ts"), null, effectFrom, effectFromUncertain, null, false);
        this.documentNameTemporality = new CprBitemporality(this.getOffsetDateTime("dok_ts-navne"), null, effectFrom, effectFromUncertain, null, false);
        this.officiaryTemporality = new CprBitemporality(this.getOffsetDateTime("myntxt_ts-navne"), null, effectFrom, effectFromUncertain, null, false);
    }

    @Override
    public boolean populateBaseData(PersonBaseData data, CprBitemporality bitemporality, Session session, ImportMetadata importMetadata) {
        boolean updated = false;
        if (bitemporality.equals(this.nameTemporality)) {
            data.setName(
                    // int authority,
                    this.getInt("start_mynkod-navne"),
                    // String adresseringsnavn,
                    null,
                    // String efternavn,
                    this.get("efternvn"),
                    // String fornavne,
                    this.get("fornvn"),
                    // String mellemnavn,
                    this.get("melnvn"),
                    // boolean efternavnMarkering,
                    this.getMarking("efternvn_mrk"),
                    // boolean fornavneMarkering,
                    this.getMarking("fornvn_mrk"),
                    // boolean mellemnavnMarkering,
                    this.getMarking("melnvn_mrk"),
                    // String egetEfternavn,
                    this.get("slægtsnvn"),
                    // boolean ownLastNameMarking,
                    this.getMarking("slægtsnvn_mrk"),
                    // boolean reportNames
                    this.getBoolean("indrap-navne"),
                    importMetadata.getImportTime()
            );
            updated = true;
        }
        if (bitemporality.equals(this.addressNameTemporality)) {
            data.setAddressName(
                    // int authority,
                    this.getInt("adrnvn_mynkod"),
                    // String addressName
                    this.get("adrnvn"),
                    importMetadata.getImportTime()
            );
            updated = true;
        }
        if (bitemporality.equals(this.documentNameTemporality)) {
            data.setNameVerification(
                    // int authority,
                    this.getInt("dok_mynkod-navne"),
                    // boolean verification
                    this.getBoolean("dok-navne"),
                    importMetadata.getImportTime()
            );
            updated = true;
        }
        if (bitemporality.equals(this.officiaryTemporality)) {
            data.setNameAuthorityText(
                    // int authority,
                    this.getInt("myntxt_mynkod-navne"),
                    // String text
                    this.get("myntxt-navne"),
                    importMetadata.getImportTime()
            );
            updated = true;
        }
        return updated;
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
                this.get("fornvn"),
                this.getMarking("fornvn_mrk"),
                this.get("melnvn"),
                this.getMarking("melnvn_mrk"),
                this.get("efternvn"),
                this.getMarking("efternvn_mrk"),
                this.get("slægtsnvn"),
                this.getMarking("slægtsnvn_mrk")
        ).setAuthority(
                this.getInt("start_mynkod-navne")
        ).setBitemporality(
                this.nameTemporality
        ));

        records.add(new AddressNameDataRecord(
                this.get("adrnvn"),
                this.getBoolean("indrap-navne")
        ).setAuthority(
                this.getInt("adrnvn_mynkod")
        ).setBitemporality(
                this.addressNameTemporality
        ));

        records.add(new NameVerificationDataRecord(
                this.getBoolean("dok-navne"),
                null
        ).setAuthority(
                this.getInt("dok_mynkod-navne")
        ).setBitemporality(
                this.documentNameTemporality
        ));

        records.add(new NameAuthorityTextDataRecord(
                this.getString("myntxt-navne", true),
                null
        ).setAuthority(
                this.getInt("myntxt_mynkod-navne")
        ).setBitemporality(
                this.officiaryTemporality
        ));

        return records;
    }


    @Override
    public List<CprBitemporality> getBitemporality() {
        ArrayList<CprBitemporality> bitemporalities = new ArrayList<>();
        if (this.has("fornvn") || this.has("melnvn") || this.has("efternvn") || this.has("slægtsnvn")) {
            bitemporalities.add(this.nameTemporality);
        }
        if (this.has("adrnvn") || this.has("adrnvn_mynkod")) {
            bitemporalities.add(this.addressNameTemporality);
        }
        if (this.has("dok-navne") || this.has("dok_mynkod-navne")) {
            bitemporalities.add(this.documentNameTemporality);
        }
        if (this.has("myntxt-navne") || this.has("myntxt_mynkod-navne")) {
            bitemporalities.add(this.officiaryTemporality);
        }
        return bitemporalities;
    }

    @Override
    public Set<PersonEffect> getEffects() {
        HashSet<PersonEffect> effects = new HashSet<>();
        effects.add(new PersonEffect(null, this.getOffsetDateTime("nvnhaenstart"), this.getMarking("haenstart_umrk-navne"), null, false));
        effects.add(new PersonEffect(null, null, false, null, false));
        return effects;
    }
}
