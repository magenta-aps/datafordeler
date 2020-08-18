package dk.magenta.datafordeler.cpr.records.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import dk.magenta.datafordeler.core.MonitorService;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.*;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cpr.data.person.PersonCustodyRelationsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/cpr/person/custody/1/rest")
public class PersonEntityCustodyService {

    @Autowired
    SessionManager sessionManager;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private DafoUserManager dafoUserManager;

    @Autowired
    protected MonitorService monitorService;

    @Autowired
    private PersonCustodyRelationsManager custodyManager;

    private Logger log = LogManager.getLogger(PersonEntityCustodyService.class.getCanonicalName());


    @PostConstruct
    public void init() {
        this.monitorService.addAccessCheckPoint("/cpr/person/custody/1/rest/1111");

    }

    @RequestMapping(method = RequestMethod.GET, path = "/{cpr}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String getSingle(@PathVariable("cpr") String cpr, HttpServletRequest request)
            throws DataFordelerException, JsonProcessingException {

        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        this.checkAndLogAccess(loggerHelper);

        if (cpr != null && !cpr.isEmpty()) {
            ArrayNode cvrList = objectMapper.createArrayNode();
            List<String> custodyList = custodyManager.findRelations(cpr);
            for (String custody : custodyList) {
                cvrList.add(custody);
            }
            return objectMapper.writeValueAsString(cvrList);
        }
        return null;
    }


    protected void checkAndLogAccess(LoggerHelper loggerHelper) throws AccessDeniedException, AccessRequiredException {
        try {
            loggerHelper.getUser().checkHasSystemRole(CprRolesDefinition.READ_CPR_ROLE);
        } catch (AccessDeniedException e) {
            loggerHelper.info("Access denied: " + e.getMessage());
            throw (e);
        }
    }
}
