package dk.magenta.datafordeler.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.database.*;
import dk.magenta.datafordeler.core.dump.Dump;
import dk.magenta.datafordeler.core.dump.Dump.Task;
import dk.magenta.datafordeler.core.dump.DumpConfiguration;
import dk.magenta.datafordeler.core.exception.*;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.io.PluginSourceData;
import dk.magenta.datafordeler.core.io.Receipt;
import dk.magenta.datafordeler.core.plugin.EntityManager;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.plugin.RegisterManager;
import dk.magenta.datafordeler.core.util.CronUtil;
import dk.magenta.datafordeler.core.util.ItemInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.*;

@Component
public class Engine {

    @Autowired
    PluginManager pluginManager;

    @Autowired
    SessionManager sessionManager;

    @Autowired
    ConfigurationSessionManager configurationSessionManager;

    @Autowired
    ObjectMapper objectMapper;

    @Value("${dafo.server.name:dafo01}")
    private String serverName;

    @Value("${dafo.dump.enabled:true}")
    private boolean dumpEnabled;

    @Value("${dafo.pull.enabled:true}")
    private boolean pullEnabled;

    @Value("${dafo.cron.enabled:true}")
    private boolean cronEnabled;

    private static Logger log = LogManager.getLogger(Engine.class.getCanonicalName());

    @Autowired(required = false)
    private RequestMappingHandlerMapping handlerMapping;

    @Autowired(required = false)
    private RequestMappingHandlerAdapter handlerAdapter;

    /**
     * Run bean initialization
     */
    @PostConstruct
    public void init() {
        this.setupPullSchedules();
        this.setupDumpSchedules();
    }

    public String getServerName() {
        return this.serverName;
    }

    public boolean isPullEnabled() {
        return this.pullEnabled;
    }

    public boolean isDumpEnabled() { return this.dumpEnabled; }

    public boolean isCronEnabled() {
        return this.cronEnabled;
    }

    public <E extends Entity<E, R>, R extends Registration<E, R, V>, V extends Effect<R, V, D>, D extends DataItem<V, D>> boolean handleEvent(PluginSourceData event, ImportMetadata importMetadata) {
        return this.handleEvent(event, null, importMetadata);
    }

    /**
     * Entry point for incoming events from the GAPI.
     * A registrationreference in the incoming event will be parsed, and the corresponding registration fetched
     * When a registration is at hand, it is saved and a receipt is sent to the entitymanager that handles the registration
     * @param event Event to be handled
     * */
    public <E extends Entity<E, R>, R extends Registration<E, R, V>, V extends Effect<R, V, D>, D extends DataItem<V, D>> boolean handleEvent(PluginSourceData event, Plugin plugin, ImportMetadata importMetadata) {
        log.info("Handling event '" + event.getId() + "'");
        OffsetDateTime eventReceived = OffsetDateTime.now();
        Receipt receipt;
        EntityManager entityManager = null;
        boolean success;
        Session session = null;
        try {
            List<? extends Registration> registrations;

            if (event.getData() == null) {
                String referenceData = event.getReference();
                if (referenceData == null) {
                    throw new MissingReferenceException(event);
                }

                log.info("Event contains a reference to a registration");
                // If the event is only a reference, request the actual object through the plugin
                URI referenceURI;
                try {
                    referenceURI = new URI(referenceData);
                } catch (URISyntaxException e) {
                    throw new InvalidReferenceException(referenceData);
                }
                if (plugin == null) {
                    plugin = pluginManager.getPluginForURI(referenceURI);
                }
                if (plugin == null) {
                    throw new PluginNotFoundException(referenceURI);
                }
                log.info("Handled by plugin " + plugin.getClass().getCanonicalName());
                entityManager = plugin.getEntityManager(referenceURI);
                if (entityManager == null) {
                    throw new EntityManagerNotFoundException(referenceURI);
                }

                try {
                    RegistrationReference reference = entityManager.parseReference(referenceURI);
                    log.info("Parsed reference: " + entityManager.getRegistrationInterface(reference));
                    registrations = entityManager.fetchRegistration(reference, importMetadata);
                    log.info("Referenced registration fetched");
                } catch (IOException e) {
                    throw new DataStreamException(e);
                }
            } else {
                log.info("Event contains a full registration");
                String schema = event.getSchema();
                if (plugin == null) {
                    plugin = pluginManager.getPluginForSchema(schema);
                }
                if (plugin == null) {
                    throw new PluginNotFoundException(schema, true);
                }
                log.info("Handled by plugin " + plugin.getClass().getCanonicalName());
                entityManager = plugin.getEntityManager(schema);
                if (entityManager == null) {
                    throw new EntityManagerNotFoundException(schema);
                }
                log.info("entityManager: "+entityManager.getClass().getCanonicalName());
                registrations = entityManager.parseData(event, importMetadata);
            }
/*
            for (Registration registration : registrations) {
                session = sessionManager.getSessionFactory().openSession();
                Transaction transaction = session.beginTransaction();
                queryManager.saveRegistration(session, registration.getEntity(), registration, true, true);
                transaction.commit();
                session.close();
            }*/

            if (!entityManager.handlesOwnSaves()) {
                session = importMetadata.getSession();
                boolean createSession = (session == null);
                Transaction transaction = null;
                if (createSession) {
                    session = sessionManager.getSessionFactory().openSession();
                    transaction = session.beginTransaction();
                }
                for (Registration registration : registrations) {
                    QueryManager.saveRegistration(
                            session, null, (R) registration,
                            false, false, true
                    );
                }
                if (createSession) {
                    transaction.commit();
                    session.close();
                    session = null;
                }
            }

            receipt = new Receipt(event.getId(), eventReceived);
            success = true;
        } catch (DataFordelerException e) {
            log.error("Error handling event", e);
            receipt = new Receipt(event.getId(), eventReceived, e);
            success = false;
        } finally {
            if (session != null) {
                session.close();
            }
        }
        if (entityManager == null) {
            log.error("No EntityManager found for event; cannot send receipt");
        } else {
            try {
               entityManager.sendReceipt(receipt);
            } catch (DataFordelerException | IOException e) {
                log.error("Failed sending receipt", e);
                e.printStackTrace();
            }
        }
        return success;
    }


