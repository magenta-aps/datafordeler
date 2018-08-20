package dk.magenta.datafordeler.geo.data.postcode;

import dk.magenta.datafordeler.geo.data.GeoEntityManager;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PostcodeEntityManager extends GeoEntityManager<PostcodeEntity, PostcodeRawData> {

    @Autowired
    private PostcodeService postcodeService;

    public PostcodeEntityManager() {
    }

    @Override
    protected String getBaseName() {
        return "postcode";
    }

    @Override
    public PostcodeService getEntityService() {
        return this.postcodeService;
    }

    @Override
    public String getDomain() {
        return "https://data.gl/geo/postcode/1/rest";
    }

    @Override
    public String getSchema() {
        return PostcodeEntity.schema;
    }

    @Override
    protected Class<PostcodeEntity> getEntityClass() {
        return PostcodeEntity.class;
    }


    @Override
    protected Class<PostcodeRawData> getRawClass() {
        return PostcodeRawData.class;
    }

    @Override
    protected UUID generateUUID(PostcodeRawData rawData) {
        return PostcodeEntity.generateUUID(rawData.properties.code);
    }

    @Override
    protected PostcodeEntity createBasicEntity(PostcodeRawData record, Session session) {
        return new PostcodeEntity(record);
    }

}
