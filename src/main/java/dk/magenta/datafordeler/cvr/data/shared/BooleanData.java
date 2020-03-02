package dk.magenta.datafordeler.cvr.data.shared;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(
    name = "cvr_company_boolean",
    indexes = {
            @Index(name = "cvr_boolean_type", columnList = "type")
    }
)
public class BooleanData extends SingleData<Boolean> {

    public enum Type {
        REKLAME_BESKYTTELSE,
        ER_PRIMAER_ENHED,
    }

    public BooleanData() {
    }

    public BooleanData(Type type) {
        this.setType(type);
    }

    //---------------------------------------------------

    public static final String DB_FIELD_TYPE = "type";
    public static final String IO_FIELD_TYPE = "type";

    @JsonIgnore
    @Column(name = DB_FIELD_TYPE)
    private Type type;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    //---------------------------------------------------

    public String getFieldName() {
        return this.type.name().toLowerCase();
    }

    @JsonAnyGetter
    public Map<String, Object> asMap() {
        HashMap<String, Object> fields = new HashMap<>();
        fields.put(this.getFieldName(), this.getValue());
        return fields;
    }

    /**
     * Return a map of attributes, including those from the superclass
     * @return
     */
    @JsonIgnore
    public Map<String, Object> databaseFields() {
        HashMap<String, Object> map = new HashMap<>(super.databaseFields());
        map.put(DB_FIELD_TYPE, this.type);
        return map;
    }

}
