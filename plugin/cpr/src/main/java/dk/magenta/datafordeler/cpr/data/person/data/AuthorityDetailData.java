package dk.magenta.datafordeler.cpr.data.person.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.cpr.data.DetailData;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlElement;
import java.util.Collections;
import java.util.Map;

/**
 * Superclass for storing data with an authority field
 */
@MappedSuperclass
public abstract class AuthorityDetailData extends DetailData {

    public static final String DB_FIELD_AUTHORITY = "authority";
    public static final String IO_FIELD_AUTHORITY = "myndighed";
    @Column(name = DB_FIELD_AUTHORITY)
    @JsonProperty(value = IO_FIELD_AUTHORITY)
    @XmlElement(name = IO_FIELD_AUTHORITY)
    private int authority;

    public int getAuthority() {
        return this.authority;
    }

    public void setAuthority(int authority) {
        if (authority != 0 || this.authority == 0) {
            this.authority = authority;
        }
    }

    @Override
    public Map<String, Object> databaseFields() {
        return Collections.singletonMap(DB_FIELD_AUTHORITY, this.authority);
    }

    @Override
    public Map<String, Object> asMap() {
        return Collections.singletonMap("authority", this.authority);
    }
}
