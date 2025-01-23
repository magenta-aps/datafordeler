package dk.magenta.datafordeler.geo.data;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.util.BitemporalityQuery;
import dk.magenta.datafordeler.geo.data.common.GeoMonotemporalRecord;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public abstract class GeoOutputWrapper<E extends GeoEntity> extends dk.magenta.datafordeler.core.fapi.RecordOutputWrapper<E> {

    @Override
    public Set<String> getRemoveFieldNames(Mode mode) {
        HashSet<String> fields = new HashSet<>(super.getRemoveFieldNames(mode));
        fields.add(GeoMonotemporalRecord.IO_FIELD_EDITOR);
        return fields;
    }

    @Override
    protected ObjectNode fallbackOutput(Mode mode, OutputContainer recordOutput, BitemporalityQuery mustMatch) {
        if (mode == Mode.LEGACY) {
            return recordOutput.getRVD(mustMatch);
        }
        return null;
    }
}
