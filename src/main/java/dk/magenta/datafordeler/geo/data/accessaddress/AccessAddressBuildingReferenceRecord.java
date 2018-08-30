package dk.magenta.datafordeler.geo.data.accessaddress;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.geo.GeoPlugin;
import dk.magenta.datafordeler.geo.data.GeoEntity;
import dk.magenta.datafordeler.geo.data.building.BuildingEntity;
import dk.magenta.datafordeler.geo.data.common.GeoMonotemporalRecord;
import dk.magenta.datafordeler.geo.data.locality.LocalityEntity;
import dk.magenta.datafordeler.geo.data.locality.LocalityQuery;
import org.hibernate.Session;

import javax.persistence.*;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = GeoPlugin.DEBUG_TABLE_PREFIX + AccessAddressBuildingReferenceRecord.TABLE_NAME, indexes = {
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + AccessAddressBuildingReferenceRecord.TABLE_NAME + AccessAddressBuildingReferenceRecord.DB_FIELD_ENTITY,
                columnList = AccessAddressBuildingReferenceRecord.DB_FIELD_ENTITY + DatabaseEntry.REF
        ),
})
public class AccessAddressBuildingReferenceRecord extends GeoMonotemporalRecord<AccessAddressEntity> {

    public static final String TABLE_NAME = "geo_access_address_building";

    public AccessAddressBuildingReferenceRecord() {
    }

    public AccessAddressBuildingReferenceRecord(UUID uuid) {
        this.uuid = uuid;
    }


    @Transient
    @JsonIgnore
    private UUID uuid;

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }



    public static final String DB_FIELD_REFERENCE = "reference";
    @ManyToOne
    @JsonIgnore
    private Identification reference;

    public Identification getReference() {
        return this.reference;
    }

    public void wire(Session session) {
        if (this.reference == null && this.uuid != null) {
            BuildingEntity localityEntity = QueryManager.getEntity(session, this.uuid, BuildingEntity.class);
            if (localityEntity != null) {
                this.reference = localityEntity.getIdentification();
            }
        }
    }

    public boolean equalData(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equalData(o)) return false;
        AccessAddressBuildingReferenceRecord that = (AccessAddressBuildingReferenceRecord) o;
        return Objects.equals(this.reference, that.reference);
    }

}
