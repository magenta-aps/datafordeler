package dk.magenta.datafordeler.geo.data.locality;

import dk.magenta.datafordeler.geo.data.GeoEntityManager;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("GeoLocalityEntityManager")
public class LocalityEntityManager extends GeoEntityManager<LocalityEntity, LocalityRawData> {

    @Autowired
    private LocalityService localityService;

    public LocalityEntityManager() {
    }

    @Override
    protected String getBaseName() {
        return "locality";
    }

    @Override
    public LocalityService getEntityService() {
        return this.localityService;
    }

    @Override
    public String getDomain() {
        return "https://data.gl/geo/locality/1/rest";
    }

    @Override
    public String getSchema() {
        return LocalityEntity.schema;
    }

    @Override
    protected Class<LocalityEntity> getEntityClass() {
        return LocalityEntity.class;
    }


    @Override
    protected Class<LocalityRawData> getRawClass() {
        return LocalityRawData.class;
    }

    @Override
    protected UUID generateUUID(LocalityRawData rawData) {
        return rawData.properties.getUUID();
    }

    @Override
    protected LocalityEntity createBasicEntity(LocalityRawData record, Session session) {
        return new LocalityEntity(record);
    }

}
