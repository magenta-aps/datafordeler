package dk.magenta.datafordeler.core.database;

import dk.magenta.datafordeler.core.plugin.Plugin;

import javax.persistence.Entity;
import javax.persistence.*;
import java.io.File;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Entity that stores data about an interrupted Pull. When a running Pull is interrupted,
 * it should result in a new InterruptedPull being stored to the database, describing:
 * * Which schema was being precessed (so the relevant EntityManager can be found)
 * * Which file(s) were being imported, so the resumed pull can run on the same data.
 * * Which chunk (offset) was being handled at the time of interruption. On interrupt,
 * the processing of this chunk would be rolled back, and so resuming should start by
 * processing this chunk from the beginning.
 */
@Entity
@Table(name="interrupted_pull")
public class InterruptedPull extends DatabaseEntry {


    @Column
    private String plugin;

    public String getPlugin() {
        return this.plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    public void setPlugin(Plugin plugin) {
        this.plugin = plugin.getName();
    }


    @Column
    private String schemaName;

    public String getSchemaName() {
        return this.schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }



    @Column
    private OffsetDateTime startTime;

    public OffsetDateTime getStartTime() {
        return this.startTime;
    }

    public void setStartTime(OffsetDateTime startTime) {
        this.startTime = startTime;
    }



    @Column
    private OffsetDateTime interruptTime;

    public OffsetDateTime getInterruptTime() {
        return this.interruptTime;
    }

    public void setInterruptTime(OffsetDateTime interruptTime) {
        this.interruptTime = interruptTime;
    }



    @OneToMany(targetEntity = InterruptedPullFile.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "interruptedPull")
    private List<InterruptedPullFile> files = new ArrayList<>();

    public List<InterruptedPullFile> getFiles() {
        return this.files;
    }

    public void addFile(File file) {
        InterruptedPullFile newFile = new InterruptedPullFile(this, file.getAbsolutePath());
        for (InterruptedPullFile existing : this.files) {
            if (existing != null && existing.equals(newFile)) {
                return;
            }
        }
        this.files.add(newFile);
    }


    public void setFiles(Collection<File> files) {
        if (files != null) {
            for (File file : files) {
                this.addFile(file);
            }
        }
    }



    @Column
    private long chunk;

    public long getChunk() {
        return this.chunk;
    }

    public void setChunk(long chunk) {
        this.chunk = chunk;
    }



    @Column
    private String importConfiguration;

    public String getImportConfiguration() {
        return this.importConfiguration;
    }

    public void setImportConfiguration(String importConfiguration) {
        this.importConfiguration = importConfiguration;
    }
}
