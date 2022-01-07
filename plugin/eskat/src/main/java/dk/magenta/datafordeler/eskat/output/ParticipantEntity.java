package dk.magenta.datafordeler.eskat.output;


import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.cvr.records.CvrRecordPeriod;

public class ParticipantEntity {
    private String cvr;
    private String cpr;
    private String companyname;
    private String personname;
    private String driftform;
    private String responsiblestart;
    private String responsibleend;
    private String companystart;
    private String companyend;

    public ParticipantEntity(String cvr, String cpr, String personname, String companyname, String driftform,
                             String responsiblestart, String responsibleend, String companystart, String companyend) {
        this.cvr = cvr;
        this.cpr = cpr;
        this.personname = personname;
        this.companyname = companyname;
        this.driftform = driftform;
        this.responsiblestart = responsiblestart;
        this.responsibleend = responsibleend;
        this.companystart = companystart;
        this.companyend = companyend;

    }

    @JsonProperty("cvr")
    public String getCvr() {
        return cvr;
    }

    @JsonProperty("cpr")
    public String getCpr() {
        return cpr;
    }

    @JsonProperty("personNavn")
    public String getPersonName() {
        return personname;
    }

    @JsonProperty("firmaNavn")
    public String getCompanyName() {
        return companyname;
    }

    @JsonProperty("firmdriftformaNavn")
    public String getDriftForm() {
        return driftform;
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
