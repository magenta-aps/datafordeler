package dk.magenta.datafordeler.cpr;

public class CprLookupDTO {
    protected String municipalityName = null;
    protected String roadName = null;
    protected String localityCode = null;
    protected String localityAbbrev = null;
    protected String localityName = null;
    protected int postalCode = 0;
    protected String postalDistrict = null;

    public String getMunicipalityName() {
        return municipalityName;
    }

    public void setMunicipalityName(String municipalityName) {
        this.municipalityName = municipalityName;
    }

    public String getRoadName() {
        return roadName;
    }

    public void setRoadName(String roadName) {
        this.roadName = roadName;
    }

    public String getLocalityCode() {
        return localityCode;
    }

    /**
     * Get the locality formatted as a int, if the localityCorde is null return 0;
     *
     * @return
     */
    public int getLocalityCodeNumber() {
        return localityCode != null && !localityCode.equals("") ? Integer.parseInt(localityCode) : 0;
    }

    public void setLocalityCode(String localityCode) {
        this.localityCode = localityCode;
    }

    public String getLocalityAbbrev() {
        return localityAbbrev;
    }

    public void setLocalityAbbrev(String localityAbbrev) {
        this.localityAbbrev = localityAbbrev;
    }

    public String getLocalityName() {
        return localityName;
    }

    public void setLocalityName(String localityName) {
        this.localityName = localityName;
    }

    public int getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(int postalCode) {
        this.postalCode = postalCode;
    }

    public String getPostalDistrict() {
        return postalDistrict;
    }

    public void setPostalDistrict(String postalDistrict) {
        this.postalDistrict = postalDistrict;
    }
}
