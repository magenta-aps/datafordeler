package dk.magenta.datafordeler.cvr.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import dk.magenta.datafordeler.core.database.Bitemporal;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cvr.access.CvrRolesDefinition;
import dk.magenta.datafordeler.cvr.query.CompanyRecordQuery;
import dk.magenta.datafordeler.cvr.query.ParticipantRecordQuery;
import dk.magenta.datafordeler.cvr.records.CompanyRecord;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.Collections;

@RestController
@RequestMapping("/cvr/owned_by/")
public class CompanyOwnedByService {

    @Autowired
    private DafoUserManager dafoUserManager;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private ObjectMapper objectMapper;

    private final Logger log = LogManager.getLogger(CompanyOwnedByService.class.getCanonicalName());


    @RequestMapping(
            path = {"/{cpr}"},
            produces = {"application/json"}
    )
    public String getRest(@PathVariable("cpr") String cpr, HttpServletRequest request) throws DataFordelerException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(this.log, request, user);
        this.checkAndLogAccess(loggerHelper);
        Session session = sessionManager.getSessionFactory().openSession();

        OffsetDateTime now = OffsetDateTime.now();
        this.applyFilter(session, Bitemporal.FILTER_EFFECTFROM_BEFORE, Bitemporal.FILTERPARAM_EFFECTFROM_BEFORE, now);
        this.applyFilter(session, Bitemporal.FILTER_EFFECTTO_AFTER, Bitemporal.FILTERPARAM_EFFECTTO_AFTER, now);

        CompanyRecordQuery companyRecordQuery = new CompanyRecordQuery();
        companyRecordQuery.setParameter(CompanyRecordQuery.VIRKSOMHEDSFORM, "10");
        companyRecordQuery.setParameter(CompanyRecordQuery.ORGANIZATIONTYPE, "FULDT_ANSVARLIG_DELTAGERE");

        ParticipantRecordQuery participantRecordQuery = new ParticipantRecordQuery();
        participantRecordQuery.setParameter(ParticipantRecordQuery.FORRETNINGSNOEGLE, cpr);

        companyRecordQuery.addRelated(participantRecordQuery, Collections.singletonMap("participantUnitNumber", "unit"));

        companyRecordQuery.setRegistrationAt(now);
        companyRecordQuery.setEffectAt(now);
        companyRecordQuery.applyFilters(session);

        ArrayNode arrayNode = objectMapper.createArrayNode();
        QueryManager.getAllEntitiesAsStream(session, companyRecordQuery, CompanyRecord.class)
                .forEach(companyRecord -> arrayNode.add(companyRecord.getCvrNumber()));

        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(arrayNode);
        } catch (JsonProcessingException e) {
            throw new DataStreamException(e);
        }
    }

    protected void checkAndLogAccess(LoggerHelper loggerHelper) throws AccessDeniedException {
        loggerHelper.logRequest();
        try {
            loggerHelper.getUser().checkHasSystemRole(CvrRolesDefinition.READ_CVR_ROLE);
            loggerHelper.getUser().checkHasSystemRole(CprRolesDefinition.READ_CPR_ROLE);
        } catch (AccessDeniedException e) {
            loggerHelper.info("Access denied: " + e.getMessage());
            throw (e);
        }
    }

    private void applyFilter(Session session, String filterName, String parameterName, Object parameterValue) {
        if (session.getSessionFactory().getDefinedFilterNames().contains(filterName)) {
            session.enableFilter(filterName).setParameter(
                    parameterName,
                    parameterValue
            );
        }
    }
}
