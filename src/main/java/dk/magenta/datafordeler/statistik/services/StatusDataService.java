package dk.magenta.datafordeler.statistik.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.*;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.ListHashMap;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonRecordQuery;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.person.data.*;
import dk.magenta.datafordeler.statistik.queries.PersonStatusQuery;
import dk.magenta.datafordeler.statistik.utils.Filter;
import dk.magenta.datafordeler.statistik.utils.Lookup;
import dk.magenta.datafordeler.statistik.utils.LookupService;
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
import java.time.OffsetDateTime;
import java.util.*;


/*Created by Efrin 06-04-2018*/

@RestController
@RequestMapping("/statistik/status_data")
public class StatusDataService extends PersonStatisticsService {

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

    @RequestMapping(method = RequestMethod.GET, path = "/")
    public void handleRequest(HttpServletRequest request, HttpServletResponse response, ServiceName serviceName)
            throws AccessDeniedException, AccessRequiredException, InvalidTokenException, InvalidClientInputException, IOException, HttpNotFoundException, MissingParameterException, InvalidCertificateException {
        super.handleRequest(request, response, ServiceName.STATUS);
    }

    @Override
    protected List<String> getColumnNames() {
        return Arrays.asList(new String[]{
                PNR, BIRTHDAY_YEAR, FIRST_NAME, LAST_NAME, STATUS_CODE,
                BIRTH_AUTHORITY, BIRTH_AUTHORITY_TEXT, CITIZENSHIP_CODE, MOTHER_PNR, FATHER_PNR, CIVIL_STATUS, SPOUSE_PNR,
                MUNICIPALITY_CODE, LOCALITY_NAME, LOCALITY_CODE, LOCALITY_ABBREVIATION, ROAD_CODE, HOUSE_NUMBER, FLOOR_NUMBER, DOOR_NUMBER,
                BNR, MOVING_IN_DATE, MOVE_PROD_DATE, POST_CODE, CIVIL_STATUS_DATE, CIVIL_STATUS_PROD_DATE, CHURCH
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
    protected List<Map<String, String>> formatPerson(PersonEntity person, Session session, LookupService lookupService, Filter filter) {
        return Collections.singletonList(this.formatPersonByRecord(person, session, lookupService, filter));
    }

    protected Map<String, String> formatPersonByRecord(PersonEntity person, Session session, LookupService lookupService, Filter filter) {
        HashMap<String, String> item = new HashMap<>();

        item.put(PNR, formatPnr(person.getPersonnummer()));

        // Loop over the list of registrations (which is already sorted (by time, ascending))
        for (NameDataRecord nameDataRecord : sort(person.getName())) {
            item.put(FIRST_NAME, nameDataRecord.getFirstNames());
            item.put(LAST_NAME, nameDataRecord.getLastName());
        }

        for (BirthPlaceDataRecord birthPlaceDataRecord : filter(person.getBirthPlace(), filter)) {
            item.put(BIRTH_AUTHORITY, Integer.toString(birthPlaceDataRecord.getAuthority()));
            item.put(BIRTH_AUTHORITY_CODE_TEXT, birthPlaceDataRecord.getBirthPlaceName());
            item.put(BIRTH_AUTHORITY_TEXT, birthPlaceDataRecord.getBirthPlaceName());
        }
        for (BirthTimeDataRecord birthTimeDataRecord : filter(person.getBirthTime(), filter)) {
            LocalDateTime birthTime = birthTimeDataRecord.getBirthDatetime();
            if (birthTime != null) {
                item.put(BIRTHDAY_YEAR, Integer.toString(birthTime.getYear()));
            }
        }
        try {
            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(person.getStatus()));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        for (PersonStatusDataRecord statusDataRecord : filter(person.getStatus(), filter)) {
            item.put(STATUS_CODE, formatStatusCode(statusDataRecord.getStatus()));
        }
        for (CitizenshipDataRecord citizenshipDataRecord : filter(person.getCitizenship(), filter)) {
            item.put(CITIZENSHIP_CODE, Integer.toString(citizenshipDataRecord.getCountryCode()));
        }
        for (ParentDataRecord parentDataRecord : filter(person.getMother(), filter)) {
            item.put(MOTHER_PNR, formatPnr(parentDataRecord.getCprNumber()));
        }
        for (ParentDataRecord parentDataRecord : filter(person.getFather(), filter)) {
            item.put(FATHER_PNR, formatPnr(parentDataRecord.getCprNumber()));
        }
        for (CivilStatusDataRecord civilStatusDataRecord : filter(person.getCivilstatus(), filter)) {
            item.put(SPOUSE_PNR, formatPnr(civilStatusDataRecord.getSpouseCpr()));
        }
        for (ChurchDataRecord churchDataRecord : filter(person.getChurchRelation(), filter)) {
            item.put(CHURCH, churchDataRecord.getChurchRelation().toString());
        }

        for (CivilStatusDataRecord civilStatusDataRecord : filter(person.getCivilstatus(), filter)) {
            item.put(CIVIL_STATUS, civilStatusDataRecord.getCivilStatus());
            item.put(CIVIL_STATUS_DATE, formatTime(civilStatusDataRecord.getEffectFrom()));
            item.put(CIVIL_STATUS_PROD_DATE, formatTime(civilStatusDataRecord.getRegistrationFrom()));
        }
        for (AddressDataRecord addressDataRecord : filter(person.getAddress(), filter)) {
            if (addressDataRecord.getMunicipalityCode() < 900) return null;
            item.put(MOVING_IN_DATE, formatTime(addressDataRecord.getEffectFrom()));
            item.put(MOVE_PROD_DATE, formatTime(addressDataRecord.getRegistrationFrom()));

            item.put(MUNICIPALITY_CODE, formatMunicipalityCode(addressDataRecord.getMunicipalityCode()));
            item.put(ROAD_CODE, formatRoadCode(addressDataRecord.getRoadCode()));
            item.put(HOUSE_NUMBER, formatHouseNnr(addressDataRecord.getHouseNumber()));
            item.put(DOOR_NUMBER, formatDoor(addressDataRecord.getDoor()));
            item.put(BNR, formatBnr(addressDataRecord.getBuildingNumber()));
            item.put(FLOOR_NUMBER, formatFloor(addressDataRecord.getFloor()));

            // Use the lookup service to extract locality & postcode data from a municipality code and road code
            Lookup lookup = lookupService.doLookup(
                    addressDataRecord.getMunicipalityCode(),
                    addressDataRecord.getRoadCode(),
                    addressDataRecord.getHouseNumber()
            );
            if (lookup != null) {
                item.put(LOCALITY_NAME, lookup.localityName);
                item.put(LOCALITY_CODE, formatLocalityCode(lookup.localityCode));
                item.put(LOCALITY_ABBREVIATION, lookup.localityAbbrev);
                item.put(POST_CODE, Integer.toString(lookup.postalCode));
            }
        }

        replaceMapValues(item, null, "");

        return item;
    }

    private static <R extends CprBitemporalRecord> List<R> filter(Collection<R> records, Filter filter) {
        List<R> sorted = sortRecords(filterRecordsByEffect(filterRecordsByRegistration(filterUndoneRecords(records), filter.registrationAt), filter.effectAt));
        //return sorted.isEmpty() ? Collections.emptyList() : Collections.singletonList(sorted.get(sorted.size()-1));
        return sorted;
    }

    private static <R extends CprBitemporalRecord> List<R> sort(Collection<R> records) {
        List<R> sorted = sortRecords(filterUndoneRecords(records));
        return sorted;
    }
}