    /**
     * Pull
     */
    private HashMap<String, TriggerKey> pullTriggerKeys = new HashMap<>();

    /**
     * Sets the schedule for the registerManager, based on the schedule defined in same
     */
    public void setupPullSchedules() {
        if (this.pullEnabled && this.cronEnabled) {
            List<Plugin> plugins = this.pluginManager.getPlugins();
            if (plugins.isEmpty()) {
                log.warn("No plugins registered!");
            }
            for (Plugin plugin : plugins) {
                RegisterManager registerManager = plugin.getRegisterManager();
                if (registerManager != null) {
                    String schedule = registerManager.getPullCronSchedule();
                    log.info("Registered plugin {} has schedule '{}'",
                            plugin.getClass().getCanonicalName(), schedule);

                    if (schedule != null && !schedule.isEmpty()) {
                        this.setupPullSchedule(registerManager, schedule, false);
                    }
                }
            }
        }
    }


    private Scheduler scheduler = null;

    /**
     * Sets the schedule for the registerManager, given a cron string
     * @param registerManager Registermanager to run pull jobs on
     * @param cronSchedule A valid cron schedule, six items, space-separated
     * @param dummyRun For test purposes. If false, no pull will actually be run.
     */
    public void setupPullSchedule(RegisterManager registerManager, String cronSchedule, boolean dummyRun) {
        if (this.pullEnabled && this.cronEnabled) {
            ScheduleBuilder scheduleBuilder;
            log.info("Scheduling pull with "+registerManager.getClass().getCanonicalName()+" with schedule "+cronSchedule);
            try {
                scheduleBuilder = makeSchedule(cronSchedule);
            } catch (Exception e) {
                log.error(e);
                return;
            }
            setupPullSchedule(registerManager, scheduleBuilder, dummyRun);
        }
    }

    /**
     * Sets the schedule for the registerManager, given a schedule
     * @param registerManager Registermanager to run pull jobs on
     * @param scheduleBuilder The schedule to use
     * @param dummyRun For test purposes. If false, no pull will actually be run.
     */
    public void setupPullSchedule(RegisterManager registerManager,
        ScheduleBuilder scheduleBuilder,
        boolean dummyRun) {
        if (this.pullEnabled && this.cronEnabled) {
            String registerManagerId = registerManager.getClass().getName() + registerManager.hashCode();

            try {
                if (scheduler == null) {
                    this.scheduler = StdSchedulerFactory.getDefaultScheduler();
                }
                if (scheduleBuilder != null) {
                    this.pullTriggerKeys.put(registerManagerId, TriggerKey.triggerKey("pullTrigger", registerManagerId));
                    // Set up new schedule, or replace existing
                    Trigger pullTrigger = TriggerBuilder.newTrigger()
                            .withIdentity(this.pullTriggerKeys.get(registerManagerId))
                            .withSchedule(scheduleBuilder).build();

                    JobDataMap jobData = new JobDataMap();
                    jobData.put(Pull.Task.DATA_ENGINE, this);
                    jobData.put(Pull.Task.DATA_REGISTERMANAGER, registerManager);
                    jobData.put(AbstractTask.DATA_DUMMYRUN, dummyRun);
                    JobDetail job = JobBuilder.newJob(Pull.Task.class)
                            .withIdentity("pullTask-" + registerManagerId)
                            .setJobData(jobData)
                            .build();

                    scheduler.scheduleJob(job, Collections.singleton(pullTrigger), true);
                    scheduler.start();
                } else {
                    // Remove old schedule
                    this.log.info("Removing cron schedule to pull from " + registerManager.getClass().getCanonicalName());
                    if (this.pullTriggerKeys.containsKey(registerManagerId)) {
                        scheduler.unscheduleJob(this.pullTriggerKeys.get(registerManagerId));
                    }
                }

            } catch (SchedulerException e) {
                this.log.error("Failed to schedule pull!", e);
            }
        }
    }

