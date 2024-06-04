package dk.magenta.datafordeler.core.io;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.hibernate.Session;

import java.net.URI;
import java.time.OffsetDateTime;

public class ImportMetadata {

    public ImportMetadata() {
        this.importTime = OffsetDateTime.now();
    }


    private OffsetDateTime importTime;

    public OffsetDateTime getImportTime() {
        return this.importTime;
    }

    public void setImportTime(OffsetDateTime importTime) {
        this.importTime = importTime;
    }


    private Session session;

    public Session getSession() {
        return this.session;
    }

    public void setSession(Session session) {
        this.session = session;
    }


    private URI currentURI;

    public URI getCurrentURI() {
        return this.currentURI;
    }

    public void setCurrentURI(URI currentURI) {
        this.currentURI = currentURI;
    }


    private boolean stop = false;

    public boolean getStop() {
        return this.stop;
    }

    public void setStop() {
        this.stop = true;
    }


    private boolean transactionInProgress;

    public boolean isTransactionInProgress() {
        return this.transactionInProgress;
    }

    public void setTransactionInProgress(boolean transactionInProgress) {
        this.transactionInProgress = transactionInProgress;
    }


    private long startChunk = 0;

    public long getStartChunk() {
        return this.startChunk;
    }

    public void setStartChunk(long startChunk) {
        this.startChunk = startChunk;
    }


    private ObjectNode importConfiguration = null;

    public ObjectNode getImportConfiguration() {
        return this.importConfiguration;
    }

    public void setImportConfiguration(ObjectNode importConfiguration) {
        this.importConfiguration = importConfiguration;
    }

    public boolean hasImportConfiguration() {
        return this.importConfiguration != null && !this.importConfiguration.isEmpty();
    }
}
