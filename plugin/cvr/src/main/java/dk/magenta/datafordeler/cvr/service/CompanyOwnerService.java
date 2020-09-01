package dk.magenta.datafordeler.cvr.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import dk.magenta.datafordeler.core.database.Bitemporal;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cvr.access.CvrRolesDefinition;
import dk.magenta.datafordeler.cvr.records.*;
import dk.magenta.datafordeler.cvr.records.unversioned.CompanyForm;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.StringJoiner;

@RestController
@RequestMapping("/cvr/owner/")
public class CompanyOwnerService {

    @Autowired
    private DafoUserManager dafoUserManager;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private ObjectMapper objectMapper;

    private Logger log = LogManager.getLogger(CompanyOwnerService.class.getCanonicalName());

    @RequestMapping(
            path = {"/{cpr}"},
            produces = {"application/json"}
    )
    public String getRest(@PathVariable("cpr") String cpr, HttpServletRequest request) throws DataFordelerException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(this.log, request, user);
        loggerHelper.info("Incoming request for cvr ownership with cpr " + cpr);
        this.checkAndLogAccess(loggerHelper);
        Session session = sessionManager.getSessionFactory().openSession();
        OffsetDateTime now = OffsetDateTime.now();
        this.applyFilter(session, Bitemporal.FILTER_EFFECTFROM_BEFORE, Bitemporal.FILTERPARAM_EFFECTFROM_BEFORE, now);
        this.applyFilter(session, Bitemporal.FILTER_EFFECTTO_AFTER, Bitemporal.FILTERPARAM_EFFECTTO_AFTER, now);

        StringJoiner condition = new StringJoiner(" ");
        condition.add("select distinct companyRecord from");
        condition.add(ParticipantRecord.class.getCanonicalName()+" participantRecord,");
        condition.add(CompanyRecord.class.getCanonicalName()+" companyRecord");

        condition.add("join companyRecord."+CompanyRecord.DB_FIELD_PARTICIPANTS+" companyParticipantRelationRecord");
        condition.add("join companyParticipantRelationRecord."+CompanyParticipantRelationRecord.DB_FIELD_PARTICIPANT_RELATION+" relationParticipantRecord");

        condition.add("join companyRecord."+CompanyRecord.DB_FIELD_FORM+" companyForm");
        condition.add("join companyForm."+ FormRecord.DB_FIELD_FORM+" form");
        condition.add("join companyParticipantRelationRecord."+CompanyParticipantRelationRecord.DB_FIELD_ORGANIZATIONS+" organizations");

        condition.add("where relationParticipantRecord."+RelationParticipantRecord.DB_FIELD_UNITNUMBER+" = participantRecord."+ParticipantRecord.DB_FIELD_UNIT_NUMBER);

        condition.add("and form."+ CompanyForm.DB_FIELD_CODE+" = :formcode");
        condition.add("and organizations."+OrganizationRecord.DB_FIELD_MAIN_TYPE+" = :organizationtype");

        condition.add("and participantRecord."+ParticipantRecord.DB_FIELD_BUSINESS_KEY+" = :cpr");

        Query<CompanyRecord> query = session.createQuery(
                condition.toString(),
                CompanyRecord.class
        );
        query.setParameter("cpr", Long.parseLong(cpr));
        query.setParameter("formcode", "10");
        query.setParameter("organizationtype", "FULDT_ANSVARLIG_DELTAGERE");
        ArrayNode arrayNode = objectMapper.createArrayNode();
        query.getResultStream().map(companyRecord -> companyRecord.getCvrNumber()).forEach(id -> arrayNode.add(id));

        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(arrayNode);
        } catch (JsonProcessingException e) {
            throw new DataStreamException(e);
        }
    }

    protected void checkAndLogAccess(LoggerHelper loggerHelper) throws AccessDeniedException {
        try {
            loggerHelper.getUser().checkHasSystemRole(CvrRolesDefinition.READ_CVR_ROLE);
            loggerHelper.getUser().checkHasSystemRole(CprRolesDefinition.READ_CPR_ROLE);
        }
        catch (AccessDeniedException e) {
            loggerHelper.info("Access denied: " + e.getMessage());
            throw(e);
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
