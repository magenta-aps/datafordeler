package dk.magenta.datafordeler.cpr.records.person;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.cpr.data.CprRecordEntityManager;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.CprDataRecord;

import java.util.HashSet;
import java.util.List;

/**
 * Superclass for Person records
 */
public abstract class PersonDataRecord extends CprDataRecord {

    public static final String RECORDTYPE_PERSON = "001";
    public static final String RECORDTYPE_BIRTH = "005";
    public static final String RECORDTYPE_CHURCH = "010";
    public static final String RECORDTYPE_HISTORIC_CHURCH = "011";
    public static final String RECORDTYPE_PROTECTION = "015";
    public static final String RECORDTYPE_HISTORIC_PROTECTION = "016";
    public static final String RECORDTYPE_CURRENT_NAME = "020";
    public static final String RECORDTYPE_HISTORIC_NAME = "021";
    public static final String RECORDTYPE_DOMESTIC_ADDRESS = "025";
    public static final String RECORDTYPE_HISTORIC_DOMESTIC_ADDRESS = "026";
    public static final String RECORDTYPE_FOREIGN_ADDRESS = "028";
    public static final String RECORDTYPE_HISTORIC_FOREIGN_ADDRESS = "029";
    public static final String RECORDTYPE_CIVILSTATUS = "035";
    public static final String RECORDTYPE_HISTORIC_CIVILSTATUS = "036";
    public static final String RECORDTYPE_CITIZENSHIP = "040";
    public static final String RECORDTYPE_HISTORIC_CITIZENSHIP = "041";
    public static final String RECORDTYPE_CHILDREN = "045";
    public static final String RECORDTYPE_GUARDIANSHIP = "050";
    public static final String RECORDTYPE_HISTORIC_CPRNUMBER = "065";
    public static final String RECORDTYPE_EVENTTYPE = "900";

    public PersonDataRecord(String line) throws ParseException {
        super(line);
        this.obtain("pnr", 4, 10);
    }

    public String getCprNumber() {
        return this.getString("pnr", false);
    }


    @Override
    public boolean filter(ObjectNode importConfiguration) {
        if (importConfiguration != null && importConfiguration.size() > 0) {
            if (importConfiguration.has(CprRecordEntityManager.IMPORTCONFIG_PNR)) {
                HashSet<String> acceptedCprNumbers = new HashSet<>(
                        getConfigValueAsText(importConfiguration.get(CprRecordEntityManager.IMPORTCONFIG_PNR), "%d")
                );
                boolean found = false;
                for (String accept : acceptedCprNumbers) {
                    if (accept.endsWith("*")) {
                        accept = accept.substring(0, accept.indexOf("*"));
                        if (this.getCprNumber().startsWith(accept)) {
                            found = true;
                            break;
                        }
                    } else if (accept.equals(this.getCprNumber())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
        }
        return super.filter(importConfiguration);
    }

    public abstract List<CprBitemporalRecord> getBitemporalRecords();
}
