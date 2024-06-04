package dk.magenta.datafordeler.geo.data.municipality;

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
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = GeoPlugin.DEBUG_TABLE_PREFIX + GeoMunicipalityEntity.TABLE_NAME, indexes = {
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + GeoMunicipalityEntity.TABLE_NAME + GeoMunicipalityEntity.DB_FIELD_SUMIFFIIK_ID,
                columnList = GeoMunicipalityEntity.DB_FIELD_SUMIFFIIK_ID
        ),
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + GeoMunicipalityEntity.TABLE_NAME + GeoMunicipalityEntity.DB_FIELD_CODE,
                columnList = GeoMunicipalityEntity.DB_FIELD_CODE
        ),
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + GeoMunicipalityEntity.TABLE_NAME + GeoMunicipalityEntity.DB_FIELD_DAFO_UPDATED,
                columnList = GeoMunicipalityEntity.DB_FIELD_DAFO_UPDATED
        ),
})
public class GeoMunicipalityEntity extends SumiffiikEntity implements IdentifiedEntity {

    public static final String TABLE_NAME = "geo_municipality";

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
    public static final String schema = "Municipality";

    public GeoMunicipalityEntity() {
    }

    public GeoMunicipalityEntity(MunicipalityRawData record) {
        super(record);
        this.setCode(record.properties.code);
    }

    public static UUID generateUUID(int municipalityCode) {
        String uuidInput = "kommune:" + municipalityCode;
        return UUID.nameUUIDFromBytes(uuidInput.getBytes());
    }


    public static final String DB_FIELD_CODE = "code";
    public static final String IO_FIELD_CODE = "kode";
    @Column(name = DB_FIELD_CODE)
    @JsonProperty
    private int code;

    public int getCode() {
        return this.code;
    }

    @JsonProperty(value = "Kommunekode")
    public void setCode(int code) {
        this.code = code;
    }


    public static final String DB_FIELD_NAME = "name";
    public static final String IO_FIELD_NAME = "navn";
    @OneToMany(mappedBy = MunicipalityNameRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_NAME)
    private Set<MunicipalityNameRecord> name = new HashSet<>();

    public MonotemporalSet<MunicipalityNameRecord> getName() {
        return new MonotemporalSet<>(this.name);
    }


    @Override
    public void update(RawData rawData, OffsetDateTime timestamp) {
        super.update(rawData, timestamp);
        if (rawData instanceof MunicipalityRawData) {
            MunicipalityRawData municipalityRawData = (MunicipalityRawData) rawData;
            this.code = municipalityRawData.properties.code;
        }
    }


    public static final String DB_FIELD_SHAPE = "shape";
    public static final String IO_FIELD_SHAPE = "form";
    @OneToMany(mappedBy = MunicipalityShapeRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_SHAPE)
    private Set<MunicipalityShapeRecord> shape = new HashSet<>();

    public MonotemporalSet<MunicipalityShapeRecord> getShape() {
        return new MonotemporalSet<>(this.shape);
    }


    @Override
    public boolean merge(GeoEntity other) {
        return false;
    }

    public void addMonotemporalRecord(GeoMonotemporalRecord record) {
        boolean added = false;
        if (record instanceof MunicipalityNameRecord) {
            added = addItem(this.name, record);
        }
        if (record instanceof MunicipalityShapeRecord) {
            added = addItem(this.shape, record);
        }
        if (added) {
            record.setEntity(this);
        }
    }

    @Override
    @JsonIgnore
    public IdentifiedEntity getNewest(Collection<IdentifiedEntity> collection) {
        return null;
    }

    @Override
    @JsonIgnore
    public Set<Set<? extends GeoMonotemporalRecord>> getAllRecords() {
        HashSet<Set<? extends GeoMonotemporalRecord>> records = new HashSet<>();
        records.add(this.name);
        records.add(this.shape);
        return records;
    }
}
