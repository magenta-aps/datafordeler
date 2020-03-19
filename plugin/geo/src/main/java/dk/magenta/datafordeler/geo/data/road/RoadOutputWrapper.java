package dk.magenta.datafordeler.geo.data.road;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.fapi.JsonModifier;
import dk.magenta.datafordeler.cpr.records.road.data.RoadEntity;
import dk.magenta.datafordeler.geo.data.GeoOutputWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@Component
public class RoadOutputWrapper extends GeoOutputWrapper<GeoRoadEntity> {

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    private void register() {
        this.register(GeoRoadEntity.class);
    }

    @Override
    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    @Override
    protected JsonModifier getModifier(Collection<GeoRoadEntity> entries) {
        return new RoadOutputJsonModifier(this, entries);
    }

    @Override
    protected void fillContainer(OutputContainer container, GeoRoadEntity item, Mode mode) {
        container.addNontemporal("vejkode", item.getCode());
        container.addMonotemporal("navn", item.getName());
        container.addMonotemporal("lokalitet", item.getLocality());
        container.addMonotemporal("kommune", item.getMunicipality());
        container.addNontemporal("sumiffiik", item.getSumiffiikId());
    }

}
