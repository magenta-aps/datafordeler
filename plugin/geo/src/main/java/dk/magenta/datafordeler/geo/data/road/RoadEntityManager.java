package dk.magenta.datafordeler.geo.data.road;

import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.geo.data.GeoEntityManager;
import dk.magenta.datafordeler.geo.data.WireCache;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("GeoRoadEntityManager")
public class RoadEntityManager extends GeoEntityManager<GeoRoadEntity, RoadRawData> {

    private final RoadService roadService;

    @Autowired
    public RoadEntityManager(@Lazy RoadService roadService) {
        this.roadService = roadService;
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
        return GeoRoadEntity.schema;
    }

    @Override
    protected Class<GeoRoadEntity> getEntityClass() {
        return GeoRoadEntity.class;
    }


    @Override
    protected Class<RoadRawData> getRawClass() {
        return RoadRawData.class;
    }

    @Override
    protected UUID generateUUID(RoadRawData rawData) {
        return rawData.properties.getUUID();
    }

    @Override
    protected GeoRoadEntity createBasicEntity(RoadRawData record, Session session) {
        return new GeoRoadEntity(record);
    }

    @Override
    public BaseQuery getQuery() {
        return new RoadQuery();
    }

    @Override
    public BaseQuery getQuery(String... strings) {
        RoadQuery roadQuery = new RoadQuery();
        for (String join : strings) {
            switch (join) {
                case "municipality":
                    roadQuery.addRelatedMunicipalityQuery();
                    break;
                case "locality":
                    roadQuery.addRelatedLocalityQuery();
                    break;
            }
        }
        return roadQuery;
    }

    protected void populateWireCache(WireCache wireCache, Session session) {
        wireCache.loadAllLocalities(session);
    }
}
