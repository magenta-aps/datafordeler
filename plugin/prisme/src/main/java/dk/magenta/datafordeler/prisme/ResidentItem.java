package dk.magenta.datafordeler.prisme;


import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Class for resident information
 */
public class ResidentItem {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String cprNummer;


    private boolean borIGL;

    private LocalDate dato;

    public ResidentItem() {
    }

    public ResidentItem(String cprNummer, boolean borIGL, LocalDate dato) {
        this.cprNummer = cprNummer;
        this.borIGL = borIGL;
        this.dato = dato;
    }

    public String getCprNummer() {
        return cprNummer;
    }

    public void setCprNummer(String cprNummer) {
        this.cprNummer = cprNummer;
    }

    public void setBorIGL(boolean borIGL) {
        this.borIGL = borIGL;
    }

    public boolean getBorIGL() {
        return borIGL;
    }

    public void setDato(LocalDate dato) {
        this.dato = dato;
    }

    public LocalDate getDato() {
        return dato;
    }

}
