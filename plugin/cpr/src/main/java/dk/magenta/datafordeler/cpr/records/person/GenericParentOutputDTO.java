package dk.magenta.datafordeler.cpr.records.person;

import dk.magenta.datafordeler.core.database.Nontemporal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

public class GenericParentOutputDTO implements Nontemporal {

    private String pituBaseUrl;
    private boolean isMother;
    private String cprNumber;

    public GenericParentOutputDTO() {
    }

    public boolean isMother() {
        return isMother;
    }

    public void setMother(boolean mother) {
        isMother = mother;
    }

    public String getCprNumber() {
        return cprNumber;
    }

    public void setCprNumber(String cprNumber) {
        this.cprNumber = cprNumber;
    }

    public void setPituBaseUrl(String pituBaseUrl) {
        this.pituBaseUrl = pituBaseUrl;
    }

    public String getParentSearchAdress() {
        return pituBaseUrl+"/cpr/person/1/rest/search?personnummer="+getCprNumber();
    }

    @Override
    public OffsetDateTime getDafoUpdated() {
        return null;
    }

    @Override
    public void setDafoUpdated(OffsetDateTime offsetDateTime) {

    }
}
