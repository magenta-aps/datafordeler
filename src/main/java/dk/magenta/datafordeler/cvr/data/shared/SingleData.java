package dk.magenta.datafordeler.cvr.data.shared;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.cvr.data.DetailData;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlElement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@MappedSuperclass
public abstract class SingleData<T> extends DetailData {

    public static final String DB_FIELD_VALUE = "value";
    public static final String IO_FIELD_VALUE = "vaerdi";

    @Column
    private T value;

    @JsonProperty(value = IO_FIELD_VALUE)
    @XmlElement(name = IO_FIELD_VALUE)
    public T getValue() {
        return this.value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @JsonIgnore
    public Map<String, Object> databaseFields() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(DB_FIELD_VALUE, this.getValue());
        return map;
    }

    @Override
    public Map<String, Object> asMap() {
        return Collections.singletonMap(DB_FIELD_VALUE, this.getValue());
    }

}
