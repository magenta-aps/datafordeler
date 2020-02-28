package dk.magenta.datafordeler.core.gapi;

import dk.magenta.datafordeler.core.Engine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

@WebServlet(name="GapiInterface", urlPatterns={"/odata/gapi", "/odata/gapi/*"})
public class GapiServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static Logger log = LogManager.getLogger(GapiServlet.class.getCanonicalName());

    @Autowired
    private Engine engine;

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        this.log.info("POST request received on address " + request.getServletPath());
        OData odata = OData.newInstance();
        ServiceMetadata edm = odata.createServiceMetadata(new GapiEdmProvider(), new ArrayList<>());
        ODataHttpHandler oDataHttpHandler = odata.createHandler(edm);
        oDataHttpHandler.register(new GapiProcessor(edm.getEdm(), this.engine));
        oDataHttpHandler.process(request, response);
        this.log.info("Request complete");
    }
}
