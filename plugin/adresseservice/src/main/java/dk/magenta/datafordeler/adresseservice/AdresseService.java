package dk.magenta.datafordeler.adresseservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.database.*;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.exception.HttpNotFoundException;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.exception.MissingParameterException;
import dk.magenta.datafordeler.core.fapi.Query;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import dk.magenta.datafordeler.gladdrreg.data.address.AddressData;
import dk.magenta.datafordeler.gladdrreg.data.address.AddressEntity;
import dk.magenta.datafordeler.gladdrreg.data.address.AddressQuery;
import dk.magenta.datafordeler.gladdrreg.data.bnumber.BNumberData;
import dk.magenta.datafordeler.gladdrreg.data.bnumber.BNumberEntity;
import dk.magenta.datafordeler.gladdrreg.data.bnumber.BNumberQuery;
import dk.magenta.datafordeler.gladdrreg.data.locality.LocalityData;
import dk.magenta.datafordeler.gladdrreg.data.locality.LocalityEntity;
import dk.magenta.datafordeler.gladdrreg.data.locality.LocalityQuery;
import dk.magenta.datafordeler.gladdrreg.data.municipality.MunicipalityData;
import dk.magenta.datafordeler.gladdrreg.data.municipality.MunicipalityEffect;
import dk.magenta.datafordeler.gladdrreg.data.municipality.MunicipalityEntity;
import dk.magenta.datafordeler.gladdrreg.data.municipality.MunicipalityRegistration;
import dk.magenta.datafordeler.gladdrreg.data.road.RoadData;
import dk.magenta.datafordeler.gladdrreg.data.road.RoadEntity;
import dk.magenta.datafordeler.gladdrreg.data.road.RoadQuery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;

@RestController
@RequestMapping("/adresse")
public class AdresseService {

    @Autowired
    SessionManager sessionManager;

    @Autowired
    private DafoUserManager dafoUserManager;

    @Autowired
    private ObjectMapper objectMapper;

    private Logger log = LogManager.getLogger(AdresseService.class.getCanonicalName());

    public static final String PARAM_MUNICIPALITY = "kommune";
    public static final String PARAM_LOCALITY = "lokalitet";
    public static final String PARAM_ROAD = "vej";
    public static final String PARAM_HOUSE = "husnr";
    public static final String PARAM_BNR = "b_nummer";
    public static final String PARAM_ADDRESS = "adresse";

    public static final String OUTPUT_UUID = "uuid";
    public static final String OUTPUT_NAME = "navn";
    public static final String OUTPUT_ABBREVIATION = "forkortelse";
    public static final String OUTPUT_MUNICIPALITYCODE = "kommunekode";
    public static final String OUTPUT_LOCALITYUUID = "lokalitet";
    public static final String OUTPUT_LOCALITYNAME = "lokalitetsnavn";
    public static final String OUTPUT_ROADUUID = "vej_uuid";
    public static final String OUTPUT_ROADCODE = "vejkode";
    public static final String OUTPUT_ROADNAME = "vejnavn";
    public static final String OUTPUT_ALTNAME = "andet_navn";
    public static final String OUTPUT_CPRNAME = "cpr_navn";
    public static final String OUTPUT_SHORTNAME = "forkortet_navn";
    public static final String OUTPUT_BNUMBER = "b_nummer";
    public static final String OUTPUT_BCALLNAME = "b_kaldenavn";
    public static final String OUTPUT_HOUSENUMBER = "husnummer";
    public static final String OUTPUT_FLOOR = "etage";
    public static final String OUTPUT_DOOR = "doer";
    public static final String OUTPUT_RESIDENCE = "bolig";


    HashMap<Integer, UUID> municipalities = new HashMap<>();

    /**
     * Load known municipalities into a local map of municipalityCode: UUID
     */
    @PostConstruct
    public void loadMunicipalities() {
        Session session = sessionManager.getSessionFactory().openSession();
        try {
            List<MunicipalityEntity> municipalities = QueryManager.getAllEntities(session, MunicipalityEntity.class);
            for (MunicipalityEntity municipality : municipalities) {
                MunicipalityData data = getData(municipality);
                if (data != null) {
                    this.municipalities.put(data.getCode(), municipality.getUUID());
                }
            }
        } finally {
            session.close();
        }
    }

