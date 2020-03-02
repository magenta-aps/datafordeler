package dk.magenta.datafordeler.geo.data.postcode;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.geo.data.GeoOutputWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PostcodeOutputWrapper extends GeoOutputWrapper<PostcodeEntity> {

    @Autowired
    private ObjectMapper objectMapper;


    @Override
    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    @Override
    protected void fillContainer(OutputContainer container, PostcodeEntity item) {
        container.addNontemporal("postnummer", item.getCode());
        container.addNontemporal("postdistrikt", item.getName());
    }

}
