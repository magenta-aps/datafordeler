package dk.magenta.datafordeler.core;

import dk.magenta.datafordeler.core.database.DumpInfo;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.exception.AccessRequiredException;
import dk.magenta.datafordeler.core.exception.InvalidCertificateException;
import dk.magenta.datafordeler.core.exception.InvalidTokenException;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.role.ReadServiceRole;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@ResponseBody
@RequestMapping("dump")
public class IndexDumpService {

    @Autowired
    private Engine engine;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private DafoUserManager dafoUserManager;

    private final Logger log = LogManager.getLogger(IndexDumpService.class.getCanonicalName());

    @PostConstruct
    public void init() {
    }

    protected void checkAccess(DafoUserDetails dafoUserDetails, Plugin plugin)
            throws AccessDeniedException {
        ReadServiceRole pluginDefaultReadRole = plugin.getRolesDefinition().getDefaultReadRole();
        dafoUserDetails.checkHasSystemRole(pluginDefaultReadRole);
    }

    private boolean hasAccess(DafoUserDetails dafoUserDetails, Plugin plugin) {
        try {
            this.checkAccess(dafoUserDetails, plugin);
            return true;
        } catch (AccessDeniedException e) {
            return false;
        }
    }

    protected void checkAndLogAccess(LoggerHelper loggerHelper,
                                     String requestPath)
            throws AccessDeniedException, AccessRequiredException {
        try {
            Plugin plugin = pluginManager.getPluginForServicePath(requestPath);
            this.checkAccess(loggerHelper.getUser(), plugin);
        } catch (AccessDeniedException e) {
            loggerHelper.info("Access denied: " + e.getMessage());
            throw (e);
        }
    }

    @RequestMapping(path = "list", produces = "text/html")
    public ModelAndView html(HttpServletRequest request)
            throws InvalidTokenException, AccessRequiredException, AccessDeniedException, InvalidCertificateException {

        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);

        Map<String, Object> filter = new HashMap<>();
        for (Plugin plugin : pluginManager.getPlugins()) {
            if (hasAccess(user, plugin))
                filter.put("plugin", plugin.getName());
        }

        loggerHelper.info("Requesting dump list with following filter: " + filter);

        if (filter.isEmpty())
            throw new AccessRequiredException(
                    "You need to have access to read from at least one plugin"
            );

        HashMap<String, Object> model = new HashMap<>();
        Session session = sessionManager.getSessionFactory().openSession();
        List<DumpInfo> dumpInfos =
                QueryManager.getAllItemsAsStream(session, DumpInfo.class)
                        .filter(
                                d -> hasAccess(user,
                                        engine.pluginManager.getPluginForServicePath(
                                                d.getRequestPath()
                                        )
                                )
                        )
                        .collect(Collectors.toList());
        model.put("dumpList", dumpInfos);
        return new ModelAndView("dumpList", model);
    }

    @RequestMapping(path = "by-id/{id}")
    public void get(@PathVariable Long id,
                    HttpServletRequest request,
                    HttpServletResponse response)
            throws InvalidTokenException, AccessDeniedException, AccessRequiredException, IOException, InvalidCertificateException {
        Map<String, Object> filter = new HashMap<>();
        filter.put("id", id);

        getDump(request, response, filter);
    }

    @RequestMapping(path = "by-name/{name}")
    public void get(@PathVariable String name,
                    HttpServletRequest request,
                    HttpServletResponse response)
            throws InvalidTokenException, AccessDeniedException, AccessRequiredException, IOException, InvalidCertificateException {
        Map<String, Object> filter = new HashMap<>();
        filter.put("name", name);

        getDump(request, response, filter);
    }

    private void getDump(HttpServletRequest request,
                         HttpServletResponse response, Map<String, Object> filter)
            throws InvalidTokenException, AccessDeniedException, AccessRequiredException, IOException, InvalidCertificateException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        Session session = sessionManager.getSessionFactory().openSession();
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        DumpInfo info = QueryManager.getItem(session, DumpInfo.class, filter);

        loggerHelper.info("Requesting dump {} from \"{}\"",
                info.getName(), info.getRequestPath());

        this.checkAndLogAccess(loggerHelper, info.getRequestPath());

        response.setContentType(info.getFormat().getMediaType().toString());
        response.setCharacterEncoding(info.getCharset().toString());

        response.getOutputStream().write(info.getData());
    }
}