    private static MunicipalityData getData(MunicipalityEntity municipality) {
        OffsetDateTime now = OffsetDateTime.now();
        MunicipalityRegistration registration = municipality.getRegistrationAt(now);
        if (registration != null) {
            for (MunicipalityEffect effect : registration.getEffectsAt(now)) {
                for (MunicipalityData data : effect.getDataItems()) {
                    if (data.getCode() != 0) {
                        return data;
                    }
                }
            }
        }
        return null;
    }


    /**
     * Finds all localities in a municipality. Only current data is included.
     *
     * @param request HTTP request containing a municipality parameter
     * @return Json-formatted string containing a list of found objects
     */
    @RequestMapping("/lokalitet")
    public void getLocalities(HttpServletRequest request, HttpServletResponse response) throws DataFordelerException, IOException {
        String payload = this.getLocalities(request);
        setHeaders(response);
        response.getWriter().write(payload);
    }

    public String getLocalities(HttpServletRequest request) throws DataFordelerException {
        String municipalityCode = request.getParameter(PARAM_MUNICIPALITY);
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        log.info(
                "Incoming REST request for AddressService.locality with municipality {}", municipalityCode
        );
        checkParameterExistence(PARAM_MUNICIPALITY, municipalityCode);
        int code = parameterAsInt(PARAM_MUNICIPALITY, municipalityCode);
        UUID municipality = this.municipalities.get(code);
        if (municipality == null) {
            throw new HttpNotFoundException("Municipality with code " + code + " not found");
        }

        LocalityQuery query = new LocalityQuery();
        setQueryNow(query);
        setQueryNoLimit(query);
        query.setMunicipality(municipality.toString());
        Session session = sessionManager.getSessionFactory().openSession();
        try {
            List<LocalityEntity> localities = QueryManager.getAllEntities(session, query, LocalityEntity.class);
            ArrayNode results = objectMapper.createArrayNode();
            for (LocalityEntity locality : localities) {
                Set<DataItem> dataItems = locality.getCurrent();
                ObjectNode localityNode = objectMapper.createObjectNode();
                localityNode.put(OUTPUT_UUID, locality.getUUID().toString());
                localityNode.set(OUTPUT_NAME, null);
                localityNode.set(OUTPUT_ABBREVIATION, null);
                for (DataItem dataItem : dataItems) {
                    LocalityData data = (LocalityData) dataItem;
                    if (data.getName() != null) {
                        localityNode.put(OUTPUT_NAME, data.getName());
                    }
                    if (data.getAbbrev() != null) {
                        localityNode.put(OUTPUT_ABBREVIATION, data.getAbbrev());
                    }
                }
                results.add(localityNode);
            }
            return results.toString();
        } finally {
            session.close();
        }
    }

    /**
     * Finds all roads in a locality. Only current data is included.
     *
     * @param request HTTP request containing a locality parameter
     * @return Json-formatted string containing a list of found objects
     */
    @RequestMapping("/vej")
    public void getRoads(HttpServletRequest request, HttpServletResponse response) throws DataFordelerException, IOException {
        String payload = this.getRoads(request);
        setHeaders(response);
        response.getWriter().write(payload);
    }

