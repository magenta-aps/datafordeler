package dk.magenta.datafordeler.cpr.records.output;

import dk.magenta.datafordeler.cpr.data.CprRecordEntity;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;

import java.util.HashSet;
import java.util.Set;

public abstract class CprRecordOutputWrapper<E extends CprRecordEntity> extends dk.magenta.datafordeler.core.fapi.RecordOutputWrapper<E> {

    @Override
    public Set<String> getRemoveFieldNames(Mode mode) {
        HashSet<String> fieldNames = new HashSet<>(super.getRemoveFieldNames(mode));
        if (mode == Mode.RVD || mode == Mode.RDV) {
            fieldNames.add(CprBitemporalRecord.IO_FIELD_EFFECT_FROM_UNCERTAIN);
            fieldNames.add(CprBitemporalRecord.IO_FIELD_EFFECT_TO_UNCERTAIN);
        }
        return fieldNames;
    }

}
