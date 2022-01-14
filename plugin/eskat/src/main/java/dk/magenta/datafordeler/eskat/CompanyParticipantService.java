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
import dk.magenta.datafordeler.cvr.query.CompanyRecordQuery;
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
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Stream;

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
        //participantRecordQuery.setEffectToAfter(now);
        participantRecordQuery.setPage(page);
        participantRecordQuery.setPageSize(pageSize);


        try(Session session = sessionManager.getSessionFactory().openSession()) {

            List<ParticipantRecord> participantlist;
            CompanyRecord companyEntity = null;

            if(!"".equals(cvr)) {

                participantlist = QueryManager.getAllEntities(session, participantRecordQuery, ParticipantRecord.class);

                CompanyRecordQuery query = new CompanyRecordQuery();
                query.setCvrNumre(cvr);
                Stream<CompanyRecord> companyEntities = QueryManager.getAllEntitiesAsStream(session, query, CompanyRecord.class);
                companyEntity = companyEntities.findFirst().orElse(null);


            }

            participantlist = QueryManager.getAllEntities(session, participantRecordQuery, ParticipantRecord.class);





            List<ParticipantEntity> oList = new ArrayList<ParticipantEntity>();

            for(ParticipantRecord participant : participantlist) {

                //System.out.println(participant.getMetadata().);
                System.out.println("--------------------------------------------------");
                //System.out.println(participant.getBusinessKey());
                //System.out.println(participant.getUnitNumber());
                //System.out.println(participant.getNames().current().iterator().next().getName());
                CvrRecordPeriod period = participant.getValidity();
                //System.out.println(period);
                if(period!=null) {
                    System.out.println(period.getValidFrom());
                    System.out.println(period.getValidTo());
                }


                ParticipantEntity participantObject = new ParticipantEntity(null, participant.getBusinessKey()+"",
                        participant.getNames().current().stream().findFirst().get().getName(), null, null, null, null, null, null, null);



                if(!"".equals(cpr)) {

                    List<CompanyParticipantRelationRecord> relations = participant.getCompanyRelation().current();

                    for (CompanyParticipantRelationRecord cprs : relations) {
                        participantObject = new ParticipantEntity(null, participantObject.getCpr(),
                                participantObject.getPersonName(), null, null, null, null, null, null, null);

                        participantObject.setCvr(cprs.getRelationCompanyRecord().getCvrNumber()+"");
                        participantObject.setDriftform(cprs.getRelationCompanyRecord().getForm().stream().findFirst().get().getLongDescription());
                        CompanyStatusRecord statusExtract = cprs.getRelationCompanyRecord().getCompanyStatus().stream().findFirst().orElse(null);
                        participantObject.setStatus(statusExtract!=null ? statusExtract.getStatus() : "N/A");
                        participantObject.setCompanyname(cprs.getRelationCompanyRecord().getNames().stream().findFirst().get().getName());


                        System.out.println("CVR "+cprs.getRelationCompanyRecord().getCvrNumber());

                        oList.add(participantObject);
                    }
                } else if(!"".equals(cvr)) {

                    participantObject.setCvr(cvr);
                    participantObject.setDriftform(companyEntity.getCompanyForm().current().stream().findFirst().get().getLongDescription()+"");
                    participantObject.setStatus(companyEntity.getMetadata().getNewestStatus()!=null ? companyEntity.getMetadata().getNewestStatus().getStatusText():  "N/A");
                    participantObject.setCompanyname(companyEntity.getMetadata().getNewestName().stream().findFirst().get().getName());


                }
                oList.add(participantObject);

            }
            return oList;
        } catch (Exception e) {
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
