package dk.magenta.datafordeler.cpr.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.core.database.IdentifiedEntity;
import dk.magenta.datafordeler.core.database.Nontemporal;
import jakarta.persistence.*;
import org.hibernate.Session;

import java.time.OffsetDateTime;
import java.util.UUID;

import static dk.magenta.datafordeler.core.database.Bitemporal.fixOffsetIn;
import static dk.magenta.datafordeler.core.database.Bitemporal.fixOffsetOut;

@MappedSuperclass
public abstract class CprRecordEntity extends DatabaseEntry implements IdentifiedEntity {

    public CprRecordEntity() {
    }

    public static final String DB_FIELD_IDENTIFICATION = "identification";

    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = DB_FIELD_IDENTIFICATION)
    public Identification identification;

    @Override
    public Identification getIdentification() {
        return this.identification;
    }

    public void setIdentification(Identification identification) {
        this.identification = identification;
    }

    public CprRecordEntity(Identification identification) {
        this.identification = identification;
    }

    public CprRecordEntity(UUID uuid, String domain) {
        this(new Identification(uuid, domain));
    }


    public UUID getUUID() {
        return this.identification.getUuid();
    }


    @Override
    public void forceLoad(Session session) {

    }

    public static final String DB_FIELD_DAFO_UPDATED = Nontemporal.DB_FIELD_UPDATED;
    public static final String IO_FIELD_DAFO_UPDATED = "dafoOpdateret";

    @Column(name = DB_FIELD_DAFO_UPDATED, columnDefinition = "datetime2")
    private OffsetDateTime dafoUpdated;

    @Column(name = DB_FIELD_DAFO_UPDATED+"_new")
    private OffsetDateTime dafoUpdatedNew;

    @JsonProperty(value = IO_FIELD_DAFO_UPDATED)
    public OffsetDateTime getDafoUpdated() {
        return fixOffsetOut(this.dafoUpdated);
    }

    public void setDafoUpdated(OffsetDateTime dafoUpdated) {
        this.dafoUpdated = fixOffsetIn(dafoUpdated);
        this.dafoUpdatedNew = dafoUpdated;
    }


}
