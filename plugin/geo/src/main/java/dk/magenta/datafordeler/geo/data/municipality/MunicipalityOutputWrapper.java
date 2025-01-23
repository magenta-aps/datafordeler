package dk.magenta.datafordeler.geo.data.municipality;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.fapi.JsonModifier;
import dk.magenta.datafordeler.core.fapi.ResultSet;
import dk.magenta.datafordeler.geo.data.GeoOutputWrapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MunicipalityOutputWrapper extends GeoOutputWrapper<GeoMunicipalityEntity> {

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    private void register() {
        this.register(GeoMunicipalityEntity.class);
    }


    @Override
    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    @Override
    protected JsonModifier getModifier(ResultSet resultSet) {
        return new MunicipalityOutputJsonModifier(this, resultSet.get(GeoMunicipalityEntity.class));
    }

    @Override
    protected void fillContainer(OutputContainer container, GeoMunicipalityEntity item, Mode mode) {
        container.addNontemporal("kommunekode", item.getCode());
        container.addMonotemporal("navn", item.getName());
        container.addNontemporal("sumiffiik", item.getSumiffiikId());
    }

}
