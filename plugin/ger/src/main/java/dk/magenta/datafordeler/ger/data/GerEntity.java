package dk.magenta.datafordeler.ger.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.core.database.IdentifiedEntity;
import dk.magenta.datafordeler.core.database.Nontemporal;
import jakarta.persistence.*;
import org.hibernate.Session;

import java.time.OffsetDateTime;
import java.util.Collection;

@MappedSuperclass
public class GerEntity extends DatabaseEntry implements IdentifiedEntity {

    public static final String DB_FIELD_IDENTIFICATION = "identification";
    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    protected Identification identification;

    @Override
    public Identification getIdentification() {
        return this.identification;
    }

    @Override
    public void forceLoad(Session session) {
    }

    @Override
    public IdentifiedEntity getNewest(Collection<IdentifiedEntity> collection) {
        return null;
    }

    public void setIdentification(Identification identification) {
        this.identification = identification;
    }

    public static final String DB_FIELD_DAFO_UPDATED = Nontemporal.DB_FIELD_UPDATED;
    public static final String IO_FIELD_DAFO_UPDATED = "dafoOpdateret";

    @Column(name = DB_FIELD_DAFO_UPDATED, columnDefinition = "datetime2")
    private OffsetDateTime dafoUpdated;

    @Column(name = DB_FIELD_DAFO_UPDATED+"_new")
    private OffsetDateTime dafoUpdatedNew;

    @JsonProperty(value = IO_FIELD_DAFO_UPDATED)
    public OffsetDateTime getDafoUpdated() {
        return this.dafoUpdated;
    }

    public void setDafoUpdated(OffsetDateTime dafoUpdated) {
        this.dafoUpdated = dafoUpdated;
        this.dafoUpdatedNew = dafoUpdated;
    }

}
