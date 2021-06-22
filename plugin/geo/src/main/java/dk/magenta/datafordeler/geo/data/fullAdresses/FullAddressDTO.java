package dk.magenta.datafordeler.geo.data.fullAdresses;

import com.fasterxml.jackson.annotation.JsonInclude;

public class FullAddressDTO {

    private String bnr;//
    private String husNummer;//
    private String blokNavn;
    private Number kommune_kode;
    private String kommune_navn;
    private String lokalitet_kode;
    private Number lokalitet_type;
    private String lokalitet_navn;
    private Number post_kode;
    private String post_navn;
    private Number status;
    private Number source;
    private String etage;
    private String doer;
    private String enhed;
    private String nummer;
    private Number vej_kode;
    private String vej_navn;
    private String accessAddress_objectId;
    private String unitAddress_objectId;


    public String getBnr() {
        return bnr;
    }

    public void setBnr(String bnr) {
        this.bnr = bnr;
    }

    public String getHusNummer() {
        return husNummer;
    }

    public void setHusNummer(String husNummer) {
        this.husNummer = husNummer;
    }

    public String getBlokNavn() {
        return blokNavn;
    }

    public void setBlokNavn(String blokNavn) {
        this.blokNavn = blokNavn;
    }

    public Number getKommune_kode() {
        return kommune_kode;
    }

    public void setKommune_kode(Number kommune_kode) {
        this.kommune_kode = kommune_kode;
    }

    public String getKommune_navn() {
        return kommune_navn;
    }

    public void setKommune_navn(String kommune_navn) {
        this.kommune_navn = kommune_navn;
    }

    public String getLokalitet_kode() {
        return lokalitet_kode;
    }

    public void setLokalitet_kode(String lokalitet_kode) {
        this.lokalitet_kode = lokalitet_kode;
    }

    public Number getLokalitet_type() {
        return lokalitet_type;
    }

    public void setLokalitet_type(Number lokalitet_type) {
        this.lokalitet_type = lokalitet_type;
    }

    public String getLokalitet_navn() {
        return lokalitet_navn;
    }

    public void setLokalitet_navn(String lokalitet_navn) {
        this.lokalitet_navn = lokalitet_navn;
    }

    public Number getPost_kode() {
        return post_kode;
    }

    public void setPost_kode(Number post_kode) {
        this.post_kode = post_kode;
    }

    public String getPost_navn() {
        return post_navn;
    }

    public void setPost_navn(String post_navn) {
        this.post_navn = post_navn;
    }

    public Number getStatus() {
        return status;
    }

    public void setStatus(Number status) {
        this.status = status;
    }

    public Number getSource() {
        return source;
    }

    public void setSource(Number source) {
        this.source = source;
    }

    public String getEtage() {
        return etage;
    }

    public void setEtage(String etage) {
        this.etage = etage;
    }

    public String getDoer() {
        return doer;
    }

    public void setDoer(String doer) {
        this.doer = doer;
    }

    public String getEnhed() {
        return enhed;
    }

    public void setEnhed(String enhed) {
        this.enhed = enhed;
    }

    public String getNummer() {
        return nummer;
    }

    public void setNummer(String nummer) {
        this.nummer = nummer;
    }

    public Number getVej_kode() {
        return vej_kode;
    }

    public void setVej_kode(Number vej_kode) {
        this.vej_kode = vej_kode;
    }

    public String getVej_navn() {
        return vej_navn;
    }

    public void setVej_navn(String vej_navn) {
        this.vej_navn = vej_navn;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getAccessAddress_objectId() {
        return accessAddress_objectId;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public void setAccessAddress_objectId(String accessAddress_objectId) {
        this.accessAddress_objectId = accessAddress_objectId;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getUnitAddress_objectId() {
        return unitAddress_objectId;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public void setUnitAddress_objectId(String unitAddress_objectId) {
        this.unitAddress_objectId = unitAddress_objectId;
    }
}
