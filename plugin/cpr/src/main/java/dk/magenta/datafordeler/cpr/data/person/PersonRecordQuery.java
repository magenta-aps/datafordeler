package dk.magenta.datafordeler.cpr.data.person;

import dk.magenta.datafordeler.core.database.LookupDefinition;
import dk.magenta.datafordeler.cpr.CprRecordLookupDefinition;
import dk.magenta.datafordeler.cpr.records.person.data.AddressDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.NameDataRecord;

/**
 * Container for a query for Persons, defining fields and database lookup
 */
public class PersonRecordQuery extends PersonQuery {

    @Override
    public LookupDefinition getLookupDefinition() {
        LookupDefinition lookupDefinition = new CprRecordLookupDefinition(this, null);
        if (!this.getPersonnumre().isEmpty()) {
            lookupDefinition.put(LookupDefinition.entityref + LookupDefinition.separator + PersonEntity.DB_FIELD_CPR_NUMBER, this.getPersonnumre(), String.class);
        }
        if (this.getFornavn() != null) {
            lookupDefinition.put(LookupDefinition.entityref + LookupDefinition.separator + PersonEntity.DB_FIELD_NAME + LookupDefinition.separator + NameDataRecord.DB_FIELD_FIRST_NAMES, this.getFornavn(), String.class);
        }
        if (this.getEfternavn() != null) {
            lookupDefinition.put(LookupDefinition.entityref + LookupDefinition.separator + PersonEntity.DB_FIELD_NAME + LookupDefinition.separator + NameDataRecord.DB_FIELD_LAST_NAME, this.getEfternavn(), String.class);
        }
        if (!this.getKommunekoder().isEmpty()) {
            lookupDefinition.put(LookupDefinition.entityref + LookupDefinition.separator + PersonEntity.DB_FIELD_ADDRESS + LookupDefinition.separator + AddressDataRecord.DB_FIELD_MUNICIPALITY_CODE, this.getKommunekoder(), Integer.class);
        }
        if (!this.getKommunekodeRestriction().isEmpty()) {
            lookupDefinition.put(LookupDefinition.entityref + LookupDefinition.separator + PersonEntity.DB_FIELD_ADDRESS + LookupDefinition.separator + AddressDataRecord.DB_FIELD_MUNICIPALITY_CODE, this.getKommunekodeRestriction(), Integer.class);
        }
        return lookupDefinition;
    }

}
