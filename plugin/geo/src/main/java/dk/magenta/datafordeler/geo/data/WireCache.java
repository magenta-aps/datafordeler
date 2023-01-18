package dk.magenta.datafordeler.geo.data;

import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.util.ListHashMap;
import dk.magenta.datafordeler.geo.data.building.BuildingEntity;
import dk.magenta.datafordeler.geo.data.locality.GeoLocalityEntity;
import dk.magenta.datafordeler.geo.data.locality.LocalityQuery;
import dk.magenta.datafordeler.geo.data.municipality.GeoMunicipalityEntity;
import dk.magenta.datafordeler.geo.data.postcode.PostcodeEntity;
import dk.magenta.datafordeler.geo.data.postcode.PostcodeQuery;
import dk.magenta.datafordeler.geo.data.road.GeoRoadEntity;
import dk.magenta.datafordeler.geo.data.road.RoadMunicipalityRecord;
import dk.magenta.datafordeler.geo.data.road.RoadQuery;
import org.hibernate.Session;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class WireCache {

    public ListHashMap<String, GeoLocalityEntity> localityCacheByCode = new ListHashMap<>();

    public List<GeoLocalityEntity> getLocality(Session session, String code) throws InvalidClientInputException {
        if (!this.localityCacheByCode.containsKey(code)) {
            LocalityQuery query = new LocalityQuery();
            query.setCode(code);
            List<GeoLocalityEntity> list = QueryManager.getAllEntities(session, query, GeoLocalityEntity.class);
            this.localityCacheByCode.add(code, list);
        }
        return this.localityCacheByCode.get(code);
    }

    public void loadAllLocalities(Session session) {
        for (GeoLocalityEntity entity : QueryManager.getAllEntities(session, GeoLocalityEntity.class)) {
            this.localityCacheByCode.add(entity.getCode(), entity);
            this.localityCacheByUUID.put(entity.getUUID(), entity);
        }
    }


    public HashMap<UUID, GeoLocalityEntity> localityCacheByUUID = new HashMap<>();

    public GeoLocalityEntity getLocality(Session session, UUID uuid) {
        if (!this.localityCacheByUUID.containsKey(uuid)) {
            GeoLocalityEntity entity = QueryManager.getEntity(session, uuid, GeoLocalityEntity.class);
            this.localityCacheByUUID.put(uuid, entity);
        }
        return this.localityCacheByUUID.get(uuid);
    }


    public HashMap<UUID, BuildingEntity> buildingCacheByUUID = new HashMap<>();

    public BuildingEntity getBuilding(Session session, UUID uuid) {
        if (!this.buildingCacheByUUID.containsKey(uuid)) {
            BuildingEntity entity = QueryManager.getEntity(session, uuid, BuildingEntity.class);
            this.buildingCacheByUUID.put(uuid, entity);
        }
        return this.buildingCacheByUUID.get(uuid);
    }

    public void loadAllBuildings(Session session) {
        for (BuildingEntity entity : QueryManager.getAllEntities(session, BuildingEntity.class)) {
            this.buildingCacheByUUID.put(entity.getUUID(), entity);
        }
    }


    public ListHashMap<Integer, GeoRoadEntity> roadCacheByCode = new ListHashMap<>();

    public List<GeoRoadEntity> getRoad(Session session, int municipalityCode, int roadCode) throws InvalidClientInputException {
        Integer code = 10000 * municipalityCode + roadCode;
        if (!this.roadCacheByCode.containsKey(code)) {
            RoadQuery query = new RoadQuery();
            query.setMunicipalityCode(municipalityCode);
            query.setCode(roadCode);
            List<GeoRoadEntity> list = QueryManager.getAllEntities(session, query, GeoRoadEntity.class);
            this.roadCacheByCode.add(code, list);
        }
        return this.roadCacheByCode.get(code);
    }

    public void loadAllRoads(Session session) {
        for (GeoRoadEntity entity : QueryManager.getAllEntities(session, GeoRoadEntity.class)) {
            RoadMunicipalityRecord roadMunicipalityRecord = entity.getMunicipality().current();
            if (roadMunicipalityRecord != null && roadMunicipalityRecord.getCode() != null) {
                Integer code = 10000 * roadMunicipalityRecord.getCode() + entity.getCode();
                this.roadCacheByCode.add(code, entity);
            }
        }
    }


    public HashMap<Integer, PostcodeEntity> postcodeCacheByCode = new HashMap<>();

    public PostcodeEntity getPostcode(Session session, int code) {
        if (!this.postcodeCacheByCode.containsKey(code)) {
            PostcodeQuery query = new PostcodeQuery();
            try {
                query.setCode(String.format("%4d", code));
            } catch (InvalidClientInputException e) {
                throw new RuntimeException(e);
            }
            List<PostcodeEntity> list = QueryManager.getAllEntities(session, query, PostcodeEntity.class);
            if (list.size() > 0) {
                this.postcodeCacheByCode.put(code, list.get(0));
            }
        }
        return this.postcodeCacheByCode.get(code);
    }


    public void loadAllPostcodes(Session session) {
        for (PostcodeEntity entity : QueryManager.getAllEntities(session, PostcodeEntity.class)) {
            this.postcodeCacheByCode.put(entity.getCode(), entity);
        }
    }

    public void loadAll(Session session) {
        this.loadAllLocalities(session);
        this.loadAllRoads(session);
        this.loadAllBuildings(session);
        this.loadAllPostcodes(session);
    }

}
