package dk.magenta.datafordeler.core.database;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Objects;

/**
 * Single file entry for InterruptedPull
 */
@Entity
@Table(name = "interrupted_pull_file")
public class InterruptedPullFile extends DatabaseEntry {

    public InterruptedPullFile() {
    }

    public InterruptedPullFile(InterruptedPull interruptedPull, String filename) {
        this.interruptedPull = interruptedPull;
        this.filename = filename;
    }

    @ManyToOne(targetEntity = InterruptedPull.class, optional = false)
    private InterruptedPull interruptedPull;

    @Column(length = 1024)
    private String filename;

    public String getFilename() {
        return this.filename;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InterruptedPullFile that = (InterruptedPullFile) o;
        return Objects.equals(interruptedPull, that.interruptedPull) &&
                Objects.equals(filename, that.filename);
    }

}
