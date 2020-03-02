package dk.magenta.datafordeler.cvr.data.participant;

import dk.magenta.datafordeler.core.database.RegistrationReference;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.fapi.FapiService;
import dk.magenta.datafordeler.cvr.configuration.CvrConfiguration;
import dk.magenta.datafordeler.cvr.configuration.CvrConfigurationManager;
import dk.magenta.datafordeler.cvr.data.CvrEntityManager;
import dk.magenta.datafordeler.cvr.records.ParticipantRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Component
public class ParticipantEntityManager extends CvrEntityManager<ParticipantEntity, ParticipantRegistration, ParticipantEffect, ParticipantBaseData, ParticipantRecord> {

    public static final OffsetDateTime MIN_SQL_SERVER_DATETIME = OffsetDateTime.of(
        1, 1, 1, 0, 0, 0, 0,
        ZoneOffset.UTC
    );

    @Autowired
    private ParticipantEntityService participantEntityService;

    @Autowired
    private SessionManager sessionManager;

    private Logger log = LogManager.getLogger(ParticipantEntityManager.class);

    public ParticipantEntityManager() {
        this.managedEntityClass = ParticipantEntity.class;
        this.managedEntityReferenceClass = ParticipantEntityReference.class;
        this.managedRegistrationClass = ParticipantRegistration.class;
        this.managedRegistrationReferenceClass = ParticipantRegistrationReference.class;
    }

    @Override
    protected String getBaseName() {
        return "participant";
    }

    @Override
    public FapiService getEntityService() {
        return this.participantEntityService;
    }

    @Override
    public String getSchema() {
        return ParticipantEntity.schema;
    }


    @Override
    protected RegistrationReference createRegistrationReference(URI uri) {
        return new ParticipantRegistrationReference(uri);
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
    protected Class<ParticipantEntity> getEntityClass() {
        return ParticipantEntity.class;
    }

    @Override
    protected UUID generateUUID(ParticipantRecord record) {
        return ParticipantEntity.generateUUID(record.getUnitType(), record.getUnitNumber());
    }

    @Override
    protected ParticipantEntity createBasicEntity(ParticipantRecord record) {
        ParticipantEntity participant = new ParticipantEntity();
        participant.setParticipantNumber(record.getUnitNumber());
        return participant;
    }

    @Override
    protected ParticipantBaseData createDataItem() {
        return new ParticipantBaseData();
    }

    @Autowired
    private CvrConfigurationManager configurationManager;

    @Override
    public boolean pullEnabled() {
        CvrConfiguration configuration = configurationManager.getConfiguration();
        return (configuration.getParticipantRegisterType() != CvrConfiguration.RegisterType.DISABLED);
    }

}
