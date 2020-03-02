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
 * Storage for data on a Person's church relation,
 * referenced by {@link PersonBaseData}
 */
@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_person_church")
public class PersonChurchData extends AuthorityDetailData {

    public static final String DB_FIELD_CHURCH_RELATION = "churchRelation";
    public static final String IO_FIELD_CHURCH_RELATION = "folkekirkeforhold";
    @Column(name = DB_FIELD_CHURCH_RELATION)
    @JsonProperty(value = IO_FIELD_CHURCH_RELATION)
    @XmlElement(name = IO_FIELD_CHURCH_RELATION)
    private Character churchRelation;

    public Character getChurchRelation() {
        return this.churchRelation;
    }

    public void setChurchRelation(Character churchRelation) {
        this.churchRelation = churchRelation;
    }

    @Override
    public Map<String, Object> databaseFields() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(DB_FIELD_CHURCH_RELATION, this.churchRelation);
        return map;
    }

    @Override
    protected PersonChurchData clone() {
        PersonChurchData clone = new PersonChurchData();
        clone.churchRelation = this.churchRelation;
        clone.setAuthority(this.getAuthority());
        clone.setDafoUpdated(this.getDafoUpdated());
        return clone;
    }

}
