package dk.magenta.datafordeler.geo.data.road;

import dk.magenta.datafordeler.geo.data.GeoEntityManager;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RoadEntityManager extends GeoEntityManager<RoadEntity, RoadRawData> {

    @Autowired
    private RoadService roadService;

    public RoadEntityManager() {
    }

    @Override
    protected String getBaseName() {
        return "road";
    }

    @Override
    public RoadService getEntityService() {
        return this.roadService;
    }

    @Override
    public String getDomain() {
        return "https://data.gl/geo/road/1/rest";
    }

    @Override
    public String getSchema() {
        return RoadEntity.schema;
    }

    @Override
    protected Class<RoadEntity> getEntityClass() {
        return RoadEntity.class;
    }


    @Override
    protected Class<RoadRawData> getRawClass() {
        return RoadRawData.class;
    }

    @Override
    protected UUID generateUUID(RoadRawData rawData) {
        return rawData.properties.getSumiffiikAsUUID();
    }

    @Override
    protected RoadEntity createBasicEntity(RoadRawData record, Session session) {
        RoadEntity r = new RoadEntity(record);
        System.out.println(r.getCode());
        return r;
    }

}
