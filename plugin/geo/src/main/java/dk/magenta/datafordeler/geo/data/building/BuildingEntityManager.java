package dk.magenta.datafordeler.geo.data.building;

import dk.magenta.datafordeler.geo.data.GeoEntityManager;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("GeoBuildingEntityManager")
public class BuildingEntityManager extends GeoEntityManager<BuildingEntity, BuildingRawData> {

    @Autowired
    private BuildingService buildingService;

    public BuildingEntityManager() {
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
    protected Class<BuildingEntity> getEntityClass() {
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

}
