package dk.magenta.datafordeler.geo.data.road;

import dk.magenta.datafordeler.geo.data.GeoOutputWrapper;
import org.springframework.stereotype.Component;

@Component
public class RoadOutputWrapper extends GeoOutputWrapper<RoadEntity> {

    @Override
    protected void fillContainer(OutputContainer container, RoadEntity item) {
        container.addNontemporal("vejkode", item.getCode());
        container.addMonotemporal("navn", item.getName());
        container.addMonotemporal("lokalitet", item.getLocality());
        container.addMonotemporal("kommune", item.getMunicipality());
        container.addNontemporal("sumiffiik", item.getSumiffiikId());
    }

}
