package dk.magenta.datafordeler.cpr.records.person;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.cpr.data.person.PersonEffect;
import dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.person.data.AddressConameDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.AddressDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.MoveMunicipalityDataRecord;
import org.hibernate.Session;

import java.util.*;

/**
 * Record for Person historic address (type 026).
 */
public class HistoricAddressRecord extends HistoricPersonDataRecord {

    private CprBitemporality addressTemporality;
    private CprBitemporality conameTemporality;
    private CprBitemporality municipalityTemporality;

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
    public boolean populateBaseData(PersonBaseData data, CprBitemporality bitemporality, Session session, ImportMetadata importMetadata) {
        boolean updated = false;

        if (bitemporality.equals(this.addressTemporality)) {
            data.setAddress(
                    // int authority,
                    this.getInt("start_mynkod-personbolig"),
                    // String bygningsnummer,
                    this.getString("bnr", true),
                    // String bynavn,
                    null,
                    // String cprKommunekode,
                    this.getInt("komkod", false),
                    // String cprKommunenavn,
                    null,
                    // String cprVejkode,
                    this.getInt("vejkod", false),
                    // String darAdresse,
                    null,
                    // String etage,
                    this.get("etage"),
                    // String husnummer,
                    this.getString("husnr", true),
                    // String postdistrikt,
                    null,
                    // String postnummer,
                    null,
                    // String sideDoer,
                    this.get("sidedoer"),
                    // String adresselinie1,
                    null,
                    // String adresselinie2,
                    null,
                    // String adresselinie3,
                    null,
                    // String adresselinie4,
                    null,
                    // String adresselinie5,
                    null,
                    // int addressTextType,
                    0,
                    // int startAuthority
                    0,
                    importMetadata.getImportTime()
            );
            updated = true;
        }

        if (bitemporality.equals(this.conameTemporality)) {
            data.setCoName(
                    this.get("convn"),
                    importMetadata.getImportTime()
            );
            updated = true;
        }
        if (bitemporality.equals(this.municipalityTemporality)) {
            data.setMoveMunicipality(
                    //int authority,
                    this.getInt("tilfra_mynkod"),
                    // LocalDateTime fraflytningsdatoKommune,
                    this.getDateTime("fraflykomdto"),
                    // boolean fraflytningsdatoKommuneUsikkerhedsmarkering,
                    this.getBoolean("fraflykomdt_umrk"),
                    // int fraflytningskommunekode,
                    this.getInt("fraflykomkod"),
                    // LocalDateTime tilflytningsdatoKommune,
                    this.getDateTime("tilflykomdto"),
                    // boolean tilflytningsdatoKommuneUsikkerhedsmarkering
                    this.getBoolean("tilflykomdt_umrk"),
                    importMetadata.getImportTime()
            );
            updated = true;
        }
        return updated;
    }

    @Override
    public boolean cleanBaseData(PersonBaseData data, CprBitemporality bitemporality, CprBitemporality outdatedTemporality, Session session) {
        boolean updated = false;
        if (bitemporality.equals(this.addressTemporality) && outdatedTemporality.equals(this.addressTemporality, CprBitemporality.EXCLUDE_EFFECT_TO)) {
            data.clearAddress(session);
            updated = true;
        }
        if (bitemporality.equals(this.conameTemporality) && outdatedTemporality.equals(this.conameTemporality, CprBitemporality.EXCLUDE_EFFECT_TO)) {
            data.clearCoName(session);
            updated = true;
        }
        if (bitemporality.equals(this.municipalityTemporality) && outdatedTemporality.equals(this.municipalityTemporality, CprBitemporality.EXCLUDE_EFFECT_TO)) {
            data.clearMoveMunicipality(session);
            updated = true;
        }
        return updated;
    }

    @Override
    public List<CprBitemporalRecord> getBitemporalRecords() {
        ArrayList<CprBitemporalRecord> records = new ArrayList<>();

        records.add(new AddressDataRecord(
                this.getInt("komkod", false),
                this.getInt("vejkod", false),
                this.getString("bnr", true),
                this.getString("husnr", true),
                this.get("etage"),
                this.getString("sidedoer", true),
                this.get("adr1-supladr"),
                this.get("adr2-supladr"),
                this.get("adr3-supladr"),
                this.get("adr4-supladr"),
                this.get("adr5-supladr"),
                this.getInt("adrtxttype"),
                this.getInt("start_mynkod-adrtxt")
        ).setAuthority(
                this.getInt("start_mynkod-personbolig")
        ).setBitemporality(
                this.addressTemporality
        ).setHistoric());

        records.add(new AddressConameDataRecord(
                this.get("convn")
        ).setAuthority(
                this.getInt("start_mynkod-personbolig")
        ).setBitemporality(
                this.getOffsetDateTime("convn_ts"),
                null,
                this.getOffsetDateTime("tilflydto"),
                this.getBoolean("tilflydto_umrk"),
                this.getOffsetDateTime("fraflydto"),
                this.getBoolean("fraflydto_umrk")
        ).setHistoric());

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
        ).setHistoric());

        return records;
    }

    @Override
    public List<CprBitemporality> getBitemporality() {
        return Arrays.asList(
                this.addressTemporality,
                this.conameTemporality,
                this.municipalityTemporality
        );
    }

    @Override
    public Set<PersonEffect> getEffects() {
        HashSet<PersonEffect> effects = new HashSet<>();
        effects.add(new PersonEffect(null, this.getOffsetDateTime("tilflydto"), this.getMarking("tilflydto_umrk"), this.getOffsetDateTime("fraflydto"), this.getMarking("fraflydto_umrk")));
        effects.add(new PersonEffect(null, null, false, null, false));
        return effects;
    }

    public int getMunicipalityCode() {
        return this.getInt("komkod");
    }
}
