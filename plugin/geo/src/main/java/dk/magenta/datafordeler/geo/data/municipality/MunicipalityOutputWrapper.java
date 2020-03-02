package dk.magenta.datafordeler.geo.data.municipality;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.geo.data.GeoOutputWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MunicipalityOutputWrapper extends GeoOutputWrapper<GeoMunicipalityEntity> {

    @Autowired
    private ObjectMapper objectMapper;


    @Override
    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    @Override
    protected void fillContainer(OutputContainer container, GeoMunicipalityEntity item) {
        container.addNontemporal("kommunekode", item.getCode());
        container.addMonotemporal("navn", item.getName());
        container.addNontemporal("sumiffiik", item.getSumiffiikId());
    }

}
