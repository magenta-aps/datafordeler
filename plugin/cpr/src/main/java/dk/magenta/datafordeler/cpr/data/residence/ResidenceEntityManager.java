package dk.magenta.datafordeler.cpr.data.residence;

import dk.magenta.datafordeler.core.database.RegistrationReference;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.cpr.data.CprGeoEntityManager;
import dk.magenta.datafordeler.cpr.data.person.PersonRegistrationReference;
import dk.magenta.datafordeler.cpr.data.residence.data.ResidenceBaseData;
import dk.magenta.datafordeler.cpr.parsers.CprSubParser;
import dk.magenta.datafordeler.cpr.parsers.ResidenceParser;
import dk.magenta.datafordeler.cpr.records.residence.ResidenceRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.UUID;

@Component
public class ResidenceEntityManager extends CprGeoEntityManager<ResidenceRecord, ResidenceEntity, ResidenceRegistration, ResidenceEffect, ResidenceBaseData> {

    @Autowired
    private ResidenceEntityService residenceEntityService;

    @Autowired
    private ResidenceParser residenceParser;

    @Autowired
    private SessionManager sessionManager;

    public ResidenceEntityManager() {
        this.managedEntityClass = ResidenceEntity.class;
    }

    @Override
    protected String getBaseName() {
        return "residence";
    }

    @Override
    public ResidenceEntityService getEntityService() {
        return this.residenceEntityService;
    }

    @Override
    public String getDomain() {
        return "https://data.gl/cpr/residence/1/rest/";
    }

    @Override
    public String getSchema() {
        return ResidenceEntity.schema;
    }

    @Override
    public BaseQuery getQuery() {
        return new ResidenceQuery();
    }

    @Override
    public BaseQuery getQuery(String... strings) {
        return this.getQuery();
    }

    @Override
    protected RegistrationReference createRegistrationReference(URI uri) {
        return new PersonRegistrationReference(uri);
    }

    @Override
    protected SessionManager getSessionManager() {
        return this.sessionManager;
    }

    @Override
    protected CprSubParser<ResidenceRecord> getParser() {
        return this.residenceParser;
    }

    @Override
    protected Class<ResidenceEntity> getEntityClass() {
        return ResidenceEntity.class;
    }

    @Override
    protected UUID generateUUID(ResidenceRecord record) {
        return ResidenceEntity.generateUUID(record.getMunicipalityCode(), record.getRoadCode(), record.getHouseNumber(), record.getFloor(), record.getDoor());
    }

    @Override
    protected ResidenceEntity createBasicEntity(ResidenceRecord record) {
        return new ResidenceEntity();
    }

    @Override
    protected ResidenceBaseData createDataItem() {
        return new ResidenceBaseData();
    }

}
