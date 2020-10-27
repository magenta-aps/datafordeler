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
import dk.magenta.datafordeler.cvr.query.ParticipantRecordQuery;
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

    /*

    40387781

    --FINDING ALL PARTICIPANTS

    SELECT *
  FROM [Datafordeler_Test].[dbo].[cvr_record_company] comp
  JOIN [Datafordeler_Test].[dbo].[cvr_record_form] form ON (comp.id = form.companyRecord_id)
  JOIN [Datafordeler_Test].[dbo].[cvr_form] formt ON (formt.id = form.companyForm_id)
  JOIN [Datafordeler_Test].[dbo].[cvr_record_company_participant_relation] compRel ON (compRel.companyRecord_id = comp.id)
  JOIN [Datafordeler_Test].[dbo].[cvr_record_company_participant_relation_participant] rpar ON (compRel.relationParticipantRecord_id = rpar.id)
  JOIN [Datafordeler_Test].[dbo].[cvr_record_company_participant_relation_organization] pRelOrg ON (pRelOrg.companyParticipantRelationRecord_id = compRel.id)
  JOIN [Datafordeler_Test].[dbo].[cvr_record_attribute] att ON (att.organizationRecord_id = pRelOrg.id)
  JOIN [Datafordeler_Test].[dbo].[cvr_record_attribute_value] attv ON (att.id = attv.attribute_id)

  WHERE companyFormCode = 10 OR
	companyFormCode = 30 OR
	companyFormCode = 50
     */

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

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            CompanyRecordQuery query = new CompanyRecordQuery();
            query.setCvrNumre(cvrNummer);
            //Get the company, there can only be 0-1
            List<CompanyRecord> companyrecords = QueryManager.getAllEntities(session, query, CompanyRecord.class);
            if (companyrecords.isEmpty()) {
                loggerHelper.urlResponsePersistablelogs(HttpStatus.NOT_FOUND.value(), "CvrCompanyOwnerHistory done");
                throw new HttpNotFoundException("Company not found " + cvrNummer);
            }
            CompanyRecord companyrecord = companyrecords.get(0);

            FormRecord formRecord = companyrecord.getMetadata().getNewestForm().stream().findFirst().orElse(null);
            if(formRecord==null) {
                loggerHelper.urlResponsePersistablelogs(HttpStatus.FORBIDDEN.value(), "CvrCompanyOwnerHistory done");
                throw new AccessDeniedException("The requested company \""+cvrNummer+"\" not active");
            }
            String formCodeString = formRecord.getCompanyFormCode();
            Integer formCode = Integer.parseInt(formCodeString);
            root.put("virksomhedsformkode", formRecord.getCompanyFormCode());

            root.put("shortDescription", formRecord.getShortDescription());
            root.put("longDescription", formRecord.getLongDescription());

            //It is legally forbidden to supply this information from companies with other formcodes then 10, 30 and 50
            if (formCode != 10 && formCode != 30 && formCode != 50) {
                loggerHelper.urlResponsePersistablelogs(HttpStatus.FORBIDDEN.value(), "CvrCompanyOwnerHistory done");
                throw new AccessDeniedException("The requested company \""+cvrNummer+"\" is not of a formcode where this request is accepted. Accepted form codes are 10, 30 and 50");
            }

            Set<CompanyParticipantRelationRecord> participants = companyrecord.getParticipants();

            ObjectMapper mapper = new ObjectMapper();
            List<CompanyOwnerItem> personalOwnerList = new ArrayList<CompanyOwnerItem>();
            List<CompanyOwnerItem> companyOwnerList = new ArrayList<CompanyOwnerItem>();

            for(CompanyParticipantRelationRecord participant : participants) {
                String from = null;
                String to = null;
                Number deltagerPnr = null;

                Long participantNumber = participant.getParticipantUnitNumber();

                //We expose all owners of the company
                if(participant.getOrganizations().stream().anyMatch(o -> o.getMainType().equals("FULDT_ANSVARLIG_DELTAGERE"))) {

                    if("PERSON".equals(participant.getRelationParticipantRecord().getUnitType())) {
                        //A company can be owned by a person, then we need to find the persons cpr-number from live-lookup
                        try {

                            ParticipantRecordQuery participantRecordQuery = new ParticipantRecordQuery();
                            participantRecordQuery.setEnhedsNummer(participantNumber.toString());
                            List<ParticipantRecord> participantList = QueryManager.getAllEntities(session, participantRecordQuery, ParticipantRecord.class);
                            if(participantList.size()>0 && participantList.get(0)!= null && participantList.get(0).getBusinessKey()!=null) {
                                deltagerPnr = participantList.get(0).getBusinessKey();
                            } else {
                                deltagerPnr = directLookup.participantLookup(participantNumber.toString()).getBusinessKey();
                            }

                            //deltagerPnr = participantRecord.getBusinessKey();
                            Iterator<OrganizationRecord> orgRecordList = participant.getOrganizations().iterator();
                            while (orgRecordList.hasNext()) {
                                OrganizationRecord orgRecord = orgRecordList.next();
                                Iterator<AttributeRecord> attributeList = orgRecord.getAttributes().iterator();
                                while (attributeList.hasNext()) {
                                    AttributeRecord attribute = attributeList.next();

                                    Iterator<AttributeValueRecord> attributeValueList = attribute.getValues().iterator();
                                    while (attributeValueList.hasNext()) {
                                        AttributeValueRecord attValue = attributeValueList.next();
                                        from = Optional.ofNullable(attValue.getValidFrom()).map(o -> o.toString()).orElse(null);
                                        to = Optional.ofNullable(attValue.getValidTo()).map(o -> o.toString()).orElse(null);
                                        CompanyOwnerItem ownerItem = new CompanyOwnerItem(participantNumber, null, String.format("%010d", deltagerPnr), from, to);
                                        personalOwnerList.add(ownerItem);
                                    }
                                }
                            }
                        } catch(Exception e) {
                            throw new InvalidReferenceException("Information for participant could not be found " + participantNumber.toString());
                        }
                    } else if("VIRKSOMHED".equals(participant.getRelationParticipantRecord().getUnitType())) {
                        //A company can be owned by a company, then we need to find the companys cvr-number from the database
                        Iterator<OrganizationRecord> orgRecord = participant.getOrganizations().iterator();
                        if (orgRecord.hasNext()) {
                            AttributeValueRecord attributes = orgRecord.next().getAttributes().iterator().next().getValues().iterator().next();
                            from = Optional.ofNullable(attributes.getValidFrom()).map(o -> o.toString()).orElse(null);
                            to = Optional.ofNullable(attributes.getValidTo()).map(o -> o.toString()).orElse(null);
                        }
                        CompanyOwnerItem ownerItem = new CompanyOwnerItem(participantNumber,  participant.getRelationParticipantRecord().getBusinessKey(), null, from, to);
                        companyOwnerList.add(ownerItem);
                    }
                }
            }
            root.putArray("pnrs", mapper.valueToTree(personalOwnerList));
            root.putArray("cvrs", mapper.valueToTree(companyOwnerList));

            loggerHelper.urlResponsePersistablelogs(HttpStatus.OK.value(), "CvrCompanyOwnerHistory done");
            return objectMapper.writeValueAsString(root.getNode());
        }
    }

    protected void checkAndLogAccess(LoggerHelper loggerHelper) throws AccessDeniedException {
        try {
            loggerHelper.getUser().checkHasSystemRole(CvrRolesDefinition.READ_CVR_ROLE);
            loggerHelper.getUser().checkHasSystemRole(CprRolesDefinition.READ_CPR_ROLE);
        } catch (AccessDeniedException e) {
            loggerHelper.info("Access denied: " + e.getMessage());
            throw (e);
        }
    }
}
