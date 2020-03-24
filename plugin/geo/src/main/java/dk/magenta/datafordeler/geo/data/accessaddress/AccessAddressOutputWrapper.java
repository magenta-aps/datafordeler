package dk.magenta.datafordeler.geo.data.accessaddress;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.fapi.JsonModifier;
import dk.magenta.datafordeler.core.fapi.ResultSet;
import dk.magenta.datafordeler.geo.data.GeoOutputWrapper;
import dk.magenta.datafordeler.geo.data.municipality.GeoMunicipalityEntity;
import dk.magenta.datafordeler.geo.data.municipality.MunicipalityOutputJsonModifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collection;

@Component
public class AccessAddressOutputWrapper extends GeoOutputWrapper<AccessAddressEntity> {

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    private void register() {
        this.register(AccessAddressEntity.class);
    }

    @Override
    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    @Override
    protected JsonModifier getModifier(ResultSet resultSet) {
        return new AccessAddressOutputJsonModifier(this, resultSet);
    }
    @Override
    protected void fillContainer(OutputContainer container, AccessAddressEntity item, Mode mode) {
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
