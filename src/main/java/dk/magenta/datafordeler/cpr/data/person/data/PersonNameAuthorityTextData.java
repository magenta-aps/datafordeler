package dk.magenta.datafordeler.cpr.data.person.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.cpr.CprPlugin;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import java.util.HashMap;
import java.util.Map;

/**
 * Storage for data on a Person's name authority,
 * referenced by {@link dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData}
 */
@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_person_name_authority_text")
public class PersonNameAuthorityTextData extends AuthorityDetailData {

    public static final String DB_FIELD_TEXT = "text";
    public static final String IO_FIELD_TEXT = "tekst";
    @Column(name = DB_FIELD_TEXT)
    @JsonProperty(value = IO_FIELD_TEXT)
    @XmlElement(name = IO_FIELD_TEXT)
    private String text;

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public Map<String, Object> databaseFields() {
        HashMap<String, Object> map = new HashMap<>(super.databaseFields());
        map.put(DB_FIELD_TEXT, this.text);
        return map;
    }

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>(super.asMap());
        map.put("text", this.text);
        return map;
    }

    @Override
    protected PersonNameAuthorityTextData clone() {
        PersonNameAuthorityTextData clone = new PersonNameAuthorityTextData();
        clone.text = this.text;
        clone.setAuthority(this.getAuthority());
        clone.setDafoUpdated(this.getDafoUpdated());
        return clone;
    }
}
