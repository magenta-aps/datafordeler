package dk.magenta.datafordeler.combined;

import com.fasterxml.jackson.annotation.JsonInclude;

public class PersonLocationObject {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String pnr = "";

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String locationCode = "";

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String birthTime = "";

    public PersonLocationObject(String pnr, String locationCode, String birthTime) {
        this.pnr = pnr;
        this.locationCode = locationCode;
        this.birthTime = birthTime;
    }

    public String getPnr() {
        return pnr;
    }

    public void setPnr(String pnr) {
        this.pnr = pnr;
    }

    public String getLocationCode() {
        return locationCode;
    }

    public void setLocationCode(String locationCode) {
        this.locationCode = locationCode;
    }

    public String getBirthTime() {
        return birthTime;
    }

    public void setBirthTime(String birthTime) {
        this.birthTime = birthTime;
    }
}
