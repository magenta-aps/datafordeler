package dk.magenta.datafordeler.cvr.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.exception.AccessRequiredException;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.fapi.Envelope;
import dk.magenta.datafordeler.core.fapi.MultiCondition;
import dk.magenta.datafordeler.core.fapi.ResultSet;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cvr.access.CvrRolesDefinition;
import dk.magenta.datafordeler.cvr.records.ParticipantRecord;
import dk.magenta.datafordeler.cvr.records.RelationParticipantRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

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
            path = {"/{cvr}"},
            produces = {"application/json"}
    )
    public String getRest(@PathVariable("cvr") String cvr, HttpServletRequest request) throws DataFordelerException {
        //Envelope envelope = new Envelope();
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(this.log, request, user);
        loggerHelper.info("Incoming request for cvr ownership with cvr " + cvr);
        this.checkAndLogAccess(loggerHelper);

        Session session = sessionManager.getSessionFactory().openSession();

        // MultiCondition condition = new MultiCondition();

        StringJoiner condition = new StringJoiner(" ");
        condition.add("select participantRecord from");
        condition.add(ParticipantRecord.class.getCanonicalName()+" participantRecord,");
        condition.add(RelationParticipantRecord.class.getCanonicalName()+" relationParticipantRecord");
        condition.add("where relationParticipantRecord."+RelationParticipantRecord.DB_FIELD_UNITNUMBER+" = participantRecord."+ParticipantRecord.DB_FIELD_UNIT_NUMBER);
        condition.add("and relationParticipantRecord.id = 1249");

        Query<ParticipantRecord> query = session.createQuery(
                condition.toString(),
                ParticipantRecord.class
        );
        ArrayNode arrayNode = objectMapper.createArrayNode();
        query.getResultStream().map(p -> p.getId().toString()).forEach(id -> arrayNode.add(id));


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
}