    public String getRoads(HttpServletRequest request) throws DataFordelerException {
        String localityUUID = request.getParameter(PARAM_LOCALITY);
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        log.info(
                "Incoming REST request for AddressService.road with locality {}", localityUUID
        );
        checkParameterExistence(PARAM_LOCALITY, localityUUID);
        UUID locality = parameterAsUUID(PARAM_LOCALITY, localityUUID);
        RoadQuery query = new RoadQuery();
        setQueryNow(query);
        setQueryNoLimit(query);
        query.setLocality(locality.toString());
        Session session = sessionManager.getSessionFactory().openSession();
        try {
            List<RoadEntity> roads = QueryManager.getAllEntities(session, query, RoadEntity.class);
            ArrayNode results = objectMapper.createArrayNode();
            for (RoadEntity road : roads) {
                Set<DataItem> dataItems = road.getCurrent();
                ObjectNode roadNode = objectMapper.createObjectNode();
                roadNode.put(OUTPUT_UUID, road.getUUID().toString());
                roadNode.set(OUTPUT_ROADCODE, null);
                roadNode.set(OUTPUT_NAME, null);
                roadNode.set(OUTPUT_ALTNAME, null);
                roadNode.set(OUTPUT_CPRNAME, null);
                roadNode.set(OUTPUT_SHORTNAME, null);
                for (DataItem dataItem : dataItems) {
                    RoadData data = (RoadData) dataItem;
                    if (data.getCode() != 0) {
                        roadNode.put(OUTPUT_ROADCODE, data.getCode());
                    }
                    if (data.getName() != null) {
                        roadNode.put(OUTPUT_NAME, data.getName());
                    }
                    if (data.getAlternateName() != null) {
                        roadNode.put(OUTPUT_ALTNAME, data.getAlternateName());
                    }
                    if (data.getCprName() != null) {
                        roadNode.put(OUTPUT_CPRNAME, data.getCprName());
                    }
                    if (data.getShortName() != null) {
                        roadNode.put(OUTPUT_SHORTNAME, data.getShortName());
                    }
                }
                results.add(roadNode);
            }
            return results.toString();
        } finally {
            session.close();
        }
    }

    /**
     * Finds all buildings on a road. Only current data is included.
     *
     * @param request HTTP request containing a road parameter
     * @return Json-formatted string containing a list of found objects
     */
    @RequestMapping("/hus")
    public void getBuildings(HttpServletRequest request, HttpServletResponse response) throws DataFordelerException, IOException {
        String payload = this.getBuildings(request);
        setHeaders(response);
        response.getWriter().write(payload);
    }

    public String getBuildings(HttpServletRequest request) throws DataFordelerException {
        String roadUUID = request.getParameter(PARAM_ROAD);
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        log.info(
                "Incoming REST request for AddressService.building with road {}", roadUUID
        );
        checkParameterExistence(PARAM_ROAD, roadUUID);
        UUID road = parameterAsUUID(PARAM_ROAD, roadUUID);

        AddressQuery addressQuery = new AddressQuery();
        setQueryNow(addressQuery);
        setQueryNoLimit(addressQuery);
        addressQuery.setRoad(road.toString());

        Session session = sessionManager.getSessionFactory().openSession();
        try {
            ArrayNode results = objectMapper.createArrayNode();

            List<AddressEntity> addressEntities = QueryManager.getAllEntities(session, addressQuery, AddressEntity.class);
            if (!addressEntities.isEmpty()) {
                HashMap<Identification, BNumberEntity> bNumberMap = getBNumbers(session, addressEntities);

                // Dedup entiteter - kun 1 pr husnummer (p.t. er der en pr. dør/etage osv)
                HashSet<String> seenHouseNumbers = new HashSet<>();

                for (AddressEntity addressEntity : addressEntities) {
                    ObjectNode addressNode = objectMapper.createObjectNode();
                    Set<DataItem> addressDataItems = addressEntity.getCurrent();
                    addressNode.set(OUTPUT_HOUSENUMBER, null);
                    addressNode.set(OUTPUT_BNUMBER, null);
                    addressNode.set(OUTPUT_BCALLNAME, null);
                    boolean seenBefore = false;
                    for (DataItem dataItem : addressDataItems) {
                        AddressData addressData = (AddressData) dataItem;
                        if (addressData.getHouseNumber() != null) {
                            String houseNumber = addressData.getHouseNumber();
                            if (seenHouseNumbers.contains(houseNumber)) {
                                seenBefore = true;
                                break;
                            } else {
                                seenHouseNumbers.add(houseNumber);
                                addressNode.put(OUTPUT_HOUSENUMBER, houseNumber);
                            }
                        }
                        if (addressData.getbNumber() != null) {
                            BNumberEntity bNumberEntity = bNumberMap.get(addressData.getbNumber());
                            if (bNumberEntity != null) {
                                for (DataItem bNumberDataItem : bNumberEntity.getCurrent()) {
                                    BNumberData bNumberData = (BNumberData) bNumberDataItem;
                                    if (bNumberData.getCode() != null) {
                                        addressNode.put(OUTPUT_BNUMBER, bNumberData.getCode());
                                    }
                                    if (bNumberData.getCallname() != null && !bNumberData.getCallname().isEmpty()) {
                                        addressNode.put(OUTPUT_BCALLNAME, bNumberData.getCallname());
                                    }
                                }
                            }
                        }
                    }
                    if (!seenBefore) {
                        results.add(addressNode);
                    }
                }
            }
            return results.toString();
        } finally {
            session.close();
        }
    }

