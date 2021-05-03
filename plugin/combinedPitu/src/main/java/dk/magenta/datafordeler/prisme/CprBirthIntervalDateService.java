package dk.magenta.datafordeler.prisme;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.MonitorService;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.exception.InvalidCertificateException;
import dk.magenta.datafordeler.core.exception.InvalidTokenException;
import dk.magenta.datafordeler.core.fapi.Envelope;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.person.data.AddressDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.BirthTimeDataRecord;
import dk.magenta.datafordeler.geo.data.accessaddress.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Get a list of persons from a specific address-area, and with a specific interval of birth
 */
@RestController
@RequestMapping("/combined/cpr/birthIntervalDate/1")
public class CprBirthIntervalDateService {

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    SessionManager sessionManager;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private DafoUserManager dafoUserManager;

    @Autowired
    private CprPlugin cprPlugin;

    @Autowired
    protected MonitorService monitorService;

    private Logger log = LogManager.getLogger(CprBirthIntervalDateService.class.getCanonicalName());

    @PostConstruct
    public void init() {
    }

    @GetMapping("/search")
    public Envelope findAll(HttpServletRequest request, @RequestParam MultiValueMap<String, String> requestParams, HttpServletResponse response) throws AccessDeniedException, InvalidTokenException, InvalidCertificateException{

        String birthAfter = requestParams.getFirst("birth.GTE");
        LocalDateTime birthAfterTS=null;
        if(birthAfter!=null) {
            birthAfterTS = dk.magenta.datafordeler.core.fapi.Query.parseDateTime(birthAfter).toLocalDateTime();
        }

        String birthBefore = requestParams.getFirst("birth.LTE");
        LocalDateTime birthBeforeTS=null;
        if(birthBefore!=null) {
            birthBeforeTS = dk.magenta.datafordeler.core.fapi.Query.parseDateTime(birthBefore).toLocalDateTime();
        }

        String pageSize = requestParams.getFirst("pageSize");
        String page = requestParams.getFirst("page");
        String  municipalitycode = requestParams.getFirst("municipalitycode");
        String  localitycode = requestParams.getFirst("localitycode");

        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);

        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.urlInvokePersistablelogs("CprBirthIntervalDateService");
        this.checkAndLogAccess(loggerHelper);

        OffsetDateTime now = OffsetDateTime.now();

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            String hql = "SELECT DISTINCT personEntity.personnummer, accessAddressLocalityRecord.code, birthDataRecord.birthDatetime  " +
                    "FROM "+ PersonEntity.class.getCanonicalName() + " personEntity "+
                    "JOIN "+ BirthTimeDataRecord.class.getCanonicalName() + " birthDataRecord ON birthDataRecord."+BirthTimeDataRecord.DB_FIELD_ENTITY+"=personEntity."+"id"+" "+
                    "JOIN "+ AddressDataRecord.class.getCanonicalName() + " addressDataRecord ON addressDataRecord."+AddressDataRecord.DB_FIELD_ENTITY+"=personEntity."+"id"+" "+

                    "JOIN "+ AccessAddressRoadRecord.class.getCanonicalName() + " accessAddressRoadRecord ON accessAddressRoadRecord."+AccessAddressRoadRecord.DB_FIELD_MUNICIPALITY_CODE+"=addressDataRecord."+AddressDataRecord.DB_FIELD_MUNICIPALITY_CODE+
                        " AND accessAddressRoadRecord."+AccessAddressRoadRecord.DB_FIELD_ROAD_CODE+"=addressDataRecord."+AddressDataRecord.DB_FIELD_ROAD_CODE+" "+
                    "JOIN "+ AccessAddressEntity.class.getCanonicalName() + " accessAddressEntity ON accessAddressRoadRecord."+AccessAddressRoadRecord.DB_FIELD_ENTITY+"=accessAddressEntity."+"id"+" "+
                    "JOIN "+ AccessAddressLocalityRecord.class.getCanonicalName() + " accessAddressLocalityRecord ON accessAddressLocalityRecord."+AccessAddressLocalityRecord.DB_FIELD_ENTITY+"=accessAddressEntity."+"id"+" "+
                    "";

            String condition = " WHERE addressDataRecord." + CprBitemporalRecord.DB_FIELD_EFFECT_TO +" IS null " +
                    "AND addressDataRecord." + CprBitemporalRecord.DB_FIELD_REGISTRATION_TO +" IS null AND "+
                    "AND addressDataRecord." + CprBitemporalRecord.DB_FIELD_UNDONE +" = 0 AND "+
            " birthDataRecord.birthDatetime <= :btb AND birthDataRecord.birthDatetime >= :bta AND ";
            if(localitycode!=null && municipalitycode!=null) {
                condition += String.format("accessAddressLocalityRecord.code = '%s' AND addressDataRecord.municipalityCode = '%s'", localitycode, municipalitycode);
            } else if(localitycode!=null) {
                condition += String.format("accessAddressLocalityRecord.code = '%s'", localitycode);
            } else if(municipalitycode!=null) {
                condition += String.format("addressDataRecord.municipalityCode = '%s'", municipalitycode);
            }

            hql += condition;

            Query query = session.createQuery(hql);
            query.setParameter("btb", birthBeforeTS);
            query.setParameter("bta", birthAfterTS);

            if(pageSize != null) {
                query.setMaxResults(Integer.valueOf(pageSize));
            } else {
                query.setMaxResults(10);
            }
            if(page != null) {
                int pageIndex = (Integer.valueOf(page)-1)*query.getMaxResults();
                query.setFirstResult(pageIndex);
            } else {
                query.setFirstResult(0);
            }

            List<Object[]> resultList = query.getResultList();

            List<PersonLocationObject> personLocationObjectList = resultList.stream().map(ob -> new PersonLocationObject(ob[0].toString(), ob[1].toString(), ((LocalDateTime)ob[2]).format(formatter))).collect(Collectors.toList());

            Envelope envelope = new Envelope();
            envelope.setRequestTimestamp(user.getCreationTime());
            envelope.setUsername(user.toString());

            envelope.setPageSize(query.getMaxResults());
            envelope.setPage(query.getFirstResult()+1);
            envelope.setPath(request.getServletPath());
            envelope.setResponseTimestamp(OffsetDateTime.now());

            envelope.setResults(personLocationObjectList);
            setHeaders(response);
            loggerHelper.urlResponsePersistablelogs(HttpStatus.OK.value(), "CprBirthIntervalDateService done");
            return envelope;
        }

    }

    private static void setHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Content-Type", "application/json; charset=utf-8");
        response.setStatus(200);
    }


    protected void checkAndLogAccess(LoggerHelper loggerHelper) throws AccessDeniedException {
        try {
            loggerHelper.getUser().checkHasSystemRole(CprRolesDefinition.READ_CPR_ROLE);
        }
        catch (AccessDeniedException e) {
            loggerHelper.info("Access denied: " + e.getMessage());
            throw(e);
        }
    }
}
