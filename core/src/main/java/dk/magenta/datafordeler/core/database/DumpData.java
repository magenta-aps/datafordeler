package dk.magenta.datafordeler.core.database;

import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import jakarta.persistence.Entity;

@Entity
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
