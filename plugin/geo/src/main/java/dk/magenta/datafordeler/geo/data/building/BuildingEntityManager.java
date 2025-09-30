package dk.magenta.datafordeler.geo.data.building;

import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.geo.data.GeoEntityManager;
import dk.magenta.datafordeler.geo.data.WireCache;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("GeoBuildingEntityManager")
public class BuildingEntityManager extends GeoEntityManager<BuildingEntity, BuildingRawData> {

    private final BuildingService buildingService;

    @Autowired
    public BuildingEntityManager(@Lazy BuildingService buildingService) {
        this.buildingService = buildingService;
    }

    @Override
    protected String getBaseName() {
        return "building";
    }

    @Override
    public BuildingService getEntityService() {
        return this.buildingService;
    }

    @Override
    public String getDomain() {
        return "https://data.gl/geo/building/1/rest";
    }

    @Override
    public String getSchema() {
        return BuildingEntity.schema;
    }

    @Override
    public BaseQuery getQuery() {
        return new BuildingQuery();
    }

    @Override
    public BaseQuery getQuery(String... strings) {
        return this.getQuery();
    }

    @Override
    public Class<BuildingEntity> getManagedEntityClass() {
        return BuildingEntity.class;
    }


    @Override
    protected Class<BuildingRawData> getRawClass() {
        return BuildingRawData.class;
    }

    @Override
    protected UUID generateUUID(BuildingRawData rawData) {
        return rawData.properties.getUUID();
    }

    @Override
    protected BuildingEntity createBasicEntity(BuildingRawData record, Session session) {
        return new BuildingEntity(record);
    }

    public static String stripBnr(String bnr) {
        if (bnr != null) {
            bnr = bnr.toUpperCase();
            if (bnr.startsWith("B-")) {
                bnr = bnr.substring(2);
            } else if (bnr.startsWith("B")) {
                bnr = bnr.substring(1);
            }
        }
        return bnr;
    }

    protected void populateWireCache(WireCache wireCache, Session session) {
        wireCache.loadAllLocalities(session);
    }

}
