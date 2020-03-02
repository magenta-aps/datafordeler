package dk.magenta.datafordeler.geo.data.unitaddress;

import dk.magenta.datafordeler.geo.data.GeoOutputWrapper;
import org.springframework.stereotype.Component;

@Component
public class UnitAddressOutputWrapper extends GeoOutputWrapper<UnitAddressEntity> {

    @Override
    protected void fillContainer(OutputContainer container, UnitAddressEntity item) {
        container.addMonotemporal("dør", item.getDoor());
        container.addMonotemporal("etage", item.getFloor());
        container.addMonotemporal("anvendelse", item.getUsage());
        container.addMonotemporal("nummer", item.getNumber());
        //container.addMonotemporal("import", item.getImportStatus());
        container.addMonotemporal("kilde", item.getSource());
        container.addMonotemporal("status", item.getStatus());
        container.addNontemporal("sumiffiik", item.getSumiffiikId());
    }

}
