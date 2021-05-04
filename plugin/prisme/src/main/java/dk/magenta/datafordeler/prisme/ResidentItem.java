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


    private boolean residentInGL;

    private LocalDate timestamp;

    public ResidentItem() {
    }

    public ResidentItem(String cprNummer, boolean residentInGL, LocalDate timestamp) {
        this.cprNummer = cprNummer;
        this.residentInGL = residentInGL;
        this.timestamp = timestamp;
    }

    public String getCprNummer() {
        return cprNummer;
    }

    public void setCprNummer(String cprNummer) {
        this.cprNummer = cprNummer;
    }

    public void setResidentInGL(boolean residentInGL) {
        this.residentInGL = residentInGL;
    }

    public boolean getResidentInGL() {
        return residentInGL;
    }

    public void setTimestamp(LocalDate timestamp) {
        this.timestamp = timestamp;
    }

    public LocalDate getTimestamp() {
        return timestamp;
    }

}
