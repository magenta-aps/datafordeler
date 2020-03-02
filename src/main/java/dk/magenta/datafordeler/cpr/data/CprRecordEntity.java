package dk.magenta.datafordeler.cpr.data;

import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.core.database.IdentifiedEntity;
import org.hibernate.Session;

import javax.persistence.*;
import java.util.UUID;

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

}
