package dk.magenta.datafordeler.cpr.data.road.data;

import dk.magenta.datafordeler.core.database.DataItem;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.LookupDefinition;
import dk.magenta.datafordeler.core.util.Equality;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.data.CprData;
import dk.magenta.datafordeler.cpr.data.DetailData;
import dk.magenta.datafordeler.cpr.data.road.RoadEffect;
import dk.magenta.datafordeler.cpr.data.unversioned.PostCode;
import org.hibernate.Hibernate;
import org.hibernate.Session;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.*;


/**
 * Base class for Road data, linking to Effects and delegating storage to referred classes
 */
@Entity
@Table(name= "cpr_road_data", indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_road_lastUpdated", columnList = DataItem.DB_FIELD_LAST_UPDATED),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_road_core", columnList = RoadBaseData.DB_FIELD_CORE + DatabaseEntry.REF)
})
public class RoadBaseData extends CprData<RoadEffect, RoadBaseData> {

    public static final String DB_FIELD_CORE = "coreData";
    public static final String IO_FIELD_CORE = "kernedata";

    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_CORE + DatabaseEntry.REF)
    private RoadCoreData coreData;

    public RoadCoreData getCoreData() {
        return this.coreData;
    }

    public static final String DB_FIELD_MEMO = "memoData";
    public static final String IO_FIELD_MEMO = "noter";

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "roadBaseData")
    @OrderBy("memoNumber")
    private List<RoadMemoData> memoData = new ArrayList<>();

    public List<RoadMemoData> getMemoData() {
        return this.memoData;
    }

    public static final String DB_FIELD_POSTCODE = "postcodeData";
    public static final String IO_FIELD_POSTCODE = "post";

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "roadBaseData")
    private Set<RoadPostcodeData> postcodeData = new HashSet<>();

    public Set<RoadPostcodeData> getPostcodeData() {
        return this.postcodeData;
    }

    public static final String DB_FIELD_CITY = "cityData";
    public static final String IO_FIELD_CITY = "by";

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "roadBaseData")
    private Set<RoadCityData> cityData = new HashSet<>();

    public Set<RoadCityData> getCityData() {
        return this.cityData;
    }


    public void setCore(int toMunicipalityCode, int toRoadCode, int fromMunicipalityCode, int fromRoadCode, String addressingName, String name, OffsetDateTime updateTime) {
        if (this.coreData == null) {
            this.coreData = new RoadCoreData();
        }
        this.coreData.setToMunicipality(toMunicipalityCode);
        this.coreData.setToRoad(toRoadCode);
        this.coreData.setFromMunicipality(fromMunicipalityCode);
        this.coreData.setFromRoad(fromRoadCode);
        this.coreData.setAddressingName(addressingName);
        this.coreData.setName(name);
        this.coreData.setDafoUpdated(updateTime);
    }

    public void addMemo(int memoNumber, String memoText, OffsetDateTime updateTime) {
        RoadMemoData memoData = null;
        for (RoadMemoData existing : this.memoData) {
            if (existing.getMemoNumber() == memoNumber) {
                memoData = existing;
            }
        }
        if (memoData == null) {
            memoData = new RoadMemoData();
            memoData.setMemoNumber(memoNumber);
            memoData.setRoadBaseData(this);
            this.memoData.add(memoData);
        }
        memoData.setMemoText(memoText);
        memoData.setDafoUpdated(updateTime);
    }

    public void addPostcode(String houseNumberFrom, String houseNumberTo, boolean even, PostCode postCode, OffsetDateTime updateTime) {
        RoadPostcodeData postcodeData = null;
        for (RoadPostcodeData existing : this.postcodeData) {
            if (Equality.equal(existing.getHouseNumberFrom(), houseNumberFrom) && Equality.equal(existing.getHouseNumberTo(), houseNumberTo) && existing.isEven() == even) {
                postcodeData = existing;
            }
        }
        if (postcodeData == null) {
            postcodeData = new RoadPostcodeData();
            postcodeData.setHouseNumberFrom(houseNumberFrom);
            postcodeData.setHouseNumberTo(houseNumberTo);
            postcodeData.setEven(even);
            postcodeData.setRoadBaseData(this);
            this.postcodeData.add(postcodeData);
        }
        postcodeData.setPostCode(postCode);
        postcodeData.setDafoUpdated(updateTime);
    }

    public void addCity(String houseNumberFrom, String houseNumberTo, boolean even, String cityName, OffsetDateTime updateTime) {
        RoadCityData cityData = null;
        for (RoadCityData existing : this.cityData) {
            if (Equality.equal(existing.getHouseNumberFrom(), houseNumberFrom) && Equality.equal(existing.getHouseNumberTo(), houseNumberTo) && existing.isEven() == even) {
                cityData = existing;
            }
        }
        if (cityData == null) {
            cityData = new RoadCityData();
            cityData.setHouseNumberFrom(houseNumberFrom);
            cityData.setHouseNumberTo(houseNumberTo);
            cityData.setEven(even);
            cityData.setRoadBaseData(this);
            this.cityData.add(cityData);
        }
        cityData.setCityName(cityName);
        cityData.setDafoUpdated(updateTime);
    }


        /**
         * Return a map of attributes, including those from the superclass
         *
         * @return
         */
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        if (this.coreData != null) {
            map.putAll(this.coreData.asMap());
        }
        if (this.memoData != null && !this.memoData.isEmpty()) {
            map.put("memo", this.memoData);
        }
        if (this.postcodeData != null && !this.postcodeData.isEmpty()) {
            map.put("postcode", this.postcodeData);
        }
        if (this.cityData != null && !this.cityData.isEmpty()) {
            map.put("city", this.cityData);
        }
        return map;
    }

    public LookupDefinition getLookupDefinition() {
        LookupDefinition lookupDefinition = new LookupDefinition(RoadBaseData.class);
        lookupDefinition.setMatchNulls(true);

        if (this.coreData != null) {
            lookupDefinition.putAll(DB_FIELD_CORE, this.coreData.databaseFields());
        }
        if (this.memoData != null) {
            lookupDefinition.putAll(DB_FIELD_MEMO, DetailData.listDatabaseFields(this.memoData));
        }
        if (this.postcodeData != null) {
            lookupDefinition.putAll(DB_FIELD_POSTCODE, DetailData.listDatabaseFields(this.postcodeData));
        }
        if (this.cityData != null) {
            lookupDefinition.putAll(DB_FIELD_CITY, DetailData.listDatabaseFields(this.cityData));
        }
        return lookupDefinition;
    }

    @Override
    public void forceLoad(Session session) {
        Hibernate.initialize(memoData);
        Hibernate.initialize(postcodeData);
        Hibernate.initialize(postcodeData);
        Hibernate.initialize(cityData);
    }


    @Override
    public RoadBaseData clone() {
        RoadBaseData clone = new RoadBaseData();
        if (this.coreData != null) {
            clone.coreData = this.coreData.clone();
        }
        if (this.cityData != null) {
            clone.cityData = new HashSet<>();
            for (RoadCityData cityData : this.cityData) {
                RoadCityData cityDataClone = cityData.clone();
                cityDataClone.setRoadBaseData(clone);
                clone.cityData.add(cityDataClone);
            }
        }
        if (this.memoData != null) {
            clone.memoData = new ArrayList<>();
            for (RoadMemoData memoData : this.memoData) {
                RoadMemoData memoDataClone = memoData.clone();
                memoDataClone.setRoadBaseData(clone);
                clone.memoData.add(memoDataClone);
            }
        }
        if (this.postcodeData != null) {
            clone.postcodeData = new HashSet<>();
            for (RoadPostcodeData postcodeData : this.postcodeData) {
                RoadPostcodeData postcodeDataClone = postcodeData.clone();
                postcodeDataClone.setRoadBaseData(clone);
                clone.postcodeData.add(postcodeDataClone);
            }
        }
        return clone;
    }
}