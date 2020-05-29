package dk.magenta.datafordeler.prisme;


import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Class for containing a single ownershipInformation for serializing to webservice
 */
public class CompanyOwnerItem {

    private String pnr;
    private UnitDetails enhedDetaljer;

    public CompanyOwnerItem() {
    }

    public CompanyOwnerItem(Number enhedsNummer, Number companyUuid, String pnr, String gyldigFra, String gyldigTil) {
        this.pnr = pnr;
        this.enhedDetaljer = new UnitDetails(enhedsNummer, companyUuid, gyldigFra, gyldigTil);
    }

    public String getPnr() {
        return pnr;
    }

    public void setPnr(String personCpr) {
        this.pnr = pnr;
    }

    public UnitDetails getEnhedDetaljer() {
        return enhedDetaljer;
    }

    public void setEnhedDetaljer(UnitDetails enhed) {
        this.enhedDetaljer = enhed;
    }

    public class UnitDetails {

        private Number enhedsNummer;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private Number cvr;

        private String gyldigFra;
        private String gyldigTil;

        public UnitDetails(Number enhedsNummer, Number cvr, String gyldigFra, String gyldigTil) {
            this.enhedsNummer = enhedsNummer;
            this.cvr = cvr;
            this.gyldigFra = gyldigFra;
            this.gyldigTil = gyldigTil;
        }

        public Number getEnhedsNummer() {
            return enhedsNummer;
        }

        public void seEnhedsNummer(Number enhedsNummer) {
            this.enhedsNummer = enhedsNummer;
        }

        public Number getCvr() {
            return cvr;
        }

        public void setCompanyUuid(Number cvr) {
            this.cvr = cvr;
        }

        public String getGyldigFra() {
            return gyldigFra;
        }

        public void setGyldigFra(String gyldigFra) {
            this.gyldigFra = gyldigFra;
        }

        public String getGyldigTil() {
            return gyldigTil;
        }

        public void setGyldigTil(String gyldigTil) {
            this.gyldigTil = gyldigTil;
        }
    }
}
