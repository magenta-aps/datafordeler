package dk.magenta.datafordeler.cpr.records.person;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dk.magenta.datafordeler.core.database.Nontemporal;

import java.time.OffsetDateTime;

public class GenericParentOutputDTO implements Nontemporal {

    private String pituBaseUrl;
    private boolean isMother;
    private String pnr;

    public GenericParentOutputDTO() {
    }

    public String getPnr() {
        return pnr;
    }

    public void setPnr(String pnr) {
        this.pnr = pnr;
    }

    public void setPituBaseUrl(String pituBaseUrl) {
        this.pituBaseUrl = pituBaseUrl;
    }

    public String getParentSearchAddress() {
        return pituBaseUrl + "/cpr/person/1/rest/search?pnr=" + getPnr();
    }

    @Override
    @JsonIgnore
    public OffsetDateTime getDafoUpdated() {
        return null;
    }

    @Override
    public void setDafoUpdated(OffsetDateTime offsetDateTime) {

    }
}
