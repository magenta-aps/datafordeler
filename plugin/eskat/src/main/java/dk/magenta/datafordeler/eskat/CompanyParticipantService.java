package dk.magenta.datafordeler.eskat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import dk.magenta.datafordeler.core.database.Bitemporal;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cvr.access.CvrRolesDefinition;
import dk.magenta.datafordeler.cvr.query.CompanyRecordQuery;
import dk.magenta.datafordeler.cvr.query.ParticipantRecordQuery;
import dk.magenta.datafordeler.cvr.records.CompanyRecord;
import dk.magenta.datafordeler.cvr.records.ParticipantRecord;
import dk.magenta.datafordeler.cvr.service.ParticipantRecordService;
import dk.magenta.datafordeler.eskat.output.ParticipantObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.criteria.CriteriaQuery;
import javax.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/eskat/companyParticipantConnection/")
public class CompanyParticipantService {

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private DafoUserManager dafoUserManager;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private ObjectMapper objectMapper;

    private Logger log = LogManager.getLogger(CompanyParticipantService.class.getCanonicalName());


    @RequestMapping(
            //path = {"/{cpr}"},
            produces = {"application/json"}
    )


    //public List<String> getSingle(@RequestParam(value = "cpr",required=false, defaultValue = "") List<String> cprs, @RequestParam(value = "cvr",required=false, defaultValue = "") List<String> cvrs, HttpServletRequest request)

    public Collection<ParticipantObject> getRest(@RequestParam(value = "cpr",required=false, defaultValue = "") String cpr,
                                                 @RequestParam(value = "navn",required=false, defaultValue = "") String navn,
                                                 @RequestParam(value = "enhedsNummer",required=false, defaultValue = "") String enhedsNummer,
                                                 @RequestParam(value = "cvr",required=false, defaultValue = "") String cvr,
                                                 @RequestParam(value = "companyName",required=false, defaultValue = "") String companyName,
                                                 @RequestParam(value = "status",required=false, defaultValue = "") String status,
                                                 @RequestParam(value = "relationstartTimeBefore",required=false, defaultValue = "") String relationstartTimeBefore,
                                                 @RequestParam(value = "relationstartTimeAfter",required=false, defaultValue = "") String relationstartTimeAfter,
                                                 @RequestParam(value = "relationendTimeBefore",required=false, defaultValue = "") String relationendTimeBefore,
                                                 @RequestParam(value = "relationendTimeAfter",required=false, defaultValue = "") String relationendTimeAfter, HttpServletRequest request) throws DataFordelerException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(this.log, request, user);
        loggerHelper.info("Incoming request for cvr ownership with cpr " + cpr);
        this.checkAndLogAccess(loggerHelper);

        OffsetDateTime now = OffsetDateTime.now();

        ParticipantRecordQuery participantRecordQuery = new ParticipantRecordQuery();
        if(!"".equals(cpr)) {
            participantRecordQuery.setBusinessKey(cpr);
        }
        if(!"".equals(navn)) {
            participantRecordQuery.setNavn(navn);
        }
        if(!"".equals(enhedsNummer)) {
            participantRecordQuery.setEnhedsNummer(enhedsNummer);
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
        if(!"".equals(relationstartTimeBefore)) {
            participantRecordQuery.setRelationStartTimeBefore(BaseQuery.parseDateTime(relationstartTimeBefore));
        }
        if(!"".equals(relationstartTimeAfter)) {
            participantRecordQuery.setRelationStartTimeAfter(BaseQuery.parseDateTime(relationstartTimeAfter));
        }
        if(!"".equals(relationendTimeBefore)) {
            participantRecordQuery.setRelationEndTimeBefore(BaseQuery.parseDateTime(relationendTimeBefore));
        }
        if(!"".equals(relationendTimeAfter)) {
            participantRecordQuery.setRelationEndTimeAfter(BaseQuery.parseDateTime(relationendTimeAfter));
        }

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            this.applyFilter(session, Bitemporal.FILTER_EFFECTFROM_BEFORE, Bitemporal.FILTERPARAM_EFFECTFROM_BEFORE, now);
            this.applyFilter(session, Bitemporal.FILTER_EFFECTTO_AFTER, Bitemporal.FILTERPARAM_EFFECTTO_AFTER, now);

            List<ParticipantRecord> participantlist = QueryManager.getAllEntities(session, participantRecordQuery, ParticipantRecord.class);

            return participantlist.stream().map(f -> new ParticipantObject(f.getCompanyRelation().current().get(0).getRelationCompanyRecord().getCvrNumber()+"",
                    f.getBusinessKey()+"", f.getNames().current().iterator().next().getName(),
                    f.getCompanyRelation().current().get(0).getRelationCompanyRecord().getNames().iterator().next().getName()+"",
                    f.getCompanyRelation().current().get(0).getRelationCompanyRecord().getCompanyStatus().iterator().next().getStatus(),
                    dateConvert(f.getCompanyRelation().current().get(0).getRegistrationFrom()),dateConvert(f.getCompanyRelation().current().get(0).getRegistrationTo()),
                    dateConvert(f.getCompanyRelation().current().get(0).getRelationCompanyRecord().getCompanyStatus().iterator().next().getEffectFrom()),
                    dateConvert(f.getCompanyRelation().current().get(0).getRelationCompanyRecord().getCompanyStatus().iterator().next().getEffectTo()))).collect(Collectors.toList());


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

    private static String dateConvert(OffsetDateTime datetime) {
        if(datetime==null) {
            return null;
        } else {
            return datetime.format(formatter);
        }
    }
}
