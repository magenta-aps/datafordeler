package dk.magenta.datafordeler.cvr.entitymanager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.HttpStatusException;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.FapiBaseService;
import dk.magenta.datafordeler.core.plugin.ScanScrollCommunicator;
import dk.magenta.datafordeler.cvr.CvrRegisterManager;
import dk.magenta.datafordeler.cvr.configuration.CvrConfiguration;
import dk.magenta.datafordeler.cvr.configuration.CvrConfigurationManager;
import dk.magenta.datafordeler.cvr.query.CompanyRecordQuery;
import dk.magenta.datafordeler.cvr.records.CompanyRecord;
import dk.magenta.datafordeler.cvr.service.CompanyRecordService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;

/**
 * Company-specific EntityManager, specifying various settings that methods in the superclass
 * will use to import data.
 */
@Component
public class MissingCompanyEntityManager extends CvrEntityManager<CompanyRecord> {

    public static final OffsetDateTime MIN_SQL_SERVER_DATETIME = OffsetDateTime.of(
            1, 1, 1, 0, 0, 0, 0,
            ZoneOffset.UTC
    );

    @Autowired
    private CompanyRecordService companyRecordService;

    @Autowired
    private SessionManager sessionManager;

    private Logger log = LogManager.getLogger(MissingCompanyEntityManager.class);

    public MissingCompanyEntityManager() {
    }

    @Override
    protected String getBaseName() {
        return "company";
    }

    @Override
    public FapiBaseService getEntityService() {
        return this.companyRecordService;
    }

    @Override
    public String getSchema() {
        return CompanyRecord.schema;
    }

    @Override
    protected SessionManager getSessionManager() {
        return this.sessionManager;
    }

    @Override
    protected String getJsonTypeName() {
        return "Vrvirksomhed";
    }

    @Override
    protected Class getRecordClass() {
        return CompanyRecord.class;
    }

    @Override
    protected UUID generateUUID(CompanyRecord record) {
        return record.generateUUID();
    }

    @Autowired
    private CvrConfigurationManager configurationManager;

    @Override
    public boolean pullEnabled() {
        CvrConfiguration configuration = configurationManager.getConfiguration();
        return (configuration.getCompanyRegisterType() != CvrConfiguration.RegisterType.DISABLED);
    }

    @Override
    public BaseQuery getQuery() {
        return new CompanyRecordQuery();
    }

    @Override
    public BaseQuery getQuery(String... strings) {
        return this.getQuery();
    }
}
