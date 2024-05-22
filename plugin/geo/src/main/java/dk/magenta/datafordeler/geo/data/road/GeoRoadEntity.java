package dk.magenta.datafordeler.geo.data.road;

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

@Entity
@Table(name = GeoPlugin.DEBUG_TABLE_PREFIX + GeoRoadEntity.TABLE_NAME, indexes = {
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + GeoRoadEntity.TABLE_NAME + GeoRoadEntity.DB_FIELD_SUMIFFIIK_ID,
                columnList = GeoRoadEntity.DB_FIELD_SUMIFFIIK_ID
        ),
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + GeoRoadEntity.TABLE_NAME + GeoRoadEntity.DB_FIELD_CODE,
                columnList = GeoRoadEntity.DB_FIELD_CODE
        ),
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + GeoRoadEntity.TABLE_NAME + GeoRoadEntity.DB_FIELD_DAFO_UPDATED,
                columnList = GeoRoadEntity.DB_FIELD_DAFO_UPDATED
        ),
})
public class GeoRoadEntity extends SumiffiikEntity implements IdentifiedEntity {

    public static final String TABLE_NAME = "geo_road";

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
    public static final String schema = "Road";

    public GeoRoadEntity() {
    }

    public GeoRoadEntity(RoadRawData record) {
        super(record);
        this.setCode(record.properties.code);
    }


    public static final String DB_FIELD_CODE = "code";
    public static final String IO_FIELD_CODE = "kode";
    @Column(name = DB_FIELD_CODE)
    @JsonProperty
    private int code;

    public int getCode() {
        return this.code;
    }

    @JsonProperty(value = "Vejkode")
    public void setCode(int code) {
        this.code = code;
    }


    public static final String DB_FIELD_NAME = "name";
    public static final String IO_FIELD_NAME = "navn";
    @OneToMany(mappedBy = RoadNameRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Filters({
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_NAME)
    private Set<RoadNameRecord> name = new HashSet<>();

    public MonotemporalSet<RoadNameRecord> getName() {
        return new MonotemporalSet<>(this.name);
    }


    public static final String DB_FIELD_LOCALITY = "locality";
    public static final String IO_FIELD_LOCALITY = "lokalitet";
    @OneToMany(mappedBy = RoadLocalityRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Filters({
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_LOCALITY)
    private Set<RoadLocalityRecord> locality = new HashSet<>();

    public MonotemporalSet<RoadLocalityRecord> getLocality() {
        return new MonotemporalSet<>(this.locality);
    }


    public static final String DB_FIELD_MUNICIPALITY = "municipality";
    public static final String IO_FIELD_MUNICIPALITY = "kommune";
    @OneToMany(mappedBy = RoadMunicipalityRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Filters({
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_MUNICIPALITY)
    private Set<RoadMunicipalityRecord> municipality = new HashSet<>();

    public MonotemporalSet<RoadMunicipalityRecord> getMunicipality() {
        return new MonotemporalSet<>(this.municipality);
    }


    public static final String DB_FIELD_SHAPE = "shape";
    public static final String IO_FIELD_SHAPE = "form";
    @OneToMany(mappedBy = RoadShapeRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Filters({
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_SHAPE)
    private Set<RoadShapeRecord> shape = new HashSet<>();

    public MonotemporalSet<RoadShapeRecord> getShape() {
        return new MonotemporalSet<>(this.shape);
    }


    @Override
    public void update(RawData rawData, OffsetDateTime timestamp) {
        super.update(rawData, timestamp);
        if (rawData instanceof RoadRawData) {
            RoadRawData roadRawData = (RoadRawData) rawData;
            this.code = roadRawData.properties.code;
        }
    }

    @Override
    public boolean merge(GeoEntity other) {
        return false;
    }

    public void addMonotemporalRecord(GeoMonotemporalRecord record) {
        boolean added = false;
        if (record instanceof RoadNameRecord) {
            added = addItem(this.name, record);
        }
        if (record instanceof RoadLocalityRecord) {
            added = addItem(this.locality, record);
        }
        if (record instanceof RoadMunicipalityRecord) {
            added = addItem(this.municipality, record);
        }
        if (record instanceof RoadShapeRecord) {
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
        records.add(this.locality);
        records.add(this.municipality);
        records.add(this.name);
        records.add(this.shape);
        return records;
    }
}
