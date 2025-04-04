package dk.magenta.datafordeler.geo.data.building;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.geo.data.GeoOutputWrapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BuildingOutputWrapper extends GeoOutputWrapper<BuildingEntity> {

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    private void register() {
        this.register(BuildingEntity.class);
    }

    @Override
    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    @Override
    protected void fillContainer(OutputContainer container, BuildingEntity item, Mode mode) {
        container.addNontemporal("anr", item.getAnr());
        container.addNontemporal("bnr", item.getBnr());
        container.addMonotemporal("lokalitet", item.getLocality());
        container.addNontemporal("sumiffiik", item.getSumiffiikId());
    }

}
