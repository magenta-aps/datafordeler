package dk.magenta.datafordeler.geo;

import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.cpr.CprLookupDTO;
import dk.magenta.datafordeler.cpr.CprLookupService;
import dk.magenta.datafordeler.geo.data.GeoHardcode;
import dk.magenta.datafordeler.geo.data.accessaddress.AccessAddressEntity;
import dk.magenta.datafordeler.geo.data.accessaddress.AccessAddressQuery;
import dk.magenta.datafordeler.geo.data.locality.GeoLocalityEntity;
import dk.magenta.datafordeler.geo.data.locality.LocalityQuery;
import dk.magenta.datafordeler.geo.data.municipality.GeoMunicipalityEntity;
import dk.magenta.datafordeler.geo.data.municipality.MunicipalityQuery;
import dk.magenta.datafordeler.geo.data.postcode.PostcodeEntity;
import dk.magenta.datafordeler.geo.data.road.GeoRoadEntity;
import dk.magenta.datafordeler.geo.data.road.RoadQuery;
import dk.magenta.datafordeler.geo.data.unitaddress.UnitAddressEntity;
import dk.magenta.datafordeler.geo.data.unitaddress.UnitAddressQuery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import java.time.OffsetDateTime;
import java.util.*;

public class GeoLookupService extends CprLookupService {

    private final Logger log = LogManager.getLogger(GeoLookupService.class.getCanonicalName());

    private final HashMap<Integer, String> municipalityCacheGR = new HashMap<>();

    public GeoLookupService(SessionManager sessionManager) {
        super(sessionManager);
    }

    public GeoLookupDTO doLookup(int municipalityCode, int roadCode) throws InvalidClientInputException {
        return this.doLookup(municipalityCode, roadCode, null);
    }

    public GeoLookupDTO doLookup(int municipalityCode, int roadCode, String houseNumber) throws InvalidClientInputException {
        return this.doLookup(municipalityCode, roadCode, houseNumber, null);
    }

