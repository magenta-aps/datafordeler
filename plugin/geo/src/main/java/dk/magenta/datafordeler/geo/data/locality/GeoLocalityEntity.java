package dk.magenta.datafordeler.geo.data.locality;

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

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = GeoPlugin.DEBUG_TABLE_PREFIX + GeoLocalityEntity.TABLE_NAME, indexes = {
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + GeoLocalityEntity.TABLE_NAME + GeoLocalityEntity.DB_FIELD_SUMIFFIIK_ID,
                columnList = GeoLocalityEntity.DB_FIELD_SUMIFFIIK_ID
        ),
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + GeoLocalityEntity.TABLE_NAME + GeoLocalityEntity.DB_FIELD_CODE,
                columnList = GeoLocalityEntity.DB_FIELD_CODE
        ),
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + GeoLocalityEntity.TABLE_NAME + GeoLocalityEntity.DB_FIELD_DAFO_UPDATED,
                columnList = GeoLocalityEntity.DB_FIELD_DAFO_UPDATED
        ),
})
public class GeoLocalityEntity extends SumiffiikEntity implements IdentifiedEntity {

    public static final String TABLE_NAME = "geo_locality";

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
    public static final String schema = "Locality";

    public GeoLocalityEntity() {
    }

    public GeoLocalityEntity(LocalityRawData record) {
        super(record);
        this.setCode(record.properties.code);
    }

    public static UUID generateUUID(int localityCode) {
        String uuidInput = "lokalitet:" + localityCode;
        return UUID.nameUUIDFromBytes(uuidInput.getBytes());
    }


    public static final String DB_FIELD_CODE = "code";
    public static final String IO_FIELD_CODE = "kode";
    @Column(name = DB_FIELD_CODE)
    @JsonProperty
    private String code;

    public String getCode() {
        return this.code;
    }

    @JsonProperty(value = "Lokalitetskode")
    public void setCode(String code) {
        this.code = code;
    }


    public static final String DB_FIELD_NAME = "name";
    public static final String IO_FIELD_NAME = "navn";
    @OneToMany(mappedBy = LocalityNameRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_NAME)
    private Set<LocalityNameRecord> name = new HashSet<>();

    public MonotemporalSet<LocalityNameRecord> getName() {
        return new MonotemporalSet<>(this.name);
    }


    public static final String DB_FIELD_ABBREVIATION = "abbreviation";
    public static final String IO_FIELD_ABBREVIATION = "forkortelse";
    @OneToMany(mappedBy = LocalityAbbreviationRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_ABBREVIATION)
    private Set<LocalityAbbreviationRecord> abbreviation = new HashSet<>();

    public MonotemporalSet<LocalityAbbreviationRecord> getAbbreviation() {
        return new MonotemporalSet<>(this.abbreviation);
    }


    public static final String DB_FIELD_TYPE = "type";
    public static final String IO_FIELD_TYPE = "type";
    @OneToMany(mappedBy = LocalityTypeRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_TYPE)
    private Set<LocalityTypeRecord> type = new HashSet<>();

    public MonotemporalSet<LocalityTypeRecord> getType() {
        return new MonotemporalSet<>(this.type);
    }


    public static final String DB_FIELD_STATUS = "status";
    public static final String IO_FIELD_STATUS = "status";
    @OneToMany(mappedBy = LocalityStatusRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_STATUS)
    private Set<LocalityStatusRecord> status = new HashSet<>();

    public MonotemporalSet<LocalityStatusRecord> getStatus() {
        return new MonotemporalSet<>(this.status);
    }


    public static final String DB_FIELD_BETEGN = "betegnelse";
    public static final String IO_FIELD_BETEGN = "betegnelse";
    @OneToMany(mappedBy = LocalityBetegnRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_BETEGN)
    private Set<LocalityBetegnRecord> betegnelse = new HashSet<>();

    public MonotemporalSet<LocalityBetegnRecord> getBetegnelse() {
        return new MonotemporalSet<>(this.betegnelse);
    }


    public static final String DB_FIELD_MUNICIPALITY = "municipality";
    public static final String IO_FIELD_MUNICIPALITY = "kommunekode";
    @OneToMany(mappedBy = LocalityMunicipalityRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_MUNICIPALITY)
    private Set<LocalityMunicipalityRecord> municipality = new HashSet<>();

    public MonotemporalSet<LocalityMunicipalityRecord> getMunicipality() {
        return new MonotemporalSet<>(this.municipality);
    }


    public static final String DB_FIELD_LOCALITY_ROADCODE = "localityRoadcode";
    public static final String IO_FIELD_LOCALITY_ROADCODE = "lokalitetVejkode";
    @OneToMany(mappedBy = LocalityMunicipalityRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_LOCALITY_ROADCODE)
    private Set<LocalityRoadcodeRecord> localityRoadcode = new HashSet<>();

    public MonotemporalSet<LocalityRoadcodeRecord> getLocalityRoadcode() {
        return new MonotemporalSet<>(this.localityRoadcode);
    }


    public static final String DB_FIELD_SHAPE = "shape";
    public static final String IO_FIELD_SHAPE = "form";
    @OneToMany(mappedBy = LocalityShapeRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_SHAPE)
    private Set<LocalityShapeRecord> shape = new HashSet<>();

    public MonotemporalSet<LocalityShapeRecord> getShape() {
        return new MonotemporalSet<>(this.shape);
    }


    @Override
    public void update(RawData rawData, OffsetDateTime timestamp) {
        super.update(rawData, timestamp);
        if (rawData instanceof LocalityRawData) {
            LocalityRawData localityRawData = (LocalityRawData) rawData;
            this.code = localityRawData.properties.code;
        }
    }


    @Override
    public boolean merge(GeoEntity other) {
        return false;
    }

    public void addMonotemporalRecord(GeoMonotemporalRecord record) {
        boolean added = false;
        if (record instanceof LocalityNameRecord) {
            added = addItem(this.name, record);
        }
        if (record instanceof LocalityAbbreviationRecord) {
            added = addItem(this.abbreviation, record);
        }
        if (record instanceof LocalityTypeRecord) {
            added = addItem(this.type, record);
        }
        if (record instanceof LocalityStatusRecord) {
            added = addItem(this.status, record);
        }
        if (record instanceof LocalityBetegnRecord) {
            added = addItem(this.betegnelse, record);
        }
        if (record instanceof LocalityMunicipalityRecord) {
            added = addItem(this.municipality, record);
        }
        if (record instanceof LocalityRoadcodeRecord) {
            added = addItem(this.localityRoadcode, record);
        }
        if (record instanceof LocalityShapeRecord) {
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
        records.add(this.abbreviation);
        records.add(this.shape);
        records.add(this.municipality);
        records.add(this.localityRoadcode);
        return records;
    }
}
