package dk.magenta.datafordeler.geo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.exception.MissingParameterException;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.DoubleListHashMap;
import dk.magenta.datafordeler.core.util.ListHashMap;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import dk.magenta.datafordeler.geo.data.accessaddress.*;
import dk.magenta.datafordeler.geo.data.common.GeoMonotemporalRecord;
import dk.magenta.datafordeler.geo.data.locality.*;
import dk.magenta.datafordeler.geo.data.municipality.GeoMunicipalityEntity;
import dk.magenta.datafordeler.geo.data.road.*;
import dk.magenta.datafordeler.geo.data.unitaddress.UnitAddressDoorRecord;
import dk.magenta.datafordeler.geo.data.unitaddress.UnitAddressEntity;
import dk.magenta.datafordeler.geo.data.unitaddress.UnitAddressFloorRecord;
import dk.magenta.datafordeler.geo.data.unitaddress.UnitAddressUsageRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.persistence.FlushModeType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController("GeoAdresseService")
@RequestMapping("/geo/adresse")
public class AdresseService {

    @Autowired
    SessionManager sessionManager;

    @Autowired
    private DafoUserManager dafoUserManager;

    @Autowired
    private ObjectMapper objectMapper;

    private Logger log = LogManager.getLogger(AdresseService.class);

    public static final String PARAM_DEBUG = "debug";

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
    public static final String OUTPUT_LOCALITYROADCODE = "lokalitetvejkode";
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
    public static final String OUTPUT_USAGE = "anvendelse";



    HashMap<Integer, UUID> municipalities = new HashMap<>();

    /**
     * Load known municipalities into a local map of municipalityCode: UUID
     */
    @PostConstruct
    public void loadMunicipalities() {
        Session session = sessionManager.getSessionFactory().openSession();
        try {
            List<GeoMunicipalityEntity> municipalities = QueryManager.getAllEntities(session, GeoMunicipalityEntity.class);
            for (GeoMunicipalityEntity municipality : municipalities) {
                this.municipalities.put(municipality.getCode(), municipality.getIdentification().getUuid());
            }
        } finally {
            session.close();
        }
    }


