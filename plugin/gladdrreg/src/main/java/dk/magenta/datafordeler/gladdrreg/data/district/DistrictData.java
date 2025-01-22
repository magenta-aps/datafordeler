package dk.magenta.datafordeler.gladdrreg.data.district;

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
@Table(name = "gladdrreg_district_data", indexes = {
        @Index(name = "gladdrreg_district_code", columnList = DistrictData.DB_FIELD_CODE),
        @Index(name = "gladdrreg_district_abbrev", columnList = DistrictData.DB_FIELD_ABBREV),
        @Index(name = "gladdrreg_district_name", columnList = DistrictData.DB_FIELD_NAME)
})
public class DistrictData extends SumiffiikData<DistrictEffect, DistrictData> {

    public static final String DB_FIELD_CODE = "code";
    public static final String IO_FIELD_CODE = "kode";

    @JsonProperty
    @XmlElement
    @Column(name = DB_FIELD_CODE)
    private int code;

    public int getCode() {
        return this.code;
    }


    public static final String DB_FIELD_ABBREV = "abbrev";
    public static final String IO_FIELD_ABBREV = "forkortelse";

    @JsonProperty
    @XmlElement
    @Column(name = DB_FIELD_ABBREV)
    private String abbrev;

    public String getAbbrev() {
        return this.abbrev;
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
        map.put("abbrev", this.abbrev);
        map.put("name", this.name);
        return map;
    }

}
