package dk.magenta.datafordeler.cpr.records.road.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.*;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.data.CprRecordEntity;
import dk.magenta.datafordeler.cpr.records.BitemporalSet;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.*;
import org.hibernate.Session;
import org.hibernate.annotations.*;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * An Entity representing a road. Bitemporal data is structured as
 * described in {@link dk.magenta.datafordeler.core.database.Entity}
 */
@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + RoadEntity.TABLE_NAME, indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + "road_identification", columnList = RoadEntity.DB_FIELD_IDENTIFICATION, unique = true),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + RoadEntity.TABLE_NAME + RoadEntity.DB_FIELD_MUNIPALITY_CODE + RoadEntity.DB_FIELD_ROAD_CODE, columnList = RoadEntity.DB_FIELD_MUNIPALITY_CODE + "," + RoadEntity.DB_FIELD_ROAD_CODE),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + RoadEntity.TABLE_NAME + RoadEntity.DB_FIELD_ROAD_CODE, columnList = RoadEntity.DB_FIELD_ROAD_CODE),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + RoadEntity.TABLE_NAME + RoadEntity.DB_FIELD_MUNIPALITY_CODE, columnList = RoadEntity.DB_FIELD_MUNIPALITY_CODE),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + RoadEntity.TABLE_NAME + RoadEntity.DB_FIELD_DAFO_UPDATED, columnList = RoadEntity.DB_FIELD_DAFO_UPDATED)
})
@XmlAccessorType(XmlAccessType.FIELD)
public class RoadEntity extends CprRecordEntity {


    public static final String TABLE_NAME = "cpr_road_entity";

    public static final String schema = "road";

    public RoadEntity() {
    }

    public RoadEntity(Identification identification) {
        super(identification);
    }

    public RoadEntity(UUID uuid, String domain) {
        super(uuid, domain);
    }

    public static final String DB_FIELD_MUNIPALITY_CODE = "municipalityCode";
    public static final String IO_FIELD_MUNIPALITY_CODE = "kommunekode";
    @Column(name = DB_FIELD_MUNIPALITY_CODE)
    @JsonProperty(IO_FIELD_MUNIPALITY_CODE)
    private int municipalityCode;

    public int getMunicipalityCode() {
        return municipalityCode;
    }

    public void setMunicipalityCode(int municipalityCode) {
        this.municipalityCode = municipalityCode;
    }


    public static final String DB_FIELD_ROAD_CODE = "roadcode";
    public static final String IO_FIELD_ROAD_CODE = "vejkode";
    @Column(name = DB_FIELD_ROAD_CODE)
    @JsonProperty(IO_FIELD_ROAD_CODE)
    private int roadcode;

    public int getRoadcode() {
        return roadcode;
    }

    public void setRoadcode(int roadcode) {
        this.roadcode = roadcode;
    }


    public static final String DB_FIELD_NAME_CODE = "name";
    public static final String IO_FIELD_NAME_CODE = "navn";
    @Column(name = DB_FIELD_NAME_CODE)
    @OneToMany(mappedBy = RoadNameBitemporalRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_NAME_CODE)
    private Set<RoadNameBitemporalRecord> name = new HashSet<>();

    public BitemporalSet<RoadNameBitemporalRecord> getName() {
        return new BitemporalSet<>(this.name);
    }


    public static final String DB_FIELD_CITY_CODE = "city";
    public static final String IO_FIELD_CITY_CODE = "by";
    @Column(name = DB_FIELD_CITY_CODE)
    @OneToMany(mappedBy = RoadCityBitemporalRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_CITY_CODE)
    private Set<RoadCityBitemporalRecord> city = new HashSet<>();

    public BitemporalSet<RoadCityBitemporalRecord> getCity() {
        return new BitemporalSet<>(this.city);
    }


    public static final String DB_FIELD_MEMO_CODE = "memo";
    public static final String IO_FIELD_MEMO_CODE = "note";
    @Column(name = DB_FIELD_MEMO_CODE)
    @OneToMany(mappedBy = RoadMemoBitemporalRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_MEMO_CODE)
    private Set<RoadMemoBitemporalRecord> memo = new HashSet<>();

    public BitemporalSet<RoadMemoBitemporalRecord> getMemo() {
        return new BitemporalSet<>(this.memo);
    }


    public static final String DB_FIELD_POST_CODE = "postcode";
    public static final String IO_FIELD_POST_CODE = "postnr";
    @Column(name = DB_FIELD_POST_CODE)
    @OneToMany(mappedBy = RoadPostalcodeBitemporalRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_POST_CODE)
    private Set<RoadPostalcodeBitemporalRecord> postcode = new HashSet<>();

    public BitemporalSet<RoadPostalcodeBitemporalRecord> getPostcode() {
        return new BitemporalSet<>(this.postcode);
    }


    public static UUID generateUUID(int municipalityCode, int roadCode) {
        String uuidInput = "road:" + municipalityCode + ":" + roadCode;
        return UUID.nameUUIDFromBytes(uuidInput.getBytes());
    }


    public void addBitemporalRecord(CprBitemporalRoadRecord record, Session session) {
        boolean added = false;
        if (record instanceof RoadCityBitemporalRecord) {
            added = addItem(this, this.city, record, session);
        }
        if (record instanceof RoadPostalcodeBitemporalRecord) {
            added = addItem(this, this.postcode, record, session);
        }
        if (record instanceof RoadMemoBitemporalRecord) {
            added = addItem(this, this.memo, record, session);
        }
        if (record instanceof RoadNameBitemporalRecord) {
            added = addItem(this, this.name, record, session);
        }
        if (added) {
            record.setEntity(this);
            if (record.getDafoUpdated() != null && (this.getDafoUpdated() == null || record.getDafoUpdated().isAfter(this.getDafoUpdated()))) {
                this.setDafoUpdated(record.getDafoUpdated());
            }
        }
    }


    private static <E extends CprBitemporalRoadRecord> boolean addItem(RoadEntity entity, Set<E> set, CprBitemporalRoadRecord newItem, Session session) {

        if (newItem != null) {
            ArrayList<E> items = new ArrayList<>(set);

            for (E oldItem : items) {
                if (newItem.equals(oldItem)) {
                    return false;
                }
            }
            return set.add((E) newItem);
        }
        return false;
    }


    @Override
    public IdentifiedEntity getNewest(Collection<IdentifiedEntity> collection) {
        return null;
    }
}
