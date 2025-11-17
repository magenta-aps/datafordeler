package dk.magenta.datafordeler.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.database.ConfigurationSessionManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.database.UserSessionManager;
import dk.magenta.datafordeler.core.exception.ConfigurationException;
import dk.magenta.datafordeler.core.exception.HttpNotFoundException;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.plugin.RegisterManager;
import dk.magenta.datafordeler.core.util.CronUtil;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Component
public class Engine {

    @Autowired
    PluginManager pluginManager;

    @Autowired
    SessionManager sessionManager;

    @Autowired
    ConfigurationSessionManager configurationSessionManager;

    @Autowired
    UserSessionManager userSessionManager;

    @Autowired
    ObjectMapper objectMapper;

    @Value("${dafo.server.name:dafo01}")
    private String serverName;

    @Value("${dafo.pull.enabled:true}")
    private boolean pullEnabled;

    @Value("${dafo.cron.enabled:true}")
    private boolean cronEnabled;

    private static final Logger log = LogManager.getLogger(Engine.class.getCanonicalName());

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
    }

    public String getServerName() {
        return this.serverName;
    }

    public boolean isPullEnabled() {
        return this.pullEnabled;
    }

    public boolean isCronEnabled() {
        return this.cronEnabled;
    }

    /**
     * Pull
     */
    private final HashMap<String, TriggerKey> pullTriggerKeys = new HashMap<>();

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
     *
     * @param registerManager Registermanager to run pull jobs on
     * @param cronSchedule    A valid cron schedule, six items, space-separated
     * @param dummyRun        For test purposes. If false, no pull will actually be run.
     */
    public void setupPullSchedule(RegisterManager registerManager, String cronSchedule, boolean dummyRun) {
        if (this.pullEnabled && this.cronEnabled) {
            ScheduleBuilder scheduleBuilder;
            log.info("Scheduling pull with " + registerManager.getClass().getCanonicalName() + " with schedule " + cronSchedule);
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
     *
     * @param registerManager Registermanager to run pull jobs on
     * @param scheduleBuilder The schedule to use
     * @param dummyRun        For test purposes. If false, no pull will actually be run.
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
                    log.info("Removing cron schedule to pull from " + registerManager.getClass().getCanonicalName());
                    if (this.pullTriggerKeys.containsKey(registerManagerId)) {
                        scheduler.unscheduleJob(this.pullTriggerKeys.get(registerManagerId));
                    }
                }

            } catch (SchedulerException e) {
                log.error("Failed to schedule pull!", e);
            }
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

    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

        HandlerExecutionChain chain = handlerMapping.getHandler(request);

        log.info("HANDLER for {} is {}", request.getRequestURI(), chain);
        if (chain != null) {
            for (HandlerInterceptor interceptor : chain.getInterceptors()) {
                log.info("INTERCEPTOR is {}", interceptor);
            }
        }

        if (chain == null || chain.getHandler() == null) {
            throw new HttpNotFoundException("No handler found for " + request.getRequestURI());
        }

        // we merely propagate any exception thrown here
        HandlerMethod method = (HandlerMethod) chain.getHandler();

        handlerAdapter.handle(request, response, method);
    }

}
