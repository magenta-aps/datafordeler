package dk.magenta.datafordeler.cpr.records;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.cpr.data.CprData;
import dk.magenta.datafordeler.cpr.data.CprEffect;
import dk.magenta.datafordeler.cpr.data.CprEntityManager;
import org.hibernate.Session;

import java.time.OffsetDateTime;
import java.util.*;


/**
 * Superclass for line records.
 */
public abstract class CprDataRecord<V extends CprEffect, B extends CprData> extends Record {

    public CprDataRecord(String line) throws ParseException {
        super(line);
    }

    public abstract boolean populateBaseData(B data, CprBitemporality bitemporality, Session session, ImportMetadata importMetadata);


    protected B getBaseDataItem(HashMap<V, B> data) {
        return this.getBaseDataItem(data, null, false, null, false);
    }

    protected B getBaseDataItem(HashMap<V, B> data, OffsetDateTime effectFrom, boolean effectFromUncertain) {
        return this.getBaseDataItem(data, effectFrom, effectFromUncertain, null, false);
    }

    protected B getBaseDataItem(HashMap<V, B> data, OffsetDateTime effectFrom, boolean effectFromUncertain, OffsetDateTime effectTo, boolean effectToUncertain) {
        V effect = null;
        for (V e : data.keySet()) {
            if (e.compareRange(effectFrom, effectFromUncertain, effectTo, effectToUncertain)) {
                effect = e;
                break;
            }
        }
        if (effect == null) {
            effect = this.createEffect(effectFrom, effectFromUncertain, effectTo, effectToUncertain);
            data.put(effect, this.createEmptyBaseData());
        }
        return data.get(effect);
    }

    protected abstract V createEffect(OffsetDateTime effectFrom, boolean effectFromUncertain, OffsetDateTime effectTo, boolean effectToUncertain);

    protected abstract B createEmptyBaseData();

    public abstract Set<V> getEffects();

    public abstract Set<OffsetDateTime> getRegistrationTimestamps();

    public abstract List<CprBitemporality> getBitemporality();

    public boolean filter(ObjectNode importConfiguration) {
        if (importConfiguration != null && importConfiguration.size() > 0) {
            if (importConfiguration.has(CprEntityManager.IMPORTCONFIG_RECORDTYPE)) {
                HashSet<String> acceptedRecordTypes = new HashSet<>(
                        getConfigValueAsText(importConfiguration.get(CprEntityManager.IMPORTCONFIG_RECORDTYPE), "%03d")
                );
                return acceptedRecordTypes.contains(this.getRecordType());
            }
        }
        return true;
    }

    protected static List<String> getConfigValueAsText(JsonNode value, String fmtNumber) {
        ArrayList<String> output = new ArrayList<>();
        if (value != null) {
            if (value.isTextual()) {
                output.add(value.asText());
            } else if (value.isIntegralNumber()) {
                if (fmtNumber == null) {
                    fmtNumber = "%d";
                }
                output.add(String.format(fmtNumber, value.asLong()));
            } else if (value.isArray()) {
                for (JsonNode j : value) {
                    output.addAll(getConfigValueAsText(j, fmtNumber));
                }
            }
        }
        return output;
    }

}