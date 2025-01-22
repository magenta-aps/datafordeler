package dk.magenta.datafordeler.eskat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.MonitorService;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.exception.AccessRequiredException;
import dk.magenta.datafordeler.core.exception.InvalidCertificateException;
import dk.magenta.datafordeler.core.exception.InvalidTokenException;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import dk.magenta.datafordeler.cvr.access.CvrRolesDefinition;
import dk.magenta.datafordeler.cvr.query.CompanyRecordQuery;
import dk.magenta.datafordeler.cvr.query.CompanyUnitRecordQuery;
import dk.magenta.datafordeler.cvr.records.CompanyRecord;
import dk.magenta.datafordeler.cvr.records.CompanyUnitRecord;
import dk.magenta.datafordeler.eskat.output.EskatRecordDetailOutputWrapper;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/eskat/companydetail/1/rest")
public class CompanyRecordDetailService {

    @Autowired
    SessionManager sessionManager;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private EskatRecordDetailOutputWrapper companyRecordOutputWrapper;

    @Autowired
    private DafoUserManager dafoUserManager;

    @Autowired
    protected MonitorService monitorService;

    private final Logger log = LogManager.getLogger(CompanyRecordDetailService.class.getCanonicalName());


    @GetMapping("/{cvr}")
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true, noRollbackFor = Exception.class)
    ResponseEntity companyDetail(HttpServletRequest request, @PathVariable String cvr) throws AccessDeniedException, AccessRequiredException, InvalidCertificateException, InvalidTokenException {
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
            LoggerHelper loggerHelper = new LoggerHelper(this.log, request, user);
            loggerHelper.info("Incoming request CompanyPunitRecordService ");
            this.checkAndLogAccess(loggerHelper);

            CompanyRecordQuery query = new CompanyRecordQuery();
            query.setParameter(CompanyRecordQuery.CVRNUMMER, cvr);
            Stream<CompanyRecord> companyEntities = QueryManager.getAllEntitiesAsStream(session, query, CompanyRecord.class);
            CompanyRecord companyEntity = companyEntities.findFirst().orElse(null);

            if (companyEntity == null) {
                String errorMessage = "Company not found";
                ObjectNode obj = objectMapper.createObjectNode();
                obj.put("errorMessage", errorMessage);
                return new ResponseEntity(obj.toString(), HttpStatus.NOT_FOUND);
            }

            List<String> productionUnits = companyEntity.getProductionUnits().currentStream().map(f -> Integer.toString(f.getpNumber())).collect(Collectors.toList());
            CompanyUnitRecordQuery companyUnitRecordQuery = new CompanyUnitRecordQuery();
            companyUnitRecordQuery.setParameter(CompanyUnitRecordQuery.P_NUMBER, productionUnits);
            Stream<CompanyUnitRecord> pUnitEntities = QueryManager.getAllEntitiesAsStream(session, companyUnitRecordQuery, CompanyUnitRecord.class);

            ObjectNode objectNode = companyRecordOutputWrapper.fillContainer(companyEntity, pUnitEntities);
            return ResponseEntity.ok(objectNode);
        } catch (AccessDeniedException | InvalidCertificateException | InvalidTokenException e) {
            String errorMessage = "Failed accessing company";
            ObjectNode obj = objectMapper.createObjectNode();
            obj.put("errorMessage", errorMessage);
            log.error(errorMessage, e);
            return new ResponseEntity(obj.toString(), HttpStatus.FORBIDDEN);
        }
    }

    protected void checkAndLogAccess(LoggerHelper loggerHelper) throws AccessDeniedException {
        try {
            loggerHelper.getUser().checkHasSystemRole(CvrRolesDefinition.READ_CVR_ROLE);
        } catch (AccessDeniedException e) {
            loggerHelper.info("Access denied: " + e.getMessage());
            throw (e);
        }
    }
}
