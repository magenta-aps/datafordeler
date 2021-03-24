package dk.magenta.datafordeler.geo;

import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class GeoLookupService extends CprLookupService {

    private Logger log = LogManager.getLogger("dk.magenta.datafordeler.geo.GeoLookupService");

    private HashMap<Integer, String> municipalityCacheGR = new HashMap<>();

    public GeoLookupService(SessionManager sessionManager) {
        super(sessionManager);
    }

    public GeoLookupDTO doLookup(int municipalityCode, int roadCode) {
        return this.doLookup(municipalityCode, roadCode, null);
    }

    public GeoLookupDTO doLookup(int municipalityCode, int roadCode, String houseNumber) {
        return this.doLookup(municipalityCode, roadCode, houseNumber, null);
    }

    public GeoLookupDTO doLookupBestEffoert(int municipalityCode, int roadCode) {
        try(Session session = sessionManager.getSessionFactory().openSession()) {

            GeoLookupDTO geoLookupDTO = new GeoLookupDTO();
            String municipalityEntity = null;
            if (municipalityCacheGR.containsKey(municipalityCode)) {
                municipalityEntity = municipalityCacheGR.get(municipalityCode);
            } else {
                MunicipalityQuery query = new MunicipalityQuery();
                query.addKommunekodeRestriction(Integer.toString(municipalityCode));
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
            roadQuery.setCode(Integer.toString(roadCode));
            setQueryNow(roadQuery);
            List<GeoRoadEntity> roadEntities = QueryManager.getAllEntities(session, roadQuery, GeoRoadEntity.class);

            if (roadEntities != null && roadEntities.size() > 0) {
                GeoRoadEntity roadEntity = roadEntities.stream().min(Comparator.comparing(GeoRoadEntity::getId)).get();
                //There can be more than one roadEntities, we just take the first one.
                //This is becrause ane road can be split into many roadentities by sideroads.
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
// Find postalcode for road. This is only available through access addresses on the road. In theory there could be several postal codes for one road
            AccessAddressQuery accessAddressQuery = new AccessAddressQuery();
            accessAddressQuery.setMunicipalityCode(municipalityCode);

            accessAddressQuery.setRoadCode(roadCode);
            setQueryNow(accessAddressQuery);
            List<AccessAddressEntity> accessAddress = QueryManager.getAllEntities(session, accessAddressQuery, AccessAddressEntity.class);

            if (accessAddress != null && accessAddress.size() > 0) {
                //There can be more than one access-address, we just take the first one.
                //There can be more than one accessaddress on a road, but they have the same postalcode and postaldistrict
                geoLookupDTO.setPostalCode(accessAddress.get(0).getPostcode().iterator().next().getPostcode());
                PostcodeEntity entity = QueryManager.getEntity(session, PostcodeEntity.generateUUID(geoLookupDTO.getPostalCode()), PostcodeEntity.class);
                geoLookupDTO.setPostalDistrict(entity.getName().iterator().next().getName());
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
                    geoLookupDTO.setLocalityName(localities.get(0).getName().iterator().next().getName());
                    geoLookupDTO.setLocalityAbbrev(localities.get(0).getAbbreviation().iterator().next().getName());
                }
            }
            return geoLookupDTO;
        }
    }

    public GeoLookupDTO doLookup(int municipalityCode, int roadCode, String houseNumber, String bNumber) {
        if (municipalityCode < 950) {
            return new GeoLookupDTO(super.doLookup(municipalityCode, roadCode, houseNumber));
        } else {
            try(Session session = sessionManager.getSessionFactory().openSession()) {

                GeoLookupDTO geoLookupDTO = new GeoLookupDTO();
                String municipalityEntity = null;
                if (municipalityCacheGR.containsKey(municipalityCode)) {
                    municipalityEntity = municipalityCacheGR.get(municipalityCode);
                } else {
                    MunicipalityQuery query = new MunicipalityQuery();
                    query.addKommunekodeRestriction(Integer.toString(municipalityCode));
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
                roadQuery.setCode(Integer.toString(roadCode));
                setQueryNow(roadQuery);
                List<GeoRoadEntity> roadEntities = QueryManager.getAllEntities(session, roadQuery, GeoRoadEntity.class);

                if (roadEntities != null && roadEntities.size() > 0) {
                    GeoRoadEntity roadEntity = roadEntities.stream().min(Comparator.comparing(GeoRoadEntity::getId)).get();
                    //There can be more than one roadEntities, we just take the first one.
                    //This is becrause ane road can be split into many roadentities by sideroads.
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


                AccessAddressQuery accessAddressQuery = new AccessAddressQuery();
                accessAddressQuery.setMunicipalityCode(municipalityCode);

                if (houseNumber != null && !houseNumber.equals("")) {
                    accessAddressQuery.setHouseNumber(houseNumber);
                }
                accessAddressQuery.setRoadCode(roadCode);
                setQueryNow(accessAddressQuery);
                List<AccessAddressEntity> accessAddress = QueryManager.getAllEntities(session, accessAddressQuery, AccessAddressEntity.class);

                if (accessAddress.size() == 0) {
                    accessAddressQuery.clearHouseNumber();
                    accessAddress = QueryManager.getAllEntities(session, accessAddressQuery, AccessAddressEntity.class);
                }

                geoLookupDTO.setbNumber(formatBNumber(bNumber));
                if (accessAddress != null && accessAddress.size() > 0) {
                    //There can be more than one access-address, we just take the first one.
                    //There can be more than one accessaddress on a road, but they have the same postalcode and postaldistrict
                    geoLookupDTO.setPostalCode(accessAddress.get(0).getPostcode().iterator().next().getPostcode());
                    PostcodeEntity entity = QueryManager.getEntity(session, PostcodeEntity.generateUUID(geoLookupDTO.getPostalCode()), PostcodeEntity.class);
                    geoLookupDTO.setPostalDistrict(entity.getName().iterator().next().getName());
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
        if(bnr==null || bnr.equals("")) {
            return null;
        }
        bnr = bnr.replaceAll("^0+", "");
        bnr = bnr.replaceAll("^B-?", "");
        return "B-" + bnr;
    }


    public String getPostalCodeDistrict(int code) {
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            PostcodeEntity entity = QueryManager.getEntity(session, PostcodeEntity.generateUUID(code), PostcodeEntity.class);
            return entity.getName().iterator().next().getName();
        }
    }

    private static void setQueryNow(BaseQuery query) {
        OffsetDateTime now = OffsetDateTime.now();
        query.setRegistrationFromBefore(now);
        query.setRegistrationToAfter(now);
        query.setEffectFromBefore(now);
        query.setEffectToAfter(now);
    }
}
