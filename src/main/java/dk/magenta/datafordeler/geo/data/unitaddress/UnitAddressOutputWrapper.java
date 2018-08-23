package dk.magenta.datafordeler.geo.data.unitaddress;

import dk.magenta.datafordeler.geo.data.GeoOutputWrapper;
import dk.magenta.datafordeler.geo.data.common.GeoMonotemporalRecord;
import dk.magenta.datafordeler.geo.data.road.RoadEntity;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class UnitAddressOutputWrapper extends GeoOutputWrapper<UnitAddressEntity> {

    @Override
    protected void fillMetadataContainer(OutputContainer container, UnitAddressEntity item) {
        container.addMonotemporal("dør", item.getDoor());
        container.addMonotemporal("etage", item.getFloor());
        container.addMonotemporal("anvendelse", item.getUsage());
        container.addMonotemporal("nummer", item.getNumber());
        container.addMonotemporal("import", item.getImportStatus());
        container.addMonotemporal("kilde", item.getSource());
        container.addMonotemporal("status", item.getStatus());
    }

    @Override
    public Set<String> getRemoveFieldNames() {
        HashSet<String> fields = new HashSet<>();
        fields.add(GeoMonotemporalRecord.IO_FIELD_EDITOR);
        return fields;
    }
}
