package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.util.Objects;

@MappedSuperclass
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseAttributeValueRecord extends CvrBitemporalRecord {

    public static final String DB_FIELD_VALUE = "value";
    public static final String IO_FIELD_VALUE = "vaerdi";

    @Column(name = DB_FIELD_VALUE, length = 8000)
    @JsonProperty(value = IO_FIELD_VALUE)
    protected String value;

    @JsonProperty(value = IO_FIELD_VALUE)
    public void setValue(String value) {
        if (value != null && value.length() > 8000) {
            value = value.substring(0, 8000);
        }
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        BaseAttributeValueRecord that = (BaseAttributeValueRecord) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }
}
