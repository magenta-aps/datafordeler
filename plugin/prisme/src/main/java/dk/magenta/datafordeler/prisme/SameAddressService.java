package dk.magenta.datafordeler.prisme;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import dk.magenta.datafordeler.core.MonitorService;
import dk.magenta.datafordeler.core.arearestriction.AreaRestriction;
import dk.magenta.datafordeler.core.arearestriction.AreaRestrictionType;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.*;
import dk.magenta.datafordeler.core.fapi.OutputWrapper;
import dk.magenta.datafordeler.core.plugin.AreaRestrictionDefinition;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import dk.magenta.datafordeler.cpr.CprAreaRestrictionDefinition;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonRecordQuery;
import dk.magenta.datafordeler.cpr.records.person.data.AddressDataRecord;
import dk.magenta.datafordeler.geo.GeoLookupDTO;
import dk.magenta.datafordeler.geo.GeoLookupService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Lookup a householde with a cpr as input parameter, return all cpr's of persons living on the same address, together with address information
 */
@RestController
@RequestMapping("/prisme/sameaddress/1")
public class SameAddressService {

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

    private final Logger log = LogManager.getLogger(SameAddressService.class.getCanonicalName());

    @Autowired
    private PersonOutputWrapperPrisme personOutputWrapper;

    @PostConstruct
    public void init() {
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{cprNumber}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String getSingle(@PathVariable("cprNumber") String cprNummer, HttpServletRequest request)
            throws AccessDeniedException, AccessRequiredException, InvalidTokenException, InvalidClientInputException, JsonProcessingException, HttpNotFoundException, InvalidCertificateException {

        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.info(
                "Incoming REST request for PrismeCprService with cprNumber " + cprNummer
        );
        this.checkAndLogAccess(loggerHelper);
        loggerHelper.urlInvokePersistablelogs("SameAddressService");

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            GeoLookupService lookupService = new GeoLookupService(sessionManager);

            PersonRecordQuery personQuery = new PersonRecordQuery();
            personQuery.setPersonnummer(cprNummer);

            OffsetDateTime now = OffsetDateTime.now();
            personQuery.setRegistrationFromBefore(now);
            personQuery.setRegistrationToAfter(now);
            personQuery.setEffectFromBefore(now);
            personQuery.setEffectToAfter(now);

            personQuery.applyFilters(session);
            this.applyAreaRestrictionsToQuery(personQuery, user);

            OutputWrapper.NodeWrapper root = new OutputWrapper.NodeWrapper(objectMapper.createObjectNode());

            List<PersonEntity> personEntities = QueryManager.getAllEntities(session, personQuery, PersonEntity.class);
            if (!personEntities.isEmpty()) {
                PersonEntity person = personEntities.get(0);

                AddressDataRecord address = FilterUtilities.findNewestUnclosed(person.getAddress());
                root.put("cprNumber", cprNummer);
                if (address != null) {
                    int municipalityCode = address.getMunicipalityCode();
                    root.put("municipalitycode", municipalityCode);
                    int roadCode = address.getRoadCode();
                    root.put("roadcode", address.getRoadCode());
                    root.put("housenumber", address.getHouseNumber());
                    root.put("floor", address.getFloor());
                    root.put("door", address.getDoor());
                    root.put("buildingNo", address.getBuildingNumber());

                    GeoLookupDTO lookup = null;
                    if (municipalityCode > 0 && lookupService != null) {
                        lookup = lookupService.doLookup(municipalityCode, roadCode);
                        if (lookup.getLocalityCodeNumber() != 0) {
                            root.put("localityCode", lookup.getLocalityCodeNumber());
                            root.put("roadName", lookup.getRoadName());
                        }
                    }
                    ArrayNode sameAddressCprs = objectMapper.createArrayNode();
                    if (lookup != null && !lookup.isAdministrativ()) {
                        PersonRecordQuery personSameAddressQuery = new PersonRecordQuery();
                        personSameAddressQuery.setPageSize("30");
                        personSameAddressQuery.addKommunekode(address.getMunicipalityCode());
                        personSameAddressQuery.addVejkode(address.getRoadCode());
                        personSameAddressQuery.addHouseNo(address.getHouseNumber());
                        personSameAddressQuery.addDoor(address.getDoor());
                        personSameAddressQuery.addFloor(address.getFloor());
                        personSameAddressQuery.addBuildingNo(address.getBuildingNumber());

                        List<PersonEntity> personEntitiesOnSameAdd = QueryManager.getAllEntities(session, personSameAddressQuery, PersonEntity.class);
                        for (PersonEntity personentity : personEntitiesOnSameAdd) {
                            sameAddressCprs.add(personentity.getPersonnummer());
                        }
                    } else {
                        sameAddressCprs.add(cprNummer);
                    }
                    root.set("sameAddressCprs", sameAddressCprs);
                    loggerHelper.urlResponsePersistablelogs(HttpStatus.OK.value(), "SameAddressService done");
                }
                return objectMapper.writeValueAsString(root.getNode());
            }
            loggerHelper.urlResponsePersistablelogs(HttpStatus.NOT_FOUND.value(), "SameAddressService done");
            throw new HttpNotFoundException("No entity with CPR number " + cprNummer + " was found");
        }
    }


    protected void checkAndLogAccess(LoggerHelper loggerHelper) throws AccessDeniedException, AccessRequiredException {
        try {
            loggerHelper.getUser().checkHasSystemRole(CprRolesDefinition.READ_CPR_ROLE);
        } catch (AccessDeniedException e) {
            loggerHelper.info("Access denied: " + e.getMessage());
            throw (e);
        }
    }

    protected void applyAreaRestrictionsToQuery(PersonRecordQuery query, DafoUserDetails user) throws InvalidClientInputException {
        Collection<AreaRestriction> restrictions = user.getAreaRestrictionsForRole(CprRolesDefinition.READ_CPR_ROLE);
        AreaRestrictionDefinition areaRestrictionDefinition = this.cprPlugin.getAreaRestrictionDefinition();
        AreaRestrictionType municipalityType = areaRestrictionDefinition.getAreaRestrictionTypeByName(CprAreaRestrictionDefinition.RESTRICTIONTYPE_KOMMUNEKODER);
        for (AreaRestriction restriction : restrictions) {
            if (restriction.getType() == municipalityType) {
                query.addKommunekode(restriction.getValue());
            }
        }
    }
}
