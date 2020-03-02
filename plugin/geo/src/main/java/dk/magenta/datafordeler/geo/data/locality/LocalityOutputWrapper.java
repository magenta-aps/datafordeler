package dk.magenta.datafordeler.geo.data.locality;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.geo.data.GeoOutputWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LocalityOutputWrapper extends GeoOutputWrapper<LocalityEntity> {

    @Autowired
    private ObjectMapper objectMapper;


    @Override
    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

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
