package dk.magenta.datafordeler.geo.data.municipality;

import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.geo.data.GeoEntityManager;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("GeoMunicipalityEntityManager")
public class MunicipalityEntityManager extends GeoEntityManager<GeoMunicipalityEntity, MunicipalityRawData> {

    @Autowired
    private MunicipalityService municipalityService;

    public MunicipalityEntityManager() {
    }

    @Override
    protected String getBaseName() {
        return "municipality";
    }

    @Override
    public MunicipalityService getEntityService() {
        return this.municipalityService;
    }

    @Override
    public String getDomain() {
        return "https://data.gl/geo/municipality/1/rest";
    }

    @Override
    public String getSchema() {
        return GeoMunicipalityEntity.schema;
    }

    @Override
    public BaseQuery getQuery() {
        return new MunicipalityQuery();
    }

    @Override
    public BaseQuery getQuery(String... strings) {
        return this.getQuery();
    }


    @Override
    protected Class<GeoMunicipalityEntity> getEntityClass() {
        return GeoMunicipalityEntity.class;
    }


    @Override
    protected Class<MunicipalityRawData> getRawClass() {
        return MunicipalityRawData.class;
    }

    @Override
    protected UUID generateUUID(MunicipalityRawData rawData) {
        return rawData.properties.getUUID();
    }

    @Override
    protected GeoMunicipalityEntity createBasicEntity(MunicipalityRawData record, Session session) {
        return new GeoMunicipalityEntity(record);
    }

}
