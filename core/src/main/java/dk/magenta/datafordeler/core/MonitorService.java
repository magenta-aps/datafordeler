package dk.magenta.datafordeler.core;

import dk.magenta.datafordeler.core.database.ConfigurationSessionManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.plugin.EntityManager;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.plugin.RegisterManager;
import dk.magenta.datafordeler.core.user.UserQueryManager;
import dk.magenta.datafordeler.core.util.CronUtil;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.TlsConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.TlsSocketStrategy;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.apache.hc.core5.util.Timeout;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.StringJoiner;

@Controller
@RequestMapping(path = "/monitor")
public class MonitorService {

    @Autowired
    SessionManager sessionManager;

    @Autowired
    ConfigurationSessionManager configurationSessionManager;

    @Autowired
    PluginManager pluginManager;

    @Autowired
    UserQueryManager userQueryManager;

    private final Logger log = LogManager.getLogger(MonitorService.class.getName());

    @RequestMapping(path = "/database")
    public void checkDatabaseConnections(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Session session = sessionManager.getSessionFactory().openSession();
        Query query = session.createQuery("select 1 from Identification").setMaxResults(1);
        query.uniqueResult();
        session.close();
        response.getWriter().println("Primary database connection ok");

        session = configurationSessionManager.getSessionFactory().openSession();
        query = session.createQuery("select 1 from Command").setMaxResults(1);
        query.uniqueResult();
        session.close();
        response.getWriter().println("Secondary database connection ok");

        userQueryManager.checkConnection();
        response.getWriter().println("Tertiary database connection ok");

        response.setStatus(200);
    }

    @RequestMapping(path = "/pull")
    public void checkPulls(HttpServletRequest request, HttpServletResponse response) throws ParseException, IOException, DataFordelerException {
        LoggerHelper loggerHelper = new LoggerHelper(log, request);
        loggerHelper.urlInvokePersistablelogs("checkPulls");

        Session session = sessionManager.getSessionFactory().openSession();
        PrintWriter output = response.getWriter();
        for (Plugin plugin : pluginManager.getPlugins()) {
            RegisterManager registerManager = plugin.getRegisterManager();
            if (registerManager != null) {
                String pullSchedule = CronUtil.reformatSchedule(registerManager.getPullCronSchedule());
                if (pullSchedule != null && !pullSchedule.isEmpty()) {
                    CronExpression cronExpression = new CronExpression(pullSchedule);
                    for (EntityManager entityManager : registerManager.getEntityManagers()) {
                        // Check that entitymanagers that should run on a cron job
                        // have completed their expected pulls within, say, 4 hours
                        // to detect stalled jobs
                        output.println("Inspecting " + entityManager.getClass().getSimpleName());
                        if (entityManager.pullEnabled()) {

                            // When does cron say we should have started last?
                            Instant expectedStart = MonitorService.getTimeBefore(cronExpression, Instant.now());
                            output.println("    Expecting last start at " + expectedStart);

                            // When did we start last
                            OffsetDateTime lastStartTime = entityManager.getLastUpdated(session);
                            output.println("    Last start was at " + lastStartTime.toInstant());

                            // fail if more than four hours have passed since expectedstart and we are not yet done
                            // not yet done = lastStartTime is somewhere before expectedStart
                            if (
                                    Instant.now().isAfter(expectedStart.plus(4, ChronoUnit.HOURS)) &&
                                            (lastStartTime == null || lastStartTime.toInstant().plusSeconds(60).isBefore(expectedStart))
                            ) {
                                output.println("It is more than 4 hours after expected start, and last start has not been updated to be after expected start");
                                response.setStatus(500);
                            }
                        } else {
                            output.println("    Disabled");
                        }
                    }
                }
            }
        }
        loggerHelper.urlResponsePersistablelogs(response.getStatus(), "Done checkPulls");
    }

    @Value("${dafo.error_file:cache/log/dafo.err}")
    private String errorFileConfig;

    @RequestMapping(path = "/errors")
    public void checkErrors(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LoggerHelper loggerHelper = new LoggerHelper(log, request);
        loggerHelper.urlInvokePersistablelogs("checkErrors");

        PrintWriter output = response.getWriter();
        String errorFilePath = this.errorFileConfig;
        File errorFile = new File(errorFilePath);
        String filePath = errorFile.getAbsolutePath();
        if (!errorFile.exists()) {
            response.setStatus(500);
            output.println("Error file " + filePath + " does not exist");
            return;
        }
        if (!errorFile.isFile()) {
            response.setStatus(500);
            output.println("Error file " + filePath + " is not a file");
            return;
        }
        if (!errorFile.canRead()) {
            response.setStatus(500);
            output.println("Error file " + filePath + " is not readable");
            return;
        }
        if (errorFile.length() > 0) {
            response.setStatus(500);
            output.println("There are errors present in file " + errorFile.getName());
        } else {
            response.setStatus(200);
        }
        output.close();
        loggerHelper.urlResponsePersistablelogs(response.getStatus(), "Done checkErrors");
    }

