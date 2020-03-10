package dk.magenta.datafordeler.geo.data.road;

import dk.magenta.datafordeler.core.database.BaseLookupDefinition;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.geo.data.GeoEntityManager;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("GeoRoadEntityManager")
public class RoadEntityManager extends GeoEntityManager<GeoRoadEntity, RoadRawData> {

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
    public Map<String, String> getJoinHandles(String entityIdentifier) {
        HashMap<String, String> handles = new HashMap<>();
        handles.put("municipalitycode", BaseLookupDefinition.getParameterPath(entityIdentifier, entityIdentifier, GeoRoadEntity.DB_FIELD_MUNICIPALITY) + BaseLookupDefinition.separator + RoadMunicipalityRecord.DB_FIELD_CODE);
        handles.put("roadcode", entityIdentifier + BaseLookupDefinition.separator + GeoRoadEntity.DB_FIELD_CODE);
        return handles;
    }

    @Override
    public BaseQuery getJoinQuery(Map<String, String> theirJoinHandles, String entityIdentifier) {
        StringJoiner buffer = new StringJoiner(" AND ");
        RoadQuery query = new RoadQuery();
        query.setEntityClassname(GeoRoadEntity.class);
        Map<String, String> ourJoinHandles = this.getJoinHandles(entityIdentifier);
        if (theirJoinHandles.containsKey("municipalitycode")) {
            buffer.add(ourJoinHandles.get("municipalitycode") + " = " + theirJoinHandles.get("municipalitycode"));
            query.addForcedJoin(RoadQuery.MUNICIPALITY);
        }
        if (theirJoinHandles.containsKey("roadcode")) {
            buffer.add(ourJoinHandles.get("roadcode") + " = " + theirJoinHandles.get("roadcode"));
        }
        query.setJoinString(buffer.toString());
        return query;
    }
}
