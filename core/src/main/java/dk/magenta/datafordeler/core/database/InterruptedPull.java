package dk.magenta.datafordeler.core.database;

import dk.magenta.datafordeler.core.plugin.Plugin;
import jakarta.persistence.Entity;
import jakarta.persistence.*;

import java.io.File;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static dk.magenta.datafordeler.core.database.Bitemporal.fixOffsetIn;
import static dk.magenta.datafordeler.core.database.Bitemporal.fixOffsetOut;

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
@Table(name = "interrupted_pull")
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


    @Column(columnDefinition = "datetime2")
    private OffsetDateTime startTime;

    @Column(name="startTime"+"_new")
    private OffsetDateTime startTimeNew;

    public OffsetDateTime getStartTime() {
        return fixOffsetOut(this.startTime);
    }

    public void setStartTime(OffsetDateTime startTime) {
        this.startTime = fixOffsetIn(startTime);
        this.startTimeNew = startTime;
    }


    @Column(columnDefinition = "datetime2")
    private OffsetDateTime interruptTime;

    @Column(name="interruptTime"+"_new")
    private OffsetDateTime interruptTimeNew;

    public OffsetDateTime getInterruptTime() {
        return fixOffsetOut(this.interruptTime);
    }

    public void setInterruptTime(OffsetDateTime interruptTime) {
        this.interruptTime = fixOffsetIn(interruptTime);
        this.interruptTimeNew = interruptTime;
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
