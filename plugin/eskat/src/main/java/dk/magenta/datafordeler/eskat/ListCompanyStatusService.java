package dk.magenta.datafordeler.eskat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.MonitorService;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.exception.AccessRequiredException;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import dk.magenta.datafordeler.cvr.access.CvrRolesDefinition;
import dk.magenta.datafordeler.cvr.query.CompanyRecordQuery;
import dk.magenta.datafordeler.cvr.records.CompanyStatusRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Webservice for
 */
@RestController
@RequestMapping("/eskat/companystatus")
public class ListCompanyStatusService {

    @Autowired
    SessionManager sessionManager;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private DafoUserManager dafoUserManager;

    @Autowired
    protected MonitorService monitorService;

    private Logger log = LogManager.getLogger(ListCompanyStatusService.class.getCanonicalName());


    @PostConstruct
    public void init() {
        this.monitorService.addAccessCheckPoint("/eskat/companystatus/lookup?cvr=1111");

    }

    @RequestMapping(method = RequestMethod.GET, path = "/{lookup}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<String> getSingle(@RequestParam(value = "cpr",required=false, defaultValue = "") List<String> cprs, @RequestParam(value = "cvr",required=false, defaultValue = "") List<String> cvrs, HttpServletRequest request)
            throws DataFordelerException, JsonProcessingException {

        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        this.checkAndLogAccess(loggerHelper);

        CompanyRecordQuery companyQuery = new CompanyRecordQuery();
        companyQuery.setPageSize(Integer.MAX_VALUE);
        List<String> convertedStatusList = null;

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            List<String> statusList = session.createQuery("SELECT DISTINCT status FROM "+CompanyStatusRecord.class.getCanonicalName(), String.class).list();
            convertedStatusList = statusList.stream().map(status -> {
               if(!"NORMAL".equals(status) && !"Aktiv".equals(status) && !"Fremtid".equals(status)) {
                   return new String("Ophørt: "+status);
               } else {
                   return new String("Aktiv: "+status);
               }
           }).collect(Collectors.toList());
        }
        return convertedStatusList;
    }


    protected void checkAndLogAccess(LoggerHelper loggerHelper) throws AccessDeniedException, AccessRequiredException {
        try {
            loggerHelper.getUser().checkHasSystemRole(CvrRolesDefinition.READ_CVR_ROLE);
        } catch (AccessDeniedException e) {
            loggerHelper.info("Access denied: " + e.getMessage());
            throw (e);
        }
    }

}
