package dk.magenta.datafordeler.combined;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.MonitorService;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.*;
import dk.magenta.datafordeler.core.fapi.Envelope;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.person.data.*;
import dk.magenta.datafordeler.geo.data.accessaddress.AccessAddressEntity;
import dk.magenta.datafordeler.geo.data.accessaddress.AccessAddressLocalityRecord;
import dk.magenta.datafordeler.geo.data.accessaddress.AccessAddressRoadRecord;
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
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Get a list of persons from a specific address-area, and with a specific interval of birth
 */
@RestController
@RequestMapping("/combined/cpr/voterlist/1")
public class CprVoterService {

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    SessionManager sessionManager;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private DafoUserManager dafoUserManager;

    @Autowired
    protected MonitorService monitorService;

    private Logger log = LogManager.getLogger(CprVoterService.class.getCanonicalName());

    @PostConstruct
    public void init() {

    }

    @GetMapping("/landstingsvalg")
    public Envelope findAll(HttpServletRequest request, @RequestParam MultiValueMap<String, String> requestParams, HttpServletResponse response) throws AccessDeniedException, MissingParameterException, InvalidTokenException, InvalidCertificateException, InvalidParameterException, InvalidClientInputException {

        ParameterMap parameters = new ParameterMap(requestParams);

        LocalDateTime voteDate = parameters.getLocalDateTime("valgdato");
        LocalDateTime voteDateMinus18Years = voteDate.minusYears(18);
        OffsetDateTime voteDateMinus6Month = voteDate.minusMonths(6).atOffset(ZoneOffset.UTC);

        LocalDateTime birthAfterTS = parameters.getLocalDateTime("foedsel.GTE");
        LocalDateTime birthBeforeTSLimit = parameters.getLocalDateTime("foedsel.LTE");
        LocalDateTime birthBeforeTS = voteDateMinus18Years;
        if (birthBeforeTSLimit != null) {
            birthBeforeTS = birthBeforeTSLimit.isBefore(voteDateMinus18Years) ? birthBeforeTSLimit : voteDateMinus18Years;
        }

        int limit = parameters.getInt("pageSize", 100, 1, 100);
        int page = parameters.getInt("page", 1, 1, Integer.MAX_VALUE);
        int offset = (page - 1) * limit;

        int municipalityCode = parameters.getInt("kommune_kode", 0, 0, 999);
        String localityCode = parameters.getFirst("lokalitet_kode");

        String order_by = parameters.getFirst("order_by");

        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);

        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.urlInvokePersistablelogs("CprBirthIntervalDateService");
        this.checkAndLogAccess(loggerHelper);

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder();
            hql.append("SELECT DISTINCT personEntity.personnummer, accessAddressLocalityRecord.code, birthDataRecord.birthDatetime , nameDataRecord.lastName " +
                    " FROM " + PersonEntity.class.getCanonicalName() + " personEntity " +
                    " JOIN " + BirthTimeDataRecord.class.getCanonicalName() + " birthDataRecord ON birthDataRecord." + BirthTimeDataRecord.DB_FIELD_ENTITY + "=personEntity." + "id" +
                    " JOIN " + AddressDataRecord.class.getCanonicalName() + " addressDataRecord ON addressDataRecord." + AddressDataRecord.DB_FIELD_ENTITY + "=personEntity." + "id" +
                    " JOIN " + NameDataRecord.class.getCanonicalName() + " nameDataRecord ON nameDataRecord." + NameDataRecord.DB_FIELD_ENTITY + "=personEntity." + "id" +
                    " JOIN " + CitizenshipDataRecord.class.getCanonicalName() + " citizenDataRecord ON citizenDataRecord." + CitizenshipDataRecord.DB_FIELD_ENTITY + "=personEntity." + "id" +
                    " LEFT OUTER JOIN " + GuardianDataRecord.class.getCanonicalName() + " guardianDataRecord ON guardianDataRecord." + GuardianDataRecord.DB_FIELD_ENTITY + "=personEntity." + "id" +

                    //TODO: The personal adresses is joined with adresses in GAR. It is important to inform customers about quality-issues if GAR is the only adress-date used
                    " JOIN " + AccessAddressRoadRecord.class.getCanonicalName() + " accessAddressRoadRecord ON accessAddressRoadRecord." + AccessAddressRoadRecord.DB_FIELD_MUNICIPALITY_CODE + "=addressDataRecord." + AddressDataRecord.DB_FIELD_MUNICIPALITY_CODE +
                    " AND accessAddressRoadRecord." + AccessAddressRoadRecord.DB_FIELD_ROAD_CODE + "=addressDataRecord." + AddressDataRecord.DB_FIELD_ROAD_CODE +
                    " JOIN " + AccessAddressEntity.class.getCanonicalName() + " accessAddressEntity ON accessAddressRoadRecord." + AccessAddressRoadRecord.DB_FIELD_ENTITY + "=accessAddressEntity." + "id" +
                    " JOIN " + AccessAddressLocalityRecord.class.getCanonicalName() + " accessAddressLocalityRecord ON accessAddressLocalityRecord." + AccessAddressLocalityRecord.DB_FIELD_ENTITY + "=accessAddressEntity." + "id");

