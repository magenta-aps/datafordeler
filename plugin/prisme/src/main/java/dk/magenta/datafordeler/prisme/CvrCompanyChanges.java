package dk.magenta.datafordeler.prisme;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import dk.magenta.datafordeler.core.MonitorService;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.exception.AccessRequiredException;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.fapi.OutputWrapper;
import dk.magenta.datafordeler.core.fapi.Query;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import dk.magenta.datafordeler.cvr.access.CvrRolesDefinition;
import dk.magenta.datafordeler.cvr.query.CompanyRecordQuery;
import dk.magenta.datafordeler.cvr.records.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.*;

@RestController
@RequestMapping("/prisme/cvr/companychanges/1")
public class CvrCompanyChanges {

    @Autowired
    SessionManager sessionManager;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private DafoUserManager dafoUserManager;

    @Autowired
    protected MonitorService monitorService;

    private Logger log = LogManager.getLogger(CvrRecordService.class.getCanonicalName());

    @PostConstruct
    public void init() {
    }

    public static final String PARAM_UPDATED_SINCE = "updatedSince";
    public static final String COMPANY_FORMS = "companyForms";

    @RequestMapping(method = RequestMethod.GET, path = "/lookup", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String getSingle(@RequestParam(value = COMPANY_FORMS,required=true, defaultValue = "") List<String> companyForms, @RequestParam(value = PARAM_UPDATED_SINCE,required=true) String updatedSince, HttpServletRequest request)
            throws DataFordelerException, JsonProcessingException {

        // Root
        OutputWrapper.NodeWrapper root = new OutputWrapper.NodeWrapper(objectMapper.createObjectNode());

        final OffsetDateTime updatedSinceTimestamp = Query.parseDateTime(updatedSince, false);

        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.info(
                "Incoming REST request for CvrCompanyChanges with updatedSince " + updatedSince
        );
        this.checkAndLogAccess(loggerHelper);
        loggerHelper.urlInvokePersistablelogs("CvrCompanyChanges");


        try(Session session = sessionManager.getSessionFactory().openSession()) {
            CompanyRecordQuery query = new CompanyRecordQuery();
            query.setPageSize("50000");
            query.setRecordAfter(updatedSinceTimestamp);
            for (String form : companyForms) {
                query.addVirksomhedsform(form);
            }
            ArrayNode companyChanges = objectMapper.createArrayNode();

            //Get the companies
            List<CompanyRecord> companyrecords =  QueryManager.getAllEntities(session, query, CompanyRecord.class);
            for(CompanyRecord company : companyrecords) {
                FormRecord formRecord = company.getMetadata().getNewestForm().stream().findFirst().orElse(null);
                if(formRecord!=null) {
                    companyChanges.add(company.getCvrNumber());
                }
            }

            root.putArray("cvrs", companyChanges);

            loggerHelper.urlResponsePersistablelogs(HttpStatus.OK.value(), "CvrCompanyChanges done");
            return objectMapper.writeValueAsString(root.getNode());
        }
    }

    protected void checkAndLogAccess(LoggerHelper loggerHelper) throws AccessDeniedException, AccessRequiredException {
        try {
            loggerHelper.getUser().checkHasSystemRole(CvrRolesDefinition.READ_CVR_ROLE);
        }
        catch (AccessDeniedException e) {
            loggerHelper.info("Access denied: " + e.getMessage());
            throw(e);
        }
    }
}
