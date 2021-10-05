package dk.magenta.datafordeler.core.database;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.Session;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Superclass for bitemporal data, pointing to Effects objects.
 * Pieces of data sharing exact bitemporality may be stored in one DataItem, pointing
 * to all the Effects applicable (and by extension, Registrations).
 */
@MappedSuperclass
@Embeddable
// @Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class DataItem<V extends Effect, D extends DataItem> extends DatabaseEntry {

    public static final String FILTER_RECORD_AFTER = "recordAfterFilter";
    public static final String FILTERPARAM_RECORD_AFTER = "recordAfterDate";

    public DataItem() {
    }

    /**
     * Add an Effect to this dataItem
     * @param effect
     */
    public void addEffect(V effect) {
        effect.dataItems.add(this);
        this.effects.add(effect);
    }

    /**
     * Remove a previously added Effect
     * @param effect
     */
    public void removeEffect(V effect) {
        effect.dataItems.remove(this);
        this.effects.remove(effect);
    }

    @ManyToMany(mappedBy = "dataItems")
    private Set<V> effects = new HashSet<V>();


    /**
     * Get all Effects for this item
     * @return
     */
    public Set<V> getEffects() {
        return effects;
    }

    /**
     * Compares this object with another DataItem
     * @param other
     * @return
     */
    public boolean equalData(D other) {
        return this.asMap().equals(other.asMap());
    }


    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL)
    private RecordCollection recordSet = null;

    public void addRecordData(RecordData recordData) {
        if (this.recordSet == null) {
            this.recordSet = new RecordCollection();
        }
        this.recordSet.addRecord(recordData);
        this.setLastUpdated(this.recordSet.getNewestRecord().getTimestamp());
    }

    @JsonIgnore
    public RecordCollection getRecordSet() {
        return this.recordSet;
    }

    public static final String DB_FIELD_LAST_UPDATED = "lastUpdated";
    public static final String IO_FIELD_LAST_UPDATED = "sidstOpdateret";

    @Column(name = DB_FIELD_LAST_UPDATED)
    private OffsetDateTime lastUpdated;

    public OffsetDateTime getLastUpdated() {
        return this.lastUpdated;
    }

    public void setLastUpdated(OffsetDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void setUpdated(OffsetDateTime lastUpdated) {
        if (this.lastUpdated == null || (lastUpdated != null && lastUpdated.isAfter(this.lastUpdated))) {
            this.setLastUpdated(lastUpdated);
        }
    }

    /**
     * Obtain contained data as a Map
     * Used for serializing DataItems merged into one wrapper
     * @return Map of all relevant attributes
     */
    public abstract Map<String, Object> asMap();

    /**
     * Obtain contained data as a Map
     * Internally used for comparing DataItems
     * @return Map of all relevant attributes
     */
    public Map<String, Object> databaseFields() {
        return this.asMap();
    }

    /**
     * Pretty-print contained data
     * @return Compiled string output
     */
    public String toString() {
        return this.toString(0);
    }

    /**
     * Pretty-print contained data
     * @param indent Number of spaces to indent the output with
     * @return Compiled string output
     */
    public String toString(int indent) {
        String indentString = new String(new char[4 * (indent)]).replace("\0", " ");
        String subIndentString = new String(new char[4 * (indent + 1)]).replace("\0", " ");
        StringJoiner s = new StringJoiner("\n");
        s.add(indentString + this.getClass().getSimpleName() + "["+this.hashCode()+"] {");

        Map<String, Object> map = this.asMap();
        for (String key : new TreeSet<>(map.keySet())) {
            s.add(subIndentString + key + ": " + map.get(key));
        }

        s.add(indentString + "}");
        return s.toString();
    }

    public HashMap<String, Identification> getReferences() {
        return new HashMap<>();
    }

    public void updateReferences(HashMap<String, Identification> references) {
    }

    public abstract void forceLoad(Session session);

}
