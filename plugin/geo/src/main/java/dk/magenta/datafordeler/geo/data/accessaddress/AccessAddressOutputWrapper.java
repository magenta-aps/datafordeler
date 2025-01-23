package dk.magenta.datafordeler.geo.data.accessaddress;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.fapi.JsonModifier;
import dk.magenta.datafordeler.core.fapi.ResultSet;
import dk.magenta.datafordeler.geo.data.GeoOutputWrapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

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
        container.addMonotemporal("postnummer", item.getPostcode());
    }


    public Map<Class, List<String>> getEligibleModifierNames() {
        HashMap<Class, List<String>> map = new HashMap<>();
        ArrayList<String> addressModifiers = new ArrayList<>();
        addressModifiers.add("geo_locality");
        addressModifiers.add("geo_road");
        addressModifiers.add("geo_municipality");
        map.put(AccessAddressEntity.class, addressModifiers);

        ArrayList<String> roadModifiers = new ArrayList<>();
        roadModifiers.add("geo_road");
        roadModifiers.add("geo_municipality");
        map.put(AccessAddressRoadRecord.class, roadModifiers);
        map.put(AccessAddressLocalityRecord.class, Collections.singletonList("geo_locality"));
        map.put(AccessAddressPostcodeRecord.class, Collections.singletonList("geo_postcode"));

        return map;
    }

}
