package dk.magenta.datafordeler.geo;

import dk.magenta.datafordeler.core.fapi.ResultSet;
import dk.magenta.datafordeler.cpr.CprLookupDTO;
import dk.magenta.datafordeler.geo.data.accessaddress.AccessAddressEntity;
import dk.magenta.datafordeler.geo.data.locality.GeoLocalityEntity;
import dk.magenta.datafordeler.geo.data.locality.LocalityAbbreviationRecord;
import dk.magenta.datafordeler.geo.data.locality.LocalityNameRecord;
import dk.magenta.datafordeler.geo.data.municipality.GeoMunicipalityEntity;
import dk.magenta.datafordeler.geo.data.municipality.MunicipalityNameRecord;
import dk.magenta.datafordeler.geo.data.postcode.PostcodeEntity;
import dk.magenta.datafordeler.geo.data.postcode.PostcodeNameRecord;
import dk.magenta.datafordeler.geo.data.road.GeoRoadEntity;
import dk.magenta.datafordeler.geo.data.road.RoadNameRecord;
import dk.magenta.datafordeler.geo.data.unitaddress.UnitAddressEntity;

import java.util.Set;
import java.util.stream.Collectors;

public class GeoLookupDTO extends CprLookupDTO {

    private boolean administrativ = false;

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
        this.accessAddressGlobalId = cprDto.getAccessAddressGlobalId();
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

        Set<UnitAddressEntity> unitAddressEntities = resultSet.get(UnitAddressEntity.class, true);
        if (unitAddressEntities != null) {
            for (UnitAddressEntity unitAddressEntity : unitAddressEntities) {
                if (this.setUnitAddress(unitAddressEntity)) {
                    break;
                }
            }
        }


    }

    public boolean isAdministrativ() {
        return administrativ;
    }

    public void setAdministrativ(boolean administrativ) {
        this.administrativ = administrativ;
    }

    private String bNumber = null;

    public String getbNumber() {
        return bNumber;
    }

    public void setbNumber(String bNumber) {
        this.bNumber = bNumber;
    }


    private String municipalityGlobalId = null;
    private String localityGlobalId = null;
    private String postcodeGlobalId = null;
    private String roadGlobalId = null;
    private String buildingGlobalId = null;
    private String accessAddressGlobalId = null;
    private String unitAddressGlobalId = null;

    public String getMunicipalityGlobalId() {
        return this.municipalityGlobalId;
    }

    public void setMunicipalityGlobalId(String municipalityGlobalId) {
        this.municipalityGlobalId = municipalityGlobalId;
    }

    public String getLocalityGlobalId() {
        return this.localityGlobalId;
    }

    public void setLocalityGlobalId(String localityGlobalId) {
        this.localityGlobalId = localityGlobalId;
    }

    public String getPostcodeGlobalId() {
        return this.postcodeGlobalId;
    }

    public void setPostcodeGlobalId(String postcodeGlobalId) {
        this.postcodeGlobalId = postcodeGlobalId;
    }

    public String getRoadGlobalId() {
        return this.roadGlobalId;
    }

    public void setRoadGlobalId(String roadGlobalId) {
        this.roadGlobalId = roadGlobalId;
    }

    public String getBuildingGlobalId() {
        return this.buildingGlobalId;
    }

    public void setBuildingGlobalId(String buildingGlobalId) {
        this.buildingGlobalId = buildingGlobalId;
    }

    public String getAccessAddressGlobalId() {
        return this.accessAddressGlobalId;
    }

    public void setAccessAddressGlobalId(String accessAddressGlobalId) {
        this.accessAddressGlobalId = accessAddressGlobalId;
    }

    public String getUnitAddressGlobalId() {
        return this.unitAddressGlobalId;
    }

    public void setUnitAddressGlobalId(String unitAddressGlobalId) {
        this.unitAddressGlobalId = unitAddressGlobalId;
    }

    private boolean setMunicipality(GeoMunicipalityEntity entity) {
        this.municipalityGlobalId = entity.getGlobalId();
        for (MunicipalityNameRecord municipalityNameRecord : entity.getName()) {
            this.municipalityName = municipalityNameRecord.getName();
            return true;
        }
        return false;
    }

    private boolean setRoad(GeoRoadEntity entity) {
        this.roadGlobalId = entity.getGlobalId();
        for (RoadNameRecord roadNameRecord : entity.getName()) {
            this.roadName = roadNameRecord.getName();
            return true;
        }
        return false;
    }

    private boolean setLocality(GeoLocalityEntity entity) {
        this.localityCode = entity.getCode();
        this.localityGlobalId = entity.getGlobalId();
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
        this.postcodeGlobalId = entity.getGlobalId();
        for (PostcodeNameRecord postNameRecord : entity.getName()) {
            this.postalDistrict = postNameRecord.getName();
            return true;
        }
        return false;
    }

    private boolean setAccessAddress(AccessAddressEntity entity) {
        this.bNumber = entity.getBnr();
        this.accessAddressGlobalId = entity.getGlobalId();
        return true;
    }

    private boolean setUnitAddress(UnitAddressEntity entity) {
        this.unitAddressGlobalId = entity.getGlobalId();
        return true;
    }
}
