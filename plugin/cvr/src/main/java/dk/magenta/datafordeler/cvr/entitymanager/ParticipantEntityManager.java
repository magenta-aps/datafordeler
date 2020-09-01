package dk.magenta.datafordeler.cvr.entitymanager;

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
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

@Component
public class ParticipantEntityManager extends CvrEntityManager<ParticipantRecord> {

    public static final OffsetDateTime MIN_SQL_SERVER_DATETIME = OffsetDateTime.of(
        1, 1, 1, 0, 0, 0, 0,
        ZoneOffset.UTC
    );

    @Autowired
    private ParticipantRecordService participantEntityService;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private DirectLookup directLookup;

    private Logger log = LogManager.getLogger(ParticipantEntityManager.class);

    public ParticipantEntityManager() {
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
        //this.enrichParticipantRecord(item);
    }

    private boolean enrichParticipantRecord(ParticipantRecord participantRecord) {
        if (participantRecord.getUnitNumber() != 0 && (participantRecord.getBusinessKey() == null || participantRecord.getBusinessKey() == 0) && participantRecord.getConfidentialEnriched() == null) {
            try {
                ParticipantRecord confidentialRecord = directLookup.participantLookup(Long.toString(participantRecord.getUnitNumber()));
                participantRecord.setBusinessKey(confidentialRecord.getBusinessKey());
                participantRecord.setConfidentialEnriched(true);
                System.out.println("Enriched ParticipantEntity "+participantRecord.getUnitNumber()+" with businessKey "+confidentialRecord.getBusinessKey());
                return true;
            } catch (DataFordelerException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    public void enrichAllParticipantRecords() {
        Session session = sessionManager.getSessionFactory().openSession();
        Query<Long> countQuery = session.createQuery("select count(ALL) from " + ParticipantRecord.class, Long.class);
        Long rowCount = countQuery.getSingleResult();

        for (int i=0; i<rowCount/1000; i++) {
            Transaction transaction = session.beginTransaction();
            Query<ParticipantRecord> query = session.createQuery("from " + ParticipantRecord.class, ParticipantRecord.class);
            query.setFirstResult(i * 1000);
            query.setMaxResults(1000);
            List<ParticipantRecord> records = query.getResultList();
            if (!records.isEmpty()) {
                records.forEach(participantRecord -> {
                    ParticipantEntityManager.this.enrichParticipantRecord(participantRecord);
                    session.save(participantRecord);
                });
                transaction.commit();
                session.flush();
                session.clear();
            } else {
                break;
            }
        }
    }

}