    @Autowired
    private Environment environment;

    private class AccessCheckpoint {
        public String path;
        public HttpMethod method = HttpMethod.GET;
        public String requestBody;
    }

    private final HashSet<AccessCheckpoint> accessCheckPoints = new HashSet<>();

    public void addAccessCheckPoint(String path) {
        AccessCheckpoint accessCheckpoint = new AccessCheckpoint();
        accessCheckpoint.path = path;
        this.accessCheckPoints.add(accessCheckpoint);
    }

    public void addAccessCheckPoint(HttpMethod method, String path, String body) {
        AccessCheckpoint accessCheckpoint = new AccessCheckpoint();
        accessCheckpoint.method = method;
        accessCheckpoint.path = path;
        accessCheckpoint.requestBody = body;
        this.accessCheckPoints.add(accessCheckpoint);
    }

    public void addAccessCheckPoint(String method, String path, String body) {
        this.addAccessCheckPoint(HttpMethod.valueOf(method), path, body);
    }


    @RequestMapping(path = "/access")
    public void checkAccess(HttpServletRequest request, HttpServletResponse response) throws IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException, URISyntaxException {
        LoggerHelper loggerHelper = new LoggerHelper(log, request);
        loggerHelper.urlInvokePersistablelogs("checkAccess");

        int port = Integer.parseInt(environment.getProperty("local.server.port"));
        StringJoiner successes = new StringJoiner("\n");
        StringJoiner failures = new StringJoiner("\n");

        HttpHost localhost = new HttpHost(request.getScheme(), "localhost", port);

        SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial((TrustStrategy) (chain, authType) -> true)
                .build();
        TlsSocketStrategy tlsStrategy = new DefaultClientTlsStrategy(sslContext);

        HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setTlsSocketStrategy(tlsStrategy)
                .setDefaultTlsConfig(TlsConfig.custom()
                        .setHandshakeTimeout(Timeout.ofSeconds(30))
                        .setSupportedProtocols(TLS.V_1_2)
                        .build())
                .build();

        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(connectionManager).build();


        for (AccessCheckpoint endpoint : this.accessCheckPoints) {
            HttpUriRequestBase httpRequest = new HttpUriRequestBase(endpoint.method.name(), new URI("http://localhost"+endpoint.path));
            if (endpoint.requestBody != null) {
                httpRequest.setEntity(new StringEntity(endpoint.requestBody));
            }
            CloseableHttpResponse resp = httpClient.execute(localhost, httpRequest);
            try {
                int code = resp.getCode();
                StringJoiner joiner = (code == 403) ? successes : failures;
                joiner.add(endpoint.method.name() + " " + endpoint.path + " : " + code + " " + resp.getReasonPhrase());
                resp.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                resp.close();
            }
        }
        httpClient.close();
        if (failures.length() > 0) {
            response.setStatus(500);
            response.getWriter().append(failures.toString());
        } else {
            response.setStatus(200);
            response.getWriter().append(successes.toString());
        }
        loggerHelper.urlResponsePersistablelogs(response.getStatus(), "Done checkAccess");
    }

    // Because org.quartz.CronExpression does not have getTimeBefore implemented
    public static Instant getTimeBefore(CronExpression cronExpression, Instant date) {
        Instant someTimeBefore = null;

        // Go back from date until we pass at least one instance where the cron would fire
        long a = 1;
        Instant d = date;
        for (int i = 0; i < 1000 && someTimeBefore == null; i++) {
            Instant earlier = d.minus(a, ChronoUnit.SECONDS);
            Date cronDate = cronExpression.getTimeAfter(
                    Date.from(earlier)
            );
            //System.out.println("Cron fires at "+cronDate.toInstant().toString());
            if (cronDate.after(Date.from(d))) {
                //System.out.println("That's after our start, go further back");
                d = earlier;
            } else {
                //System.out.println("That's before our start. We're happy");
                someTimeBefore = earlier;
            }
            a *= 2;
        }
        if (someTimeBefore == null) {
            return null;
        }

        // Go forward in cron fire times until we hit our original date
        Date cronDate = Date.from(someTimeBefore);
        Date end = Date.from(date);
        while (true) {
            Date next = cronExpression.getTimeAfter(cronDate);
            //System.out.println("Next fire time after "+cronDate.toInstant().toString()+" is "+next.toInstant().toString());
            if (!next.before(end)) {
                break;
            }
            cronDate = next;
        }
        return cronDate.toInstant();
    }

}
