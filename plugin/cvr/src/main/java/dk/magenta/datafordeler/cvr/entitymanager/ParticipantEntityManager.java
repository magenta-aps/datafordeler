package dk.magenta.datafordeler.cvr.entitymanager;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.FapiBaseService;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.cvr.DirectLookup;
import dk.magenta.datafordeler.cvr.configuration.CvrConfiguration;
import dk.magenta.datafordeler.cvr.configuration.CvrConfigurationManager;
import dk.magenta.datafordeler.cvr.query.ParticipantRecordQuery;
import dk.magenta.datafordeler.cvr.records.ParticipantRecord;
import dk.magenta.datafordeler.cvr.service.ParticipantRecordService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
public class ParticipantEntityManager extends CvrEntityManager<ParticipantRecord> {

    public static final OffsetDateTime MIN_SQL_SERVER_DATETIME = OffsetDateTime.of(
            1, 1, 1, 0, 0, 0, 0,
            ZoneOffset.UTC
    );

    private ParticipantRecordService participantEntityService;

    private SessionManager sessionManager;

    private DirectLookup directLookup;

    private final Logger log = LogManager.getLogger(ParticipantEntityManager.class);

    @Autowired
    public ParticipantEntityManager(@Lazy ParticipantRecordService participantRecordService, @Lazy SessionManager sessionManager, @Lazy DirectLookup directLookup) {
        this.participantEntityService = participantRecordService;
        this.sessionManager = sessionManager;
        this.directLookup = directLookup;
    }

    @Override
    protected String getBaseName() {
        return "participant";
    }

    @Override
    public FapiBaseService getEntityService() {
        return this.participantEntityService;
    }

    @Override
    public String getSchema() {
        return ParticipantRecord.schema;
    }

    @Override
    protected SessionManager getSessionManager() {
        return this.sessionManager;
    }

    @Override
    protected String getJsonTypeName() {
        return "Vrdeltagerperson";
    }

    @Override
    protected Class getRecordClass() {
        return ParticipantRecord.class;
    }

    @Override
    protected UUID generateUUID(ParticipantRecord record) {
        return record.generateUUID();
    }

    @Autowired
    private CvrConfigurationManager configurationManager;

    @Override
    public boolean pullEnabled() {
        CvrConfiguration configuration = configurationManager.getConfiguration();
        return (configuration.getParticipantRegisterType() != CvrConfiguration.RegisterType.DISABLED);
    }

    @Override
    public BaseQuery getQuery() {
        return new ParticipantRecordQuery();
    }

    @Override
    public BaseQuery getQuery(String... strings) {
        return this.getQuery();
    }

    @Override
    protected void beforeParseSave(ParticipantRecord item, ImportMetadata importMetadata, Session session) {
        super.beforeParseSave(item, importMetadata, session);
        this.enrichParticipantRecord(item);
    }

    private boolean enrichParticipantRecord(ParticipantRecord participantRecord) {
        if (participantRecord.getUnitNumber() != 0 && (participantRecord.getBusinessKey() == null || participantRecord.getBusinessKey() == 0) && participantRecord.getConfidentialEnriched() == null) {
            try {
                ParticipantRecord confidentialRecord = directLookup.participantLookup(Long.toString(participantRecord.getUnitNumber()));
                if (confidentialRecord.getBusinessKey() != null) {
                    participantRecord.setBusinessKey(confidentialRecord.getBusinessKey());
                    participantRecord.setConfidentialEnriched(true);
                } else {
                    participantRecord.setConfidentialEnriched(false);
                }
                return true;
            } catch (DataFordelerException e) {
                log.warn(e);
            }
        }
        return false;
    }

    //@PostConstruct
    public void enrichAllParticipantRecords() {
        Session session = sessionManager.getSessionFactory().openSession();
        Query<Long> countQuery = session.createQuery(
                "select count(ALL id) from " + ParticipantRecord.class.getCanonicalName(),
                Long.class
        );
        Long rowCount = countQuery.getSingleResult();

        for (int i = 0; i <= rowCount / 1000; i++) {
            Transaction transaction = session.beginTransaction();
            Query<ParticipantRecord> query = session.createQuery(
                    "from " + ParticipantRecord.class.getCanonicalName(),
                    ParticipantRecord.class
            );
            query.setFirstResult(i * 1000);
            query.setMaxResults(1000);
            List<ParticipantRecord> records = query.getResultList();
            if (!records.isEmpty()) {
                for (ParticipantRecord participantRecord : records) {
                    boolean updated = ParticipantEntityManager.this.enrichParticipantRecord(participantRecord);
                    if (updated) {
                        session.persist(participantRecord);
                    }
                }
                session.flush();
                transaction.commit();
                session.clear();
            } else {
                break;
            }
        }
    }

    protected ObjectNode queryFromMunicipalities(List<Integer> municipalities) {
        return queryFromIntegerTerms("Vrdeltagerperson.beliggenhedsadresse.kommune.kommuneKode", municipalities);
    }
    protected ObjectNode queryFromUpdatedSince(OffsetDateTime updatedSince) {
        return queryFromUpdatedSince("Vrdeltagerperson.sidstOpdateret", updatedSince);
    }
    protected ObjectNode queryFromUnitNumbers(List<Integer> unitNumbers) {
        return queryFromIntegerTerms("Vrdeltagerperson.enhedsNummer", unitNumbers);
    }

    public String getDailyQuery(OffsetDateTime lastUpdated, List<Integer> subscribedCompanyList, List<Integer> missingCompanyList) {
        return finalizeQuery(
                combineQueryAnd(
                        queryFromMunicipalities(Arrays.asList(954, 955, 956, 957, 958, 959, 960, 961, 962)),
                        queryFromUpdatedSince(lastUpdated)
                )
        );
    }
    public String getSpecificQuery(List<Integer> ids) {
        return finalizeQuery(queryFromUnitNumbers(ids));
    }
}
