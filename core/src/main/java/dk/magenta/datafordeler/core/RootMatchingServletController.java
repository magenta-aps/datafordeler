package dk.magenta.datafordeler.core;

import org.apache.cxf.Bus;
import org.apache.cxf.common.classloader.ClassLoaderUtils;
import org.apache.cxf.common.classloader.ClassLoaderUtils.ClassLoaderHolder;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.resource.ResourceManager;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.cxf.transport.http.DestinationRegistry;
import org.apache.cxf.transport.servlet.ServletController;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

public class RootMatchingServletController extends ServletController {

    private static final Logger log = LogUtils.getL7dLogger(ServletController.class);

    public RootMatchingServletController(
            DestinationRegistry destinationRegistry,
            ServletConfig config, HttpServlet serviceListGenerator) {
        super(destinationRegistry, config, serviceListGenerator);
    }

    @Override
    public boolean invoke(HttpServletRequest request, HttpServletResponse res, boolean returnErrors)
            throws ServletException {
        try {
            String pathInfo = request.getPathInfo() == null ? "" : request.getPathInfo();
            // We need to match the full path, so add the servlestPath to pathInfo
            pathInfo = request.getServletPath() + pathInfo;
            AbstractHTTPDestination d = this.destinationRegistry.getDestinationForPath(pathInfo, true);

            if (d == null) {
                if (!isHideServiceList && (request.getRequestURI().endsWith(serviceListRelativePath)
                        || request.getRequestURI().endsWith(serviceListRelativePath + "/")
                        || StringUtils.isEmpty(pathInfo)
                        || "/".equals(pathInfo))) {
                    if (isAuthServiceListPage) {
                        setAuthServiceListPageAttribute(request);
                    }
                    setBaseURLAttribute(request);
                    serviceListGenerator.service(request, res);
                } else {
                    d = destinationRegistry.checkRestfulRequest(pathInfo);
                    if (d == null || d.getMessageObserver() == null) {
                        if (returnErrors) {
                            log.warning("Can't find the the request for "
                                    + request.getRequestURL() + "'s Observer ");
                            generateNotFound(request, res);
                        }
                        return false;
                    }
                }
            }
            if (d != null && d.getMessageObserver() != null) {
                Bus bus = d.getBus();
                ClassLoaderHolder orig = null;
                try {
                    if (bus != null) {
                        ClassLoader loader = bus.getExtension(ClassLoader.class);
                        if (loader == null) {
                            ResourceManager manager = bus.getExtension(ResourceManager.class);
                            if (manager != null) {
                                loader = manager.resolveResource("", ClassLoader.class);
                            }
                        }
                        if (loader != null) {
                            //need to set the context classloader to the loader of the bundle
                            orig = ClassLoaderUtils.setThreadContextClassloader(loader);
                        }
                    }
                    updateDestination(request, d);
                    invokeDestination(request, res, d);
                } finally {
                    if (orig != null) {
                        orig.reset();
                    }
                }
            }
        } catch (IOException e) {
            throw new ServletException(e);
        }
        return true;
    }

    private void setAuthServiceListPageAttribute(HttpServletRequest request) {
        request.setAttribute(ServletController.AUTH_SERVICE_LIST, this.isAuthServiceListPage);
        request.setAttribute(ServletController.AUTH_SERVICE_LIST_REALM, this.authServiceListPageRealm);
    }

}
