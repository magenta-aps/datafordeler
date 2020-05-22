package dk.magenta.datafordeler.cvr.query;

import dk.magenta.datafordeler.core.database.BaseLookupDefinition;
import dk.magenta.datafordeler.core.database.FieldDefinition;
import dk.magenta.datafordeler.core.database.LookupDefinition;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.cvr.records.CvrBitemporalRecord;

import java.time.OffsetDateTime;
import java.util.StringJoiner;

public class CvrRecordLookupDefinition extends BaseLookupDefinition {

    public CvrRecordLookupDefinition() {
    }

    public CvrRecordLookupDefinition(BaseQuery query) {
        super(query);
        if (query.getRecordAfter() != null) {
            this.put(LookupDefinition.entityref + LookupDefinition.separator + CvrBitemporalRecord.DB_FIELD_LAST_UPDATED, query.getRecordAfter(), OffsetDateTime.class, LookupDefinition.Operator.GT);
        }
    }

    @Override
    public boolean usingRVDModel() {
        return false;
    }

}
