package dk.magenta.datafordeler.geo.data.locality;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.fapi.JsonModifier;
import dk.magenta.datafordeler.core.fapi.ResultSet;
import dk.magenta.datafordeler.geo.data.GeoOutputWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class LocalityOutputWrapper extends GeoOutputWrapper<GeoLocalityEntity> {

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    private void register() {
        this.register(GeoLocalityEntity.class);
    }

    @Override
    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    @Override
    protected JsonModifier getModifier(ResultSet resultSet) {
        return new LocalityOutputJsonModifier(this, resultSet);
    }

    @Override
    protected void fillContainer(OutputContainer container, GeoLocalityEntity item, Mode mode) {
        container.addNontemporal("lokalitetskode", item.getCode());
        container.addMonotemporal("navn", item.getName());
        container.addMonotemporal("forkortelse", item.getAbbreviation());
        container.addMonotemporal("type", item.getType());
        container.addMonotemporal("kommune", item.getMunicipality());
        container.addNontemporal("sumiffiik", item.getSumiffiikId());
    }

}
