package dk.magenta.datafordeler.prisme;


/**
 * Class for containing a single ownershipInformation for serializing to webservice
 */
public class CompanyOwnerItem {

    private Number personUuid;
    private String personCpr;
    private String gyldigFra;
    private String gyldigTil;

    public CompanyOwnerItem() {
    }

    public CompanyOwnerItem(Number personUuid, String personCpr, String gyldigFra, String gyldigTil) {
        this.personUuid = personUuid;
        this.personCpr = personCpr;
        this.gyldigFra = gyldigFra;
        this.gyldigTil = gyldigTil;

    }


    public Number getPersonUuid() {
        return personUuid;
    }

    public void setPersonUuid(Number personUuid) {
        this.personUuid = personUuid;
    }

    public String getPersonCpr() {
        return personCpr;
    }

    public void setPersonCpr(String personCpr) {
        this.personCpr = personCpr;
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
