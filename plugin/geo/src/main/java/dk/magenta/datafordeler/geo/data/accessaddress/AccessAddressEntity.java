package dk.magenta.datafordeler.geo.data.accessaddress;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dk.magenta.datafordeler.core.database.IdentifiedEntity;
import dk.magenta.datafordeler.core.database.Monotemporal;
import dk.magenta.datafordeler.core.database.Nontemporal;
import dk.magenta.datafordeler.geo.GeoPlugin;
import dk.magenta.datafordeler.geo.data.GeoEntity;
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
@Table(name = GeoPlugin.DEBUG_TABLE_PREFIX + AccessAddressEntity.TABLE_NAME, indexes = {
        @Index(name = GeoPlugin.DEBUG_TABLE_PREFIX + AccessAddressEntity.TABLE_NAME + AccessAddressEntity.DB_FIELD_BNR, columnList = AccessAddressEntity.DB_FIELD_BNR),
})
public class AccessAddressEntity extends SumiffiikEntity implements IdentifiedEntity {

    public static final String TABLE_NAME = "geo_access_address";

    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="type")
    public static final String schema = "AccessAddress";

    public AccessAddressEntity() {
    }

    public AccessAddressEntity(AccessAddressRawData record) {
        super(record);
        this.setBnr(record.properties.bnr);
    }

    public static UUID generateUUID(String bnr) {
        String uuidInput = "adgangsadresse:"+bnr;
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
    Set<AccessAddressHouseNumberRecord> houseNumber = new HashSet<>();

    public Set<AccessAddressHouseNumberRecord> getHouseNumber() {
        return this.houseNumber;
    }


    public static final String DB_FIELD_BLOCK_NAME= "blockName";
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

    public Set<AccessAddressBlockNameRecord> getBlockName() {
        return this.blockName;
    }



    public static final String DB_FIELD_ROAD = "road";
    public static final String IO_FIELD_ROAD = "vej";
    @OneToMany(mappedBy = AccessAddressRoadRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_ROAD)
    Set<AccessAddressRoadRecord> road = new HashSet<>();

    public Set<AccessAddressRoadRecord> getRoad() {
        return this.road;
    }



    public static final String DB_FIELD_LOCALITY = "locality";
    public static final String IO_FIELD_LOCALITY = "lokalitet";
    @OneToMany(mappedBy = AccessAddressLocalityRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_LOCALITY)
    Set<AccessAddressLocalityRecord> locality = new HashSet<>();

    public Set<AccessAddressLocalityRecord> getLocality() {
        return this.locality;
    }



    public static final String DB_FIELD_POSTCODE = "postcode";
    public static final String IO_FIELD_POSTCODE = "postnummer";
    @OneToMany(mappedBy = AccessAddressPostcodeRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_POSTCODE)
    Set<AccessAddressPostcodeRecord> postcode = new HashSet<>();

    public Set<AccessAddressPostcodeRecord> getPostcode() {
        return this.postcode;
    }



    public static final String DB_FIELD_BUILDING = "building";
    public static final String IO_FIELD_BUILDING = "building";
    @OneToMany(mappedBy = AccessAddressBuildingReferenceRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_BUILDING)
    Set<AccessAddressBuildingReferenceRecord> building = new HashSet<>();

    public Set<AccessAddressBuildingReferenceRecord> getBuilding() {
        return this.building;
    }



    public static final String DB_FIELD_STATUS = "status";
    public static final String IO_FIELD_STATUS = "status";
    @OneToMany(mappedBy = AccessAddressStatusRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_STATUS)
    Set<AccessAddressStatusRecord> status = new HashSet<>();

    public Set<AccessAddressStatusRecord> getStatus() {
        return this.status;
    }



    public static final String DB_FIELD_SOURCE = "source";
    public static final String IO_FIELD_SOURCE = "source";
    @OneToMany(mappedBy = AccessAddressSourceRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_SOURCE)
    Set<AccessAddressSourceRecord> source = new HashSet<>();

    public Set<AccessAddressSourceRecord> getSource() {
        return this.source;
    }

    

    public static final String DB_FIELD_SHAPE = "shape";
    public static final String IO_FIELD_SHAPE = "form";
    @OneToMany(mappedBy = AccessAddressShapeRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_SHAPE)
    Set<AccessAddressShapeRecord> shape = new HashSet<>();

    public Set<AccessAddressShapeRecord> getShape() {
        return this.shape;
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

}
