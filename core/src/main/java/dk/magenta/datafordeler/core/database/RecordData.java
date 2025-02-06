package dk.magenta.datafordeler.core.database;

import dk.magenta.datafordeler.core.util.Equality;
import jakarta.persistence.Entity;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

/**
 * Storage of how we got a piece of data into the system, ie. source, date, ...
 */
@Entity
@Table(name = "record")
public class RecordData extends DatabaseEntry implements Comparable<RecordData> {

    public RecordData() {
    }

    public RecordData(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @ManyToOne
    private RecordCollection collection;

    public RecordCollection getDataItem() {
        return this.collection;
    }

    public void setCollection(RecordCollection collection) {
        this.collection = collection;
    }


    @Column
    private OffsetDateTime timestamp;

    public OffsetDateTime getTimestamp() {
        return this.timestamp;
    }


    @Lob
    @Column
    private String sourceData;

    public String getSourceData() {
        return this.sourceData;
    }

    public void setSourceData(String sourceData) {
        this.sourceData = sourceData;
    }


    @Column(length = 1024)
    private String sourceReference;

    public String getSourceReference() {
        return this.sourceReference;
    }

    public void setSourceReference(String sourceReference) {
        this.sourceReference = sourceReference;
    }

    @Override
    public int compareTo(RecordData o) {
        return Equality.compare(this.timestamp, o == null ? null : o.timestamp, OffsetDateTime.class, false);
    }
}
