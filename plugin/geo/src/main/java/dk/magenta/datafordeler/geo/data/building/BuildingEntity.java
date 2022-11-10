package dk.magenta.datafordeler.geo.data.building;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dk.magenta.datafordeler.core.database.IdentifiedEntity;
import dk.magenta.datafordeler.core.database.Monotemporal;
import dk.magenta.datafordeler.core.database.Nontemporal;
import dk.magenta.datafordeler.geo.GeoPlugin;
import dk.magenta.datafordeler.geo.data.GeoEntity;
import dk.magenta.datafordeler.geo.data.MonotemporalSet;
import dk.magenta.datafordeler.geo.data.RawData;
import dk.magenta.datafordeler.geo.data.SumiffiikEntity;
import dk.magenta.datafordeler.geo.data.common.GeoMonotemporalRecord;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = GeoPlugin.DEBUG_TABLE_PREFIX + BuildingEntity.TABLE_NAME, indexes = {
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + BuildingEntity.TABLE_NAME + BuildingEntity.DB_FIELD_SUMIFFIIK_ID,
                columnList = BuildingEntity.DB_FIELD_SUMIFFIIK_ID
        ),
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + BuildingEntity.TABLE_NAME + BuildingEntity.DB_FIELD_BNR,
                columnList = BuildingEntity.DB_FIELD_BNR
        ),
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + BuildingEntity.TABLE_NAME + BuildingEntity.DB_FIELD_DAFO_UPDATED,
                columnList = BuildingEntity.DB_FIELD_DAFO_UPDATED
        ),
})
public class BuildingEntity extends SumiffiikEntity implements IdentifiedEntity {

    public static final String TABLE_NAME = "geo_building";

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
    public static final String schema = "Building";

    public BuildingEntity() {
    }

    public BuildingEntity(BuildingRawData record) {
        super(record);
        this.setBnr(record.properties.bnr);
        this.setAnr(record.properties.anr);
    }

    public static UUID generateUUID(int localityCode) {
        String uuidInput = "bygning:" + localityCode;
        return UUID.nameUUIDFromBytes(uuidInput.getBytes());
    }


    public static final String DB_FIELD_ANR = "anr";
    public static final String IO_FIELD_ANR = "anr";
    @Column(name = DB_FIELD_ANR)
    @JsonProperty
    private String anr;

    public String getAnr() {
        return this.anr;
    }

    @JsonProperty(value = "anr")
    public void setAnr(String anr) {
        this.anr = anr;
    }


    public static final String DB_FIELD_BNR = "bnr";
    public static final String IO_FIELD_BNR = "bnr";
    @Column(name = DB_FIELD_BNR)
    @JsonProperty
    private String bnr;

    public String getBnr() {
        return this.bnr;
    }

    @JsonProperty(value = "bnr")
    public void setBnr(String bnr) {
        this.bnr = bnr;
    }


    public static final String DB_FIELD_LOCALITY = "locality";
    public static final String IO_FIELD_LOCALITY = "lokalitet";
    @OneToMany(mappedBy = BuildingLocalityRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_LOCALITY)
    private Set<BuildingLocalityRecord> locality = new HashSet<>();

    public MonotemporalSet<BuildingLocalityRecord> getLocality() {
        return new MonotemporalSet<>(this.locality);
    }


    public static final String DB_FIELD_SHAPE = "shape";
    public static final String IO_FIELD_SHAPE = "form";
    @OneToMany(mappedBy = BuildingShapeRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_SHAPE)
    private Set<BuildingShapeRecord> shape = new HashSet<>();

    public MonotemporalSet<BuildingShapeRecord> getShape() {
        return new MonotemporalSet<>(this.shape);
    }


    @Override
    public void update(RawData rawData, OffsetDateTime timestamp) {
        super.update(rawData, timestamp);
        if (rawData instanceof BuildingRawData) {
            BuildingRawData buildingRawData = (BuildingRawData) rawData;
            this.anr = buildingRawData.properties.anr;
            this.bnr = buildingRawData.properties.bnr;
        }
    }


    @Override
    public boolean merge(GeoEntity other) {
        return false;
    }

    public void addMonotemporalRecord(GeoMonotemporalRecord record) {
        boolean added = false;
        if (record instanceof BuildingLocalityRecord) {
            added = addItem(this.locality, record);
        }
        if (record instanceof BuildingShapeRecord) {
            added = addItem(this.shape, record);
        }
        if (added) {
            record.setEntity(this);
        }
    }

    @Override
    @JsonIgnore
    public Set<Set<? extends GeoMonotemporalRecord>> getAllRecords() {
        HashSet<Set<? extends GeoMonotemporalRecord>> records = new HashSet<>();
        records.add(this.locality);
        records.add(this.shape);
        return records;
    }

    @Override
    @JsonIgnore
    public IdentifiedEntity getNewest(Collection<IdentifiedEntity> collection) {
        return null;
    }

}
