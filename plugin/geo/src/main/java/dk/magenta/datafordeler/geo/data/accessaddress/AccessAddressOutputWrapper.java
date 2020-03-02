package dk.magenta.datafordeler.geo.data.accessaddress;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.geo.data.GeoOutputWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AccessAddressOutputWrapper extends GeoOutputWrapper<AccessAddressEntity> {

    @Autowired
    private ObjectMapper objectMapper;


    @Override
    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    @Override
    protected void fillContainer(OutputContainer container, AccessAddressEntity item) {
        container.addMonotemporal("husNummer", item.getHouseNumber());
        container.addNontemporal("bnr", item.getBnr());
        container.addMonotemporal("blokNavn", item.getBlockName());
        container.addMonotemporal("lokalitet", item.getLocality());
        container.addMonotemporal("dataKilde", item.getSource());
        container.addMonotemporal("vej", item.getRoad());
        container.addMonotemporal("status", item.getStatus());
        container.addNontemporal("sumiffiik", item.getSumiffiikId());
    }

}
