package dk.magenta.datafordeler.prisme;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import dk.magenta.datafordeler.cvr.CvrPlugin;
import dk.magenta.datafordeler.cvr.access.CvrAreaRestrictionDefinition;
import dk.magenta.datafordeler.cvr.access.CvrRolesDefinition;
import dk.magenta.datafordeler.cvr.entitymanager.CompanyEntityManager;
import dk.magenta.datafordeler.cvr.records.CompanyRecord;
import dk.magenta.datafordeler.geo.GeoLookupService;
import dk.magenta.datafordeler.ger.data.company.CompanyEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
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
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

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

    @Autowired
    private GerCompanyLookup gerCompanyLookup;

    private final Logger log = LogManager.getLogger(CvrServiceDirect.class.getCanonicalName());


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

    private class BooleanWrapper {
        public boolean value;
    }

    @RequestMapping(method = RequestMethod.POST, path = "/", produces = {MediaType.APPLICATION_JSON_VALUE})
    public StreamingResponseBody getBulk(HttpServletRequest request)
            throws AccessDeniedException, AccessRequiredException, InvalidTokenException, InvalidClientInputException, IOException, HttpNotFoundException, InvalidCertificateException {
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

        final OffsetDateTime updatedSince = requestObject.has(PARAM_UPDATED_SINCE) ? Query.parseDateTime(requestObject.get(PARAM_UPDATED_SINCE).asText(), false) : null;

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
        loggerHelper.urlInvokePersistablelogs("CvrServiceDirect");

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

        loggerHelper.urlResponsePersistablelogs("CvrServiceDirect");
        return new StreamingResponseBody() {
            @Override
            public void writeTo(OutputStream outputStream) throws IOException {

                final Session lookupSession = sessionManager.getSessionFactory().openSession();
                GeoLookupService lookupService = new GeoLookupService(sessionManager);

                HashSet<CompanyRecord> records = companyEntityManager.directLookup(cvr, updatedSince, CvrServiceDirect.this.getMunicipalityRestrictions(user));
                Stream<ObjectNode> cvrFormattedOutput = records.stream().map(
                        record -> CvrServiceDirect.this.wrapRecord(record, lookupService, returnParticipantDetails)
                );

                for (CompanyRecord record : records) {
                    cvr.remove(Integer.toString(record.getCvrNumber()));
                }

                HashSet<CompanyEntity> gerCompanies = gerCompanyLookup.lookup(lookupSession, cvr);
                Stream<ObjectNode> gerFormattedOutput = gerCompanies.stream().map(
                        companyEntity -> CvrServiceDirect.this.wrapGerCompany(companyEntity, lookupService, returnParticipantDetails)
                );

                Stream<ObjectNode> formattedOutput = Stream.concat(cvrFormattedOutput, gerFormattedOutput);


                try {
                    final BooleanWrapper first = new BooleanWrapper();
                    first.value = true;
                    outputStream.write(START_OBJECT);

                    formattedOutput.forEach(new Consumer<ObjectNode>() {
                        @Override
                        public void accept(ObjectNode jsonNode) {
                            StringBuilder sb = new StringBuilder();
                            if (!first.value) {
                                sb.append(OBJECT_SEPARATOR);
                            } else {
                                first.value = false;
                            }
                            try {
                                sb.append("\"" + jsonNode.get("cvrNummer").textValue() + "\":");
                                sb.append(objectMapper.writeValueAsString(jsonNode));
                                outputStream.write(sb.toString().getBytes(StandardCharsets.UTF_8));
                                outputStream.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    });
                    outputStream.write(END_OBJECT);
                    outputStream.flush();
                } finally {
                    lookupSession.close();
                }
            }
        };
    }

}
