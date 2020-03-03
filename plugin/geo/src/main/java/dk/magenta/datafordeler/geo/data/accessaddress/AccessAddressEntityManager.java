package dk.magenta.datafordeler.geo.data.accessaddress;

import dk.magenta.datafordeler.geo.data.GeoEntityManager;
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
