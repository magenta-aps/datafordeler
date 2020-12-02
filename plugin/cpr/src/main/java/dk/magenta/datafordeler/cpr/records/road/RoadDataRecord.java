package dk.magenta.datafordeler.cpr.records.road;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.cpr.data.CprRecordEntityManager;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprDataRecord;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Superclass for Road records
 */
public abstract class RoadDataRecord extends CprDataRecord {

    public static final String RECORDTYPE_ROAD = "001";
    public static final String RECORDTYPE_ROADCITY = "003";
    public static final String RECORDTYPE_ROADPOSTCODE = "004";
    public static final String RECORDTYPE_ROADMEMO = "005";
    // TODO: Add one for each data type

    public RoadDataRecord(String line) throws ParseException {
        super(line);
        this.obtain("komkod", 4, 4);
        this.obtain("vejkod", 8, 4);
    }


    public boolean filter(ObjectNode importConfiguration) {
        if (importConfiguration != null && importConfiguration.size() > 0) {
            if (importConfiguration.has(CprRecordEntityManager.IMPORTCONFIG_RECORDTYPE)) {
                HashSet<String> acceptedRecordTypes = new HashSet<>(
                        getConfigValueAsText(importConfiguration.get(CprRecordEntityManager.IMPORTCONFIG_RECORDTYPE), "%03d")
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


    public int getMunicipalityCode() {
        return this.getInt("komkod");
    }

    public int getRoadCode() {
        return this.getInt("vejkod");
    }

    public abstract List<CprBitemporalRecord> getBitemporalRecords();

}