    /**
     * Finds all addreses on a road, filtered by housenumber or bnumber.
     * Only current data is included.
     *
     * @param request HTTP request containing a road parameter,
     *                and optionally a house parameter or bnr parameter
     * @return Json-formatted string containing a list of found objects
     */
    @RequestMapping("/adresse")
    public void getAddresses(HttpServletRequest request, HttpServletResponse response) throws DataFordelerException, IOException {
        String payload = this.getAddresses(request);
        setHeaders(response);
        response.getWriter().write(payload);
    }

    public String getAddresses(HttpServletRequest request) throws DataFordelerException {
        String roadUUID = request.getParameter(PARAM_ROAD);
        String houseNumber = request.getParameter(PARAM_HOUSE);
        String buildingNumber = request.getParameter(PARAM_BNR);
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        log.info(
                "Incoming REST request for AddressService.address with road {}, houseNumber {}, bNumber {}", roadUUID, houseNumber, buildingNumber
        );
        checkParameterExistence(PARAM_ROAD, roadUUID);
        UUID road = parameterAsUUID(PARAM_ROAD, roadUUID);

        Session session = sessionManager.getSessionFactory().openSession();
        try {

            AddressQuery query = new AddressQuery();
            setQueryNow(query);
            setQueryNoLimit(query);
            query.setRoad(road.toString());
            if (houseNumber != null && !houseNumber.trim().isEmpty()) {
                houseNumber = houseNumber.trim();
                query.addHouseNumber(houseNumber);
                query.addHouseNumber("0" + houseNumber);
                query.addHouseNumber("00" + houseNumber);
            }
            if (buildingNumber != null && !buildingNumber.trim().isEmpty()) {
                BNumberQuery bNumberQuery = new BNumberQuery();
                bNumberQuery.setCode(buildingNumber.trim());
                List<BNumberEntity> bNumberEntities = QueryManager.getAllEntities(session, bNumberQuery, BNumberEntity.class);
                if (bNumberEntities.isEmpty()) {
                    // Queried bnumber not found - return no results
                    return "[]";
                }
                for (BNumberEntity bNumberEntity : bNumberEntities) {
                    query.addBnr(bNumberEntity.getUUID().toString());
                }
            }
            // We only get bnumber references here, and must look them up in the bnumber table
            List<AddressEntity> addressEntities = QueryManager.getAllEntities(session, query, AddressEntity.class);
            ArrayNode results = objectMapper.createArrayNode();
            if (!addressEntities.isEmpty()) {
                HashMap<Identification, BNumberEntity> bNumberMap = getBNumbers(session, addressEntities);

                for (AddressEntity addressEntity : addressEntities) {
                    Set<DataItem> addressDataItems = addressEntity.getCurrent();
                    ObjectNode addressNode = objectMapper.createObjectNode();
                    addressNode.put(OUTPUT_UUID, addressEntity.getUUID().toString());
                    addressNode.set(OUTPUT_HOUSENUMBER, null);
                    addressNode.set(OUTPUT_FLOOR, null);
                    addressNode.set(OUTPUT_DOOR, null);
                    addressNode.set(OUTPUT_BNUMBER, null);
                    addressNode.set(OUTPUT_RESIDENCE, null);
                    for (DataItem dataItem : addressDataItems) {
                        AddressData addressData = (AddressData) dataItem;
                        if (addressData.getHouseNumber() != null) {
                            addressNode.put(OUTPUT_HOUSENUMBER, addressData.getHouseNumber());
                        }
                        if (addressData.getFloor() != null && !addressData.getFloor().isEmpty()) {
                            addressNode.put(OUTPUT_FLOOR, addressData.getFloor());
                        }
                        if (addressData.getRoom() != null && !addressData.getRoom().isEmpty()) {
                            addressNode.put(OUTPUT_DOOR, addressData.getRoom());
                        }
                        if (addressData.getbNumber() != null) {
                            BNumberEntity bNumberEntity = bNumberMap.get(addressData.getbNumber());
                            if (bNumberEntity != null) {
                                for (DataItem bNumberDataItem : bNumberEntity.getCurrent()) {
                                    BNumberData bNumberData = (BNumberData) bNumberDataItem;
                                    if (bNumberData.getCode() != null) {
                                        addressNode.put(OUTPUT_BNUMBER, bNumberData.getCode());
                                    }
                                }
                            }
                        }
                    }
                    results.add(addressNode);
                }
            }

            return results.toString();
        } finally {
            session.close();
        }
    }

