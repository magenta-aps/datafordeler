package dk.magenta.datafordeler.cvr.data.shared;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.HashMap;
import java.util.Map;

import static dk.magenta.datafordeler.cvr.data.shared.SingleData.DB_FIELD_VALUE;
import static dk.magenta.datafordeler.cvr.data.shared.TextData.DB_FIELD_TYPE;

@Entity
@Table(name = "cvr_text", indexes = {
        @Index(name = "cvr_text_type", columnList = DB_FIELD_TYPE),
        @Index(name = "cvr_text_data", columnList = DB_FIELD_TYPE + ", " + DB_FIELD_VALUE)
})
public class TextData extends SingleData<String> {

    public static final String DB_FIELD_TYPE = "type";
    public static final String IO_FIELD_TYPE = "type";

    public enum Type {
        NAVN,
        EMAILADRESSE,
        TELEFONNUMMER,
        TELEFAXNUMMER,
        TYPE
    }

    public TextData() {
    }

    public TextData(Type type) {
        this.setType(type);
    }

    @JsonIgnore
    @Column(name = DB_FIELD_TYPE)
    private Type type;

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

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

}
