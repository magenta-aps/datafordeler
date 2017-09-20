package dk.magenta.datafordeler.core.database;

import javax.persistence.CascadeType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Collection;
import java.util.HashSet;

@javax.persistence.Entity
@Table(name = "record_collection")
public class RecordCollection extends DatabaseEntry {

    public RecordCollection() {
    }

    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL)
    private Collection<RecordData> records = new HashSet<>();

    public Collection<RecordData> getRecords() {
        return this.records;
    }

    public void addRecord(RecordData record) {
        this.records.add(record);
        record.setCollection(this);
    }

    public RecordData getNewestRecord() {
        RecordData newest = null;
        for (RecordData recordData : this.records) {
            if (recordData.compareTo(newest) > 0) {
                newest = recordData;
            }
        }
        return newest;
    }
}
