package dk.magenta.datafordeler.prisme;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Class for containing a single adressentry for serializing to webservice
 */
public class PersonAdressItem {

    @JsonIgnore
    private long id;

    private int myndighedskode;
    private int vejkode;
    private String kommune;
    private String adresse;
    private int postnummer;
    private String bynavn;
    private int stedkode;
    private String landekode;
    private String startdato;
    private String slutdato;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getMyndighedskode() {
        return myndighedskode;
    }

    public void setMyndighedskode(int myndighedskode) {
        this.myndighedskode = myndighedskode;
    }

    public int getVejkode() {
        return vejkode;
    }

    public void setVejkode(int vejkode) {
        this.vejkode = vejkode;
    }

    public String getKommune() {
        return kommune;
    }

    public void setKommune(String kommune) {
        this.kommune = kommune;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public int getPostnummer() {
        return postnummer;
    }

    public void setPostnummer(int postnummer) {
        this.postnummer = postnummer;
    }

    public String getBynavn() {
        return bynavn;
    }

    public void setBynavn(String bynavn) {
        this.bynavn = bynavn;
    }

    public int getStedkode() {
        return stedkode;
    }

    public void setStedkode(int stedkode) {
        this.stedkode = stedkode;
    }

    public String getLandekode() {
        return landekode;
    }

    public void setLandekode(String landekode) {
        this.landekode = landekode;
    }

    public String getStartdato() {
        return startdato;
    }

    public void setStartdato(String startdato) {
        this.startdato = startdato;
    }

    public String getSlutdato() {
        return slutdato;
    }

    public void setSlutdato(String slutdato) {
        this.slutdato = slutdato;
    }
}
