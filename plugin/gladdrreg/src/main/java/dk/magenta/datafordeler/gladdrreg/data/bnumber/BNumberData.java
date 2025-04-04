package dk.magenta.datafordeler.gladdrreg.data.bnumber;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.gladdrreg.data.SumiffiikData;

import jakarta.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lars on 16-05-17.
 */
@Entity
@Table(name = "gladdrreg_bnumber_data", indexes = {
        @Index(name = "gladdrreg_bnumber_code", columnList = BNumberData.DB_FIELD_CODE),
        @Index(name = "gladdrreg_bnumber_type", columnList = BNumberData.DB_FIELD_TYPE),
        @Index(name = "gladdrreg_bnumber_name", columnList = BNumberData.DB_FIELD_TYPE)
})
public class BNumberData extends SumiffiikData<BNumberEffect, BNumberData> {

    public static final String DB_FIELD_CODE = "code";
    public static final String IO_FIELD_CODE = "bnummer";

    @JsonProperty
    @XmlElement
    @Column(name = DB_FIELD_CODE)
    private String code;

    public String getCode() {
        return this.code;
    }


    public static final String DB_FIELD_TYPE = "b_type";
    public static final String IO_FIELD_TYPE = "type";

    @JsonProperty
    @XmlElement
    @Column(name = DB_FIELD_TYPE)
    private String b_type;

    public String getType() {
        return this.b_type;
    }

    public static final String DB_FIELD_CALLNAME = "b_callname";
    public static final String IO_FIELD_CALLNAME = "kaldenavn";

    @JsonProperty
    @XmlElement
    @Column(name = DB_FIELD_CALLNAME)
    private String b_callname;

    public String getCallname() {
        return this.b_callname;
    }


    public static final String DB_FIELD_LOCATION = "location";
    public static final String IO_FIELD_LOCATION = "lokation";

    @JsonProperty
    @XmlElement
    @ManyToOne
    @JoinColumn(name = DB_FIELD_LOCATION + DatabaseEntry.REF)
    private Identification location;

    public Identification getLocation() {
        return this.location;
    }


    public static final String DB_FIELD_MUNICIPALITY = "municipality";
    public static final String IO_FIELD_MUNICIPALITY = "kommune";

    @JsonProperty
    @XmlElement
    @ManyToOne
    @JoinColumn(name = DB_FIELD_MUNICIPALITY + DatabaseEntry.REF)
    private Identification municipality;

    public Identification getMunicipality() {
        return this.municipality;
    }


    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>(super.asMap());
        map.put("code", this.code);
        map.put("b_type", this.b_type);
        map.put("b_callname", this.b_callname);
        return map;
    }

    @Override
    @JsonIgnore
    public HashMap<String, Identification> getReferences() {
        HashMap<String, Identification> references = super.getReferences();
        references.put("municipality", this.municipality);
        references.put("location", this.location);
        return references;
    }

    @Override
    public void updateReferences(HashMap<String, Identification> references) {
        super.updateReferences(references);
        if (references.containsKey("municipality")) {
            this.municipality = references.get("municipality");
        }
        if (references.containsKey("location")) {
            this.location = references.get("location");
        }
    }

}
