package dk.magenta.datafordeler.eskat.output;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.cvr.records.CvrRecordPeriod;

public class ParticipantEntity {
    private String cvr;
    private String cpr;
    private String companyname;
    private String personname;
    private String driftform;
    private String status;
    private String responsiblestart;
    private String responsibleend;
    private String companystart;
    private String companyend;

    public ParticipantEntity(String cvr, String cpr, String personname, String companyname, String driftform, String status,
                             String responsiblestart, String responsibleend, String companystart, String companyend) {
        this.cvr = cvr;
        this.cpr = cpr;
        this.personname = personname;
        this.companyname = companyname;
        this.driftform = driftform;
        this.status = status;
        this.responsiblestart = responsiblestart;
        this.responsibleend = responsibleend;
        this.companystart = companystart;
        this.companyend = companyend;

    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("cvr")
    public String getCvr() {
        return cvr;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("cpr")
    public String getCpr() {
        return cpr;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("personNavn")
    public String getPersonName() {
        return personname;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("firmaNavn")
    public String getCompanyName() {
        return companyname;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("firmadriftformNavn")
    public String getDriftForm() {
        return driftform;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("ansvarlig-" + CvrRecordPeriod.IO_FIELD_VALID_FROM)
    public String getResponsibleStart() {
        return responsiblestart;
    }

    @JsonProperty("ansvarlig-" + CvrRecordPeriod.IO_FIELD_VALID_TO)
    public String getResponsibleEnd() {
        return responsibleend;
    }

    @JsonProperty("virksomhed-" + CvrRecordPeriod.IO_FIELD_VALID_FROM)
    public String getCompanyStart() {
        return companystart;
    }

    @JsonProperty("virksomhed-" + CvrRecordPeriod.IO_FIELD_VALID_TO)
    public String getCompanyEnd() {
        return companyend;
    }
}
