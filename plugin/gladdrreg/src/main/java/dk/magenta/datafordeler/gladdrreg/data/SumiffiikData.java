package dk.magenta.datafordeler.gladdrreg.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.database.DataItem;
import dk.magenta.datafordeler.core.database.Effect;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlElement;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lars on 19-05-17.
 */
@MappedSuperclass
public class SumiffiikData<V extends Effect, D extends DataItem> extends CommonData<V, D> {

    public static final String DB_FIELD_SUMIFFIIK = "sumiffiik";
    public static final String IO_FIELD_SUMIFFIIK = "sumiffiik";

    @Column(name = DB_FIELD_SUMIFFIIK)
    @JsonProperty
    @XmlElement
    private String sumiffiik;

    public String getSumiffiik() {
        return this.sumiffiik;
    }


    public static final String DB_FIELD_DOMAIN = "sumiffiik_domain";
    public static final String IO_FIELD_DOMAIN = "sumiffiik_domain";

    @Column(name = DB_FIELD_DOMAIN)
    @JsonProperty
    @XmlElement
    private String sumiffiik_domain;

    public String getSumiffiik_domain() {
        return this.sumiffiik_domain;
    }


    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>(super.asMap());
        map.put("sumiffiik", this.sumiffiik);
        map.put("sumiffiik_domain", this.sumiffiik_domain);
        return map;
    }

}
