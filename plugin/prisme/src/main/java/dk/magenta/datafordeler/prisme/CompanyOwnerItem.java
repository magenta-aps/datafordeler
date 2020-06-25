package dk.magenta.datafordeler.prisme;


import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Class for containing a single ownershipInformation for serializing to webservice
 */
public class CompanyOwnerItem {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String pnr;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Number cvr;

    private UnitDetails enhedDetaljer;

    public CompanyOwnerItem() {
    }

    public CompanyOwnerItem(Number enhedsNummer, Number cvr, String pnr, String gyldigFra, String gyldigTil) {
        this.pnr = pnr;
        this.cvr = cvr;
        this.enhedDetaljer = new UnitDetails(enhedsNummer, gyldigFra, gyldigTil);
    }

    public String getPnr() {
        return pnr;
    }

    public void setPnr(String pnr) {
        this.pnr = pnr;
    }

    public Number getCvr() {
        return cvr;
    }

    public void setCvr(Number cvr) {
        this.cvr = cvr;
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

        public UnitDetails(Number enhedsNummer, String gyldigFra, String gyldigTil) {
            this.enhedsNummer = enhedsNummer;
            this.gyldigFra = gyldigFra;
            this.gyldigTil = gyldigTil;
        }

        public Number getEnhedsNummer() {
            return enhedsNummer;
        }

        public void seEnhedsNummer(Number enhedsNummer) {
            this.enhedsNummer = enhedsNummer;
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
