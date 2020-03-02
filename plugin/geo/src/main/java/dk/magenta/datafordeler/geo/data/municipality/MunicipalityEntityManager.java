package dk.magenta.datafordeler.geo.data.municipality;

import dk.magenta.datafordeler.geo.data.GeoEntityManager;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("GeoMunicipalityEntityManager")
public class MunicipalityEntityManager extends GeoEntityManager<MunicipalityEntity, MunicipalityRawData> {

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
        return MunicipalityEntity.schema;
    }

    @Override
    protected Class<MunicipalityEntity> getEntityClass() {
        return MunicipalityEntity.class;
    }


    @Override
    protected Class<MunicipalityRawData> getRawClass() {
        return MunicipalityRawData.class;
    }

    @Override
    protected UUID generateUUID(MunicipalityRawData rawData) {
        return rawData.properties.getUUID();
        //return MunicipalityEntity.generateUUID(rawData.properties.code);
    }

    @Override
    protected MunicipalityEntity createBasicEntity(MunicipalityRawData record, Session session) {
        return new MunicipalityEntity(record);
    }

}