    /**
     * Finds all localities in a municipality. Only current data is included.
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
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.info(
                "Incoming REST request for AddressService.locality with municipality {}", municipalityCode
        );
        checkParameterExistence(PARAM_MUNICIPALITY, municipalityCode);
        int code = parameterAsInt(PARAM_MUNICIPALITY, municipalityCode);
        return this.getLocalities(Integer.toString(code));
    }

    public String getLocalities(String municipality) {
        LocalityQuery query = new LocalityQuery();
        setQueryNow(query);
        setQueryNoLimit(query);
        query.setMunicipality(municipality);
        Session session = sessionManager.getSessionFactory().openSession();
        try {
            List<GeoLocalityEntity> localities = QueryManager.getAllEntities(session, query, GeoLocalityEntity.class);
            ArrayNode results = objectMapper.createArrayNode();
            for (GeoLocalityEntity locality : localities) {
                ObjectNode localityNode = objectMapper.createObjectNode();
                localityNode.put(OUTPUT_UUID, locality.getUUID().toString());

                LocalityNameRecord nameRecord = current(locality.getName());
                if (nameRecord != null) {
                    localityNode.put(OUTPUT_NAME, nameRecord.getName());
                }
                LocalityAbbreviationRecord abbreviationRecord = current(locality.getAbbreviation());
                if (abbreviationRecord != null) {
                    localityNode.put(OUTPUT_ABBREVIATION, abbreviationRecord.getName());
                }
                LocalityRoadcodeRecord localityRoadcodeRecord = current(locality.getLocalityRoadcode());
                if (localityRoadcodeRecord != null) {
                    localityNode.put(OUTPUT_LOCALITYROADCODE, localityRoadcodeRecord.getCode());
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
        String locality = request.getParameter(PARAM_LOCALITY);
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.info(
                "Incoming REST request for AddressService.road with locality {}", locality
        );
        checkParameterExistence(PARAM_LOCALITY, locality);
        return this.getRoads(parameterAsUUID(PARAM_LOCALITY, locality));
    }

    public String getRoads(UUID locality) {
        RoadQuery query = new RoadQuery();
        setQueryNow(query);
        setQueryNoLimit(query);
        query.setLocalityUUID(locality);
        Session session = sessionManager.getSessionFactory().openSession();
        try {

            org.hibernate.query.Query databaseQuery = session.createQuery(
                    "SELECT DISTINCT road FROM " + GeoRoadEntity.class.getCanonicalName() + " road " +
                        "JOIN road.locality locality " +
                        "JOIN locality.reference locality_reference " +

                        //"JOIN " + AccessAddressRoadRecord.class.getCanonicalName() + " access_road ON access_road.reference = road.identification " +
                        "LEFT JOIN road.municipality road_municipality "+
                        "LEFT JOIN "+AccessAddressRoadRecord.class.getCanonicalName()+" access_road ON access_road.roadCode = road.code AND access_road.municipalityCode = road_municipality.code "+

                        "JOIN " + AccessAddressEntity.class.getCanonicalName() + " access ON access_road.entity = access " +
                        "JOIN " + UnitAddressEntity.class.getCanonicalName() + " unit ON unit.accessAddress = access.identification " +
                        "JOIN unit.usage unit_usage " +
                        "WHERE locality_reference.uuid = :uuid "+
                        "AND road.code != null " +
                        "AND road.code != 0 "
                        //"AND unit_usage.usage = 1 "
            );
            databaseQuery.setParameter("uuid", locality);

            ArrayNode results = objectMapper.createArrayNode();
            ListHashMap<String, GeoRoadEntity> roadMap = new ListHashMap<>();
            for (Object result : databaseQuery.getResultList()) {
                GeoRoadEntity geoRoadEntity = (GeoRoadEntity) result;
                for (RoadNameRecord nameRecord : geoRoadEntity.getName()) {
                    if (nameRecord.getRegistrationTo() == null) {
                        String nameValue = nameRecord.getName();
                        roadMap.add(nameValue != null ? nameValue.trim() : null, geoRoadEntity);
                    }
                }
            }

            if (roadMap.size() == 0 || (roadMap.size() == 1 && roadMap.keySet().contains(null))) {
                GeoLocalityEntity geoLocalityEntity = QueryManager.getEntity(session, locality, GeoLocalityEntity.class);
                if (geoLocalityEntity != null) {
                    ObjectNode roadNode = objectMapper.createObjectNode();
                    LocalityNameRecord localityNameRecord = current(geoLocalityEntity.getName());
                    if (localityNameRecord != null) {
                        roadNode.put(OUTPUT_NAME, localityNameRecord.getName());
                    }
                    LocalityRoadcodeRecord localityRoadcodeRecord = current(geoLocalityEntity.getLocalityRoadcode());
                    if (localityRoadcodeRecord != null) {
                        roadNode.put(OUTPUT_ROADCODE, localityRoadcodeRecord.getCode());
                    }
                    roadNode.put(OUTPUT_UUID, locality.toString());
                    results.add(roadNode);
                }
            }

            ArrayList<String> roadNames = new ArrayList<>(roadMap.keySet());
            roadNames.sort(Comparator.nullsLast(Comparator.naturalOrder()));

            for (String roadName : roadNames) {
                if (roadName != null) {
                    ObjectNode roadNode = objectMapper.createObjectNode();
                    roadNode.put(OUTPUT_NAME, roadName);

                    boolean hasUUID = false;
                    for (GeoRoadEntity road : roadMap.get(roadName)) {
                        RoadNameRecord nameRecord = current(road.getName());
                        if (nameRecord != null) {
                            String altName = nameRecord.getAddressingName();
                            roadNode.put(OUTPUT_ALTNAME, altName != null ? altName.trim() : null);
                        }
                        if (road.getCode() != 0) {
                            roadNode.put(OUTPUT_ROADCODE, road.getCode());
                        }
                        RoadMunicipalityRecord municipalityRecord = current(road.getMunicipality());
                        if (municipalityRecord != null) {
                            roadNode.put(OUTPUT_MUNICIPALITYCODE, municipalityRecord.getCode());
                        }
                        if (!hasUUID && road.getUUID() != null) {
                            roadNode.put(OUTPUT_UUID, road.getUUID().toString());
                            hasUUID = true;
                        }
                    }
                    results.add(roadNode);
                }
            }
            return results.toString();
        } finally {
            session.close();
        }
    }

    private Set<UUID> getWholeRoad(Session session, UUID roadSegment) {
        HashSet<UUID> uuids = new HashSet<>();
        uuids.add(roadSegment);
        GeoRoadEntity geoRoadEntity = QueryManager.getEntity(session, roadSegment, GeoRoadEntity.class);
        if (geoRoadEntity != null) {
            String localityCode = null;
            for (RoadLocalityRecord localityRecord : geoRoadEntity.getLocality()) {
                if (localityRecord.getRegistrationTo() == null) {
                    localityCode = localityRecord.getCode();
                    break;
                }
            }
            String name = null;
            int code = geoRoadEntity.getCode();
            if (code == 0) {
                for (RoadNameRecord nameRecord : geoRoadEntity.getName()) {
                    if (nameRecord.getRegistrationTo() == null) {
                        name = nameRecord.getName();
                        break;
                    }
                }
            }

            if (localityCode != null && (code != 0 || name != null)) {
                RoadQuery query = new RoadQuery();
                query.setPageSize(500);
                query.setLocality(localityCode);
                if (code != 0) {
                    query.setCode(Integer.toString(code));
                }
                if (name != null) {
                    query.setName(name);
                }

                for (GeoRoadEntity geoRoadEntity1 : QueryManager.getAllEntities(session, query, GeoRoadEntity.class)) {
                    UUID u = geoRoadEntity1.getUUID();
                    if (u != null) {
                        uuids.add(u);
                    }
                }
            }
        }
        return uuids;
    }



    /**
     * Finds all buildings on a road. Only current data is included.
     * @param request HTTP request containing a road parameter
     * @return Json-formatted string containing a list of found objects
     */
    @RequestMapping("/hus")
    public void getAccessAddresses(HttpServletRequest request, HttpServletResponse response) throws DataFordelerException, IOException {
        String payload = this.getAccessAddresses(request);
        setHeaders(response);
        response.getWriter().write(payload);
    }

