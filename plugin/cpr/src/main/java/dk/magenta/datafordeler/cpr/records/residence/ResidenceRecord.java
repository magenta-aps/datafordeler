package dk.magenta.datafordeler.cpr.records.residence;

import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.cpr.data.residence.ResidenceEffect;
import dk.magenta.datafordeler.cpr.data.residence.data.ResidenceBaseData;
import dk.magenta.datafordeler.cpr.records.CprBitemporality;
import dk.magenta.datafordeler.cpr.records.CprDataRecord;
import org.hibernate.Session;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Record for Residence (type 002).
 */
public class ResidenceRecord extends CprDataRecord<ResidenceEffect, ResidenceBaseData> {

    public static final String RECORDTYPE_RESIDENCE = "002";

    private CprBitemporality residenceTemporality;

    public ResidenceRecord(String line) throws ParseException {
        super(line);
        this.obtain("komkod", 4, 4);
        this.obtain("vejkod", 8, 4);
        this.obtain("husnr", 12, 4);
        this.obtain("etage", 16, 2);
        this.obtain("sidedoer", 18, 4);
        this.obtain("timestamp", 22, 12);
        this.obtain("haenstart", 35, 12);
        this.obtain("lokalitet", 59, 34);

        this.residenceTemporality = new CprBitemporality(this.getOffsetDateTime("timestamp"), null, this.getOffsetDateTime("haenstart"), false, null, false);
    }

    @Override
    public String getRecordType() {
        return RECORDTYPE_RESIDENCE;
    }

    @Override
    public boolean populateBaseData(ResidenceBaseData data, CprBitemporality bitemporality, Session session, ImportMetadata importMetadata) {
        if (bitemporality.equals(this.residenceTemporality)) {
            OffsetDateTime updateTime = importMetadata.getImportTime();
            data.setKommunekode(this.getInt("komkod"), updateTime);
            data.setVejkode(this.getInt("vejkod"), updateTime);
            data.setHusnummer(this.getString("husnr", true), updateTime);
            data.setEtage(this.getString("etage", true), updateTime);
            data.setSideDoer(this.getString("sidedoer", true), updateTime);
            data.setLokalitet(this.getString("lokalitet", true), updateTime);
            return true;
        }
        return false;
    }

    @Override
    protected ResidenceEffect createEffect(OffsetDateTime effectFrom, boolean effectFromUncertain, OffsetDateTime effectTo, boolean effectToUncertain) {
        ResidenceEffect effect = new ResidenceEffect(null, effectFrom, effectTo);
        effect.setEffectFromUncertain(effectFromUncertain);
        effect.setEffectToUncertain(effectToUncertain);
        return effect;
    }

    @Override
    protected ResidenceBaseData createEmptyBaseData() {
        return new ResidenceBaseData();
    }

    @Override
    public Set<ResidenceEffect> getEffects() {
        HashSet<ResidenceEffect> effects = new HashSet<>();
        effects.add(new ResidenceEffect(null, this.getOffsetDateTime("haenstart"), false, null, false));
        return effects;
    }

    @Override
    public HashSet<OffsetDateTime> getRegistrationTimestamps() {
        HashSet<OffsetDateTime> timestamps = new HashSet<>();
        timestamps.add(this.residenceTemporality.registrationFrom);
        return timestamps;
    }

    @Override
    public List<CprBitemporality> getBitemporality() {
        return Collections.singletonList(this.residenceTemporality);
    }


    public int getMunicipalityCode() {
        return this.getInt("komkod");
    }

    public int getRoadCode() {
        return this.getInt("vejkod");
    }

    public String getHouseNumber() {
        return this.getString("husnr", true);
    }

    public String getFloor() {
        return this.getString("etage", true);
    }

    public String getDoor() {
        return this.getString("sidedoer",true);
    }

}