    /**
     * Finds all addreses on a road, filtered by housenumber or bnumber.
     * Only current data is included.
     *
     * @param request HTTP request containing a road parameter,
     *                and optionally a house parameter or bnr parameter
     * @return Json-formatted string containing a list of found objects
     */
    @RequestMapping("/adresseoplysninger")
    public void getAddressData(HttpServletRequest request, HttpServletResponse response) throws DataFordelerException, IOException {
        String payload = this.getAddressData(request);
        setHeaders(response);
        response.getWriter().write(payload);
    }

    public String getAddressData(HttpServletRequest request) throws DataFordelerException {
        String addressUUID = request.getParameter(PARAM_ADDRESS);
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        log.info(
                "Incoming REST request for AddressService.addressdata with address {}", addressUUID
        );
        checkParameterExistence(PARAM_ADDRESS, addressUUID);
        UUID address = parameterAsUUID(PARAM_ADDRESS, addressUUID);

        Session session = sessionManager.getSessionFactory().openSession();
        try {
            // We only get bnumber references here, and must look them up in the bnumber table

            ObjectNode addressNode = objectMapper.createObjectNode();

            AddressEntity addressEntity = QueryManager.getEntity(session, address, AddressEntity.class);
            if (addressEntity != null) {
                HashMap<Identification, BNumberEntity> bNumberMap = getBNumbers(session, Collections.singletonList(addressEntity));
                HashMap<Identification, RoadEntity> roadMap = getRoads(session, Collections.singletonList(addressEntity));
                HashMap<Identification, LocalityEntity> localityMap = getLocalities(session, roadMap.values());

                addressNode.put(OUTPUT_UUID, addressEntity.getUUID().toString());
                addressNode.set(OUTPUT_HOUSENUMBER, null);
                addressNode.set(OUTPUT_FLOOR, null);
                addressNode.set(OUTPUT_DOOR, null);
                addressNode.set(OUTPUT_BNUMBER, null);
                addressNode.set(OUTPUT_ROADUUID, null);
                addressNode.set(OUTPUT_ROADCODE, null);
                addressNode.set(OUTPUT_ROADNAME, null);
                addressNode.set(OUTPUT_LOCALITYUUID, null);
                addressNode.set(OUTPUT_LOCALITYNAME, null);
                addressNode.set(OUTPUT_MUNICIPALITYCODE, null);
                addressNode.set(OUTPUT_RESIDENCE, null);
                for (DataItem dataItem : addressEntity.getCurrent()) {
                    AddressData addressData = (AddressData) dataItem;
                    if (addressData.getHouseNumber() != null) {
                        addressNode.put(OUTPUT_HOUSENUMBER, addressData.getHouseNumber());
                    }
                    if (addressData.getFloor() != null && !addressData.getFloor().isEmpty()) {
                        addressNode.put(OUTPUT_FLOOR, addressData.getFloor());
                    }
                    if (addressData.getRoom() != null && !addressData.getRoom().isEmpty()) {
                        addressNode.put(OUTPUT_DOOR, addressData.getRoom());
                    }
                    if (addressData.getResidence() != null) {
                        addressNode.put(OUTPUT_RESIDENCE, addressData.getResidence());
                    }
                    if (addressData.getbNumber() != null) {
                        BNumberEntity bNumberEntity = bNumberMap.get(addressData.getbNumber());
                        if (bNumberEntity != null) {
                            for (DataItem bNumberDataItem : bNumberEntity.getCurrent()) {
                                BNumberData bNumberData = (BNumberData) bNumberDataItem;
                                if (bNumberData.getCode() != null) {
                                    addressNode.put(OUTPUT_BNUMBER, bNumberData.getCode());
                                }
                            }
                        }
                    }
                    if (addressData.getRoad() != null && roadMap.keySet().contains(addressData.getRoad())) {
                        RoadEntity roadEntity = roadMap.get(addressData.getRoad());
                        if (roadEntity != null) {
                            addressNode.put(OUTPUT_ROADUUID, roadEntity.getUUID().toString());
                            for (DataItem roadDataItem : roadEntity.getCurrent()) {
                                RoadData roadData = (RoadData) roadDataItem;
                                if (roadData.getCode() != 0) {
                                    addressNode.put(OUTPUT_ROADCODE, roadData.getCode());
                                }
                                if (roadData.getName() != null && !roadData.getName().isEmpty()) {
                                    addressNode.put(OUTPUT_ROADNAME, roadData.getName());
                                }
                                if (roadData.getLocation() != null) {
                                    LocalityEntity localityEntity = localityMap.get(roadData.getLocation());
                                    if (localityEntity != null) {
                                        addressNode.put(OUTPUT_LOCALITYUUID, localityEntity.getUUID().toString());
                                        for (DataItem localityDataItem : localityEntity.getCurrent()) {
                                            LocalityData localityData = (LocalityData) localityDataItem;
                                            if (localityData.getName() != null && !localityData.getName().isEmpty()) {
                                                addressNode.put(OUTPUT_LOCALITYNAME, localityData.getName());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // No need to look up in the municipality table, because we already loaded all municipalities
                    if (addressData.getMunicipality() != null) {
                        for (Map.Entry<Integer, UUID> municipalityEntry : this.municipalities.entrySet()) {
                            if (municipalityEntry.getValue() != null && municipalityEntry.getValue().equals(addressData.getMunicipality().getUuid())) {
                                addressNode.put(OUTPUT_MUNICIPALITYCODE, municipalityEntry.getKey());
                                break;
                            }
                        }
                    }
                }
            }
            return addressNode.toString();
        } finally {
            session.close();
        }
    }

    private static HashMap<Identification, BNumberEntity> getBNumbers(Session session, Collection<AddressEntity> addressEntities) {
        HashSet<Identification> bNumbers = new HashSet<>();
        for (AddressEntity addressEntity : addressEntities) {
            Set<DataItem> addressDataItems = addressEntity.getCurrent();
            for (DataItem dataItem : addressDataItems) {
                AddressData data = (AddressData) dataItem;
                if (data.getbNumber() != null) {
                    bNumbers.add(data.getbNumber());
                }
            }
        }
        return getBNumbers(session, bNumbers);
    }

    private static HashMap<Identification, BNumberEntity> getBNumbers(Session session, HashSet<Identification> identifications) {
        HashMap<Identification, BNumberEntity> bNumberMap = new HashMap<>();
        if (!identifications.isEmpty()) {
            org.hibernate.query.Query<Object[]> bQuery = session.createQuery(
                    "SELECT DISTINCT e, e.identification FROM " + BNumberEntity.class.getCanonicalName() + " e " +
                            "WHERE e.identification in (:identifications)"
            );
            bQuery.setParameterList("identifications", identifications);

            for (Object[] resultItem : bQuery.getResultList()) {
                BNumberEntity bNumberEntity = (BNumberEntity) resultItem[0];
                Identification identification = (Identification) resultItem[1];
                bNumberMap.put(identification, bNumberEntity);
            }
        }
        return bNumberMap;
    }


    private static HashMap<Identification, RoadEntity> getRoads(Session session, Collection<AddressEntity> addressEntities) {
        HashSet<Identification> identifications = new HashSet<>();
        for (AddressEntity addressEntity : addressEntities) {
            Set<DataItem> addressDataItems = addressEntity.getCurrent();
            for (DataItem dataItem : addressDataItems) {
                AddressData data = (AddressData) dataItem;
                if (data.getRoad() != null) {
                    identifications.add(data.getRoad());
                }
            }
        }
        return getRoads(session, identifications);
    }

    private static HashMap<Identification, RoadEntity> getRoads(Session session, HashSet<Identification> identifications) {
        HashMap<Identification, RoadEntity> roadMap = new HashMap<>();
        if (!identifications.isEmpty()) {
            org.hibernate.query.Query<Object[]> bQuery = session.createQuery(
                    "SELECT DISTINCT e, e.identification FROM " + RoadEntity.class.getCanonicalName() + " e " +
                            "WHERE e.identification in (:identifications)"
            );
            bQuery.setParameterList("identifications", identifications);

            for (Object[] resultItem : bQuery.getResultList()) {
                RoadEntity roadEntity = (RoadEntity) resultItem[0];
                Identification identification = (Identification) resultItem[1];
                roadMap.put(identification, roadEntity);
            }
        }
        return roadMap;
    }

    private static HashMap<Identification, LocalityEntity> getLocalities(Session session, Collection<RoadEntity> roadEntities) {
        HashSet<Identification> identifications = new HashSet<>();
        for (RoadEntity roadEntity : roadEntities) {
            Set<DataItem> roadDataItems = roadEntity.getCurrent();
            for (DataItem dataItem : roadDataItems) {
                RoadData data = (RoadData) dataItem;
                if (data.getLocation() != null) {
                    identifications.add(data.getLocation());
                }
            }
        }
        return getLocalities(session, identifications);
    }

    private static HashMap<Identification, LocalityEntity> getLocalities(Session session, HashSet<Identification> identifications) {
        HashMap<Identification, LocalityEntity> localityMap = new HashMap<>();
        if (!identifications.isEmpty()) {
            org.hibernate.query.Query<Object[]> bQuery = session.createQuery(
                    "SELECT DISTINCT e, e.identification FROM " + LocalityEntity.class.getCanonicalName() + " e " +
                            "WHERE e.identification in (:identifications)"
            );
            bQuery.setParameterList("identifications", identifications);

            for (Object[] resultItem : bQuery.getResultList()) {
                LocalityEntity localityEntity = (LocalityEntity) resultItem[0];
                Identification identification = (Identification) resultItem[1];
                localityMap.put(identification, localityEntity);
            }
        }
        return localityMap;
    }


    private static void checkParameterExistence(String name, String value) throws MissingParameterException {
        if (value == null || value.trim().isEmpty()) {
            throw new MissingParameterException(name);
        }
    }

    private static int parameterAsInt(String name, String value) throws InvalidClientInputException {
        try {
            return Integer.parseInt(value, 10);
        } catch (NumberFormatException e) {
            throw new InvalidClientInputException("Parameter " + name + " must be a number", e);
        }
    }

    private static UUID parameterAsUUID(String name, String value) throws InvalidClientInputException {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new InvalidClientInputException("Parameter " + name + " must be a uuid", e);
        }
    }

    private static void setQueryNow(Query query) {
        OffsetDateTime now = OffsetDateTime.now();
        query.setRegistrationFrom(now);
        query.setRegistrationTo(now);
        query.setEffectFrom(now);
        query.setEffectTo(now);
    }

    private static void setQueryNoLimit(Query query) {
        query.setPage(1);
        query.setPageSize(Integer.MAX_VALUE);
    }

    private static void setHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Content-Type", "application/json; charset=utf-8");
    }
}