    public String getAccessAddresses(HttpServletRequest request) throws DataFordelerException {
        String roadUUID = request.getParameter(PARAM_ROAD);
        boolean debug = "1".equals(request.getParameter(PARAM_DEBUG));
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.info(
                "Incoming REST request for AddressService.building with road {}", roadUUID
        );
        checkParameterExistence(PARAM_ROAD, roadUUID);
        UUID road = parameterAsUUID(PARAM_ROAD, roadUUID);
        return this.getAccessAddresses(road, debug);
    }

    public String getAccessAddresses(UUID road, boolean debug) {
        Session session = sessionManager.getSessionFactory().openSession();

        StringJoiner where = new StringJoiner(" AND ");
        where.add("unit_usage.usage = 1");

        ArrayList<UUID> segments = new ArrayList<>(this.getWholeRoad(session, road));
        where.add("(road_identification.uuid IN :road OR locality_identification.uuid IN :road)");

        org.hibernate.query.Query databaseQuery = session.createQuery(
                "SELECT DISTINCT access FROM " + UnitAddressEntity.class.getCanonicalName() + " unit " +
                        "JOIN unit.usage unit_usage " +
                        "LEFT JOIN " + AccessAddressEntity.class.getCanonicalName() + " access ON unit.accessAddress = access.identification " +
                        "LEFT JOIN access.road access_road " +

                        "LEFT JOIN access_road.reference road_identification " +
                        //"LEFT JOIN " + GeoRoadEntity.class.getCanonicalName() + " road on road.code = access_road.roadCode " +
                        //"JOIN " + RoadMunicipalityRecord.class.getCanonicalName() + " road_municipality on road_municipality.entity = road and road_municipality.code = access_road.municipalityCode " +
                        //"LEFT JOIN road.identification road_identification " +

                        "LEFT JOIN access.locality access_locality " +
                        "LEFT JOIN access_locality.reference locality_identification " +
                        "WHERE " + where.toString() + " " +
                        "order by access.bnr"
        );



        databaseQuery.setParameterList("road", segments);

        HashSet<String> bnrs = new HashSet<>();
        ArrayNode results = objectMapper.createArrayNode();

        DoubleListHashMap<String, String, ObjectNode> houseNumberMap = new DoubleListHashMap<>();


        // TODO: Fjern kun bagvedstillet bogstav hvis længde > 3
        try {
            for (Object result : databaseQuery.getResultList()) {
                AccessAddressEntity addressEntity = (AccessAddressEntity) result;
                String bnr = stripBnr(addressEntity.getBnr(), true);
                //if (!bnrs.contains(bnr)) {
                    AccessAddressHouseNumberRecord houseNumber = current(addressEntity.getHouseNumber());
                    String houseNumberValue = null;
                    if (houseNumber != null) {
                        houseNumberValue = houseNumber.getNumber();
                    }
                    if ((!"0".equals(houseNumberValue) && bnr != null) || debug) {
                        ObjectNode addressNode = objectMapper.createObjectNode();
                        addressNode.put(OUTPUT_BNUMBER, bnr);
                        addressNode.put(OUTPUT_HOUSENUMBER, houseNumberValue);
                        AccessAddressBlockNameRecord blockName = current(addressEntity.getBlockName());
                        addressNode.set(OUTPUT_BCALLNAME, null);
                        if (blockName != null) {
                            addressNode.put(OUTPUT_BCALLNAME, blockName.getName());
                        }
                        bnrs.add(bnr);
                        houseNumberMap.add(houseNumberValue, bnr, addressNode);
                        if (debug && !(!"0".equals(houseNumberValue) && bnr != null)) {
                            addressNode.put("comment1", "Excluded due to missing housenumber and bnr");
                        }
                        if (debug) {
                            addressNode.put("accessAddress_objectId", addressEntity.getObjectId());
                        }
                    }
                //}
            }

            // If a number exist with different BNRs, remove both
            for (String houseNumber : houseNumberMap.keySet()) {
                HashMap<String, ArrayList<ObjectNode>> housesByBnr = houseNumberMap.get(houseNumber);
                if (housesByBnr.size() == 1 || debug) {
                    for (String bnr : housesByBnr.keySet()) {
                        for (ObjectNode node : housesByBnr.get(bnr)) {
                            if (debug && housesByBnr.size() > 1) {
                                node.put("comment2", "Excluded due to collision on housenumber "+houseNumber+", colliding: ["+housesByBnr.keySet()+"]");
                            }
                            results.add(node);
                        }
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
     * @param request HTTP request containing a road parameter,
     *                and optionally a house parameter or bnr parameter
     * @return Json-formatted string containing a list of found objects
     */
    @RequestMapping("/adresse")
    public void getUnitAddresses(HttpServletRequest request, HttpServletResponse response) throws DataFordelerException, IOException {
        String payload = this.getUnitAddresses(request);
        setHeaders(response);
        response.getWriter().write(payload);
    }

    public String getUnitAddresses(HttpServletRequest request) throws DataFordelerException {
        String roadUUID = request.getParameter(PARAM_ROAD);
        String houseNumber = request.getParameter(PARAM_HOUSE);
        String buildingNumber = request.getParameter(PARAM_BNR);
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.info(
                "Incoming REST request for AddressService.address with road {}, houseNumber {}, bNumber {}", roadUUID, houseNumber, buildingNumber
        );
        checkParameterExistence(PARAM_ROAD, roadUUID);
        UUID road = parameterAsUUID(PARAM_ROAD, roadUUID);
        return this.getUnitAddresses(road, houseNumber, buildingNumber);
    }

    Pattern endsWithLetter = Pattern.compile(".*[a-z]$", Pattern.CASE_INSENSITIVE);

    public String getUnitAddresses(UUID roadUUID, String houseNumber, String buildingNumber) {
        if (houseNumber != null && houseNumber.trim().isEmpty()) {
            houseNumber = null;
        }
        if (buildingNumber != null && buildingNumber.trim().isEmpty()) {
            buildingNumber = null;
        }
        Session session = sessionManager.getSessionFactory().openSession();
        ArrayNode results = objectMapper.createArrayNode();
        try {
            AccessAddressQuery query = new AccessAddressQuery();
            setQueryNow(query);
            setQueryNoLimit(query);

            StringJoiner where = new StringJoiner(" AND ");
            where.add("unit_usage.usage = 1");

            String roadQueryPart = "";
            if (roadUUID != null) {
                for (UUID uuid : this.getWholeRoad(session, roadUUID)) {
                    query.addRoadUUID(uuid);
                }
                roadQueryPart = "LEFT JOIN access.road access_road " +

                        "LEFT JOIN access_road.reference road_identification " +
                        //"LEFT JOIN " + GeoRoadEntity.class.getCanonicalName() + " road on road.code = access_road.roadCode " +
                        //"JOIN " + RoadMunicipalityRecord.class.getCanonicalName() + " road_municipality on road_municipality.entity = road and road_municipality.code = access_road.municipalityCode " +
                        //"LEFT JOIN road.identification road_identification " +


                        "LEFT JOIN access.locality access_locality " +
                        "LEFT JOIN access_locality.reference locality_identification ";
                where.add("(road_identification.uuid IN :road OR locality_identification.uuid IN :road)");
            }

            String houseNumberQueryPart = "LEFT JOIN access.houseNumber houseNumber ";
            if (houseNumber != null) {
                houseNumber = houseNumber.trim();
                query.addHouseNumber(houseNumber);
                query.addHouseNumber("0" + houseNumber);
                query.addHouseNumber("00" + houseNumber);
                where.add("houseNumber.number IN :hnr");
            } else {
                where.add("houseNumber.number != '0'");
            }
            if (buildingNumber != null) {
                String strippedBnr = prefixBnr(buildingNumber.trim());
                query.addBnr(strippedBnr);
                Matcher m = endsWithLetter.matcher(strippedBnr);
                if (!m.find()) {
                    query.addBnr(strippedBnr + "A");
                    query.addBnr(strippedBnr + "B");
                    query.addBnr(strippedBnr + "C");
                    query.addBnr(strippedBnr + "D");
                    query.addBnr(strippedBnr + "E");
                    query.addBnr(strippedBnr + "F");
                }
                where.add("access.bnr IN :bnr");
            } else {
                where.add("(access.bnr is null or access.bnr != 'B-0000')");
            }

            org.hibernate.query.Query databaseQuery = session.createQuery(
                    "SELECT DISTINCT unit, access FROM " + UnitAddressEntity.class.getCanonicalName() + " unit " +
                       "JOIN unit.usage unit_usage " +
                       "LEFT JOIN " + AccessAddressEntity.class.getCanonicalName() + " access ON unit.accessAddress = access.identification " +
                       roadQueryPart +
                       houseNumberQueryPart +
                       "WHERE " + where.toString() + " " +
                       "order by access.bnr"
            );

            if (roadUUID != null) {
                databaseQuery.setParameterList("road", query.getRoadUUID());
            }
            if (houseNumber != null) {
                databaseQuery.setParameterList("hnr", query.getHouseNumber());
            }
            if (buildingNumber != null) {
                databaseQuery.setParameterList("bnr", query.getBnr());
            }
            databaseQuery.setFlushMode(FlushModeType.COMMIT);

            DoubleListHashMap<String, String, ObjectNode> houseNumberMap = new DoubleListHashMap<>();

            HashSet<String> existing = new HashSet<>();
            for (Object result : databaseQuery.getResultList()) {
                Object[] resultItems = (Object[]) result;
                UnitAddressEntity unitAddressEntity = (UnitAddressEntity) resultItems[0];
                AccessAddressEntity accessAddressEntity = (AccessAddressEntity) resultItems[1];

                ObjectNode addressNode = objectMapper.createObjectNode();
                String houseNumberValue = null;
                String floorValue = null;
                String doorValue = null;

                AccessAddressHouseNumberRecord houseNumberRecord = current(accessAddressEntity.getHouseNumber());
                if (houseNumberRecord != null) {
                    houseNumberValue = houseNumberRecord.getNumber();
                }
                UnitAddressFloorRecord floor = current(unitAddressEntity.getFloor());
                if (floor != null) {
                    floorValue = floor.getFloor();
                }
                UnitAddressDoorRecord door = current(unitAddressEntity.getDoor());
                if (door != null) {
                    doorValue = door.getDoor();
                }
                String bnr = accessAddressEntity.getBnr();

                String key = bnr + "|" + floorValue + "|" + doorValue;

                if (!existing.contains(key)) {
                    existing.add(key);

                    if (floorValue != null && !floorValue.isEmpty()) {
                        addressNode.put(OUTPUT_FLOOR, floorValue);
                    }
                    if (doorValue != null && !doorValue.isEmpty()) {
                        addressNode.put(OUTPUT_DOOR, door.getDoor());
                    }

                    addressNode.put(OUTPUT_UUID, unitAddressEntity.getUUID().toString());
                    addressNode.put(OUTPUT_HOUSENUMBER, houseNumberValue);

                    AccessAddressBlockNameRecord blockname = current(accessAddressEntity.getBlockName());
                    if (blockname != null) {
                        addressNode.put(OUTPUT_BCALLNAME, blockname.getName());
                    }

                    addressNode.put(OUTPUT_BNUMBER, stripBnr(bnr, true));
                    if (doorValue == null || doorValue.isEmpty()) {
                        String bnrDoor = bnrExtraLetter(bnr);
                        if (bnrDoor != null) {
                            addressNode.put(OUTPUT_DOOR, bnrDoor);
                        }
                    }


                    UnitAddressUsageRecord usage = current(unitAddressEntity.getUsage());
                    if (usage != null) {
                        addressNode.put(OUTPUT_USAGE, usage.getUsage());
                    }

                    houseNumberMap.add(houseNumberValue, bnr, addressNode);
                    //results.add(addressNode);
                }
            }

            // If a number exist with different BNRs, remove both
            List<String> numbers = new ArrayList<>(houseNumberMap.keySet());
            numbers.sort(fuzzyNumberComparator);

            for (String number : numbers) {
                HashMap<String, ArrayList<ObjectNode>> housesByBnr = houseNumberMap.get(number);
                if (housesByBnr.size() == 1) {

                    List<String> bnrs = new ArrayList<>(housesByBnr.keySet());
                    bnrs.sort(String::compareToIgnoreCase);

                    for (String bnr : bnrs) {
                        ArrayList<ObjectNode> houses = housesByBnr.get(bnr);
                        houses.sort(
                                Comparator.nullsFirst(
                                        Comparator.<ObjectNode, String>comparing(
                                                jsonNode -> jsonNode.get(OUTPUT_FLOOR) != null ? jsonNode.get(OUTPUT_FLOOR).textValue() : null,
                                                Comparator.nullsFirst(fuzzyNumberComparator)
                                        )
                                ).thenComparing(
                                        Comparator.nullsFirst(
                                                Comparator.<ObjectNode, String>comparing(
                                                        jsonNode -> jsonNode.get(OUTPUT_DOOR) != null ? jsonNode.get(OUTPUT_DOOR).textValue() : null,
                                                        Comparator.nullsFirst(fuzzyNumberComparator)
                                                )
                                        )
                                )
                        );
                        for (ObjectNode node : houses) {
                            results.add(node);
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
        return results.toString();
    }

    private static Pattern numberPattern = Pattern.compile("^(\\d+).*$");
    private static final Integer extractNumber(String str) {
        Matcher m = numberPattern.matcher(str);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1), 10);
            } catch (NumberFormatException e) {}
        }
        return null;
    }
    public static final Comparator<String> fuzzyNumberComparator = (o1, o2) -> {
        if (o1 == null && o2 == null) return 0;
        Integer i1 = extractNumber(o1);
        Integer i2 = extractNumber(o2);
        if (i1 != null && i2 != null) {
            int r = Integer.compare(i1, i2);
            if (r != 0) return r;
        }
        return o1 == null ? -1 : o1.compareToIgnoreCase(o2);
    };

    /**
     * Finds more detailed data on unit address
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
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.info(
                "Incoming REST request for AddressService.addressdata with address {}", addressUUID
        );
        checkParameterExistence(PARAM_ADDRESS, addressUUID);
        UUID address = parameterAsUUID(PARAM_ADDRESS, addressUUID);
        return this.getAddressData(address);
    }

    public String getAddressData(UUID unitAddressUUID) {
        Session session = sessionManager.getSessionFactory().openSession();
        try {
            // We only get bnumber references here, and must look them up in the bnumber table

            ObjectNode addressNode = objectMapper.createObjectNode();

            org.hibernate.query.Query databaseQuery = session.createQuery(
                    "SELECT DISTINCT unit, access, road, locality "+
                    "FROM "+UnitAddressEntity.class.getCanonicalName()+" unit "+
                    "JOIN unit.identification unit_identification "+

                    "LEFT JOIN "+AccessAddressEntity.class.getCanonicalName()+" access ON unit.accessAddress = access.identification "+

                    "LEFT JOIN access.road access_road "+
                    "LEFT JOIN "+ GeoRoadEntity.class.getCanonicalName()+" road ON access_road.reference = road.identification "+
                    //"LEFT JOIN "+RoadMunicipalityRecord.class.getCanonicalName()+" road_municipality ON road_municipality.code = access_road.municipalityCode "+
                    //"LEFT JOIN "+GeoRoadEntity.class.getCanonicalName()+" road ON access_road.roadCode = road.code AND road_municipality.entity = road "+

                    "LEFT JOIN access.locality access_locality "+
                    "LEFT JOIN "+ GeoLocalityEntity.class.getCanonicalName()+" locality ON access_locality.reference = locality.identification "+

                    "WHERE unit_identification.uuid = :uuid " +
                    "ORDER BY access.bnr"
            );
            databaseQuery.setParameter("uuid", unitAddressUUID);
            databaseQuery.setFlushMode(FlushModeType.COMMIT);


            for (Object result : databaseQuery.getResultList()) {
                Object[] results = (Object[]) result;
                UnitAddressEntity unitAddress = results.length > 0 ? (UnitAddressEntity) results[0] : null;
                AccessAddressEntity accessAddress = results.length > 1 ? (AccessAddressEntity) results[1] : null;
                GeoRoadEntity road = results.length > 2 ? (GeoRoadEntity) results[2] : null;
                GeoLocalityEntity locality = results.length > 3 ? (GeoLocalityEntity) results[3] : null;
                String doorValue = null;

                if (unitAddress != null) {
                    addressNode.put(OUTPUT_UUID, unitAddress.getUUID().toString());
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
                    addressNode.set(OUTPUT_USAGE, null);

                    UnitAddressFloorRecord floor = current(unitAddress.getFloor());
                    if (floor != null) {
                        String floorValue = floor.getFloor();
                        if (floorValue != null && !floorValue.isEmpty()) {
                            addressNode.put(OUTPUT_FLOOR, floorValue);
                        }
                    }
                    UnitAddressDoorRecord door = current(unitAddress.getDoor());
                    if (door != null) {
                        doorValue = door.getDoor();
                        if (doorValue != null && !doorValue.isEmpty()) {
                            addressNode.put(OUTPUT_DOOR, doorValue);
                        }
                    }
                    UnitAddressUsageRecord usage = current(unitAddress.getUsage());
                    if (usage != null) {
                        addressNode.put(OUTPUT_USAGE, usage.getUsage());
                    }

                    if (accessAddress != null) {
                        AccessAddressHouseNumberRecord houseNumber = current(accessAddress.getHouseNumber());
                        if (houseNumber != null) {
                            addressNode.put(OUTPUT_HOUSENUMBER, houseNumber.getNumber());
                        }

                        String bnr = accessAddress.getBnr();
                        addressNode.put(OUTPUT_BNUMBER, stripBnr(bnr, true));
                        if (doorValue == null || doorValue.isEmpty()) {
                            String bnrDoor = bnrExtraLetter(bnr);
                            if (bnrDoor != null) {
                                addressNode.put(OUTPUT_DOOR, bnrDoor);
                            }
                        }

                        AccessAddressBlockNameRecord blockName = current(accessAddress.getBlockName());
                        if (blockName != null) {
                            addressNode.put(OUTPUT_BCALLNAME, blockName.getName());
                        }
                    }

                    if (road != null) {
                        addressNode.put(OUTPUT_ROADUUID, road.getUUID().toString());
                        RoadNameRecord roadName = current(road.getName());
                        if (roadName != null) {
                            addressNode.put(OUTPUT_ROADNAME, roadName.getName());
                        }
                        RoadMunicipalityRecord municipality = current(road.getMunicipality());
                        if (municipality != null) {
                            addressNode.put(OUTPUT_MUNICIPALITYCODE, municipality.getCode());
                        }
                    }

                    if (road != null && road.getCode() != 0) {
                        addressNode.put(OUTPUT_ROADCODE, road.getCode());
                    } else if (accessAddress != null) {
                        AccessAddressRoadRecord roadRecord = current(accessAddress.getRoad());
                        if (roadRecord != null) {
                            addressNode.put(OUTPUT_ROADCODE, roadRecord.getRoadCode());
                            addressNode.put(OUTPUT_MUNICIPALITYCODE, roadRecord.getMunicipalityCode());
                        }
                    }

                    if (locality != null) {
                        addressNode.put(OUTPUT_LOCALITYUUID, locality.getUUID().toString());
                        LocalityNameRecord localityName = current(locality.getName());
                        if (localityName != null) {
                            addressNode.put(OUTPUT_LOCALITYNAME, localityName.getName());
                        }
                        LocalityRoadcodeRecord localityRoadcode = current(locality.getLocalityRoadcode());
                        if (localityRoadcode != null && localityRoadcode.getCode() != null && (addressNode.get(OUTPUT_ROADCODE) == null || addressNode.get(OUTPUT_ROADCODE).intValue() == 0)) {
                            addressNode.put(OUTPUT_ROADCODE, localityRoadcode.getCode());
                        }
                    }
                }
            }
            return addressNode.toString();
        } finally {
            session.close();
        }
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
            throw new InvalidClientInputException("Parameter "+name+" must be a number", e);
        }
    }

    private static UUID parameterAsUUID(String name, String value) throws InvalidClientInputException {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new InvalidClientInputException("Parameter "+name+" must be a uuid", e);
        }
    }

    private static void setQueryNow(BaseQuery query) {
        OffsetDateTime now = OffsetDateTime.now();
        query.setRegistrationFromBefore(now);
        query.setRegistrationToAfter(now);
        query.setEffectFromBefore(now);
        query.setEffectToAfter(now);
    }
    private static void setQueryNoLimit(BaseQuery query) {
        query.setPage(1);
        query.setPageSize(Integer.MAX_VALUE);
    }
    private static void setHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Content-Type", "application/json; charset=utf-8");
    }

    private static <T extends GeoMonotemporalRecord> T current(Collection<T> items) {
        ArrayList<T> candidates = new ArrayList<>();
        for (T item : items) {
            if (item.getRegistrationTo() == null) {
                candidates.add(item);
            }
        }
        if (candidates.size() > 1) {
            candidates.sort(Comparator.comparing(GeoMonotemporalRecord::getRegistrationFrom));
        }
        return candidates.isEmpty() ? null : candidates.get(candidates.size()-1);
    }

    private static Pattern bnrPattern = Pattern.compile("(B-)?(0+)?(\\d+)([a-z])?", Pattern.CASE_INSENSITIVE);
    private static String stripBnr(String bnr) {
        return stripBnr(bnr, false);
    }
    private static String stripBnr(String bnr, boolean removeSuffixOnlyIfLong) {
        if (bnr != null) {
            Matcher m = bnrPattern.matcher(bnr);
            if (m.find()) {
                String core = m.group(3);
                if (removeSuffixOnlyIfLong && core.length() < 4 && m.group(4) != null) {
                    return core + m.group(4);
                }
                return core;
            }
        }
        return null;
    }

    private static String prefixBnr(String bnr) {
        if (!bnr.startsWith("B")) {
            bnr = "B-" + bnr;
        }
        return bnr;
    }

    private static String bnrExtraLetter(String bnr) {
        if (bnr != null) {
            Matcher m = bnrPattern.matcher(bnr);
            if (m.find()) {
                if (m.group(3).length() >= 4) {
                    return m.group(4);
                }
            }
        }
        return null;
    }
}
