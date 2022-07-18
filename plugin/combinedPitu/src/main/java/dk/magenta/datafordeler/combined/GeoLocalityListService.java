package dk.magenta.datafordeler.combined;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.MonitorService;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.*;
import dk.magenta.datafordeler.core.fapi.Envelope;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.person.data.AddressDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.BirthTimeDataRecord;
import dk.magenta.datafordeler.geo.data.accessaddress.AccessAddressEntity;
import dk.magenta.datafordeler.geo.data.accessaddress.AccessAddressLocalityRecord;
import dk.magenta.datafordeler.geo.data.accessaddress.AccessAddressRoadRecord;
import dk.magenta.datafordeler.geo.data.locality.GeoLocalityEntity;
import dk.magenta.datafordeler.geo.data.locality.LocalityQuery;
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
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Get a list of locations
 */
@RestController
@RequestMapping("/combined/localityList/1")
public class GeoLocalityListService {

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    SessionManager sessionManager;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private DafoUserManager dafoUserManager;

    @Autowired
    protected MonitorService monitorService;

    private Logger log = LogManager.getLogger(GeoLocalityListService.class.getCanonicalName());

    @PostConstruct
    public void init() {

    }

    @GetMapping("/search")
    public Envelope findAll(HttpServletRequest request, @RequestParam MultiValueMap<String, String> requestParams, HttpServletResponse response) throws AccessDeniedException, MissingParameterException, InvalidTokenException, InvalidCertificateException, InvalidParameterException {
        try (Session session = sessionManager.getSessionFactory().openSession()) {

            String pageSize = requestParams.getFirst("pageSize");

            String page = requestParams.getFirst("page");
            String municipalitycode = requestParams.getFirst("kommune_kode");
            String localityCode = requestParams.getFirst("lokalitet_kode");

            DafoUserDetails user = dafoUserManager.getUserFromRequest(request);

            LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
            loggerHelper.urlInvokePersistablelogs("GeoLocalityListService");
            Envelope envelope = new Envelope();
            LocalityQuery lq = new LocalityQuery();
            List<GeoLocalityEntity> localityEntities;

            lq.setStatus(1);
            lq.setPage(page);
            lq.setPageSize(pageSize);

            if (localityCode != null) {
                lq.setCode(localityCode);
            }
            if (municipalitycode != null) {
                lq.setMunicipality(municipalitycode);
            }

            localityEntities = QueryManager.getAllItems(session, GeoLocalityEntity.class);

            envelope.setRequestTimestamp(user.getCreationTime());
            envelope.setUsername(user.toString());

            envelope.setPageSize(lq.getPageSize());
            envelope.setPage(lq.getPage());
            envelope.setPath(request.getServletPath());
            envelope.setResponseTimestamp(OffsetDateTime.now());


            List<String> list = localityEntities.stream().map(f -> f.getCode()).collect(Collectors.toList());
            envelope.setResults(list);

            setHeaders(response);
            loggerHelper.urlResponsePersistablelogs(HttpStatus.OK.value(), "CprBirthIntervalDateService done");
            return envelope;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
