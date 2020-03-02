package dk.magenta.datafordeler.cvr.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.core.util.ListHashMap;

import javax.persistence.MappedSuperclass;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@MappedSuperclass
//@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class DetailData extends DatabaseEntry {

    public abstract Map<String, Object> asMap();

    @JsonIgnore
    public HashMap<String, Identification> getReferences() {
        return new HashMap<>();
    }

    public void updateReferences(HashMap<String, Identification> references) {
    }

    /**
     * Obtain contained data as a Map
     * Internally used for comparing DataItems
     * @return Map of all relevant attributes
     */
    public Map<String, Object> databaseFields() {
        return this.asMap();
    }

    public static Map<String, Object> listDatabaseFields(Collection<? extends DetailData> list) {
        ListHashMap<String, Object> map = new ListHashMap<>();
        for (DetailData data : list) {
            Map<String, Object> fields = data.databaseFields();
            for (String key : fields.keySet()) {
                map.add(key, fields.get(key));
            }
        }
        HashMap<String, Object> out = new HashMap<>();
        for (String key : map.keySet()) {
            out.put(key, map.get(key));
        }
        return out;
    }
}
