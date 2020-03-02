package dk.magenta.datafordeler.geo.data.building;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.geo.data.GeoOutputWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BuildingOutputWrapper extends GeoOutputWrapper<BuildingEntity> {

    @Autowired
    private ObjectMapper objectMapper;


    @Override
    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    @Override
    protected void fillContainer(OutputContainer container, BuildingEntity item) {
        container.addNontemporal("anr", item.getAnr());
        container.addNontemporal("bnr", item.getBnr());
        container.addMonotemporal("lokalitet", item.getLocality());
        container.addNontemporal("sumiffiik", item.getSumiffiikId());
    }

}
