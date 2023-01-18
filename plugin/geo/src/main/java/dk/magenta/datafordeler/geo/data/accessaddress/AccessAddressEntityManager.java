package dk.magenta.datafordeler.geo.data.accessaddress;

import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.geo.data.GeoEntityManager;
import dk.magenta.datafordeler.geo.data.WireCache;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("GeoAccessAddressEntityManager")
public class AccessAddressEntityManager extends GeoEntityManager<AccessAddressEntity, AccessAddressRawData> {

    @Autowired
    private AccessAddressService accessAddressService;

    public AccessAddressEntityManager() {
    }

    @Override
    protected String getBaseName() {
        return "accessaddress";
    }

    @Override
    public AccessAddressService getEntityService() {
        return this.accessAddressService;
    }

    @Override
    public String getDomain() {
        return "https://data.gl/geo/accessaddress/1/rest";
    }

    @Override
    public String getSchema() {
        return AccessAddressEntity.schema;
    }

    @Override
    public BaseQuery getQuery() {
        return new AccessAddressQuery();
    }

    @Override
    public BaseQuery getQuery(String... strings) {
        AccessAddressQuery accessAddressQuery = new AccessAddressQuery();
        for (String join : strings) {
            switch (join) {
                case "municipality":
                    accessAddressQuery.addRelatedMunicipalityQuery();
                    break;
                case "road":
                    accessAddressQuery.addRelatedRoadQuery();
                    break;
                case "locality":
                    accessAddressQuery.addRelatedLocalityQuery();
                    break;
                case "postcode":
                    accessAddressQuery.addRelatedPostcodeQuery();
                    break;
            }
        }
        return accessAddressQuery;
    }

    @Override
    protected void populateWireCache(WireCache wireCache, Session session) {
        wireCache.loadAll(session);
    }

    @Override
    protected Class<AccessAddressEntity> getEntityClass() {
        return AccessAddressEntity.class;
    }


    @Override
    protected Class<AccessAddressRawData> getRawClass() {
        return AccessAddressRawData.class;
    }

    @Override
    protected UUID generateUUID(AccessAddressRawData rawData) {
        return rawData.properties.getUUID();
    }

    @Override
    protected AccessAddressEntity createBasicEntity(AccessAddressRawData record, Session session) {
        return new AccessAddressEntity(record);
    }

}
