package dk.magenta.datafordeler.eskat.output;


public class ParticipantObject {
    private String cvr;
    private String cpr;
    private String companyname;
    private String personname;
    private String driftform;
    private String responsiblestart;
    private String responsibleend;
    private String companystart;
    private String companyend;

    public ParticipantObject(String cvr, String cpr, String personname, String companyname, String driftform,
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

    public String getCvr() {
        return cvr;
    }

    public String getCpr() {
        return cpr;
    }

    public String getPersonName() {
        return personname;
    }

    public String getCompanyName() {
        return companyname;
    }

    public String getDriftForm() {
        return driftform;
    }

    public String getResponsibleStart() {
        return responsiblestart;
    }

    public String getResponsibleEnd() {
        return responsibleend;
    }

    public String getCompanyStart() {
        return companystart;
    }

    public String getCompanyEnd() {
        return companyend;
    }
}
