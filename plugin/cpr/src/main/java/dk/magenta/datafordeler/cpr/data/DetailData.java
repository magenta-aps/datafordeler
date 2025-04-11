package dk.magenta.datafordeler.cpr.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.core.util.ListHashMap;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static dk.magenta.datafordeler.core.database.Bitemporal.fixOffsetIn;
import static dk.magenta.datafordeler.core.database.Bitemporal.fixOffsetOut;

@MappedSuperclass
//@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class DetailData extends DatabaseEntry {

    public static final String DB_FIELD_DAFO_UPDATED = "dafoUpdated";
    public static final String IO_FIELD_DAFO_UPDATED = "dafoOpdateret";

    @Column(name = DB_FIELD_DAFO_UPDATED, columnDefinition = "datetime2")
    private OffsetDateTime dafoUpdated = null;

    @JsonProperty(value = IO_FIELD_DAFO_UPDATED)
    public OffsetDateTime getDafoUpdated() {
        return fixOffsetOut(this.dafoUpdated);
    }

    public void setDafoUpdated(OffsetDateTime dafoUpdated) {
        this.dafoUpdated = fixOffsetIn(dafoUpdated);
    }

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
     *
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
