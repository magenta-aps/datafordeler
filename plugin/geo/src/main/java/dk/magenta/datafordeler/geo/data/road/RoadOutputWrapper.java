package dk.magenta.datafordeler.geo.data.road;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.geo.data.GeoOutputWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RoadOutputWrapper extends GeoOutputWrapper<GeoRoadEntity> {

    @Autowired
    private ObjectMapper objectMapper;


    @Override
    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    @Override
    protected void fillContainer(OutputContainer container, GeoRoadEntity item) {
        container.addNontemporal("vejkode", item.getCode());
        container.addMonotemporal("navn", item.getName());
        container.addMonotemporal("lokalitet", item.getLocality());
        container.addMonotemporal("kommune", item.getMunicipality());
        container.addNontemporal("sumiffiik", item.getSumiffiikId());
    }

}
