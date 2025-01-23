package dk.magenta.datafordeler.geo.data.road;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.fapi.JsonModifier;
import dk.magenta.datafordeler.core.fapi.ResultSet;
import dk.magenta.datafordeler.cpr.records.road.RoadPostcodeRecord;
import dk.magenta.datafordeler.geo.data.GeoOutputWrapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    protected JsonModifier getModifier(ResultSet resultSet) {
        return new RoadOutputJsonModifier(this, resultSet.get(GeoRoadEntity.class));
    }

    @Override
    protected void fillContainer(OutputContainer container, GeoRoadEntity item, Mode mode) {
        container.addNontemporal("vejkode", item.getCode());
        container.addMonotemporal("navn", item.getName());
        container.addMonotemporal("lokalitet", item.getLocality());
        container.addMonotemporal("kommune", item.getMunicipality());
        container.addNontemporal("sumiffiik", item.getSumiffiikId());
    }

    public Map<Class, List<String>> getEligibleModifierNames() {
        HashMap<Class, List<String>> map = new HashMap<>();
        map.put(RoadMunicipalityRecord.class, Collections.singletonList("geo_municipality"));
        //map.put(RoadLocalityRecord.class, Collections.singletonList("geo_locality"));
        map.put(RoadPostcodeRecord.class, Collections.singletonList("geo_postcode"));
        return map;
    }

}
