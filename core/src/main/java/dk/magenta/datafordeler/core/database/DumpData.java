package dk.magenta.datafordeler.core.database;

import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.Table;

@javax.persistence.Entity
@Table(name = "dump_data")
public class DumpData extends DatabaseEntry {
    @Column(nullable = false)
    @Lob
    private byte[] data;

    public DumpData() {
    }

    public DumpData(byte[] data) {
        this.data = data;
    }

    byte[] getData() {
        return data;
    }

    public String toString() {
        return String.format("DumpData(%d)", this.getId());
    }
}
