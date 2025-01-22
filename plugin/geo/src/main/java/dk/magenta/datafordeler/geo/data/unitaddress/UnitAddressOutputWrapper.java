package dk.magenta.datafordeler.geo.data.unitaddress;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.geo.data.GeoOutputWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UnitAddressOutputWrapper extends GeoOutputWrapper<UnitAddressEntity> {

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    private void register() {
        this.register(UnitAddressEntity.class);
    }

    @Override
    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    @Override
    protected void fillContainer(OutputContainer container, UnitAddressEntity item, Mode mode) {
        container.addMonotemporal("etage", item.getFloor());
        container.addMonotemporal("anvendelse", item.getUsage());
        container.addMonotemporal("nummer", item.getNumber());
        container.addMonotemporal("kilde", item.getSource());
        container.addMonotemporal("status", item.getStatus());
        container.addNontemporal("sumiffiik", item.getSumiffiikId());
        container.addNontemporal("accessaddress", item.getAccessAddress());
    }

    public Map<Class, List<String>> getEligibleModifierNames() {
        HashMap<Class, List<String>> map = new HashMap<>();
        ArrayList<String> addressModifiers = new ArrayList<>();
        addressModifiers.add("geo_accessaddress");
        addressModifiers.add("geo_locality");
        addressModifiers.add("geo_road");
        addressModifiers.add("geo_municipality");
        map.put(Identification.class, addressModifiers);
        return map;
    }

}
