package dk.magenta.datafordeler.cpr.data.person;

import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.RegistrationReference;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.cpr.data.CprEntityManager;
import dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData;
import dk.magenta.datafordeler.cpr.parsers.CprSubParser;
import dk.magenta.datafordeler.cpr.parsers.PersonParser;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.person.AddressRecord;
import dk.magenta.datafordeler.cpr.records.person.CprBitemporalPersonRecord;
import dk.magenta.datafordeler.cpr.records.person.ForeignAddressRecord;
import dk.magenta.datafordeler.cpr.records.person.PersonDataRecord;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.*;

@Component
public class PersonEntityManager extends CprEntityManager<PersonDataRecord, PersonEntity, PersonRegistration, PersonEffect, PersonBaseData> {

    @Value("${dafo.cpr.person.subscription-enabled:false}")
    private boolean setupSubscriptionEnabled;

    @Value("${dafo.cpr.person.local-subscription-folder:cache}")
    private String localSubscriptionFolder;

    @Value("${dafo.cpr.person.jobid:0}")
    private int jobId;

    @Value("${dafo.cpr.person.customer-id:0}")
    private int customerId;


    @Autowired
    private PersonEntityService personEntityService;

    @Autowired
    private PersonParser personParser;

    @Autowired
    private SessionManager sessionManager;

    public PersonEntityManager() {
        this.managedEntityClass = PersonEntity.class;
        this.managedEntityReferenceClass = PersonEntityReference.class;
        this.managedRegistrationClass = PersonRegistration.class;
        this.managedRegistrationReferenceClass = PersonRegistrationReference.class;
    }

    public int getJobId() {
        return this.jobId;
    }

    public int getCustomerId() {
        return this.customerId;
    }

    public String getLocalSubscriptionFolder() {
        return this.localSubscriptionFolder;
    }

    public boolean isSetupSubscriptionEnabled() {
        return this.setupSubscriptionEnabled;
    }

    @Override
    protected String getBaseName() {
        return "person";
    }

    @Override
    public PersonEntityService getEntityService() {
        return this.personEntityService;
    }

    @Override
    public String getDomain() {
        return "https://data.gl/cpr/person/1/rest/";
    }

    @Override
    public String getSchema() {
        return PersonEntity.schema;
    }

    private HashSet<String> nonGreenlandicCprNumbers = new HashSet<>();

    @Override
    public List<PersonRegistration> parseData(InputStream registrationData, ImportMetadata importMetadata) throws DataFordelerException {
        try {
            List<PersonRegistration> result = super.parseData(registrationData, importMetadata);
            if (this.isSetupSubscriptionEnabled() && !this.nonGreenlandicCprNumbers.isEmpty() && (importMetadata.getImportConfiguration() == null || importMetadata.getImportConfiguration().size() == 0)) {
                this.createSubscription(this.nonGreenlandicCprNumbers);
            }
            return result;
        } finally {
            this.nonGreenlandicCprNumbers.clear();
        }
    }

    @Override
    protected void handleRecord(PersonDataRecord record, ImportMetadata importMetadata) {
        super.handleRecord(record, importMetadata);
        if (record != null) {
            if (record instanceof AddressRecord) {
                AddressRecord addressRecord = (AddressRecord) record;
                if (addressRecord.getMunicipalityCode() < 900) {
                    this.nonGreenlandicCprNumbers.add(addressRecord.getCprNumber());
                }
            } else if (record instanceof ForeignAddressRecord) {
                ForeignAddressRecord foreignAddressRecord = (ForeignAddressRecord) record;
                this.nonGreenlandicCprNumbers.add(foreignAddressRecord.getCprNumber());
            }
        }
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
    protected CprSubParser<PersonDataRecord> getParser() {
        return this.personParser;
    }

    @Override
    protected Class<PersonEntity> getEntityClass() {
        return PersonEntity.class;
    }

    @Override
    protected UUID generateUUID(PersonDataRecord record) {
        return PersonEntity.generateUUID(record.getCprNumber());
    }

    @Override
    protected PersonEntity createBasicEntity(PersonDataRecord record) {
        PersonEntity personEntity = new PersonEntity();
        personEntity.setPersonnummer(record.getCprNumber());
        return personEntity;
    }

    private PersonEntity createBasicEntity(String cprNumber) {
        PersonEntity personEntity = new PersonEntity();
        personEntity.setPersonnummer(cprNumber);
        return personEntity;
    }

    @Override
    protected PersonBaseData createDataItem() {
        return new PersonBaseData();
    }

    private void createSubscription(HashSet<String> addCprNumbers) throws DataFordelerException {
        this.createSubscription(addCprNumbers, new HashSet<>());
    }

    private void createSubscription(HashSet<String> addCprNumbers, HashSet<String> removeCprNumbers) throws DataFordelerException {
        this.log.info("Collected these numbers for subscription: "+addCprNumbers);
        String charset = this.getConfiguration().getRegisterCharset(this);
        String keyConstant = "";
        StringJoiner content = new StringJoiner("\r\n");

        Session session = sessionManager.getSessionFactory().openSession();
        try {
            List<PersonSubscription> existingSubscriptions = QueryManager.getAllItems(session, PersonSubscription.class);
            HashMap<String, PersonSubscription> map = new HashMap<>();
            for (PersonSubscription subscription : existingSubscriptions) {
                map.put(subscription.getPersonNumber(), subscription);
            }

            addCprNumbers.removeAll(removeCprNumbers);
            addCprNumbers.removeAll(map.keySet());


            HashMap<String, HashSet<String>> loop = new HashMap<>();
            loop.put("OP", addCprNumbers);
            loop.put("SL", removeCprNumbers);

            for (String operation : loop.keySet()) {
                HashSet<String> cprNumbers = loop.get(operation);
                for (String cprNumber : cprNumbers) {
                    content.add(
                            String.format(
                                    "%02d%04d%02d%2s%10s%15s%45s",
                                    6,
                                    this.getCustomerId(),
                                    0,
                                    operation,
                                    cprNumber,
                                    keyConstant,
                                    ""
                            )
                    );
                }
            }

            for (String cprNumber : addCprNumbers) {
                content.add(
                        String.format(
                                "%02d%06d%10s%15s",
                                7,
                                this.getJobId(),
                                cprNumber,
                                keyConstant,
                                ""
                        )
                );
            }

            this.addSubscription(content.toString(), charset, this);

            session.beginTransaction();
            try {
                for (String add : addCprNumbers) {
                    PersonSubscription newSubscription = new PersonSubscription();
                    newSubscription.setPersonNumber(add);
                    session.save(newSubscription);
                }
                for (String remove : removeCprNumbers) {
                    PersonSubscription removeSubscription = map.get(remove);
                    if (removeSubscription != null) {
                        session.delete(removeSubscription);
                    }
                }
                session.getTransaction().commit();
            } catch (Exception e) {
                session.getTransaction().rollback();
            }
        } finally {
            session.close();
        }
    }

    @Override
    protected void parseAlternate(PersonEntity entity, Collection<PersonDataRecord> records, ImportMetadata importMetadata) {
        OffsetDateTime updateTime = importMetadata.getImportTime();
        for (PersonDataRecord record : records) {
            for (CprBitemporalRecord bitemporalRecord : record.getBitemporalRecords()) {
                bitemporalRecord.setDafoUpdated(updateTime);
                bitemporalRecord.setOrigin(record.getOrigin());
                entity.addBitemporalRecord((CprBitemporalPersonRecord) bitemporalRecord, importMetadata.getSession());
            }
        }
    }

}
