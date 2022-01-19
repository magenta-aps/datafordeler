package dk.magenta.datafordeler.eskat;

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
import dk.magenta.datafordeler.cvr.records.*;
import dk.magenta.datafordeler.eskat.output.ParticipantEntity;
import dk.magenta.datafordeler.eskat.query.EskatCompanyRecordQuery;
import dk.magenta.datafordeler.eskat.query.EskatParticipantRecordQuery;
import dk.magenta.datafordeler.eskat.utils.DateConverter;
import dk.magenta.datafordeler.eskat.utils.ParticipantUnwrapper;
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
            path = "/dummy",
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    public Collection<ParticipantEntity> getRest(@RequestParam(value = "cpr",required=false, defaultValue = "") String cpr,
                                                 @RequestParam(value = "personNavn",required=false, defaultValue = "") String navn,
                                                 @RequestParam(value = "cvr",required=false, defaultValue = "") String cvr,
                                                 @RequestParam(value = "firmaNavn",required=false, defaultValue = "") String companyName,
                                                 @RequestParam(value = "status",required=false, defaultValue = "") String status,
                                                 @RequestParam(value = "relationstartTime.LTE",required=false, defaultValue = "") String relationstartTimeLTE,
                                                 @RequestParam(value = "relationstartTime.GTE",required=false, defaultValue = "") String relationstartTimeGTE,
                                                 @RequestParam(value = "relationendTime.LTE",required=false, defaultValue = "") String relationendTimeLTE,
                                                 @RequestParam(value = "relationendTime.GTE",required=false, defaultValue = "") String relationendTimeGTE,
                                                 @RequestParam(value = "page",required=false, defaultValue = "1") Integer page,
                                                 @RequestParam(value = "pageSize",required=false, defaultValue = "10") Integer pageSize,
                                                 HttpServletRequest request) throws DataFordelerException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(this.log, request, user);
        loggerHelper.info("Incoming request for cvr ownership with cpr " + cpr);
        this.checkAndLogAccess(loggerHelper);

        OffsetDateTime now = OffsetDateTime.now();

        EskatParticipantRecordQuery participantRecordQuery = new EskatParticipantRecordQuery();
        if(!"".equals(cpr)) {
            participantRecordQuery.setBusinessKey(cpr);
        }
        if(!"".equals(navn)) {
            participantRecordQuery.setNavn(navn);
        }
        if(!"".equals(cvr)) {
            participantRecordQuery.setCvrnumber(cvr);
        }
        if(!"".equals(companyName)) {
            participantRecordQuery.setCompanyNames(companyName);
        }
        if("Aktiv".equals(status)) {
            participantRecordQuery.setStatuses(Arrays.asList("NORMAL", "Aktiv", "Fremtid"));
        }
        if("!Aktiv".equals(status)) {
            participantRecordQuery.setStatuses(Arrays.asList("Ikke Aktiv"));
        }

        if(!"".equals(relationstartTimeLTE)) {
            participantRecordQuery.setRelationStartTimeLTE(DateConverter.parseDate(relationstartTimeLTE));
        }
        if(!"".equals(relationstartTimeGTE)) {
            participantRecordQuery.setRelationStartTimeGTE(DateConverter.parseDate(relationstartTimeGTE));
        }
        if(!"".equals(relationendTimeLTE)) {
            participantRecordQuery.setRelationEndTimeLTE(DateConverter.parseDate(relationendTimeLTE));
        }
        if(!"".equals(relationendTimeGTE)) {
            participantRecordQuery.setRelationEndTimeGTE(DateConverter.parseDate(relationendTimeGTE));
        }

        participantRecordQuery.setRegistrationFromBefore(now);
        participantRecordQuery.setRegistrationToAfter(now);
        participantRecordQuery.setEffectFromBefore(now);
        participantRecordQuery.setEffectToAfter(now);
        participantRecordQuery.setPage(page);
        participantRecordQuery.setPageSize(pageSize);


        try(Session session = sessionManager.getSessionFactory().openSession()) {
            this.applyFilter(session, Bitemporal.FILTER_EFFECTFROM_BEFORE, Bitemporal.FILTERPARAM_EFFECTFROM_BEFORE, now);
            this.applyFilter(session, Bitemporal.FILTER_EFFECTTO_AFTER, Bitemporal.FILTERPARAM_EFFECTTO_AFTER, now);

            List<ParticipantRecord> participantlist = QueryManager.getAllEntities(session, participantRecordQuery, ParticipantRecord.class);

            List<ParticipantEntity> oList = new ArrayList<ParticipantEntity>();

            for(ParticipantRecord participant : participantlist) {
                List<CompanyParticipantRelationRecord> relations =  participant.getCompanyRelation().current();

                oList.addAll(ParticipantUnwrapper.CompanyParticipantRelationRecord(relations, participant.getBusinessKey()+"", participant.getNames().current().iterator().next().getName()));
            }
            return oList;
        } catch (Exception e) {
            throw new DataStreamException(e);
        }
    }


    @RequestMapping(
            path = "/search",
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    public Collection<ParticipantEntity> getRestD(@RequestParam(value = "cpr",required=false, defaultValue = "") String cpr,
                                                 @RequestParam(value = "personNavn",required=false, defaultValue = "") String personNavn,
                                                 @RequestParam(value = "cvr",required=false, defaultValue = "") String cvr,
                                                 @RequestParam(value = "firmaNavn",required=false, defaultValue = "") String companyName,
                                                 @RequestParam(value = "status",required=false, defaultValue = "") String status,
                                                 @RequestParam(value = "companystartTime.LTE",required=false, defaultValue = "") String companystartTimeLTE,
                                                 @RequestParam(value = "companystartTime.GTE",required=false, defaultValue = "") String companystartTimeGTE,
                                                 @RequestParam(value = "companyendTime.LTE",required=false, defaultValue = "") String companyendTimeLTE,
                                                 @RequestParam(value = "companyendTime.GTE",required=false, defaultValue = "") String companyendTimeGTE,
                                                 @RequestParam(value = "page",required=false, defaultValue = "1") Integer page,
                                                 @RequestParam(value = "pageSize",required=false, defaultValue = "10") Integer pageSize,
                                                 HttpServletRequest request) throws DataFordelerException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(this.log, request, user);
        loggerHelper.info("Incoming request for cvr ownership with cpr " + cpr);
        this.checkAndLogAccess(loggerHelper);

        OffsetDateTime now = OffsetDateTime.now();

        EskatParticipantRecordQuery participantRecordQuery = new EskatParticipantRecordQuery();
        EskatCompanyRecordQuery companyRecordQuery = new EskatCompanyRecordQuery();
        if(!"".equals(cpr)) {
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
        if("Aktiv".equals(status)) {
            companyRecordQuery.setCompanyStatus(Arrays.asList("NORMAL", "Aktiv", "Fremtid"));
        }
        if("!Aktiv".equals(status)) {
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

        participantRecordQuery.setRegistrationFromBefore(now);
        participantRecordQuery.setRegistrationToAfter(now);
        participantRecordQuery.setEffectFromBefore(now);
        participantRecordQuery.setEffectToAfter(now);
        participantRecordQuery.setPage(page);
        participantRecordQuery.setPageSize(pageSize);

        try(Session session = sessionManager.getSessionFactory().openSession()) {

            List<ParticipantEntity> oList = new ArrayList<ParticipantEntity>();

            if(companyRecordQuery.isSearchSet()) {
                List<CompanyRecord> companyEntities = QueryManager.getAllEntities(session, companyRecordQuery, CompanyRecord.class);
                for(CompanyRecord company : companyEntities) {
                    List<CompanyParticipantRelationRecord> participants = company.getParticipants().current();
                    for(CompanyParticipantRelationRecord participant : participants) {
                        FormRecord form = company.getCompanyForm().current().stream().findFirst().orElse(null);
                        StatusRecord statusRecord = company.getStatus().current().stream().findFirst().orElse(null);
                        LifecycleRecord lifeCycle = company.getLifecycle().current().stream().findFirst().orElse(null);
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
                                company.getNames().current().stream().findFirst().get().getName(),
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
                                participant.getNames().current().stream().findFirst().get().getName(),
                                relationCompany.getNames().stream().findFirst().get().getName(),
                                relationCompany.getForm().stream().findFirst().get().getLongDescription(),
                                statusRecord!=null?statusRecord.getStatusText() : null,
                                period!=null?DateConverter.dateConvert(period.getValidFrom()):null,
                                period!=null?DateConverter.dateConvert(period.getValidTo()):null,
                                DateConverter.dateConvert(relationCompany.getLifecycle().stream().findFirst().get().getValidFrom()),
                                DateConverter.dateConvert(relationCompany.getLifecycle().stream().findFirst().get().getValidTo()));
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
        if(attRecord!=null&&attRecord.size()>0) {
            Set<AttributeValueRecord> attributes = attRecord.stream().findFirst().orElse(null).getValues();
            if(attributes!=null&&attributes.size()>0) {
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
