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

/**
 * Denne service benyttes til at konstruere valglister til grønlandske valg.
 * Valglisterne konstrueres når denne service kaldes, og resultatet gemmes lokalt på serverens disk.
 * <p>
 * Der findes 2 forskellige typer af valg, der er landstingsvalg og kommunalvalg, og der skal konstrueres en valgliste til hver af disse for sig
 * <p>
 * Begge lister skal levere alle CPR-numre i den grønlandske datafordeler, men med frasortering af personer, som beskrevet nedenfor:
 * <p>
 * For begge typer af lister, skal der frasorteres alle personer, som ikke er fyldt 18 år på tidspunktet angivet med parametrene effectDate og registrationAt
 * <p>
 * For landstingsvalget skal der derudover frasorteres alle personer, som ikke har haft bopæl i grønland i et halvt år dette angives ved at sætte filterTime1 til T minus et halvt år
 * <p>
 * For kommunalvalget sættes filterTime1 til samme dato som effectDate og registrationAt, derudover sættes municipalityFilter til kommunekoden
 * <p>
 * Om en person har været bosiddende i grønland siden filterTime1 identificeret ved at iterere over alle statuskoder på personen, som har været delvist gældende siden filterTime1.
 * Hvis nogen af disse statuskoder har anden værdi end 5 eller 7, så frasorteres denne person fra listen, derudover filtreres også på kommunekode < 900
 * <p>
 * <p>
 * Området som personer bor i er vigtig ved brugen af denne liste, da det identificere hvor disse personer skal stemme.
 * Der er en del fejl i GAR, som benyttes til at finde personers fysiske adresse på baggrund af personernes data i cpr-registeret.
 * Der laves derfor kun match i GAR på baggrund af kommunekode og vejkode, da dette øger sandsynligheden for at finde et match.
 * Hvis der ikke findes et match på postnummer, og postdistrikt, så findes dette via cpr-registerets danske adresseregister.
 * <p>
 * <p>
 * Ved valget i Grønland 2021 laves 2 lister, en over landstingsval, og en over kommunalvalg i sermersooq
 * For landstingsvalget benyttes følgende url i datafordeleren, il at lave listen:
 * statistik/vote_list_data/?effectDate=2021-03-29&registrationAt=2021-03-29&filterTime1=2020-10-06
 * <p>
 * <p>
 * Og til kommunalvalget benyttes følgende url i datafordeleren
 * statistik/vote_list_data/?effectDate=2021-03-29&registrationAt=2021-03-29&filterTime1=2021-03-29&munipialicityFilter=956
 * <p>
 * Alle personerne i listerne har stemmeret til valget ud fra de kriterier jeg har fået oplyst
 * <p>
 * Listerne indeholder følgende kolonner:
 * Pnr - CPR-nummeret på personen
 * Fornavn - Fammensat fornavn og mellemnavn
 * Efternavn -
 * FoedData - Datoen hvor personen er født
 * StatKod - Koden på personens statsborgerskab
 * LokNavn - Novnet på den lokalitet personen bor i følge folkeregidter og fremsøgning på baggrund af vejkode og kommunekode
 * LokKode - Lokalitetskoden på den lokalitet personen bor i følge folkeregidter og fremsøgning på baggrund af vejkode og kommunekode
 * LokKortNavn - Det korte navn på den lokalitet personen bor i følge folkeregidter og fremsøgning på baggrund af vejkode og kommunekode
 * VejNavn - Navnet på den vej personen bor i følge folkeregidter og fremsøgning på baggrund af vejkode og kommunekode
 * HusNr -
 * Etage -
 * SideDoer -
 * Bnr -
 * Postnr - Postnummer fundet på baggrund af vejkode og kommunekode, der laves fallback til det danske register hvis informationen ikke kan findes i GAR
 * Postdistrikt - Postdistrikt fundet på baggrund af vejkode og kommunekode, der laves fallback til det danske register hvis informationen ikke kan findes i GAR
 * GUARDIAN - Der angives CPR-nummeret i fald der findes en værge for personen
 */


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

    private final Logger log = LogManager.getLogger(BirthDataService.class.getCanonicalName());

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
     *
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
     *
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
        return Arrays.asList(PNR, FIRST_NAME, LAST_NAME, "FoedDato", STATUS_CODE, CITIZENSHIP_CODE,
                MUNICIPALITY_CODE, LOCALITY_NAME, LOCALITY_CODE, LOCALITY_ABBREVIATION, ROAD_CODE, ROAD_NAME, HOUSE_NUMBER, FLOOR_NUMBER, DOOR_NUMBER,
                BNR, POST_CODE, POST_DISTRICT, "GUARDIAN");
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
        if (nameDataRecord != null) {
            String firstname = nameDataRecord.getFirstNames();
            if (!StringUtils.isEmpty(nameDataRecord.getMiddleName())) {
                firstname += " " + nameDataRecord.getMiddleName();
            }
            item.put(FIRST_NAME, firstname);
            item.put(LAST_NAME, nameDataRecord.getLastName());
        }

        BirthTimeDataRecord birthTimeDataRecord = filter(person.getBirthTime(), filter);
        if (birthTimeDataRecord != null) {
            LocalDateTime birthTime = birthTimeDataRecord.getBirthDatetime();
            if (birthTime != null) {
                //If a person is less than 18 years at this date, we return without adding the person
                if (filter.effectAt.minusYears(18).isBefore(birthTime.atOffset(ZoneOffset.UTC))) {
                    return null;
                }
                item.put("FoedDato", formatTime(birthTimeDataRecord.getBirthDatetime().toLocalDate()));
            } else {
                item.put("FoedDato", "UNDEFINED");
            }
        }
        if (filter.filterTime1 != null) {
            //If this is not null, we need to validate back in time
            List<PersonStatusDataRecord> statusDataRecords = findAllUnclosedInRegistrationAndNotUndone(person.getStatus(), filter.filterTime1);
            if (statusDataRecords != null) {
                for (PersonStatusDataRecord status : statusDataRecords) {
                    //Status 5 and 7 indicates that the person has an active address in greenland
                    if (status.getStatus() != 5 && status.getStatus() != 7) {
                        return null;
                    }
                }
            }

            List<AddressDataRecord> addressDataRecords = findAllUnclosedInRegistrationAndNotUndone(person.getAddress(), filter.filterTime1);
            if (addressDataRecords != null) {
                for (AddressDataRecord addressDataRecord : addressDataRecords) {
                    if (addressDataRecord.getMunicipalityCode() < 900) {
                        return null;
                    }
                }
            }
        }

        PersonStatusDataRecord statusDataRecord = filter(person.getStatus(), filter);
        if (statusDataRecord != null) {
            //Stastus 5 and 7 indicates that the person has an active address in greenland
            if (statusDataRecord.getStatus() != 5 && statusDataRecord.getStatus() != 7) {
                return null;
            } else {
                item.put(STATUS_CODE, "" + statusDataRecord.getStatus());
            }
        } else {
            item.put(STATUS_CODE, "UNDEFINED");
        }

        CitizenshipDataRecord citizenshipDataRecord = filter(person.getCitizenship(), filter);
        if (citizenshipDataRecord != null) {
            item.put(CITIZENSHIP_CODE, Integer.toString(citizenshipDataRecord.getCountryCode()));
        }

        AddressDataRecord addressDataRecord = filter(person.getAddress(), filter);
        if (addressDataRecord != null) {
            if (addressDataRecord.getMunicipalityCode() < 900) {
                return null;
            } else if (filter.municipalityFilter != null && addressDataRecord.getMunicipalityCode() != filter.municipalityFilter) {
                return null;
            }

            item.put(MUNICIPALITY_CODE, formatMunicipalityCode(addressDataRecord.getMunicipalityCode()));
            item.put(ROAD_CODE, formatRoadCode(addressDataRecord.getRoadCode()));
            item.put(HOUSE_NUMBER, formatHouseNnr(addressDataRecord.getHouseNumber()));
            item.put(DOOR_NUMBER, formatDoor(addressDataRecord.getDoor()));
            item.put(BNR, formatBnr(addressDataRecord.getBuildingNumber()));
            item.put(FLOOR_NUMBER, formatFloor(addressDataRecord.getFloor()));

            // Use the lookup service to extract locality & postcode data from a municipality code and road code
            GeoLookupDTO lookup = lookupService.doLookupBestEffort(
                    addressDataRecord.getMunicipalityCode(),
                    addressDataRecord.getRoadCode()
            );
            if (lookup != null) {
                item.put(LOCALITY_NAME, lookup.getLocalityName());
                item.put(LOCALITY_CODE, lookup.getLocalityCode());
                item.put(LOCALITY_ABBREVIATION, lookup.getLocalityAbbrev());
                item.put(POST_CODE, Integer.toString(lookup.getPostalCode()));
                item.put(POST_DISTRICT, lookup.getPostalDistrict());
                item.put(ROAD_NAME, lookup.getRoadName());
            }
        }

        GuardianDataRecord guardianDataRecord = filter(person.getGuardian(), filter);
        if (guardianDataRecord != null) {
            item.put("GUARDIAN", guardianDataRecord.getRelationPnr());
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
