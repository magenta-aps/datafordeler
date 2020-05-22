package dk.magenta.datafordeler.prisme;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import dk.magenta.datafordeler.core.MonitorService;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.*;
import dk.magenta.datafordeler.core.fapi.OutputWrapper;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cvr.DirectLookup;
import dk.magenta.datafordeler.cvr.access.CvrRolesDefinition;
import dk.magenta.datafordeler.cvr.query.CompanyRecordQuery;
import dk.magenta.datafordeler.cvr.records.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/prisme/cvr/ownerhistory/1")
public class CvrCompanyOwnerHistory {

    @Autowired
    SessionManager sessionManager;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private DafoUserManager dafoUserManager;

    @Autowired
    protected MonitorService monitorService;

    @Autowired
    private DirectLookup directLookup;

    private Logger log = LogManager.getLogger(CvrCompanyOwnerHistory.class.getCanonicalName());

    @PostConstruct
    public void init() {
        this.monitorService.addAccessCheckPoint("/prisme/cvr/ownerhistory/1/1234");
        this.monitorService.addAccessCheckPoint("POST", "/prisme/cvr/ownerhistory/1/", "{}");
    }


    @RequestMapping(method = RequestMethod.GET, path = "/{cvrNummer}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String getSingle(@PathVariable("cvrNummer") String cvrNummer, HttpServletRequest request)
            throws DataFordelerException, JsonProcessingException {

        // Root
        OutputWrapper.NodeWrapper root = new OutputWrapper.NodeWrapper(objectMapper.createObjectNode());
        root.put("cvrNummer", cvrNummer);


        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.info(
                "Incoming REST request for PrismeCvrService with cvrNummer " + cvrNummer
        );
        this.checkAndLogAccess(loggerHelper);
        loggerHelper.urlInvokePersistablelogs("CvrCompanyOwnerHistory");

        HashSet<String> cvrNumbers = new HashSet<>();
        cvrNumbers.add(cvrNummer);

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            CompanyRecordQuery query = new CompanyRecordQuery();
            query.setCvrNumre(cvrNumbers);
            //Get the company, there can only be 0-1
            List<CompanyRecord> companyrecords =  QueryManager.getAllEntities(session, query, CompanyRecord.class);
            if(companyrecords.size()==0) {
                loggerHelper.urlResponsePersistablelogs(HttpStatus.NOT_FOUND.value(), "CvrCompanyOwnerHistory done");
                throw new HttpNotFoundException("Company not found " + cvrNummer);
            }
            CompanyRecord companyrecord = companyrecords.get(0);

            FormRecord formRecord = companyrecord.getMetadata().getNewestForm().iterator().next();
            String formCodeString = formRecord.getCompanyFormCode();
            Integer formCode = Integer.parseInt(formCodeString);
            root.put("virksomhedsformkode", formRecord.getCompanyFormCode());

            root.put("shortDescribtion", formRecord.getShortDescription());
            root.put("longDescribtion", formRecord.getLongDescription());

            //It is legally forbidden to supply this information from companies with other formcodes then 10, 30 and 50
            if(formCode != 10 && formCode != 30 && formCode != 50) {
                loggerHelper.urlResponsePersistablelogs(HttpStatus.FORBIDDEN.value(), "CvrCompanyOwnerHistory done");
                throw new AccessDeniedException("The requested company is not of a formcode where this request is accepted " + cvrNummer);
            }

            Set<CompanyParticipantRelationRecord> participants = companyrecord.getParticipants();

            ObjectMapper mapper = new ObjectMapper();
            List<CompanyOwnerItem> personAdressItemList = new ArrayList<CompanyOwnerItem>();

            for(CompanyParticipantRelationRecord participant : participants) {
                String from = null;
                String to = null;
                Number deltagerPnr = null;

                Long participantNumber = participant.getParticipantUnitNumber();

                if("PERSON".equals(participant.getRelationParticipantRecord().getUnitType())) {


                    ParticipantRecord participantRecord = directLookup.participantLookup("11111111");

                    deltagerPnr = participantRecord.getBusinessKey();
                    Iterator<OrganizationRecord> orgRecord = participant.getOrganizations().iterator();
                    if(orgRecord.hasNext()) {
                        AttributeValueRecord attributes = orgRecord.next().getAttributes().iterator().next().getValues().iterator().next();
                        from = Optional.ofNullable(attributes.getValidFrom()).map(o -> o.toString()).orElse(null);
                        to = Optional.ofNullable(attributes.getValidTo()).map(o -> o.toString()).orElse(null);
                    }
                    CompanyOwnerItem ownerItem = new CompanyOwnerItem(participantNumber, String.format("%010d", deltagerPnr), from, to);
                    personAdressItemList.add(ownerItem);
                }
            }
            ArrayNode jsonAdressArray = mapper.valueToTree(personAdressItemList);
            root.putArray("owners", jsonAdressArray);
            loggerHelper.urlResponsePersistablelogs(HttpStatus.OK.value(), "CvrCompanyOwnerHistory done");
            return objectMapper.writeValueAsString(root.getNode());
        }
    }

    protected void checkAndLogAccess(LoggerHelper loggerHelper) throws AccessDeniedException, AccessRequiredException {
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
