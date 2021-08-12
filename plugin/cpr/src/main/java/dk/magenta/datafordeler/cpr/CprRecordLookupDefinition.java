package dk.magenta.datafordeler.cpr;

import dk.magenta.datafordeler.core.database.DataItem;
import dk.magenta.datafordeler.core.database.FieldDefinition;
import dk.magenta.datafordeler.core.database.LookupDefinition;
import dk.magenta.datafordeler.core.fapi.Query;

import java.util.StringJoiner;

public class CprRecordLookupDefinition extends LookupDefinition {

    public CprRecordLookupDefinition(Class<? extends DataItem> dataClass) {
        super(dataClass);
    }

    public CprRecordLookupDefinition(Query query, Class<? extends DataItem> dataClass) {
        super(query, dataClass);

        /*if (query.getRecordAfter() != null) {
            this.put(LookupDefinition.entityref + LookupDefinition.separator + CvrBitemporalRecord.DB_FIELD_LAST_UPDATED, query.getRecordAfter(), OffsetDateTime.class, Operator.GT);
        }*/
    }

    @Override
    public boolean usingRVDModel() {
        return false;
    }

    @Override
    public String getHqlWhereString(String dataItemKey, String entityKey, String prefix) {
        StringJoiner extraWhere = new StringJoiner(" AND ");
        for (FieldDefinition fieldDefinition : this.fieldDefinitions) {
            extraWhere.add("(" + this.getHqlWherePart(dataItemKey, entityKey, fieldDefinition, true) + ")");
        }
        if (extraWhere.length() > 0) {
            return prefix + " " + extraWhere.toString();
        }
        return "";
    }

}
