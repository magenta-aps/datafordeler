package dk.magenta.datafordeler.gladdrreg.data.postalcode;

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
@javax.persistence.Entity
@Table(name = "gladdrreg_postalcode_data", indexes = {
        @Index(name = "gladdrreg_postalcode_code", columnList = PostalCodeData.DB_FIELD_CODE),
        @Index(name = "gladdrreg_postalcode_name", columnList = PostalCodeData.DB_FIELD_NAME)
})
public class PostalCodeData extends SumiffiikData<PostalCodeEffect, PostalCodeData> {

    public static final String DB_FIELD_CODE = "code";
    public static final String IO_FIELD_CODE = "postnummer";

    @JsonProperty
    @XmlElement
    @Column(name = DB_FIELD_CODE)
    private int code;

    public int getCode() {
        return this.code;
    }

    public static final String DB_FIELD_NAME = "name";
    public static final String IO_FIELD_NAME = "navn";

    @JsonProperty
    @XmlElement
    @Column(name = DB_FIELD_NAME)
    private String name;

    public String getName() {
        return this.name;
    }

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>(super.asMap());
        map.put("code", this.code);
        map.put("name", this.name);
        return map;
    }

}
