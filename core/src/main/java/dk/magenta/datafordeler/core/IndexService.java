package dk.magenta.datafordeler.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.fapi.FapiBaseService;
import dk.magenta.datafordeler.core.fapi.ServiceDescriptor;
import dk.magenta.datafordeler.core.plugin.EntityManager;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.plugin.RegisterManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Controller
@ResponseBody
@RequestMapping("/")
public class IndexService {

    @Autowired
    private PluginManager pluginManager;

    // No need to recreate the response every time we are asked, since it's the same for every call on a given server execution
    private String preparedJsonResponse;

    private ArrayList<ServiceDescriptor> serviceDescriptors;

    @PostConstruct
    public void init() {
        this.pluginManager.addPostConstructCallBackHandler(pluginManager -> {
            HashMap<String, FapiBaseService> serviceMap = new HashMap<>();
            for (Plugin plugin : pluginManager.getPlugins()) {
                RegisterManager registerManager = plugin.getRegisterManager();
                if (registerManager != null) {
                    for (EntityManager entityManager : registerManager.getEntityManagers()) {
                        FapiBaseService restService = entityManager.getEntityService();
                        if (restService != null) {
                            String[] servicePaths = restService.getServicePaths();
                            if (servicePaths != null) {
                                for (String servicePath : servicePaths) {
                                    serviceMap.put(servicePath, restService);
                                }
                            }
                        }
                    }
                }
            }

            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode root = objectMapper.createObjectNode();

            IndexService.this.serviceDescriptors = new ArrayList<>();

            List<String> servicePaths = new ArrayList<>(serviceMap.keySet());
            Collections.sort(servicePaths);

            for (String servicePath : servicePaths) {
                FapiBaseService service = serviceMap.get(servicePath);
                ServiceDescriptor serviceDescriptor = service.getServiceDescriptor(servicePath);
                if (serviceDescriptor != null) {
                    IndexService.this.serviceDescriptors.add(serviceDescriptor);
                }
            }

            root.set("services", objectMapper.valueToTree(IndexService.this.serviceDescriptors));
            IndexService.this.preparedJsonResponse = root.toString();
        });

    }

    @RequestMapping(path = "", produces = "application/json")
    public String json() {
        return this.preparedJsonResponse;
    }

    @RequestMapping(path = "", produces = "text/html")
    public ModelAndView html() {
        HashMap<String, Object> model = new HashMap<>();
        model.put("serviceDescriptors", this.serviceDescriptors);
        return new ModelAndView("serviceList", model);
    }

}
