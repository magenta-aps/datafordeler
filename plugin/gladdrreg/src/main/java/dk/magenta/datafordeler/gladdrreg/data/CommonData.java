package dk.magenta.datafordeler.gladdrreg.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.database.DataItem;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.Effect;
import dk.magenta.datafordeler.core.database.Identification;
import org.hibernate.Session;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlElement;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lars on 19-05-17.
 */
@MappedSuperclass
public class CommonData<V extends Effect, D extends DataItem> extends DataItem<V, D> {

    public static final String DB_FIELD_ACTIVE = "active";
    public static final String IO_FIELD_ACTIVE = "aktiv";

    @Column(name = DB_FIELD_ACTIVE)
    @JsonProperty
    @XmlElement
    private boolean active;

    public boolean isActive() {
        return this.active;
    }


    public static final String DB_FIELD_NOTE = "note";
    public static final String IO_FIELD_NOTE = "note";

    @Column(name = DB_FIELD_NOTE)
    @JsonProperty
    @XmlElement
    private String note;

    public String getNote() {
        return this.note;
    }


    public static final String DB_FIELD_USER = "registration_user";
    public static final String IO_FIELD_USER = "registration_user";

    @Column(name = DB_FIELD_USER)
    @JsonProperty
    @XmlElement
    private String registration_user;

    public String getRegistration_user() {
        return this.registration_user;
    }


    public static final String DB_FIELD_STATE = "state";
    public static final String IO_FIELD_STATE = "tilstand";

    @ManyToOne
    @JsonProperty
    @XmlElement
    @JoinColumn(name = DB_FIELD_STATE + DatabaseEntry.REF)
    private Identification state;

    public Identification getState() {
        return this.state;
    }


    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("active", this.active);
        map.put("note", this.note);
        map.put("registration_user", this.registration_user);
        map.put("state", this.state);
        return map;
    }

    @Override
    @JsonIgnore
    public HashMap<String, Identification> getReferences() {
        HashMap<String, Identification> references = super.getReferences();
        references.put("state", this.state);
        return references;
    }

    @Override
    public void updateReferences(HashMap<String, Identification> references) {
        super.updateReferences(references);
        if (references.containsKey("state")) {
            this.state = references.get("state");
        }
    }

    @Override
    public void forceLoad(Session session) {
    }
}
