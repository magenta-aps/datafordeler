package dk.magenta.datafordeler.geo.data.building;

import dk.magenta.datafordeler.geo.data.GeoOutputWrapper;
import org.springframework.stereotype.Component;

@Component
public class BuildingOutputWrapper extends GeoOutputWrapper<BuildingEntity> {

    @Override
    protected void fillContainer(OutputContainer container, BuildingEntity item) {
        container.addNontemporal("anr", item.getAnr());
        container.addNontemporal("bnr", item.getBnr());
        container.addMonotemporal("lokalitet", item.getLocality());
        container.addNontemporal("sumiffiik", item.getSumiffiikId());
    }

}
