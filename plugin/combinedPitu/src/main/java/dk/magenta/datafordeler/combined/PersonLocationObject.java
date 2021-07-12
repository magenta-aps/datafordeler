package dk.magenta.datafordeler.combined;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PersonLocationObject {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String pnr = "";

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String localityCode = "";

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String birthTime = "";

    public PersonLocationObject(String pnr, String localityCode, String birthTime) {
        this.pnr = pnr;
        this.localityCode = localityCode;
        this.birthTime = birthTime;
    }

    public String getPnr() {
        return pnr;
    }

    public void setPnr(String pnr) {
        this.pnr = pnr;
    }

    @JsonProperty(value = "lokalitet_kode")
    public String getLocalityCode() {
        return localityCode;
    }

    public void setLocalityCode(String locationCode) {
        this.localityCode = localityCode;
    }

    public String getBirthTime() {
        return birthTime;
    }

    public void setBirthTime(String birthTime) {
        this.birthTime = birthTime;
    }
}
