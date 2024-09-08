package dk.magenta.datafordeler.geo.data.accessaddress;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dk.magenta.datafordeler.core.PluginManager;
import dk.magenta.datafordeler.core.database.IdentifiedEntity;
import dk.magenta.datafordeler.core.database.Monotemporal;
import dk.magenta.datafordeler.core.database.Nontemporal;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.plugin.Plugin;
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
import java.util.*;

@Entity
@Table(name = GeoPlugin.DEBUG_TABLE_PREFIX + AccessAddressEntity.TABLE_NAME, indexes = {
        @Index(name = GeoPlugin.DEBUG_TABLE_PREFIX + AccessAddressEntity.TABLE_NAME + AccessAddressEntity.DB_FIELD_BNR, columnList = AccessAddressEntity.DB_FIELD_BNR),
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + AccessAddressEntity.TABLE_NAME + AccessAddressEntity.DB_FIELD_DAFO_UPDATED,
                columnList = AccessAddressEntity.DB_FIELD_DAFO_UPDATED
        ),
})
public class AccessAddressEntity extends SumiffiikEntity implements IdentifiedEntity {

    public static final String TABLE_NAME = "geo_access_address";

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
    public static final String schema = "AccessAddress";

    public AccessAddressEntity() {
    }

    public AccessAddressEntity(AccessAddressRawData record) {
        super(record);
        this.setBnr(record.properties.bnr);
    }

