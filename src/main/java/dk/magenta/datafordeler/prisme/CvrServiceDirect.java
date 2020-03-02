package dk.magenta.datafordeler.prisme;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.arearestriction.AreaRestriction;
import dk.magenta.datafordeler.core.arearestriction.AreaRestrictionType;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.*;
import dk.magenta.datafordeler.core.fapi.Query;
import dk.magenta.datafordeler.core.plugin.AreaRestrictionDefinition;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import dk.magenta.datafordeler.cvr.CvrAreaRestrictionDefinition;
import dk.magenta.datafordeler.cvr.CvrPlugin;
import dk.magenta.datafordeler.cvr.CvrRolesDefinition;
import dk.magenta.datafordeler.cvr.data.company.CompanyEntityManager;
import dk.magenta.datafordeler.cvr.records.CompanyRecord;
import dk.magenta.datafordeler.cvr.records.CvrBitemporalRecord;
import org.hibernate.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/prisme/cvr/2")
public class CvrServiceDirect extends CvrRecordService {

    @Autowired
    SessionManager sessionManager;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private DafoUserManager dafoUserManager;

    @Autowired
    private CvrPlugin cvrPlugin;

    @Autowired
    private CompanyEntityManager companyEntityManager;

    private Logger log = LogManager.getLogger(CvrServiceDirect.class.getCanonicalName());

	
    @PostConstruct
    public void init() {
        this.monitorService.addAccessCheckPoint("/prisme/cvr/2/1234");
        this.monitorService.addAccessCheckPoint("POST", "/prisme/cvr/2/", "{}");
    }
	
    private List<Integer> getMunicipalityRestrictions(DafoUserDetails user) {
        Collection<AreaRestriction> restrictions = user.getAreaRestrictionsForRole(CvrRolesDefinition.READ_CVR_ROLE);
        if (!restrictions.isEmpty()) {
            AreaRestrictionDefinition areaRestrictionDefinition = this.cvrPlugin.getAreaRestrictionDefinition();
            AreaRestrictionType municipalityType = areaRestrictionDefinition.getAreaRestrictionTypeByName(CvrAreaRestrictionDefinition.RESTRICTIONTYPE_KOMMUNEKODER);
            List<Integer> municipalityRestriction = new ArrayList<>();
            for (AreaRestriction restriction : restrictions) {
                if (restriction.getType() == municipalityType) {
                    municipalityRestriction.add(Integer.parseInt(restriction.getValue(), 10));
                }
            }
            return municipalityRestriction;
        }
        return null;
    }

    @Override
    protected Collection<CompanyRecord> getCompanies(Session session, Collection<String> cvrNumbers, DafoUserDetails user) throws DataFordelerException {
        return companyEntityManager.directLookup(new HashSet<>(cvrNumbers), null, this.getMunicipalityRestrictions(user));
    }

    @RequestMapping(method = RequestMethod.POST, path = "/", produces = {MediaType.APPLICATION_JSON_VALUE})
    public StreamingResponseBody getBulk(HttpServletRequest request)
            throws AccessDeniedException, AccessRequiredException, InvalidTokenException, InvalidClientInputException, IOException, HttpNotFoundException {
        JsonNode requestBody;
        try {
            requestBody = objectMapper.readTree(request.getInputStream());
        } catch (IOException e) {
            throw new InvalidClientInputException(e.getMessage());
        }
        if (!requestBody.isObject()) {
            throw new InvalidClientInputException("Input is not a JSON object");
        }
        ObjectNode requestObject = (ObjectNode) requestBody;

        final OffsetDateTime updatedSince = requestObject.has(PARAM_UPDATED_SINCE) ? Query.parseDateTime(requestObject.get(PARAM_UPDATED_SINCE).asText()) : null;

        final List<String> cvrNumbers = (requestObject.has(PARAM_CVR_NUMBER)) ? this.getCvrNumber(requestObject.get(PARAM_CVR_NUMBER)) : null;

        boolean returnParticipantDetails = "1".equals(request.getParameter(PARAM_RETURN_PARTICIPANT_DETAILS));

        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.info(
                "Incoming REST request for PrismeCprService with " +
                        PARAM_UPDATED_SINCE + " = " + updatedSince + ", " +
                        PARAM_CVR_NUMBER + " = " + (cvrNumbers != null && cvrNumbers.size() > 10 ? (cvrNumbers.size() + " cpr numbers") : cvrNumbers) + " and " +
                        PARAM_RETURN_PARTICIPANT_DETAILS + " = " + returnParticipantDetails
        );
        this.checkAndLogAccess(loggerHelper, returnParticipantDetails);

        HashSet<String> cvr = new HashSet<>();

        if (cvrNumbers != null) {
            for (String cvrNumber : cvrNumbers) {
                try {
                    cvr.add(Integer.toString(Integer.parseInt(cvrNumber, 10)));
                } catch (NumberFormatException e) {
                }
            }
        }
        if (cvr.isEmpty()) {
            throw new InvalidClientInputException("Please specify at least one CVR number");
        }

        HashSet<CompanyRecord> records = companyEntityManager.directLookup(cvr, updatedSince, this.getMunicipalityRestrictions(user));

        return new StreamingResponseBody() {
            @Override
            public void writeTo(OutputStream outputStream) throws IOException {
                final Session lookupSession = sessionManager.getSessionFactory().openSession();
                LookupService lookupService = new LookupService(lookupSession);
                try {
                    boolean first = true;
                    outputStream.write(START_OBJECT);
                    for (CompanyRecord record : records) {
                        if (!first) {
                            outputStream.flush();
                            outputStream.write(OBJECT_SEPARATOR);
                        } else {
                            first = false;
                        }
                        outputStream.write(("\"" + record.getCvrNumber() + "\":").getBytes());
                        outputStream.write(
                                objectMapper.writeValueAsString(
                                        CvrServiceDirect.this.wrapRecord(record, lookupService, returnParticipantDetails)
                                ).getBytes("UTF-8")
                        );
                    }
                    outputStream.write(END_OBJECT);
                    outputStream.flush();
                } finally {
                    lookupSession.close();
                }
            }
        };
    }

}
