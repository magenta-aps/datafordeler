package dk.magenta.datafordeler.statistik.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.*;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonRecordQuery;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.person.data.*;
import dk.magenta.datafordeler.geo.GeoLookupDTO;
import dk.magenta.datafordeler.geo.GeoLookupService;
import dk.magenta.datafordeler.statistik.queries.PersonStatusQuery;
import dk.magenta.datafordeler.statistik.utils.Filter;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;


@RestController
@RequestMapping("/statistik/vote_list_data")
public class VoteListDataService extends PersonStatisticsService {

    @Autowired
    SessionManager sessionManager;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private CsvMapper csvMapper;

    @Autowired
    private DafoUserManager dafoUserManager;

    @Autowired
    private CprPlugin cprPlugin;

    private Logger log = LogManager.getLogger(BirthDataService.class.getCanonicalName());

    @Override
    protected String[] requiredParameters() {
        return new String[]{StatisticsService.EFFECT_DATE_PARAMETER};
    }

    @Override
    protected CprPlugin getCprPlugin() {
        return this.cprPlugin;
    }

    /**
     * Calls handlerequest in super with the ID of the report as a parameter
     * @param request
     * @param response
     * @throws AccessDeniedException
     * @throws AccessRequiredException
     * @throws InvalidTokenException
     * @throws IOException
     * @throws MissingParameterException
     * @throws InvalidClientInputException
     * @throws HttpNotFoundException
     * @throws InvalidCertificateException
     */
    @RequestMapping(method = RequestMethod.GET, path = "/")
    public void get(HttpServletRequest request, HttpServletResponse response)
            throws AccessDeniedException, AccessRequiredException, InvalidTokenException, InvalidClientInputException, IOException, HttpNotFoundException, MissingParameterException, InvalidCertificateException {
        super.handleRequest(request, response, ServiceName.VOTE);
    }

    /**
     * Post is used for starting the generation of a report
     * @param request
     * @param response
     * @throws AccessDeniedException
     * @throws AccessRequiredException
     * @throws InvalidTokenException
     * @throws IOException
     * @throws MissingParameterException
     * @throws InvalidClientInputException
     * @throws HttpNotFoundException
     * @throws InvalidCertificateException
     */
    @RequestMapping(method = RequestMethod.POST, path = "/")
    public void handlePost(HttpServletRequest request, HttpServletResponse response)
            throws AccessDeniedException, AccessRequiredException, InvalidTokenException, IOException, MissingParameterException, InvalidClientInputException, HttpNotFoundException, InvalidCertificateException {
        super.handleRequest(request, response, ServiceName.VOTE);
    }

    @Override
    protected List<String> getColumnNames() {
        return Arrays.asList(new String[]{
                PNR, FIRST_NAME, LAST_NAME, BIRTHDAY_YEAR, STATUS_CODE, CITIZENSHIP_CODE,
                MUNICIPALITY_CODE, LOCALITY_NAME, LOCALITY_CODE, LOCALITY_ABBREVIATION, ROAD_CODE, ROAD_NAME, HOUSE_NUMBER, FLOOR_NUMBER, DOOR_NUMBER,
                BNR, POST_CODE, PROTECTION_TYPE
        });
    }

    @Override
    protected SessionManager getSessionManager() {
        return this.sessionManager;
    }

    @Override
    protected CsvMapper getCsvMapper() {
        return this.csvMapper;
    }

    @Override
    protected DafoUserManager getDafoUserManager() {
        return this.dafoUserManager;
    }

    @Override
    protected Logger getLogger() {
        return this.log;
    }

    @Override
    protected PersonRecordQuery getQuery(Filter filter) {
        return new PersonStatusQuery(filter);
    }

    @Override
    protected List<Map<String, String>> formatPerson(PersonEntity person, Session session, GeoLookupService lookupService, Filter filter) {
        return Collections.singletonList(this.formatPersonByRecord(person, session, lookupService, filter));
    }

