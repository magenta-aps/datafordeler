package dk.magenta.datafordeler.core.database;

import dk.magenta.datafordeler.core.util.Equality;
import jakarta.persistence.Entity;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

import static dk.magenta.datafordeler.core.database.Bitemporal.fixOffsetIn;
import static dk.magenta.datafordeler.core.database.Bitemporal.fixOffsetOut;

/**
 * Storage of how we got a piece of data into the system, ie. source, date, ...
 */
@Entity
@Table(name = "record")
public class RecordData extends DatabaseEntry implements Comparable<RecordData> {

    public RecordData() {
    }

    public RecordData(OffsetDateTime timestamp) {
        this.timestamp = fixOffsetIn(timestamp);
        this.timestampNew = timestamp;
    }

    @ManyToOne
    private RecordCollection collection;

    public RecordCollection getDataItem() {
        return this.collection;
    }

    public void setCollection(RecordCollection collection) {
        this.collection = collection;
    }


    @Column(columnDefinition = "datetime2")
    private OffsetDateTime timestamp;

    public OffsetDateTime getTimestamp() {
        return fixOffsetOut(this.timestamp);
    }

    @Column(name="timestamp"+"_new")
    private OffsetDateTime timestampNew;


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
