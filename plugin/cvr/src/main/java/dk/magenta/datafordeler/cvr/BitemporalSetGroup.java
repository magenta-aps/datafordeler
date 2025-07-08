package dk.magenta.datafordeler.cvr;

import dk.magenta.datafordeler.core.util.DoubleHashMap;
import dk.magenta.datafordeler.cvr.records.CvrBitemporalRecord;
import dk.magenta.datafordeler.cvr.records.CvrRecord;

import java.util.function.Consumer;

public class BitemporalSetGroup<R extends CvrBitemporalRecord, P extends CvrRecord> extends DoubleHashMap<Integer, Integer, BitemporalSet<R, P>> {
    public void traverse(Consumer<RecordSet<? extends CvrRecord, ? extends CvrRecord>> groupCallback, Consumer<CvrRecord> itemCallback) {
        for (BitemporalSet<R, P> values : this.subvalues()) {
            values.traverse(groupCallback, itemCallback);
        }
    }
}
