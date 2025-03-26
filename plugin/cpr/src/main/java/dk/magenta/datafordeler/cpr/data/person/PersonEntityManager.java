package dk.magenta.datafordeler.cpr.data.person;

import com.fasterxml.jackson.core.JsonProcessingException;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.ConfigurationException;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.util.CronUtil;
import dk.magenta.datafordeler.cpr.configuration.CprConfigurationManager;
import dk.magenta.datafordeler.cpr.data.CprRecordEntityManager;
import dk.magenta.datafordeler.cpr.direct.CprDirectLookup;
import dk.magenta.datafordeler.cpr.direct.CprDirectPasswordUpdate;
import dk.magenta.datafordeler.cpr.parsers.CprSubParser;
import dk.magenta.datafordeler.cpr.parsers.PersonParser;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.person.*;
import dk.magenta.datafordeler.cpr.records.person.data.BirthTimeDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.ParentDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.PersonEventDataRecord;
import dk.magenta.datafordeler.cpr.records.service.PersonEntityRecordService;
import dk.magenta.datafordeler.cpr.synchronization.SubscriptionTimerTask;
import jakarta.annotation.PostConstruct;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class PersonEntityManager extends CprRecordEntityManager<PersonDataRecord, PersonEntity> {

    private CprConfigurationManager configurationManager;

    private PersonEntityRecordService personEntityService;

    private PersonParser personParser;

    private SessionManager sessionManager;

    @Autowired
    private CprDirectLookup directLookup;

    private static PersonEntityManager instance;

    @Autowired
    public PersonEntityManager(@Lazy CprConfigurationManager configurationManager, @Lazy PersonEntityRecordService personEntityRecordService, @Lazy SessionManager sessionManager, @Lazy PersonParser personParser) {
        instance = this;
        this.configurationManager = configurationManager;
        this.personEntityService = personEntityRecordService;
        this.sessionManager = sessionManager;
        this.personParser = personParser;
    }

    public int getJobId() {
        return configurationManager.getConfiguration().getJobId();
    }

    public int getCustomerId() {
        return configurationManager.getConfiguration().getCustomerId();
    }

    public String getLocalSubscriptionFolder() {
        return configurationManager.getConfiguration().getLocalSubscriptionFolder();
    }

    public boolean isSetupSubscriptionEnabled() {
        return configurationManager.getConfiguration().isSubscriptionEnabled();
    }

    @Override
    protected String getBaseName() {
        return "person";
    }

    @Override
    public PersonEntityRecordService getEntityService() {
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

    private final HashSet<String> nonGreenlandicCprNumbers = new HashSet<>();

    private final HashSet<String> nonGreenlandicFatherCprNumbers = new HashSet<>();

    private final HashSet<String> nonGreenlandicChildrenCprNumbers = new HashSet<>();

    /**
     * Parse the file of persons.
     * If the file contains any fathers that is unknown to DAFO add it
     *
     * @param registrationData
     * @param importMetadata
     * @throws DataFordelerException
     */
    @Override
    public void parseData(InputStream registrationData, ImportMetadata importMetadata) throws DataFordelerException {
        try {
            //With this flag true initiated testdata is cleared before initiation of new data is initiated
            if (importMetadata.getImportConfiguration() != null &&
                    importMetadata.getImportConfiguration().has("cleantestdatafirst") &&
                    importMetadata.getImportConfiguration().get("cleantestdatafirst").booleanValue()) {
                cleanDemoData();
            }
            super.parseData(registrationData, importMetadata);
            if (this.isSetupSubscriptionEnabled() && !this.nonGreenlandicCprNumbers.isEmpty() && !importMetadata.hasImportConfiguration()) {
                this.createSubscription(this.nonGreenlandicCprNumbers);
            }
            if (this.isSetupSubscriptionEnabled() && !this.nonGreenlandicFatherCprNumbers.isEmpty() && !importMetadata.hasImportConfiguration()) {
                try (Session session = sessionManager.getSessionFactory().openSession()) {
                    PersonRecordQuery personQuery = new PersonRecordQuery();
                    personQuery.setParameter(PersonRecordQuery.PERSONNUMMER, nonGreenlandicFatherCprNumbers);
                    personQuery.applyFilters(session);
                    List<PersonEntity> personEntities = QueryManager.getAllEntities(session, personQuery, PersonEntity.class);
                    for (PersonEntity person : personEntities) {
                        nonGreenlandicFatherCprNumbers.remove(person.getPersonnummer());
                    }
                }
                this.createSubscription(this.nonGreenlandicFatherCprNumbers);
            }
            if (this.isSetupSubscriptionEnabled() && !this.nonGreenlandicChildrenCprNumbers.isEmpty() && !importMetadata.hasImportConfiguration()) {
                try (Session session = sessionManager.getSessionFactory().openSession()) {
                    PersonRecordQuery personQuery = new PersonRecordQuery();
                    personQuery.setParameter(PersonRecordQuery.PERSONNUMMER, nonGreenlandicChildrenCprNumbers);
                    personQuery.applyFilters(session);
                    List<PersonEntity> personEntities = QueryManager.getAllEntities(session, personQuery, PersonEntity.class);
                    for (PersonEntity person : personEntities) {
                        nonGreenlandicChildrenCprNumbers.remove(person.getPersonnummer());
                    }
                }
                this.createSubscription(this.nonGreenlandicChildrenCprNumbers);
            }
        } finally {
            this.nonGreenlandicCprNumbers.clear();
            this.nonGreenlandicFatherCprNumbers.clear();
            this.nonGreenlandicChildrenCprNumbers.clear();
        }
    }

    /**
     * Clean demopersons which has been initiated in the database.
     * Demopersons is used on the demoenvironment for demo and education purposes
     */
    public void cleanDemoData() {
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            PersonRecordQuery personQuery = new PersonRecordQuery();
            String[] testPersonList = configurationManager.getConfiguration().getTestpersonList().split(",");
            personQuery.setParameter(
                    PersonRecordQuery.PERSONNUMMER,
                    Arrays.stream(testPersonList).filter(s -> !s.isBlank()).map(String::strip).collect(Collectors.toList())
            );
            session.beginTransaction();
            personQuery.setPageSize(1000);
            personQuery.applyFilters(session);
            List<PersonEntity> personEntities = QueryManager.getAllEntities(session, personQuery, PersonEntity.class);
            for (PersonEntity personForDeletion : personEntities) {
                session.delete(personForDeletion);
            }
            session.getTransaction().commit();
        } catch (Exception e) {
            log.error("Failed cleaning data", e);
        }
    }


    /**
     * Handle parsing if records from cpr
     * If a person is leaving Greenland they should be added.
     * If a person is under 18 years old, and has a father with no connection to Greenland they should be added.
     *
     * @param record
     * @param importMetadata
     */
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
            } else if (record instanceof ChildrenRecord) {

                /// We only create a subscription on a child if the child is less then 18 years old.
                // We make a lot of assumptions while figuring out if this the case.
                // This logic is used for deciding if there is needed a subscription of someones child

                // This case is a very rare cornercase to make sure that if someone from greenland datafordeler
                // has minor children that does not exist in datafordeler, we create a subscription on thease children.
                // The birthtime of the child is unknown since we only has the record of them being someones child.
                // The decision is that if both the effect from time of the birth-record and the cpr-number indicate that the child is not 18 years old yet we will make a subscription
                ChildrenRecord childRecord = (ChildrenRecord) record;

                //The effecttime of the child is the same time as the birthtime, if 18 years after the birthtime is after now, we need to create a subscription on the child
                if (OffsetDateTime.now().minusYears(18).isBefore(Optional.ofNullable(childRecord.getEffectDateTime()).orElse(OffsetDateTime.MIN))) {
                    String childPnr = childRecord.getPnrChild().substring(0, 2);
                    String childBirthDay = childPnr.substring(0, 2);
                    String childBirthMonth = childPnr.substring(2, 4);
                    String childBirthDYear = childPnr.substring(4, 6);
                    LocalDate now = LocalDate.now();
                    int yearOfServerTime = now.get(ChronoField.YEAR);
                    String serverCurrentCentury = Integer.toString(yearOfServerTime).substring(0, 2);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate parsedBirthDateBasedOnCpr = LocalDate.parse(serverCurrentCentury + childBirthDYear + "-" + childBirthMonth + "-" + childBirthDay, formatter);
                    //The child that gets passed is born before the timestamp of the server, this means that if the child is born after the current timestamp it is in the last century.
                    if (parsedBirthDateBasedOnCpr.isAfter(now)) {
                        //If we make a calculation that this child is born after current time
                        parsedBirthDateBasedOnCpr = parsedBirthDateBasedOnCpr.minusYears(100);
                    }

                    if (parsedBirthDateBasedOnCpr.plusYears(18).isAfter(now)) {
                        nonGreenlandicChildrenCprNumbers.add(childRecord.getPnrChild());
                    }
                }
            } else if (record instanceof PersonRecord) {

                PersonRecord person = (PersonRecord) record;
                List<CprBitemporalRecord> bitemporalRecords = person.getBitemporalRecords();

                ParentDataRecord father = (ParentDataRecord) bitemporalRecords.stream().
                        filter(bitemporalCprRecord -> bitemporalCprRecord instanceof ParentDataRecord && !((ParentDataRecord) bitemporalCprRecord).isMother()).
                        findAny().orElse(null);

                BirthTimeDataRecord birthTime = (BirthTimeDataRecord) bitemporalRecords.stream().
                        filter(bitemporalCprRecord -> bitemporalCprRecord instanceof BirthTimeDataRecord).
                        findAny().orElse(null);

                if (birthTime != null && father != null && birthTime.getBirthDatetime() != null && birthTime.getBirthDatetime().isAfter(LocalDateTime.now().minusYears(18))) {
                    if (!father.getCprNumber().isEmpty() && !father.getCprNumber().equals("0000000000")) {
                        log.debug("fatherAdd " + father.getCprNumber());
                        nonGreenlandicFatherCprNumbers.add(father.getCprNumber());
                    }
                }
            }
        }
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
        PersonEntity entity = new PersonEntity();
        entity.setPersonnummer(record.getCprNumber());
        return entity;
    }

    private PersonEntity createBasicEntity(String cprNumber) {
        PersonEntity personEntity = new PersonEntity();
        personEntity.setPersonnummer(cprNumber);
        return personEntity;
    }

    public void createSubscription(Set<String> addCprNumbers) {
        this.createSubscription(addCprNumbers, Collections.EMPTY_SET);
    }

    /**
     * Create subscriptions by adding them to the table of subscriptions
     *
     * @param addCprNumbers
     * @param removeCprNumbers
     */
    public void createSubscription(Set<String> addCprNumbers, Set<String> removeCprNumbers) {
        this.log.info("Collected these numbers for subscription: " + addCprNumbers);

        HashSet<String> cprNumbersToBeAdded = new HashSet<String>(addCprNumbers);
        Session session = sessionManager.getSessionFactory().openSession();
        try {
            List<PersonSubscription> existingSubscriptions = QueryManager.getAllItems(session, PersonSubscription.class);
            HashMap<String, PersonSubscription> map = new HashMap<>();
            for (PersonSubscription subscription : existingSubscriptions) {
                map.put(subscription.getPersonNumber(), subscription);
            }

            cprNumbersToBeAdded.removeAll(removeCprNumbers);
            cprNumbersToBeAdded.removeAll(map.keySet());

            session.beginTransaction();
            try {
                for (String add : cprNumbersToBeAdded) {
                    PersonSubscription newSubscription = new PersonSubscription();
                    newSubscription.setPersonNumber(add);
                    newSubscription.setAssignment(PersonSubscriptionAssignmentStatus.CreatedInTable);
                    session.persist(newSubscription);
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
                log.warn(e);
            }
        } finally {
            session.close();
        }
    }

    /**
     * Create the subscription-file from the table of subscriptions, and upload them to FTP-server
     */
    public void createSubscriptionFile() {
        log.info("Creating subscription file");
        String charset = this.getConfiguration().getRegisterCharset(this);

        Transaction transaction = null;
        Session session = sessionManager.getSessionFactory().openSession();
        try {
            transaction = session.beginTransaction();

            Query<PersonSubscription> subscriptionQuery = session.createQuery(
                    "from "+PersonSubscription.class.getCanonicalName()+" "+
                    "where "+PersonSubscription.DB_FIELD_CPR_ASSIGNMENT_STATUS+"="+PersonSubscriptionAssignmentStatus.CreatedInTable.ordinal(),
                    PersonSubscription.class
            );
            List<PersonSubscription> subscriptionList = subscriptionQuery.getResultList();

            // If there is no subscription to upload just log
            if (subscriptionList.size() == 0) {
                log.info("No subscriptions found for upload");
                return;
            }

            for (PersonSubscription subscription : subscriptionList) {
                subscription.setAssignment(PersonSubscriptionAssignmentStatus.UploadedToCpr);
            }

            StringJoiner content = new StringJoiner("\r\n");

            for (PersonSubscription subscription : subscriptionList) {
                content.add(
                        String.format(
                                "%02d%04d%02d%2s%10s%15s%45s",
                                6,
                                this.getCustomerId(),
                                0,
                                "OP",
                                subscription.getPersonNumber(),
                                "",
                                ""
                        )
                );
            }

            for (PersonSubscription subscription : subscriptionList) {
                content.add(
                        String.format(
                                "%02d%06d%10s%15s",
                                7,
                                this.getJobId(),
                                subscription.getPersonNumber(),
                                "",
                                ""
                        )
                );
            }
            log.info("Uploading subscription file with "+subscriptionList.size()+" items");
            this.addSubscription(content.toString(), charset, this);
            for (PersonSubscription subscription : subscriptionList) {
                session.persist(subscription);
            }
            transaction.commit();

        } catch (Exception e) {
            log.error(e);
            transaction.rollback();
        } finally {
            session.close();
        }
    }

    private final HashMap<String, Integer> cnts = new HashMap<>();

    protected void parseAlternate(PersonEntity entity, Collection<PersonDataRecord> records, ImportMetadata importMetadata) {
        OffsetDateTime updateTime = importMetadata.getImportTime();
        int i = 1;
        Integer c = cnts.get(entity.getPersonnummer());
        if (c != null) {
            i = c;
        }
        for (PersonDataRecord record : records) {

            if (record instanceof PersonEventRecord) {
                for (PersonEventDataRecord event : ((PersonEventRecord) record).getPersonEvents()) {
                    entity.addEvent(event, importMetadata.getSession());
                }
            }

            for (CprBitemporalRecord bitemporalRecord : record.getBitemporalRecords()) {
                bitemporalRecord.setDafoUpdated(updateTime);
                bitemporalRecord.setOrigin(record.getOrigin());
                bitemporalRecord.cnt = i;
                bitemporalRecord.line = record.getLine();
                entity.addBitemporalRecord((CprBitemporalPersonRecord) bitemporalRecord, importMetadata.getSession());
            }
            i++;
        }
        cnts.put(entity.getPersonnummer(), i);
    }

    public static String json(Object o) {
        try {
            return instance.getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @PostConstruct
    public void setupDirectPasswordChange() {
        if (configurationManager.getConfiguration().isDirectPasswordChangeEnable()) {
            try {
                Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
                ScheduleBuilder scheduleBuilder = CronScheduleBuilder.monthlyOnDayAndHourAndMinute(1, 8, 0);
                TriggerKey triggerKey = TriggerKey.triggerKey("directPasswordChangeTrigger");
                Trigger trigger = TriggerBuilder.newTrigger()
                        .withIdentity(triggerKey)
                        .withSchedule(scheduleBuilder).build();
                JobDataMap jobData = new JobDataMap();
                jobData.put(CprDirectPasswordUpdate.Task.DATA_CONFIGURATIONMANAGER, this.getCprConfigurationManager());
                jobData.put(CprDirectPasswordUpdate.Task.DATA_DIRECTLOOKUP, this.directLookup);
                JobDetail job = JobBuilder.newJob(CprDirectPasswordUpdate.Task.class).setJobData(jobData).build();
                scheduler.scheduleJob(job, Collections.singleton(trigger), true);
                scheduler.start();
            } catch (SchedulerException e) {
                log.error(e);
            }
        }
    }

    @PostConstruct
    public void setupSubscriptionUploader() {
        if (configurationManager.getConfiguration().isSubscriptionEnabled()) {
            try {
                String cronExpression = CronUtil.reformatSchedule(configurationManager.getConfiguration().getSubscriptionGenerateSchedule());
                if (cronExpression != null) {
                    CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);
                    Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
                    JobDataMap jobData = new JobDataMap();
                    jobData.put("personManager", this);
                    JobDetail job = JobBuilder.newJob(SubscriptionTimerTask.Task.class)
                            .withIdentity("CprSubscription")
                            .setJobData(jobData)
                            .build();
                    Trigger trigger = TriggerBuilder.newTrigger()
                            .withIdentity("CprSubscription")
                            .withSchedule(scheduleBuilder)
                            .build();
                    scheduler.scheduleJob(job, Collections.singleton(trigger), true);
                    scheduler.start();
                }
            } catch (SchedulerException | ConfigurationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public BaseQuery getQuery() {
        return new PersonRecordQuery();
    }

    @Override
    public BaseQuery getQuery(String... strings) {
        return this.getQuery();
    }

}
