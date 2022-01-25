package dk.magenta.datafordeler.eskat;

import dk.magenta.datafordeler.core.database.Bitemporal;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.InvalidParameterException;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cvr.access.CvrRolesDefinition;
import dk.magenta.datafordeler.cvr.records.*;
import dk.magenta.datafordeler.eskat.output.ParticipantEntity;
import dk.magenta.datafordeler.eskat.query.EskatCompanyRecordQuery;
import dk.magenta.datafordeler.eskat.query.EskatParticipantRecordQuery;
import dk.magenta.datafordeler.eskat.utils.DateConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.*;

@RestController
@RequestMapping("/eskat/companyParticipantConnection/1/rest")
public class CompanyParticipantService {

    @Autowired
    private DafoUserManager dafoUserManager;

    @Autowired
    private SessionManager sessionManager;

    private Logger log = LogManager.getLogger(CompanyParticipantService.class.getCanonicalName());


    @RequestMapping(
            path = "/search",
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    public Collection<ParticipantEntity> getRestD(@RequestParam(value = "cpr",required=false, defaultValue = "") String cpr,
                                                 @RequestParam(value = "personNavn",required=false, defaultValue = "") String personNavn,
                                                 @RequestParam(value = "cvr",required=false, defaultValue = "") String cvr,
                                                 @RequestParam(value = "firmaNavn",required=false, defaultValue = "") String companyName,
                                                 @RequestParam(value = "aktivitet",required=false, defaultValue = "") String aktivitet,
                                                 @RequestParam(value = "companystartTime.LTE",required=false, defaultValue = "") String companystartTimeLTE,
                                                 @RequestParam(value = "companystartTime.GTE",required=false, defaultValue = "") String companystartTimeGTE,
                                                 @RequestParam(value = "companyendTime.LTE",required=false, defaultValue = "") String companyendTimeLTE,
                                                 @RequestParam(value = "companyendTime.GTE",required=false, defaultValue = "") String companyendTimeGTE,
                                                 @RequestParam(value = "page",required=false, defaultValue = "1") Integer page,
                                                 @RequestParam(value = "pageSize",required=false, defaultValue = "10") Integer pageSize,
                                                 HttpServletRequest request) throws DataFordelerException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(this.log, request, user);
        loggerHelper.info("Incoming request CompanyParticipantService ");
        this.checkAndLogAccess(loggerHelper);

        OffsetDateTime now = OffsetDateTime.now();

        EskatParticipantRecordQuery participantRecordQuery = new EskatParticipantRecordQuery();
        EskatCompanyRecordQuery companyRecordQuery = new EskatCompanyRecordQuery();
        if(!cpr.isEmpty()) {
            participantRecordQuery.setBusinessKey(cpr);
        }
        if(!"".equals(personNavn)) {
            participantRecordQuery.setNavn(personNavn);
        }
        if(!"".equals(cvr)) {
            companyRecordQuery.setCvrNumre(cvr);
        }
        if(!"".equals(companyName)) {
            companyRecordQuery.setVirksomhedsnavn(companyName);
        }
        if("Aktiv".equals(aktivitet)) {
            companyRecordQuery.setCompanyStatus(Arrays.asList("NORMAL", "Aktiv", "Fremtid"));
        }
        if("!Aktiv".equals(aktivitet)) {
            companyRecordQuery.setCompanyStatus(Arrays.asList("Ikke Aktiv", "UNDER REKONSTRUKTION", "OPLØST EFTER KONKURS",
            "UNDER KONKURS", "TVANGSOPLØST", "UNDER FRIVILLIG LIKVIDATION", "UNDER REASSUMERING", "OPLØST EFTER SPALTNING",
            "UDEN RETSVIRKNING", "SLETTET", "UNDER TVANGSOPLØSNING", "OPLØST EFTER ERKLÆRING", "OPLØST EFTER FRIVILLIG LIKVIDATION", "OPLØST EFTER FUSION"));
        }

        if(!"".equals(companystartTimeLTE)) {
            companyRecordQuery.setCompanyStartDateLTE(DateConverter.parseDate(companystartTimeLTE));
        }
        if(!"".equals(companystartTimeGTE)) {
            companyRecordQuery.setCompanyStartDateGTE(DateConverter.parseDate(companystartTimeGTE));
        }
        if(!"".equals(companyendTimeLTE)) {
            companyRecordQuery.setCompanyEndDateLTE(DateConverter.parseDate(companyendTimeLTE));
        }
        if(!"".equals(companyendTimeGTE)) {
            companyRecordQuery.setCompanyEndDateGTE(DateConverter.parseDate(companyendTimeGTE));
        }

        companyRecordQuery.setRegistrationFromBefore(now);
        companyRecordQuery.setRegistrationToAfter(now);
        companyRecordQuery.setEffectFromBefore(now);
        companyRecordQuery.setPage(page);
        companyRecordQuery.setPageSize(pageSize);

        participantRecordQuery.setRegistrationAt(now);
        participantRecordQuery.setEffectAt(now);
        participantRecordQuery.setPage(page);
        participantRecordQuery.setPageSize(pageSize);
        if(participantRecordQuery.getPageSize()>100) {
            throw new InvalidParameterException("pageSize");
        }

        try(Session session = sessionManager.getSessionFactory().openSession()) {

            List<ParticipantEntity> oList = new ArrayList<ParticipantEntity>();

            if(companyRecordQuery.isSearchSet()) {
                List<CompanyRecord> companyEntities = QueryManager.getAllEntities(session, companyRecordQuery, CompanyRecord.class);
                for(CompanyRecord company : companyEntities) {
                    List<CompanyParticipantRelationRecord> participants = company.getParticipants().current();
                    for(CompanyParticipantRelationRecord participant : participants) {
                        FormRecord form = company.getCompanyForm().getLast(true, false);
                        StatusRecord statusRecord = company.getStatus().getLast(true, false);
                        LifecycleRecord lifeCycle = company.getLifecycle().getLast(true, false);
                        String companyFrom = null;
                        String companyTo = null;
                        if(lifeCycle!=null) {
                            companyFrom = DateConverter.dateConvert(lifeCycle.getValidFrom());
                            companyTo = DateConverter.dateConvert(lifeCycle.getValidTo());
                        }

                        CvrRecordPeriod period = findValidity(participant);
                        ParticipantEntity participantObject = new ParticipantEntity(company.getCvrNumberString(),
                                participant.getParticipantRecord()!=null? Long.toString(participant.getRelationParticipantRecord().getBusinessKey()) : null,
                                participant.getRelationParticipantRecord().getNames().stream().findFirst().get().getName(),
                                company.getNames().getLast(true, false).getName(),
                                form!=null?form.getLongDescription() : null,
                                statusRecord!=null?statusRecord.getStatusText() : null,
                                period!=null?DateConverter.dateConvert(period.getValidFrom()):null,
                                period!=null?DateConverter.dateConvert(period.getValidTo()):null,
                                companyFrom, companyTo);
                        oList.add(participantObject);
                    }
                }

            } else if(participantRecordQuery.isSearchSet()) {
                List<ParticipantRecord> participantlist = QueryManager.getAllEntities(session, participantRecordQuery, ParticipantRecord.class);
                for(ParticipantRecord participant : participantlist) {
                    List<CompanyParticipantRelationRecord> relations =  participant.getCompanyRelation().current();
                    for(CompanyParticipantRelationRecord participantRelation : relations) {
                        RelationCompanyRecord relationCompany = participantRelation.getRelationCompanyRecord();

                        StatusRecord statusRecord = relationCompany.getStatus().stream().findFirst().orElse(null);
                        CvrRecordPeriod period = findValidity(participantRelation);
                        ParticipantEntity participantObject = new ParticipantEntity(
                                Long.toString(relationCompany.getCvrNumber()),
                                participant.getBusinessKey()!=null? Long.toString(participant.getBusinessKey()) : null,
                                participant.getNames().getLast(true, false).getName(),
                                relationCompany.getNames().getLast(true, false).getName(),
                                relationCompany.getForm().getLast(true, false).getLongDescription(),
                                statusRecord!=null?statusRecord.getStatusText() : null,
                                period!=null?DateConverter.dateConvert(period.getValidFrom()):null,
                                period!=null?DateConverter.dateConvert(period.getValidTo()):null,
                                DateConverter.dateConvert(relationCompany.getLifecycle().getLast(true, false).getValidFrom()),
                                DateConverter.dateConvert(relationCompany.getLifecycle().getLast(true, false).getValidTo()));
                        oList.add(participantObject);
                    }
                }
            }

            return oList;
        } catch (Exception e) {
            throw new DataStreamException(e);
        }
    }

    /**
     * Find the validity of the current participants relation.
     * The correct element is found by selecting the last organization, which is the newest one, after that the validity of any value is selected
     * @param participantRelation
     * @return
     */
    private CvrRecordPeriod findValidity(CompanyParticipantRelationRecord participantRelation) {
        Set<AttributeRecord> attRecord = participantRelation.getOrganizations().stream().reduce((first, second) -> second).get().getAttributes();
        if(attRecord!=null && !attRecord.isEmpty()) {
            Set<AttributeValueRecord> attributes = attRecord.stream().findFirst().orElse(null).getValues();
            if(attributes!=null && !attributes.isEmpty()) {
                return attributes.stream().findFirst().orElse(null).getValidity();
            }
        }
        return null;
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
