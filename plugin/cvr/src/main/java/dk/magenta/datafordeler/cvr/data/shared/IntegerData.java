package dk.magenta.datafordeler.cvr.data.shared;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Map;

import static dk.magenta.datafordeler.cvr.data.shared.SingleData.DB_FIELD_VALUE;

@Entity
@Table(name = "cvr_integer", indexes = {
        @Index(name = "cvr_integer_data", columnList = DB_FIELD_VALUE)
})
public class IntegerData extends SingleData<Long> {

    public IntegerData() {
    }

    public Map<String, Object> asMap() {
        return null;
    }

}
