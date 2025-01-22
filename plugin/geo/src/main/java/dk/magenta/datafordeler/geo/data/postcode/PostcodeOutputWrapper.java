package dk.magenta.datafordeler.geo.data.postcode;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.geo.data.GeoOutputWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class PostcodeOutputWrapper extends GeoOutputWrapper<PostcodeEntity> {

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    private void register() {
        this.register(PostcodeEntity.class);
    }

    @Override
    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    @Override
    protected void fillContainer(OutputContainer container, PostcodeEntity item, Mode mode) {
        container.addNontemporal("postnummer", item.getCode());
        container.addMonotemporal("postdistrikt", item.getName());
    }

}