    public boolean setupDumpSchedules() {
        if (!dumpEnabled || !this.cronEnabled) {
            log.info("Scheduled dump jobs disabled for this server!");
            return false;
        }

        Session session =
            configurationSessionManager.getSessionFactory().openSession();

        try {
            return QueryManager.getAllItemsAsStream(session,
                DumpConfiguration.class)
                .allMatch(
                    c -> setupDumpSchedule(c, false)
                );
        } finally {
            session.close();
        }
    }

    /**
     * Sets the schedule for dumps
     * @param config The dump configuration
     * @param dummyRun For test purposes. If false, no pull will actually be run.
     */
    boolean setupDumpSchedule(DumpConfiguration config, boolean
        dummyRun) {
        try {
            if (scheduler == null) {
                this.scheduler = StdSchedulerFactory.getDefaultScheduler();
            }

            String triggerID = String.format("DUMP-%d", config.getId());

            // Remove old schedule
            if (this.pullTriggerKeys.containsKey(triggerID)) {
                this.log.info("Removing schedule for dump");
                scheduler.unscheduleJob(this.pullTriggerKeys.get(triggerID));
            }

            CronScheduleBuilder scheduleBuilder = makeSchedule(
                config.getSchedule()
            );

            if (scheduleBuilder != null) {
                this.log.info("Setting up dump with schedule {}",
                    scheduleBuilder);
                this.pullTriggerKeys.put(triggerID,
                    TriggerKey.triggerKey("dumpTrigger", triggerID));

                // Set up new schedule, or replace existing
                Trigger dumpTrigger = TriggerBuilder.newTrigger()
                    .withIdentity(this.pullTriggerKeys.get(triggerID))
                    .withSchedule(
                        scheduleBuilder
                    ).build();
                this.log.info("The trigger is {}",
                    dumpTrigger);

                JobDataMap jobData = new JobDataMap();
                jobData.put(Dump.Task.DATA_ENGINE, this);
                jobData.put(Task.DATA_SESSIONMANAGER, this.sessionManager);
                jobData.put(Dump.Task.DATA_CONFIG, config);
                jobData.put(Dump.Task.DATA_DUMMYRUN, dummyRun);
                JobDetail job = JobBuilder.newJob(Dump.Task.class)
                    .withIdentity(triggerID)
                    .setJobData(jobData)
                    .build();

                scheduler.scheduleJob(job, Collections.singleton(dumpTrigger), true);
                scheduler.start();
            }

            return true;
        } catch (Exception e) {
            this.log.error("failed to schedule dump!", e);
            return false;
        }
    }

    private CronScheduleBuilder makeSchedule(String schedule) throws ConfigurationException {
        String s = CronUtil.reformatSchedule(schedule);
        if (s == null) {
            return null;
        }
        log.info("Reformatted cronjob specification: " + s);
        return CronScheduleBuilder.cronSchedule(s);
    }

    private void stopScheduler() {
        if (this.scheduler != null) {
            try {
                for (String key : this.pullTriggerKeys.keySet()) {
                    scheduler.unscheduleJob(this.pullTriggerKeys.get(key));
                    this.scheduler.deleteJob(JobKey.jobKey(key));
                }
                this.scheduler.shutdown(true);
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleRequest(HttpServletRequest request,
        HttpServletResponse response) throws Exception {

        HandlerExecutionChain chain =
            handlerMapping.getHandler(request);

        log.info("HANDLER for {} is {}", request.getRequestURI(), chain);
        if (chain != null) {
            for (HandlerInterceptor interceptor : chain.getInterceptors()) {
                log.info("INTERCEPTOR is {}", interceptor);
            }
        }

        if (chain == null || chain.getHandler() == null) {
            throw new HttpNotFoundException("No handler found for " +
                request.getRequestURI());
        }

        // we merely propagate any exception thrown here
        HandlerMethod method = (HandlerMethod) chain.getHandler();

        handlerAdapter.handle(request, response, method);
    }
}
