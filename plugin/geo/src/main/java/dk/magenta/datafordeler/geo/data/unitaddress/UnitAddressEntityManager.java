package dk.magenta.datafordeler.geo.data.unitaddress;

import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.geo.data.GeoEntityManager;
import dk.magenta.datafordeler.geo.data.accessaddress.AccessAddressEntityManager;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("GeoUnitAddressEntityManager")
public class UnitAddressEntityManager extends GeoEntityManager<UnitAddressEntity, UnitAddressRawData> {

    private final UnitAddressService unitAddressService;

    @Autowired
    private AccessAddressEntityManager accessAddressEntityManager;

    @Autowired
    public UnitAddressEntityManager(@Lazy UnitAddressService unitAddressService) {
        this.unitAddressService = unitAddressService;
    }

    @Override
    protected String getBaseName() {
        return "unitaddress";
    }

    @Override
    public UnitAddressService getEntityService() {
        return this.unitAddressService;
    }

    @Override
    public String getDomain() {
        return "https://data.gl/geo/unitaddress/1/rest";
    }

    @Override
    public String getSchema() {
        return UnitAddressEntity.schema;
    }

    @Override
    public BaseQuery getQuery() {
        return new UnitAddressQuery();
    }

    @Override
    public BaseQuery getQuery(String... strings) {
        UnitAddressQuery unitAddressQuery = new UnitAddressQuery();
        for (String join : strings) {
            switch (join) {
                case "accessaddress":
                    unitAddressQuery.addRelatedAccessAddressQuery();
                    break;
            }
        }
        return unitAddressQuery;
    }

    @Override
    public Class<UnitAddressEntity> getManagedEntityClass() {
        return UnitAddressEntity.class;
    }


    @Override
    protected Class<UnitAddressRawData> getRawClass() {
        return UnitAddressRawData.class;
    }

    @Override
    protected UUID generateUUID(UnitAddressRawData rawData) {
        return rawData.properties.getUUID();
    }

    @Override
    protected UnitAddressEntity createBasicEntity(UnitAddressRawData record, Session session) {
        UnitAddressEntity unitAddressEntity = new UnitAddressEntity(record);
        UUID accessAddressUUID = record.properties.getAccessAddressSumiffiikAsUUID();
        unitAddressEntity.setAccessAddress(
                accessAddressUUID != null ?
                        QueryManager.getOrCreateIdentification(
                                session,
                                accessAddressUUID,
                                accessAddressEntityManager.getDomain()
                        ) : null
        );
        return unitAddressEntity;
    }

}