            hql.append(" WHERE addressDataRecord." + CprBitemporalRecord.DB_FIELD_EFFECT_TO + " IS null " +
                    "AND addressDataRecord." + CprBitemporalRecord.DB_FIELD_EFFECT_FROM + "  <= :halfyear " + //TODO: There is no need to optimize this now, we still do not know if the output is a json-based webservice og *.csv files as VoteListDataService, but we know that there can be more then one adress within the interval
                    // If a *.CSV file is needed, I would get all adresses in the interval within hibernate model and find out is any of them is outside the requested municipality-code
                    "AND addressDataRecord." + CprBitemporalRecord.DB_FIELD_REGISTRATION_TO + " IS null " +
                    "AND addressDataRecord." + CprBitemporalRecord.DB_FIELD_UNDONE + " = 0 " +
                    "AND citizenDataRecord.countryCode = 5100 " +
                    "AND guardianDataRecord." + GuardianDataRecord.DB_FIELD_ENTITY + " IS null ");

            if (birthAfterTS != null) {
                hql.append("AND (birthDataRecord.birthDatetime >= :bta) ");
            }

            hql.append("AND birthDataRecord.birthDatetime <= :btb ");

            if (localityCode != null) {
                hql.append("AND (accessAddressLocalityRecord.code = :locality) ");
            }

            hql.append("AND addressDataRecord.municipalityCode >= 950 ");
            if (municipalityCode != 0) {
                hql.append("AND (addressDataRecord.municipalityCode = :municipality) ");
            }

            if ("pnr".equals(order_by)) {
                hql.append(" ORDER BY personEntity.personnummer");
            } else if ("efternavn".equals(order_by)) {
                hql.append(" ORDER BY nameDataRecord.lastName");
            }

            Query<Object[]> query = session.createQuery(hql.toString());
            query.setParameter("halfyear", voteDateMinus6Month);
            query.setParameter("btb", birthBeforeTS);
            if (birthAfterTS != null) {
                query.setParameter("bta", birthAfterTS);
            }
            if (localityCode != null) {
                query.setParameter("locality", localityCode);
            }
            if (municipalityCode != 0) {
                query.setParameter("municipality", municipalityCode);
            }

            query.setMaxResults(limit);
            query.setFirstResult(offset);

            List<Object[]> resultList = query.getResultList();

            List<PersonLocationObject> personLocationObjectList = resultList.stream().map(
                obj -> new PersonLocationObject(
                    obj[0].toString(),
                    obj[1].toString(),
                    ((LocalDateTime)obj[2]).format(formatter)
                )
            ).collect(Collectors.toList());

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
        } catch(NumberFormatException | DateTimeParseException e) {
            throw new InvalidParameterException("Invalid parameters");
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
        } catch (AccessDeniedException e) {
            loggerHelper.info("Access denied: " + e.getMessage());
            throw (e);
        }
    }
}