    public static UUID generateUUID(String bnr) {
        String uuidInput = "adgangsadresse:" + bnr;
        return UUID.nameUUIDFromBytes(uuidInput.getBytes());
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


    public static final String DB_FIELD_HOUSE_NUMBER = "houseNumber";
    public static final String IO_FIELD_HOUSE_NUMBER = "husNummer";
    @OneToMany(mappedBy = AccessAddressHouseNumberRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_HOUSE_NUMBER)
    private Set<AccessAddressHouseNumberRecord> houseNumber = new HashSet<>();

    public MonotemporalSet<AccessAddressHouseNumberRecord> getHouseNumber() {
        return new MonotemporalSet<>(this.houseNumber);
    }


    public static final String DB_FIELD_BLOCK_NAME = "blockName";
    public static final String IO_FIELD_BLOCK_NAME = "blokNavn";
    @OneToMany(mappedBy = AccessAddressBlockNameRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_BLOCK_NAME)
    Set<AccessAddressBlockNameRecord> blockName = new HashSet<>();

    public MonotemporalSet<AccessAddressBlockNameRecord> getBlockName() {
        return new MonotemporalSet<>(this.blockName);
    }


    public static final String DB_FIELD_ROAD = "road";
    public static final String IO_FIELD_ROAD = "vej";
    @OneToMany(mappedBy = AccessAddressRoadRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Filters({
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_ROAD)
    private Set<AccessAddressRoadRecord> road = new HashSet<>();

    public MonotemporalSet<AccessAddressRoadRecord> getRoad() {
        return new MonotemporalSet<>(this.road);
    }


    public static final String DB_FIELD_LOCALITY = "locality";
    public static final String IO_FIELD_LOCALITY = "lokalitet";
    @OneToMany(mappedBy = AccessAddressLocalityRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Filters({
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_LOCALITY)
    private Set<AccessAddressLocalityRecord> locality = new HashSet<>();

    public MonotemporalSet<AccessAddressLocalityRecord> getLocality() {
        return new MonotemporalSet<>(this.locality);
    }


    public static final String DB_FIELD_POSTCODE = "postcode";
    public static final String IO_FIELD_POSTCODE = "postnummer";
    @OneToMany(mappedBy = AccessAddressPostcodeRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Filters({
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_POSTCODE)
    private Set<AccessAddressPostcodeRecord> postcode = new HashSet<>();

    public MonotemporalSet<AccessAddressPostcodeRecord> getPostcode() {
        return new MonotemporalSet<>(this.postcode);
    }


    public static final String DB_FIELD_BUILDING = "building";
    public static final String IO_FIELD_BUILDING = "building";
    @OneToMany(mappedBy = AccessAddressBuildingReferenceRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Filters({
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_BUILDING)
    private Set<AccessAddressBuildingReferenceRecord> building = new HashSet<>();

    public MonotemporalSet<AccessAddressBuildingReferenceRecord> getBuilding() {
        return new MonotemporalSet<>(this.building);
    }


    public static final String DB_FIELD_STATUS = "status";
    public static final String IO_FIELD_STATUS = "status";
    @OneToMany(mappedBy = AccessAddressStatusRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Filters({
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_STATUS)
    private Set<AccessAddressStatusRecord> status = new HashSet<>();

    public MonotemporalSet<AccessAddressStatusRecord> getStatus() {
        return new MonotemporalSet<>(this.status);
    }


    public static final String DB_FIELD_SOURCE = "source";
    public static final String IO_FIELD_SOURCE = "source";
    @OneToMany(mappedBy = AccessAddressSourceRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Filters({
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_SOURCE)
    private Set<AccessAddressSourceRecord> source = new HashSet<>();

    public MonotemporalSet<AccessAddressSourceRecord> getSource() {
        return new MonotemporalSet<>(this.source);
    }


    public static final String DB_FIELD_SHAPE = "shape";
    public static final String IO_FIELD_SHAPE = "form";
    @OneToMany(mappedBy = AccessAddressShapeRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Filters({
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_SHAPE)
    private Set<AccessAddressShapeRecord> shape = new HashSet<>();

    public MonotemporalSet<AccessAddressShapeRecord> getShape() {
        return new MonotemporalSet<>(this.shape);
    }


    @Override
    public void update(RawData rawData, OffsetDateTime timestamp) {
        super.update(rawData, timestamp);
        if (rawData instanceof AccessAddressRawData) {
            AccessAddressRawData accessAddressRawData = (AccessAddressRawData) rawData;
            this.bnr = accessAddressRawData.properties.bnr;
        }
    }


    @Override
    public boolean merge(GeoEntity other) {
        return false;
    }

    public void addMonotemporalRecord(GeoMonotemporalRecord record) {
        boolean added = false;
        if (record instanceof AccessAddressRoadRecord) {
            added = addItem(this.road, record);
        }
        if (record instanceof AccessAddressHouseNumberRecord) {
            added = addItem(this.houseNumber, record);
        }
        if (record instanceof AccessAddressBlockNameRecord) {
            added = addItem(this.blockName, record);
        }
        if (record instanceof AccessAddressLocalityRecord) {
            added = addItem(this.locality, record);
        }
        if (record instanceof AccessAddressPostcodeRecord) {
            added = addItem(this.postcode, record);
        }
        if (record instanceof AccessAddressBuildingReferenceRecord) {
            added = addItem(this.building, record);
        }
        if (record instanceof AccessAddressStatusRecord) {
            added = addItem(this.status, record);
        }
        if (record instanceof AccessAddressSourceRecord) {
            added = addItem(this.source, record);
        }
        if (record instanceof AccessAddressShapeRecord) {
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
        records.add(this.road);
        records.add(this.building);
        records.add(this.blockName);
        records.add(this.houseNumber);
        records.add(this.postcode);
        records.add(this.source);
        records.add(this.status);
        records.add(this.shape);
        return records;
    }


    @JsonIgnore
    @Override
    public List<BaseQuery> getAssoc() {
        PluginManager pluginManager = PluginManager.getInstance();
        ArrayList<BaseQuery> queries = new ArrayList<>();
        HashMap<String, String> map = new HashMap<>();
        AccessAddressRoadRecord roadRecord = this.getRoad().current();
        map.put("municipalitycode", Integer.toString(roadRecord.getMunicipalityCode()));
        map.put("roadcode", Integer.toString(roadRecord.getRoadCode()));

        Plugin geoPlugin = pluginManager.getPluginByName("geo");
        try {
            if (geoPlugin != null) {
                queries.addAll(geoPlugin.getQueries(map));
            }
            Plugin cprPlugin = pluginManager.getPluginByName("cpr");
            if (cprPlugin != null) {
                queries.addAll(cprPlugin.getQueries(map));
            }
        } catch (InvalidClientInputException e) {
            // All inputs are stringified integers, and exception is only thrown when it's a string we can't parse as int
            throw new RuntimeException(e);
        }

        return queries;
    }

}
