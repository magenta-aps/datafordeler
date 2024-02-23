package dk.magenta.datafordeler.geo.data.unitaddress;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dk.magenta.datafordeler.core.PluginManager;
import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.core.database.IdentifiedEntity;
import dk.magenta.datafordeler.core.database.Monotemporal;
import dk.magenta.datafordeler.core.database.Nontemporal;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.geo.GeoPlugin;
import dk.magenta.datafordeler.geo.data.GeoEntity;
import dk.magenta.datafordeler.geo.data.MonotemporalSet;
import dk.magenta.datafordeler.geo.data.SumiffiikEntity;
import dk.magenta.datafordeler.geo.data.common.GeoMonotemporalRecord;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = GeoPlugin.DEBUG_TABLE_PREFIX + UnitAddressEntity.TABLE_NAME, indexes = {
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + UnitAddressEntity.TABLE_NAME + UnitAddressEntity.DB_FIELD_SUMIFFIIK_ID,
                columnList = UnitAddressEntity.DB_FIELD_SUMIFFIIK_ID
        ),
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + UnitAddressEntity.TABLE_NAME + UnitAddressEntity.DB_FIELD_DAFO_UPDATED,
                columnList = UnitAddressEntity.DB_FIELD_DAFO_UPDATED
        ),
})
public class UnitAddressEntity extends SumiffiikEntity implements IdentifiedEntity {

    public static final String TABLE_NAME = "geo_unit_address";

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
    public static final String schema = "UnitAddress";

    public UnitAddressEntity() {
    }

    public UnitAddressEntity(UnitAddressRawData record) {
        super(record);
    }


    public static final String DB_FIELD_ACCESS_ADDRESS = "accessAddress";
    public static final String IO_FIELD_ACCESS_ADDRESS = "adgangsAdresse";
    @ManyToOne(targetEntity = Identification.class)
    private Identification accessAddress;

    public Identification getAccessAddress() {
        return this.accessAddress;
    }

    public void setAccessAddress(Identification accessAddress) {
        this.accessAddress = accessAddress;
    }


    public static final String DB_FIELD_FLOOR = "floor";
    public static final String IO_FIELD_FLOOR = "etage";
    @OneToMany(mappedBy = UnitAddressFloorRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Filters({
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_FLOOR)
    private Set<UnitAddressFloorRecord> floor = new HashSet<>();

    public MonotemporalSet<UnitAddressFloorRecord> getFloor() {
        return new MonotemporalSet<>(this.floor);
    }


    public static final String DB_FIELD_DOOR = "door";
    public static final String IO_FIELD_DOOR = "sidedør";
    @OneToMany(mappedBy = UnitAddressDoorRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Filters({
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_DOOR)
    private Set<UnitAddressDoorRecord> door = new HashSet<>();

    public MonotemporalSet<UnitAddressDoorRecord> getDoor() {
        return new MonotemporalSet(this.door);
    }


    public static final String DB_FIELD_NUMBER = "number";
    public static final String IO_FIELD_NUMBER = "nummer";
    @OneToMany(mappedBy = UnitAddressNumberRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Filters({
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_NUMBER)
    private Set<UnitAddressNumberRecord> number = new HashSet<>();

    public MonotemporalSet<UnitAddressNumberRecord> getNumber() {
        return new MonotemporalSet<>(this.number);
    }


    public static final String DB_FIELD_USAGE = "usage";
    public static final String IO_FIELD_USAGE = "anvendelse";
    @OneToMany(mappedBy = UnitAddressUsageRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Filters({
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_USAGE)
    private Set<UnitAddressUsageRecord> usage = new HashSet<>();

    public MonotemporalSet<UnitAddressUsageRecord> getUsage() {
        return new MonotemporalSet<>(this.usage);
    }


    public static final String DB_FIELD_STATUS = "status";
    public static final String IO_FIELD_STATUS = "status";
    @OneToMany(mappedBy = UnitAddressStatusRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Filters({
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_STATUS)
    private Set<UnitAddressStatusRecord> status = new HashSet<>();

    public MonotemporalSet<UnitAddressStatusRecord> getStatus() {
        return new MonotemporalSet<>(this.status);
    }


    public static final String DB_FIELD_SOURCE = "source";
    public static final String IO_FIELD_SOURCE = "source";
    @OneToMany(mappedBy = UnitAddressSourceRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Filters({
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_SOURCE)
    private Set<UnitAddressSourceRecord> source = new HashSet<>();

    public MonotemporalSet<UnitAddressSourceRecord> getSource() {
        return new MonotemporalSet<>(this.source);
    }


    @Override
    public boolean merge(GeoEntity other) {
        return false;
    }

    public void addMonotemporalRecord(GeoMonotemporalRecord record) {
        boolean added = false;
        if (record instanceof UnitAddressFloorRecord) {
            added = addItem(this.floor, record);
        }
        if (record instanceof UnitAddressDoorRecord) {
            added = addItem(this.door, record);
        }
        if (record instanceof UnitAddressNumberRecord) {
            added = addItem(this.number, record);
        }
        if (record instanceof UnitAddressUsageRecord) {
            added = addItem(this.usage, record);
        }
        if (record instanceof UnitAddressStatusRecord) {
            added = addItem(this.status, record);
        }
        if (record instanceof UnitAddressSourceRecord) {
            added = addItem(this.source, record);
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
        records.add(this.door);
        records.add(this.floor);
        records.add(this.number);
        records.add(this.usage);
        records.add(this.source);
        records.add(this.status);
        return records;
    }


    @JsonIgnore
    @Override
    public List<BaseQuery> getAssoc() {
        PluginManager pluginManager = PluginManager.getInstance();
        ArrayList<BaseQuery> queries = new ArrayList<>();
        HashMap<String, String> map = new HashMap<>();
        map.put("accessaddress", this.accessAddress.getUuid().toString());

        Plugin geoPlugin = pluginManager.getPluginByName("geo");
        if (geoPlugin != null) {
            try {
                queries.addAll(geoPlugin.getQueries(map));
            } catch (InvalidClientInputException e) {
                throw new RuntimeException(e);
            }
        }

        return queries;
    }
}
