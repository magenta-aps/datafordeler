package dk.magenta.datafordeler.core.util;

import dk.magenta.datafordeler.core.database.Entity;
import dk.magenta.datafordeler.core.fapi.Envelope;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * A logger helper that helps outputting request and user information with log messages.
 */
public class LoggerHelper {

    final Level URLINVOKE = Level.forName("[URLINVOKE]", 250);
    final Level URLRESPONSE = Level.forName("[URLRESPONSE]", 251);

    private final Logger logger;
    private final HttpServletRequest request;
    private DafoUserDetails user;
    private String prefix = "";
    private String urlInvokePrefix = "";

    public LoggerHelper(Logger logger, HttpServletRequest request, DafoUserDetails user) {
        this.logger = logger;
        this.request = request;
        this.user = user;
        updatePrefix();
    }

    public LoggerHelper(Logger logger, HttpServletRequest request) {
        this(logger, request, null);
    }

    public DafoUserDetails getUser() {
        return user;
    }

    public void setUser(DafoUserDetails user) {
        this.user = user;
        updatePrefix();
    }

    private void updatePrefix() {
        String remoteAddr = request.getHeader("X-Forwarded-For");
        if (remoteAddr == null) {
            remoteAddr = request.getRemoteAddr();
        }
        prefix = String.format(
                "%s - %s: ",
                remoteAddr,
                user == null ? "<unknown>" : user.toString()
        );
        urlInvokePrefix = "[" + request.getMethod() + "]";
        urlInvokePrefix += "[" + request.getRequestURI() + "]";
        urlInvokePrefix += "[";
        Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            String paramName = params.nextElement();
            urlInvokePrefix += paramName + "," + request.getParameter(paramName) + ";";
        }
        urlInvokePrefix += "]";
    }

    public <E extends Entity> void logResult(Envelope result) {
        info("Query result - " + result.toLogString());
    }

    public <E extends Entity> void logResult(Envelope result, String queryString) {
        info("Query result - " + result.toLogString(queryString));
    }

    public void trace(String msg, Object... args) {
        logger.trace(prefix + msg, args);
    }

    public void urlInvokePersistablelogs(String msg) {
        logger.log(URLINVOKE, prefix + urlInvokePrefix + "[" + msg + "]");
    }

    public void urlResponsePersistablelogs(int statusCode, String msg) {
        logger.log(URLRESPONSE, prefix + urlInvokePrefix + "[" + statusCode + "]" + "[" + msg + "]");
    }

    public void urlResponsePersistablelogs(String msg) {
        logger.log(URLRESPONSE, prefix + urlInvokePrefix + "[]" + "[" + msg + "]");
    }

    public void debug(String msg, Object... args) {
        logger.debug(prefix + msg, args);
    }

    public void info(String msg, Object... args) {
        logger.info(prefix + msg, args);
    }

    public void warn(String msg, Object... args) {
        logger.warn(prefix + msg, args);
    }

    public void error(String msg, Object... args) {
        logger.error(prefix + msg, args);
    }

    public void error(String msg, Throwable exception) {
        logger.error(prefix + msg, exception);
    }
}
