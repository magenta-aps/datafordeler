package dk.magenta.datafordeler.prisme;


import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Class for resident information
 */
public class ResidentItem {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String pnr;


    private boolean residentInGL;

    private LocalDate timestamp;

    public ResidentItem() {
    }

    public ResidentItem(String pnr, boolean residentInGL, LocalDate timestamp) {
        this.pnr = pnr;
        this.residentInGL = residentInGL;
        this.timestamp = timestamp;
    }

    public String getPnr() {
        return pnr;
    }

    public void setPnr(String pnr) {
        this.pnr = pnr;
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
