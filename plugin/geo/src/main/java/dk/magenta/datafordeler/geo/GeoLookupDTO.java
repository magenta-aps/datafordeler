package dk.magenta.datafordeler.geo;

import dk.magenta.datafordeler.core.fapi.ResultSet;
import dk.magenta.datafordeler.cpr.CprLookupDTO;
import dk.magenta.datafordeler.geo.data.accessaddress.AccessAddressEntity;
import dk.magenta.datafordeler.geo.data.accessaddress.AccessAddressHouseNumberRecord;
import dk.magenta.datafordeler.geo.data.locality.GeoLocalityEntity;
import dk.magenta.datafordeler.geo.data.locality.LocalityAbbreviationRecord;
import dk.magenta.datafordeler.geo.data.locality.LocalityEntityManager;
import dk.magenta.datafordeler.geo.data.locality.LocalityNameRecord;
import dk.magenta.datafordeler.geo.data.municipality.GeoMunicipalityEntity;
import dk.magenta.datafordeler.geo.data.municipality.MunicipalityNameRecord;
import dk.magenta.datafordeler.geo.data.postcode.PostcodeEntity;
import dk.magenta.datafordeler.geo.data.postcode.PostcodeNameRecord;
import dk.magenta.datafordeler.geo.data.road.GeoRoadEntity;
import dk.magenta.datafordeler.geo.data.road.RoadNameRecord;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class GeoLookupDTO extends CprLookupDTO {

    public GeoLookupDTO() {
    }

    public GeoLookupDTO(CprLookupDTO cprDto) {
        this.municipalityName = cprDto.getMunicipalityName();
        this.roadName = cprDto.getRoadName();
        this.localityCode = cprDto.getLocalityCode();
        this.localityAbbrev = cprDto.getLocalityAbbrev();
        this.localityName = cprDto.getLocalityName();
        this.postalCode = cprDto.getPostalCode();
        this.postalDistrict = cprDto.getPostalDistrict();
    }

    public GeoLookupDTO(ResultSet resultSet) {

        Set<GeoMunicipalityEntity> municipalityEntities = resultSet.get(GeoMunicipalityEntity.class);
        if (municipalityEntities != null) {
            for (GeoMunicipalityEntity municipalityEntity : municipalityEntities) {
                if (this.setMunicipality(municipalityEntity)) {
                    break;
                }
            }
        }
        Set<GeoRoadEntity> roadEntities = resultSet.get(GeoRoadEntity.class);
        if (roadEntities != null) {
            System.out.println("roadEntities: "+roadEntities.stream().map(r -> r.getName().current().getName()).collect(Collectors.toList()));
            for (GeoRoadEntity roadEntity : roadEntities) {
                if (this.setRoad(roadEntity)) {
                    break;
                }
            }
        }
        Set<GeoLocalityEntity> geoLocalityEntities = resultSet.get(GeoLocalityEntity.class);
        if (geoLocalityEntities != null) {
            for (GeoLocalityEntity geoLocalityEntity : geoLocalityEntities) {
                if (this.setLocality(geoLocalityEntity)) {
                    break;
                }
            }
        }
        Set<PostcodeEntity> postcodeEntities = resultSet.get(PostcodeEntity.class);
        if (postcodeEntities != null) {
            for (PostcodeEntity postcodeEntity : postcodeEntities) {
                if (this.setPostalcode(postcodeEntity)) {
                    break;
                }
            }
        }
        Set<AccessAddressEntity> accessAddressEntities = resultSet.get(AccessAddressEntity.class, true);
        if (accessAddressEntities != null) {
            for (AccessAddressEntity accessAddressEntity : accessAddressEntities) {
                if (this.setAccessAddress(accessAddressEntity)) {
                    break;
                }
            }
        }


    }

    private String bNumber = null;

    public String getbNumber() {
        return bNumber;
    }

    public void setbNumber(String bNumber) {
        this.bNumber = bNumber;
    }

    private boolean setMunicipality(GeoMunicipalityEntity entity) {
        for (MunicipalityNameRecord municipalityNameRecord : entity.getName()) {
            this.municipalityName = municipalityNameRecord.getName();
            return true;
        }
        return false;
    }

    private boolean setRoad(GeoRoadEntity entity) {
        for (RoadNameRecord roadNameRecord : entity.getName()) {
            System.out.println(roadNameRecord.getName());
            this.roadName = roadNameRecord.getName();
            //return true;
        }
        return false;
    }

    private boolean setLocality(GeoLocalityEntity entity) {
        this.localityCode = entity.getCode();
        for (LocalityNameRecord localityNameRecord : entity.getName()) {
            this.localityName = localityNameRecord.getName();
        }
        for (LocalityAbbreviationRecord localityAbbreviationRecord : entity.getAbbreviation()) {
            this.localityAbbrev = localityAbbreviationRecord.getName();
        }
        return true;
    }


    private boolean setPostalcode(PostcodeEntity entity) {
        this.postalCode = entity.getCode();
        for (PostcodeNameRecord postNameRecord : entity.getName()) {
            this.postalDistrict = postNameRecord.getName();
            return true;
        }
        return false;
    }

    private boolean setAccessAddress(AccessAddressEntity entity) {
        this.bNumber = entity.getBnr();
        return true;
    }
}
