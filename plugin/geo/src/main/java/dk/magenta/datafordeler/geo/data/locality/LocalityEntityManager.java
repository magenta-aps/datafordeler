package dk.magenta.datafordeler.geo.data.locality;

import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.geo.data.GeoEntityManager;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("GeoLocalityEntityManager")
public class LocalityEntityManager extends GeoEntityManager<GeoLocalityEntity, LocalityRawData> {

    private final LocalityService localityService;

    @Autowired
    public LocalityEntityManager(@Lazy LocalityService localityService) {
        this.localityService = localityService;
    }

    @Override
    protected String getBaseName() {
        return "locality";
    }

    @Override
    public LocalityService getEntityService() {
        return this.localityService;
    }

    @Override
    public String getDomain() {
        return "https://data.gl/geo/locality/1/rest";
    }

    @Override
    public String getSchema() {
        return GeoLocalityEntity.schema;
    }

    @Override
    public BaseQuery getQuery() {
        return new LocalityQuery();
    }

    @Override
    public BaseQuery getQuery(String... strings) {
        LocalityQuery localityQuery = new LocalityQuery();
        for (String join : strings) {
            switch (join) {
                case "municipality":
                    localityQuery.addRelatedMunicipalityQuery();
                    break;
            }
        }
        return localityQuery;
    }

    @Override
    protected Class<GeoLocalityEntity> getEntityClass() {
        return GeoLocalityEntity.class;
    }


    @Override
    protected Class<LocalityRawData> getRawClass() {
        return LocalityRawData.class;
    }

    @Override
    protected UUID generateUUID(LocalityRawData rawData) {
        return rawData.properties.getUUID();
    }

    @Override
    protected GeoLocalityEntity createBasicEntity(LocalityRawData record, Session session) {
        return new GeoLocalityEntity(record);
    }

}
