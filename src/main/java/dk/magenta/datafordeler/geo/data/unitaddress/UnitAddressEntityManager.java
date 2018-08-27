package dk.magenta.datafordeler.geo.data.unitaddress;

import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.geo.data.GeoEntityManager;
import dk.magenta.datafordeler.geo.data.accessaddress.AccessAddressEntityManager;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("GeoUnitAddressEntityManager")
public class UnitAddressEntityManager extends GeoEntityManager<UnitAddressEntity, UnitAddressRawData> {

    @Autowired
    private UnitAddressService unitAddressService;

    @Autowired
    private AccessAddressEntityManager accessAddressEntityManager;

    public UnitAddressEntityManager() {
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
    protected Class<UnitAddressEntity> getEntityClass() {
        return UnitAddressEntity.class;
    }


    @Override
    protected Class<UnitAddressRawData> getRawClass() {
        return UnitAddressRawData.class;
    }

    @Override
    protected UUID generateUUID(UnitAddressRawData rawData) {
        return rawData.properties.getSumiffiikAsUUID();
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
