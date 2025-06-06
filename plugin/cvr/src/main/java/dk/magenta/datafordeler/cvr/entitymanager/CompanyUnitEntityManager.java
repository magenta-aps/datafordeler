package dk.magenta.datafordeler.cvr.entitymanager;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.FapiBaseService;
import dk.magenta.datafordeler.cvr.configuration.CvrConfiguration;
import dk.magenta.datafordeler.cvr.configuration.CvrConfigurationManager;
import dk.magenta.datafordeler.cvr.query.CompanyUnitRecordQuery;
import dk.magenta.datafordeler.cvr.records.CompanyUnitRecord;
import dk.magenta.datafordeler.cvr.service.CompanyUnitRecordService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
public class CompanyUnitEntityManager extends CvrEntityManager<CompanyUnitRecord> {

    public static final OffsetDateTime MIN_SQL_SERVER_DATETIME = OffsetDateTime.of(
            1, 1, 1, 0, 0, 0, 0,
            ZoneOffset.UTC
    );

    private CompanyUnitRecordService companyUnitEntityService;

    private SessionManager sessionManager;

    private final Logger log = LogManager.getLogger(CompanyUnitEntityManager.class.getCanonicalName());

    @Autowired
    public CompanyUnitEntityManager(@Lazy CompanyUnitRecordService companyUnitEntityService, @Lazy SessionManager sessionManager) {
        this.companyUnitEntityService = companyUnitEntityService;
        this.sessionManager = sessionManager;
    }

    @Override
    protected String getBaseName() {
        return "companyunit";
    }

    @Override
    public FapiBaseService getEntityService() {
        return this.companyUnitEntityService;
    }

    @Override
    public String getSchema() {
        return CompanyUnitRecord.schema;
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
    protected UUID generateUUID(CompanyUnitRecord record) {
        return record.generateUUID();
    }

    @Autowired
    private CvrConfigurationManager configurationManager;

    @Override
    public boolean pullEnabled() {
        CvrConfiguration configuration = configurationManager.getConfiguration();
        return (configuration.getCompanyUnitRegisterType() != CvrConfiguration.RegisterType.DISABLED);
    }

    @Override
    public BaseQuery getQuery() {
        return new CompanyUnitRecordQuery();
    }

    @Override
    public BaseQuery getQuery(String... strings) {
        return this.getQuery();
    }



    protected ObjectNode queryFromMunicipalities(List<Integer> municipalities) {
        return queryFromIntegerTerms("VrproduktionsEnhed.beliggenhedsadresse.kommune.kommuneKode", municipalities);
    }
    protected ObjectNode queryFromUpdatedSince(OffsetDateTime updatedSince) {
        return queryFromUpdatedSince("VrproduktionsEnhed.sidstOpdateret", updatedSince);
    }
    protected ObjectNode queryFromPnumbers(List<Integer> pNumbers) {
        return queryFromIntegerTerms("VrproduktionsEnhed.pNummer", pNumbers);
    }

    public String getDailyQuery(Session session, OffsetDateTime lastUpdated) {
        return finalizeQuery(
            combineQueryAnd(
                    queryFromMunicipalities(Arrays.asList(954, 955, 956, 957, 958, 959, 960, 961, 962)),
                    queryFromUpdatedSince(lastUpdated)
            )
        );
    }

    public String getSpecificQuery(List<Integer> ids) {
        return finalizeQuery(queryFromPnumbers(ids));
    }

}