    protected Map<String, String> formatPersonByRecord(PersonEntity person, Session session, GeoLookupService lookupService, Filter filter) {
        HashMap<String, String> item = new HashMap<>();

        item.put(PNR, formatPnr(person.getPersonnummer()));

        // Loop over the list of registrations (which is already sorted (by time, ascending))
        NameDataRecord nameDataRecord = filter(person.getName(), filter);
        if(nameDataRecord!=null) {
            String firstname = nameDataRecord.getFirstNames();
            if(!StringUtils.isEmpty(nameDataRecord.getMiddleName())) {
                firstname += " "+nameDataRecord.getMiddleName();
            }
            item.put(FIRST_NAME, firstname);
            item.put(LAST_NAME, nameDataRecord.getLastName());
        }

        BirthTimeDataRecord birthTimeDataRecord = filter(person.getBirthTime(), filter);
        if(birthTimeDataRecord!=null) {
            LocalDateTime birthTime = birthTimeDataRecord.getBirthDatetime();
            if (birthTime != null) {
                //If a person is less than 18 years at this date, we return without adding the person
                if(filter.effectAt.minusYears(18).isBefore(birthTime.atOffset(ZoneOffset.UTC)))
                {
                    return null;
                }
                item.put(BIRTHDAY_YEAR, Integer.toString(birthTime.getYear()));
            }
        }

        PersonStatusDataRecord statusDataRecord = filter(person.getStatus(), filter);
        if(statusDataRecord!=null) {
            item.put(STATUS_CODE, formatStatusCode(statusDataRecord.getStatus()));
        }

        CitizenshipDataRecord citizenshipDataRecord = filter(person.getCitizenship(), filter);
        if(citizenshipDataRecord!=null) {
            item.put(CITIZENSHIP_CODE, Integer.toString(citizenshipDataRecord.getCountryCode()));
        }

        ProtectionDataRecord protectionDataRecord = filter(person.getProtection(), filter);
        if(protectionDataRecord!=null) {
            item.put(PROTECTION_TYPE, Integer.toString(protectionDataRecord.getProtectionType()));
        }


        AddressDataRecord addressDataRecord = filter(person.getAddress(), filter);
        if(addressDataRecord!=null) {
            if (addressDataRecord.getMunicipalityCode() < 900) return null;

            item.put(MUNICIPALITY_CODE, formatMunicipalityCode(addressDataRecord.getMunicipalityCode()));
            item.put(ROAD_CODE, formatRoadCode(addressDataRecord.getRoadCode()));
            item.put(HOUSE_NUMBER, formatHouseNnr(addressDataRecord.getHouseNumber()));
            item.put(DOOR_NUMBER, formatDoor(addressDataRecord.getDoor()));
            item.put(BNR, formatBnr(addressDataRecord.getBuildingNumber()));
            item.put(FLOOR_NUMBER, formatFloor(addressDataRecord.getFloor()));

            // Use the lookup service to extract locality & postcode data from a municipality code and road code
            GeoLookupDTO lookup = lookupService.doLookup(
                    addressDataRecord.getMunicipalityCode(),
                    addressDataRecord.getRoadCode(),
                    addressDataRecord.getHouseNumber()
            );
            if (lookup != null) {
                item.put(LOCALITY_NAME, lookup.getLocalityName());
                item.put(LOCALITY_CODE, lookup.getLocalityCode());
                item.put(LOCALITY_ABBREVIATION, lookup.getLocalityAbbrev());
                item.put(POST_CODE, Integer.toString(lookup.getPostalCode()));
                item.put(ROAD_NAME, lookup.getRoadName());
            }
        }


        replaceMapValues(item, null, "");

        return item;
    }

    private static <R extends CprBitemporalRecord> R filter(Collection<R> records, Filter filter) {
        return findMostImportant(filterRecordsByEffect(filterRecordsByRegistration(filterUndoneRecords(records), filter.registrationAt), filter.effectAt));
    }

    private static <R extends CprBitemporalRecord> List<R> sort(Collection<R> records) {
        List<R> sorted = sortRecords(filterUndoneRecords(records));
        return sorted;
    }
}
