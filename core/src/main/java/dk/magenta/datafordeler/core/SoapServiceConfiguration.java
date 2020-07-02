package dk.magenta.datafordeler.core;

import dk.magenta.datafordeler.core.exception.InvalidServiceOwnerDefinitionException;
import dk.magenta.datafordeler.core.fapi.FapiBaseService;
import dk.magenta.datafordeler.core.fapi.SoapHandler;
import dk.magenta.datafordeler.core.plugin.EntityManager;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.plugin.RegisterManager;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@Configuration
public class SoapServiceConfiguration {

    @Autowired
    PluginManager pluginManager;

    private static Logger log = LogManager.getLogger(Application.class.getCanonicalName());

    private final Pattern ownerValidation = Pattern.compile("^[a-zA-Z0-9_]+$");

    private ArrayList<JaxWsServerFactoryBean> serverBeans = new ArrayList<>();

    public List<JaxWsServerFactoryBean> getServerBeans() {
        return this.serverBeans;
    }

    @Bean
    protected ServletRegistrationBean fapiDispatcherServlet()
            throws InvalidServiceOwnerDefinitionException {
        // this.log.info("Initialize ServletRegistrationBean for Plugin "+this.getPlugin().getClass().getCanonicalName());


        RootMatchingCXFServlet cxfServlet = new RootMatchingCXFServlet();
        SpringBus bus = new SpringBus();
        cxfServlet.setBus(bus);


        this.log.info("Known plugins:");
        for (Plugin plugin : pluginManager.getPlugins()) {
            this.log.info("    " + plugin.getName());
        }

        List<String> serviceOwnerMatchers = new ArrayList<>();

        for (Plugin plugin : pluginManager.getPlugins()) {
            RegisterManager registerManager = plugin.getRegisterManager();
            if (registerManager != null) {
                this.log.info("Handling plugin " + plugin.getName() + " with " + registerManager.getEntityManagers().size() + " EntityManagers");
                String serviceOwner = plugin.getServiceOwner();
                this.log.info("Service owner for " + plugin.getClass().getCanonicalName() + " is " + serviceOwner);
                if (!ownerValidation.matcher(serviceOwner).matches()) {
                    this.log.error("Invalid service owner: " + serviceOwner);
                    throw new InvalidServiceOwnerDefinitionException(plugin, serviceOwner, this.ownerValidation);
                }
                for (EntityManager entityManager : registerManager.getEntityManagers()) {
                    FapiBaseService service = entityManager.getEntityService();
                    if (service != null) {
                        String base = "/" + serviceOwner + "/" + service.getServiceName() + "/" + service.getVersion();

                        // SOAP
                        this.log.info("Setting up SOAP handler on " + base + "/soap");
                        JaxWsServerFactoryBean serverFactoryBean = new JaxWsServerFactoryBean();
                        serverFactoryBean.setBus(bus);
                        serverFactoryBean.setAddress(base + "/soap");
                        serviceOwnerMatchers.add(base + "/soap");
                        serverFactoryBean.setServiceBean(service);

                        serverFactoryBean.addHandlers(Collections.singletonList(new SoapHandler()));
                        serverFactoryBean.create();
                        this.serverBeans.add(serverFactoryBean);
                    }
                }
            }
        }

        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(
                cxfServlet,
                //"/*"
                serviceOwnerMatchers.toArray(new String[serviceOwnerMatchers.size()])
        );
        servletRegistrationBean.setName("FapiServlet");
        this.log.info("ServletRegistrationBean \"" + servletRegistrationBean.getServletName() + "\" created");

        return servletRegistrationBean;
    }
}
