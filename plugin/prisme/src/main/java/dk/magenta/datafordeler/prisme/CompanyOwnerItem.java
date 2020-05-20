package dk.magenta.datafordeler.prisme;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Class for containing a single ownershipInformation for serializing to webservice
 */
public class CompanyOwnerItem {

    @JsonIgnore
    private long id;
    private Number personUuid;
    private String personCpr;
    private String gyldigFra;
    private String gyldigTil;

    public CompanyOwnerItem() {
    }

    public CompanyOwnerItem(long id, Number personUuid, String personCpr, String gyldigFra, String gyldigTil) {
        this.id = id;
        this.personUuid = personUuid;
        this.personCpr = personCpr;
        this.gyldigFra = gyldigFra;
        this.gyldigTil = gyldigTil;

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