    public GeoLookupDTO doLookupBestEffort(int municipalityCode, int roadCode) throws InvalidClientInputException {
        try (Session session = sessionManager.getSessionFactory().openSession()) {

            GeoLookupDTO geoLookupDTO = new GeoLookupDTO();
            String municipalityEntity = municipalityCacheGR.get(municipalityCode);
            if (municipalityEntity == null) {
                MunicipalityQuery query = new MunicipalityQuery();
                query.addCode(municipalityCode);
                setQueryNow(query);
                List<GeoMunicipalityEntity> municipalities = QueryManager.getAllEntities(session, query, GeoMunicipalityEntity.class);
                for (GeoMunicipalityEntity municipality : municipalities) {
                    municipalityCacheGR.put(municipality.getCode(), municipality.getName().current().getName());
                }
                municipalityEntity = municipalityCacheGR.get(municipalityCode);
            }
            if (municipalityEntity != null) {
                geoLookupDTO.setMunicipalityName(municipalityEntity);
            }

            RoadQuery roadQuery = new RoadQuery();
            roadQuery.setMunicipalityCode(municipalityCode);
            roadQuery.setCode(roadCode);
            setQueryNow(roadQuery);
            List<GeoRoadEntity> roadEntities = QueryManager.getAllEntities(session, roadQuery, GeoRoadEntity.class);

            if (roadEntities != null && roadEntities.size() > 0) {
                GeoRoadEntity roadEntity = roadEntities.stream().min(Comparator.comparing(GeoRoadEntity::getId)).get();
                //There can be more than one roadEntities, we just take the first one.
                //This is because a road can be split into many roadentities by sideroads.
                //If all sideroads do not have the same name, it is an error in the delivered data.
                geoLookupDTO.setRoadName(roadEntity.getName().iterator().next().getName());
                geoLookupDTO.setLocalityCode(roadEntity.getLocality().iterator().next().getCode());
            } else {
                GeoHardcode.HardcodedAdressStructure hardcodedAdress = GeoHardcode.getHardcodedRoadname(municipalityCode, roadCode);
                if (hardcodedAdress != null) {
                    geoLookupDTO.setAdministrativ(true);
                    geoLookupDTO.setRoadName(hardcodedAdress.getVejnavn());
                    geoLookupDTO.setPostalCode(hardcodedAdress.getPostcode());
                    geoLookupDTO.setLocalityCode(hardcodedAdress.getLocationcode());
                    geoLookupDTO.setPostalDistrict(hardcodedAdress.getCityname());
                }
            }
            // Find postalcode for road. This is only available through access addresses on the road. In theory there could be several postal codes for one road
            AccessAddressQuery accessAddressQuery = new AccessAddressQuery();
            accessAddressQuery.setMunicipalityCode(municipalityCode);

            accessAddressQuery.setRoadCode(roadCode);
            setQueryNow(accessAddressQuery);
            List<AccessAddressEntity> accessAddresses = QueryManager.getAllEntities(session, accessAddressQuery, AccessAddressEntity.class);

            if (accessAddresses != null && accessAddresses.size() > 0) {
                //There can be more than one access-address, we just take the first one.
                //There can be more than one accessaddress on a road, but they have the same postalcode and postaldistrict
                for (AccessAddressEntity accessAddress : accessAddresses) {
                    if (!accessAddress.getPostcode().isEmpty() && accessAddress.getPostcode().current() != null) {
                        geoLookupDTO.setPostalCode(accessAddress.getPostcode().current().getPostcode());
                        PostcodeEntity entity = QueryManager.getEntity(session, PostcodeEntity.generateUUID(geoLookupDTO.getPostalCode()), PostcodeEntity.class);
                        geoLookupDTO.setPostalDistrict(entity.getName().current().getName());
                        break;
                    }
                }

            } else {
                //Fallback to supplying with danish postalcodes
                CprLookupDTO cprDto = super.doLookup(municipalityCode, roadCode, "");
                geoLookupDTO.setPostalCode(cprDto.getPostalCode());
                geoLookupDTO.setPostalDistrict(cprDto.getPostalDistrict());
            }

            LocalityQuery localityQuery = new LocalityQuery();
            if (geoLookupDTO.getLocalityCode() != null) {
                localityQuery.setCode(geoLookupDTO.getLocalityCode());
                localityQuery.setMunicipality(Integer.toString(municipalityCode));
                setQueryNow(localityQuery);
                List<GeoLocalityEntity> localities = QueryManager.getAllEntities(session, localityQuery, GeoLocalityEntity.class);
                if (localities != null && localities.size() > 0) {
                    geoLookupDTO.setLocalityName(localities.get(0).getName().current().getName());
                    geoLookupDTO.setLocalityAbbrev(localities.get(0).getAbbreviation().current().getName());
                }
            }
            return geoLookupDTO;
        }
    }
    public GeoLookupDTO doLookup(int municipalityCode, int roadCode, String houseNumber, String bNumber) throws InvalidClientInputException {
        return this.doLookup(municipalityCode, roadCode, houseNumber, bNumber, null, null, false);
    }
    public GeoLookupDTO doLookup(int municipalityCode, int roadCode, String houseNumber, String bNumber, String floor, String door, boolean includeGlobalIds) throws InvalidClientInputException {
        if (municipalityCode < 950) {
            return new GeoLookupDTO(super.doLookup(municipalityCode, roadCode, houseNumber));
        } else {
            try (Session session = sessionManager.getSessionFactory().openSession()) {

                GeoLookupDTO geoLookupDTO = new GeoLookupDTO();
                String municipalityEntity = municipalityCacheGR.get(municipalityCode);
                if (municipalityEntity == null) {
                    MunicipalityQuery query = new MunicipalityQuery();
                    query.addCode(municipalityCode);
                    setQueryNow(query);
                    List<GeoMunicipalityEntity> municipalities = QueryManager.getAllEntities(session, query, GeoMunicipalityEntity.class);
                    for (GeoMunicipalityEntity municipality : municipalities) {
                        municipalityCacheGR.put(municipality.getCode(), municipality.getName().current().getName());
                    }
                    municipalityEntity = municipalityCacheGR.get(municipalityCode);
                }
                if (municipalityEntity != null) {
                    geoLookupDTO.setMunicipalityName(municipalityEntity);
                }

                RoadQuery roadQuery = new RoadQuery();
                roadQuery.setMunicipalityCode(municipalityCode);
                roadQuery.setCode(roadCode);
                setQueryNow(roadQuery);
                List<GeoRoadEntity> roadEntities = QueryManager.getAllEntities(session, roadQuery, GeoRoadEntity.class);

                if (roadEntities != null && roadEntities.size() > 0) {
                    GeoRoadEntity roadEntity = roadEntities.stream().min(Comparator.comparing(GeoRoadEntity::getId)).get();
                    //There can be more than one roadEntities, we just take the first one.
                    //This is because any road can be split into many roadentities by sideroads.
                    //If all sideeroads does not have the same name, it is an error at the delivered data.
                    geoLookupDTO.setRoadName(roadEntity.getName().iterator().next().getName());
                    geoLookupDTO.setLocalityCode(roadEntity.getLocality().iterator().next().getCode());
                } else {
                    GeoHardcode.HardcodedAdressStructure hardcodedAdress = GeoHardcode.getHardcodedRoadname(municipalityCode, roadCode);
                    if (hardcodedAdress != null) {
                        geoLookupDTO.setAdministrativ(true);
                        geoLookupDTO.setRoadName(hardcodedAdress.getVejnavn());
                        geoLookupDTO.setPostalCode(hardcodedAdress.getPostcode());
                        geoLookupDTO.setLocalityCode(hardcodedAdress.getLocationcode());
                        geoLookupDTO.setPostalDistrict(hardcodedAdress.getCityname());
                    }
                }

                geoLookupDTO.setbNumber(formatBNumber(bNumber));

                AccessAddressQuery accessAddressQuery = new AccessAddressQuery();
                accessAddressQuery.setMunicipalityCode(municipalityCode);

                if (houseNumber != null && !houseNumber.equals("")) {
                    accessAddressQuery.setHouseNumber(houseNumber);
                }
                accessAddressQuery.setRoadCode(roadCode);
                setQueryNow(accessAddressQuery);
                List<AccessAddressEntity> accessAddresses = QueryManager.getAllEntities(session, accessAddressQuery, AccessAddressEntity.class);

                if (accessAddresses.isEmpty()) {
                    accessAddressQuery.clearHouseNumber();
                    accessAddresses = QueryManager.getAllEntities(session, accessAddressQuery, AccessAddressEntity.class);
                } else if (accessAddresses.size() == 1 && includeGlobalIds) {
                    AccessAddressEntity accessAddressEntity = accessAddresses.get(0);
                    geoLookupDTO.setAccessAddressGlobalId(accessAddressEntity.getGlobalId());

                    if (floor != null || door != null) {
                        UnitAddressQuery unitAddressQuery = new UnitAddressQuery();
                        unitAddressQuery.addAccessAddress(accessAddressEntity);
                        if (floor != null) {
                            unitAddressQuery.addFloor(floor);
                        }
                        if (door != null) {
                            unitAddressQuery.addDoor(door);
                        }

                        List<UnitAddressEntity> unitAddresses = QueryManager.getAllEntities(session, unitAddressQuery, UnitAddressEntity.class);
                        if (!unitAddresses.isEmpty()) {
                            geoLookupDTO.setUnitAddressGlobalId(unitAddresses.get(0).getGlobalId());
                        } else {
                            /*
                            unitAddressQuery.setDoor(null);
                            unitAddresses = QueryManager.getAllEntities(session, unitAddressQuery, UnitAddressEntity.class);
                            if (unitAddresses.size() == 1) {
                                System.out.println("Skipped door and found one");
                                geoLookupDTO.setUnitAddressGlobalId(unitAddresses.get(0).getGlobalId());
                            }
                            */
                        }
                    }

                }
                if (accessAddresses != null && !accessAddresses.isEmpty()) {
                    //There can be more than one access-address, we just take the first one.
                    //There can be more than one accessaddress on a road, but they have the same postalcode and postaldistrict
                    for (AccessAddressEntity accessAddress : accessAddresses) {
                        if (!accessAddress.getPostcode().isEmpty() && accessAddress.getPostcode().current() != null) {
                            int postcode = accessAddress.getPostcode().current().getPostcode();
                            geoLookupDTO.setPostalCode(postcode);
                            PostcodeEntity entity = QueryManager.getEntity(session, PostcodeEntity.generateUUID(postcode), PostcodeEntity.class);
                            if (entity != null) {
                                geoLookupDTO.setPostalDistrict(entity.getName().current().getName());
                            }
                        }
                        if (geoLookupDTO.getLocalityCode() == null && !accessAddress.getLocality().isEmpty() && accessAddress.getLocality().current() != null) {
                            String localityCode = accessAddress.getLocality().current().getCode();
                            geoLookupDTO.setLocalityCode(localityCode);
                        }
                        if (geoLookupDTO.getPostalCode() != 0 && geoLookupDTO.getLocalityCode() != null) {
                            break;
                        }
                    }
                }

                LocalityQuery localityQuery = new LocalityQuery();
                if (geoLookupDTO.getLocalityCode() != null) {
                    localityQuery.setCode(geoLookupDTO.getLocalityCode());
                    localityQuery.setMunicipality(Integer.toString(municipalityCode));
                    setQueryNow(localityQuery);
                    List<GeoLocalityEntity> localities = QueryManager.getAllEntities(session, localityQuery, GeoLocalityEntity.class);
                    if (localities != null && localities.size() > 0) {
                        geoLookupDTO.setLocalityName(localities.get(0).getName().iterator().next().getName());
                        geoLookupDTO.setLocalityAbbrev(localities.get(0).getAbbreviation().iterator().next().getName());
                    }
                }
                return geoLookupDTO;
            }
        }
    }

    private static String formatBNumber(String bnr) {
        if (bnr == null || bnr.equals("")) {
            return null;
        }
        bnr = bnr.replaceAll("^0+", "");
        bnr = bnr.replaceAll("^B-?", "");
        return "B-" + bnr;
    }


    public String getPostalCodeDistrict(int code) {
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            PostcodeEntity entity = QueryManager.getEntity(session, PostcodeEntity.generateUUID(code), PostcodeEntity.class);
            if (entity != null) {
                return entity.getName().iterator().next().getName();
            }
        }
        return null;
    }

    private static void setQueryNow(BaseQuery query) {
        OffsetDateTime now = OffsetDateTime.now();
        query.setRegistrationFromBefore(now);
        query.setRegistrationToAfter(now);
        query.setEffectFromBefore(now);
        query.setEffectToAfter(now);
    }
}
