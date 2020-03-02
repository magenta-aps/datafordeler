package dk.magenta.datafordeler.geo;

import dk.magenta.datafordeler.cpr.CprLookupDTO;

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

    private String bNumber = null;

    public String getbNumber() {
        return bNumber;
    }

    public void setbNumber(String bNumber) {
        this.bNumber = bNumber;
    }
}
