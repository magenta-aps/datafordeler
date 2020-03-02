package dk.magenta.datafordeler.cvr.data.companyunit;

import dk.magenta.datafordeler.core.database.RegistrationReference;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.fapi.FapiService;
import dk.magenta.datafordeler.cvr.configuration.CvrConfiguration;
import dk.magenta.datafordeler.cvr.configuration.CvrConfigurationManager;
import dk.magenta.datafordeler.cvr.data.CvrEntityManager;
import dk.magenta.datafordeler.cvr.records.CompanyUnitRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Component
public class CompanyUnitEntityManager extends CvrEntityManager<CompanyUnitEntity, CompanyUnitRegistration, CompanyUnitEffect, CompanyUnitBaseData, CompanyUnitRecord> {

    public static final OffsetDateTime MIN_SQL_SERVER_DATETIME = OffsetDateTime.of(
        1, 1, 1, 0, 0, 0, 0,
        ZoneOffset.UTC
    );

    @Autowired
    private CompanyUnitEntityService companyUnitEntityService;

    @Autowired
    private SessionManager sessionManager;

    private Logger log = LogManager.getLogger(CompanyUnitEntityManager.class);

    public CompanyUnitEntityManager() {
        this.managedEntityClass = CompanyUnitEntity.class;
        this.managedEntityReferenceClass = CompanyUnitEntityReference.class;
        this.managedRegistrationClass = CompanyUnitRegistration.class;
        this.managedRegistrationReferenceClass = CompanyUnitRegistrationReference.class;
    }

    @Override
    protected String getBaseName() {
        return "companyunit";
    }

    @Override
    public FapiService getEntityService() {
        return this.companyUnitEntityService;
    }

    @Override
    public String getSchema() {
        return CompanyUnitEntity.schema;
    }

    @Override
    protected RegistrationReference createRegistrationReference(URI uri) {
        return new CompanyUnitRegistrationReference(uri);
    }

    @Override
    protected SessionManager getSessionManager() {
        return this.sessionManager;
    }

    @Override
    protected String getJsonTypeName() {
        return "VrproduktionsEnhed";
    }

    @Override
    protected Class<CompanyUnitRecord> getRecordClass() {
        return CompanyUnitRecord.class;
    }

    @Override
    protected Class<CompanyUnitEntity> getEntityClass() {
        return CompanyUnitEntity.class;
    }

    @Override
    protected UUID generateUUID(CompanyUnitRecord record) {
        return CompanyUnitEntity.generateUUID(record.getpNumber());
    }

    @Override
    protected CompanyUnitEntity createBasicEntity(CompanyUnitRecord record) {
        CompanyUnitEntity entity = new CompanyUnitEntity();
        entity.setPNumber(record.getpNumber());
        return entity;
    }

    @Override
    protected CompanyUnitBaseData createDataItem() {
        return new CompanyUnitBaseData();
    }

    @Autowired
    private CvrConfigurationManager configurationManager;

    @Override
    public boolean pullEnabled() {
        CvrConfiguration configuration = configurationManager.getConfiguration();
        return (configuration.getCompanyUnitRegisterType() != CvrConfiguration.RegisterType.DISABLED);
    }

}
