package dk.magenta.datafordeler.gladdrreg.data.state;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.gladdrreg.data.SumiffiikData;

import jakarta.persistence.Column;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lars on 16-05-17.
 */
@Entity
@Table(name = "gladdrreg_state_data", indexes = {
        @Index(name = "gladdrreg_state_code", columnList = StateData.DB_FIELD_CODE),
        @Index(name = "gladdrreg_state_name", columnList = StateData.DB_FIELD_NAME),
        @Index(name = "gladdrreg_state_description", columnList = StateData.DB_FIELD_DESCRIPTION)
})
public class StateData extends SumiffiikData<StateEffect, StateData> {

    public static final String DB_FIELD_CODE = "code";
    public static final String IO_FIELD_CODE = "kode";

    @Column(name = DB_FIELD_CODE)
    @JsonProperty
    @XmlElement
    private String code;

    public String getCode() {
        return this.code;
    }


    public static final String DB_FIELD_NAME = "name";
    public static final String IO_FIELD_NAME = "navn";

    @Column(name = DB_FIELD_NAME)
    @JsonProperty
    @XmlElement
    private String name;

    public String getName() {
        return this.name;
    }


    public static final String DB_FIELD_DESCRIPTION = "description";
    public static final String IO_FIELD_DESCRIPTION = "beskrivelse";

    @Column(name = DB_FIELD_DESCRIPTION)
    @JsonProperty
    @XmlElement
    private String description;

    public String getDescription() {
        return this.description;
    }


    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>(super.asMap());
        map.put("code", this.code);
        map.put("name", this.name);
        map.put("description", this.description);
        return map;
    }

}
