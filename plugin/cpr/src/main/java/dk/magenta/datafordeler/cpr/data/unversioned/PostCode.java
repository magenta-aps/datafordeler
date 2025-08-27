package dk.magenta.datafordeler.cpr.data.unversioned;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.cpr.CprPlugin;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.hibernate.Session;

import java.util.Collections;
import java.util.UUID;

/**
 * Nontemporal storage of postcodes, to be referenced by bitemporal data items.
 */
@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_postcode", indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_postcode", columnList = "postnummer")
})
public class PostCode extends UnversionedEntity {

    public static final String DB_FIELD_POSTNUMBER = "postnummer";
    public static final String IO_FIELD_POSTNUMBER = "postnummer";

    @Column(name = DB_FIELD_POSTNUMBER)
    @JsonProperty(value = IO_FIELD_POSTNUMBER)
    private int postnummer;

    public int getPostnummer() {
        return this.postnummer;
    }

    public void setPostnummer(int postnummer) {
        this.postnummer = postnummer;
    }


    public static final String DB_FIELD_POSTDISTRICT = "postdistrikt";
    public static final String IO_FIELD_POSTDISTRICT = "postdistrikt";

    @Column(name = DB_FIELD_POSTDISTRICT)
    @JsonProperty(value = IO_FIELD_POSTDISTRICT)
    private String postdistrikt;

    public String getPostdistrikt() {
        return this.postdistrikt;
    }

    public void setPostdistrikt(String postdistrikt) {
        this.postdistrikt = postdistrikt;
    }

    public static String getDomain() {
        return "https://data.gl/cpr/postcode/1/rest/";
    }

    public static PostCode getPostcode(int code, String text, Session session) {
        PostCode postcode = QueryManager.getItem(session, PostCode.class, Collections.singletonMap("postnummer", code));
        if (postcode == null) {
            postcode = new PostCode();
            postcode.setPostnummer(code);
            postcode.setPostdistrikt(text);
            postcode.setIdentification(
                    QueryManager.getOrCreateIdentification(session, generateUUID(code), PostCode.getDomain())
            );
            session.persist(postcode);
        }
        return postcode;
    }

    public static UUID generateUUID(int postnummer) {
        String uuidInput = "postcode:" + postnummer;
        return UUID.nameUUIDFromBytes(uuidInput.getBytes());
    }

}
