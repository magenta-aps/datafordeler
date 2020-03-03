package dk.magenta.datafordeler.geo;

import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
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
import dk.magenta.datafordeler.geo.data.road.RoadNameRecord;
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

    private HashMap<Integer, GeoMunicipalityEntity> municipalityCacheGR = new HashMap<>();

    public GeoLookupService(Session session) {
        super(session);
    }

    public Session getSession() {
        return super.getSession();
    }

    public GeoLookupDTO doLookup(int municipalityCode, int roadCode) {
        return this.doLookup(municipalityCode, roadCode, null);
    }

    public GeoLookupDTO doLookup(int municipalityCode, int roadCode, String houseNumber) {
        return this.doLookup(municipalityCode, roadCode, houseNumber, null);
    }

    public GeoLookupDTO doLookup(int municipalityCode, int roadCode, String houseNumber, String bNumber) {
        if (municipalityCode < 950) {
            return new GeoLookupDTO(super.doLookup(municipalityCode, roadCode, houseNumber));
        } else {
            GeoLookupDTO geoLookupDTO = new GeoLookupDTO();
            GeoMunicipalityEntity municipalityEntity = null;
            if (municipalityCacheGR.containsKey(municipalityCode)) {
                municipalityEntity = municipalityCacheGR.get(municipalityCode);
            } else {
                MunicipalityQuery query = new MunicipalityQuery();
                query.addKommunekodeRestriction(Integer.toString(municipalityCode));
                setQueryNow(query);
                List<GeoMunicipalityEntity> municipilalicities = QueryManager.getAllEntities(super.getSession(), query, GeoMunicipalityEntity.class);
                for(GeoMunicipalityEntity municipilalicity : municipilalicities) {
                    municipalityCacheGR.put(municipilalicity.getCode(), municipilalicity);
                }
                municipalityEntity = municipalityCacheGR.get(municipalityCode);
            }
            if (municipalityEntity != null) {
                geoLookupDTO.setMunicipalityName(municipalityEntity.getName().iterator().next().getName());
            }


            RoadQuery roadQuery = new RoadQuery();
            roadQuery.setMunicipality(Integer.toString(municipalityCode));
            roadQuery.setCode(Integer.toString(roadCode));
            setQueryNow(roadQuery);
            List<GeoRoadEntity> roadEntities = QueryManager.getAllEntities(super.getSession(), roadQuery, GeoRoadEntity.class);

            if (roadEntities != null && roadEntities.size() > 0) {
                GeoRoadEntity roadEntity = roadEntities.stream().min( Comparator.comparing(GeoRoadEntity::getId)).get();
                //There can be more than one roadEntities, we just take the first one.
                //This is becrause ane road can be split into many roadentities by sideroads.
                //If all sideeroads does not have the same name, it is an error at the delivered data.
                geoLookupDTO.setRoadName(roadEntity.getName().iterator().next().getName());
                geoLookupDTO.setLocalityCode(roadEntity.getLocality().iterator().next().getCode());
            } else {
                GeoHardcode.HardcodedAdressStructure hardcodedAdress = GeoHardcode.getHardcodedRoadname(municipalityCode, roadCode);
                if(hardcodedAdress!=null) {
                    geoLookupDTO.setRoadName(hardcodedAdress.getVejnavn());
                    geoLookupDTO.setPostalCode(hardcodedAdress.getPostcode());
                    geoLookupDTO.setLocalityCode(hardcodedAdress.getLocationcode());
                    geoLookupDTO.setPostalDistrict(hardcodedAdress.getCityname());
                }
            }


            AccessAddressQuery accessAddressQuery = new AccessAddressQuery();
            accessAddressQuery.setMunicipality(Integer.toString(municipalityCode));

            if(houseNumber!=null && !houseNumber.equals("")) {
                accessAddressQuery.setHouseNumber(houseNumber);
            }
            accessAddressQuery.setRoad(roadCode);
            setQueryNow(accessAddressQuery);
            List<AccessAddressEntity> accessAddress = QueryManager.getAllEntities(super.getSession(), accessAddressQuery, AccessAddressEntity.class);

            if(accessAddress.size()==0) {
                accessAddressQuery.clearHouseNumber();
                accessAddress = QueryManager.getAllEntities(super.getSession(), accessAddressQuery, AccessAddressEntity.class);
            }

            geoLookupDTO.setbNumber(formatBNumber(bNumber));
            if (accessAddress != null && accessAddress.size() > 0) {
                //There can be more than one access-address, we just take the first one.
                //There can be more than one accessaddress on a road, but they have the same postalcode and postaldistrict
                geoLookupDTO.setPostalCode(accessAddress.get(0).getPostcode().iterator().next().getPostcode());
                PostcodeEntity entity = QueryManager.getEntity(super.getSession(), PostcodeEntity.generateUUID(geoLookupDTO.getPostalCode()), PostcodeEntity.class);
                geoLookupDTO.setPostalDistrict(entity.getName().iterator().next().getName());
            }

            LocalityQuery localityQuery = new LocalityQuery();
            if(geoLookupDTO.getLocalityCode()!=null) {
                localityQuery.setCode(geoLookupDTO.getLocalityCode());
                localityQuery.setMunicipality(Integer.toString(municipalityCode));
                setQueryNow(localityQuery);
                List<GeoLocalityEntity> localities = QueryManager.getAllEntities(super.getSession(), localityQuery, GeoLocalityEntity.class);
                if (localities != null && localities.size() > 0) {
                    geoLookupDTO.setLocalityName(localities.get(0).getName().iterator().next().getName());
                    geoLookupDTO.setLocalityAbbrev(localities.get(0).getAbbreviation().iterator().next().getName());
                }
            }
            return geoLookupDTO;
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
        PostcodeEntity entity = QueryManager.getEntity(super.getSession(), PostcodeEntity.generateUUID(code), PostcodeEntity.class);
        return entity.getName().iterator().next().getName();
    }

    private static void setQueryNow(BaseQuery query) {
        OffsetDateTime now = OffsetDateTime.now();
        query.setRegistrationFrom(now);
        query.setRegistrationTo(now);
        query.setEffectFrom(now);
        query.setEffectTo(now);
    }
}
