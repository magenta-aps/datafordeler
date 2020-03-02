package dk.magenta.datafordeler.geo.data.locality;

import dk.magenta.datafordeler.geo.data.GeoOutputWrapper;
import org.springframework.stereotype.Component;

@Component
public class LocalityOutputWrapper extends GeoOutputWrapper<LocalityEntity> {

    @Override
    protected void fillContainer(OutputContainer container, LocalityEntity item) {
        container.addNontemporal("lokalitetskode", item.getCode());
        container.addMonotemporal("navn", item.getName());
        container.addMonotemporal("forkortelse", item.getAbbreviation());
        container.addMonotemporal("type", item.getType());
        container.addMonotemporal("kommune", item.getMunicipality());
        container.addNontemporal("sumiffiik", item.getSumiffiikId());
    }

}
