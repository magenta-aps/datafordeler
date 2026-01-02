package dk.magenta.datafordeler.cpr.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.*;
import dk.magenta.datafordeler.core.migration.MigrateModel;
import jakarta.persistence.*;
import org.hibernate.Session;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static dk.magenta.datafordeler.core.database.Bitemporal.fixOffsetIn;

@MappedSuperclass
public abstract class CprRecordEntity extends DatabaseEntry implements IdentifiedEntity, MigrateModel {

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

    @JsonIgnore
    @Column(name = DB_FIELD_DAFO_UPDATED+"_new")
    private OffsetDateTime dafoUpdatedNew;

    @JsonProperty(value = IO_FIELD_DAFO_UPDATED)
    public OffsetDateTime getDafoUpdated() {
        return this.dafoUpdatedNew;
    }

    public void setDafoUpdated(OffsetDateTime dafoUpdated) {
        this.dafoUpdated = fixOffsetIn(dafoUpdated);
        this.dafoUpdatedNew = dafoUpdated;
    }

    public void updateTimestamp() {
        this.dafoUpdatedNew = Bitemporal.fixOffsetOut(this.dafoUpdated);
    }

    public static List<String> updateFields() {
        return List.of(DB_FIELD_DAFO_UPDATED);
    }

}
