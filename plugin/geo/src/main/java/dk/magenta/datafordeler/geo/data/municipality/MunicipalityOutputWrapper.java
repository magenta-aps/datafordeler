package dk.magenta.datafordeler.geo.data.municipality;

import dk.magenta.datafordeler.geo.data.GeoOutputWrapper;
import org.springframework.stereotype.Component;

@Component
public class MunicipalityOutputWrapper extends GeoOutputWrapper<MunicipalityEntity> {

    @Override
    protected void fillContainer(OutputContainer container, MunicipalityEntity item) {
        container.addNontemporal("kommunekode", item.getCode());
        container.addMonotemporal("navn", item.getName());
        container.addNontemporal("sumiffiik", item.getSumiffiikId());
    }

}
