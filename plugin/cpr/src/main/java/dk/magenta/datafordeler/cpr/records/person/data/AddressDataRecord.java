package dk.magenta.datafordeler.cpr.records.person.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.PluginManager;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.person.CprBitemporalPersonRecord;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import java.util.*;

@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + AddressDataRecord.TABLE_NAME, indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + AddressDataRecord.TABLE_NAME + CprBitemporalPersonRecord.DB_FIELD_ENTITY, columnList = CprBitemporalPersonRecord.DB_FIELD_ENTITY + DatabaseEntry.REF),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + AddressDataRecord.TABLE_NAME + AddressDataRecord.DB_FIELD_MUNICIPALITY_CODE, columnList = AddressDataRecord.DB_FIELD_MUNICIPALITY_CODE),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + AddressDataRecord.TABLE_NAME + AddressDataRecord.DB_FIELD_BUILDING_NUMBER, columnList = AddressDataRecord.DB_FIELD_BUILDING_NUMBER),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + AddressDataRecord.TABLE_NAME + AddressDataRecord.DB_FIELD_ROAD_CODE, columnList = AddressDataRecord.DB_FIELD_ROAD_CODE),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + AddressDataRecord.TABLE_NAME + AddressDataRecord.DB_FIELD_FLOOR, columnList = AddressDataRecord.DB_FIELD_FLOOR),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + AddressDataRecord.TABLE_NAME + AddressDataRecord.DB_FIELD_DOOR, columnList = AddressDataRecord.DB_FIELD_DOOR),

        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + AddressDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + AddressDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_TO, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_TO),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + AddressDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_FROM, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + AddressDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_TO, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_TO),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + AddressDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_CORRECTION_OF, columnList = CprBitemporalRecord.DB_FIELD_CORRECTION_OF + DatabaseEntry.REF),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + AddressDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REPLACED_BY, columnList = CprBitemporalRecord.DB_FIELD_REPLACED_BY + DatabaseEntry.REF)
})
public class AddressDataRecord extends CprBitemporalPersonRecord<AddressDataRecord> {

    public static final String TABLE_NAME = "cpr_person_address_record";

    public AddressDataRecord() {
    }

    public AddressDataRecord(int municipalityCode, int roadCode, String buildingNumber, String houseNumber, String floor, String door, String roadAddressLine1, String roadAddressLine2, String roadAddressLine3, String roadAddressLine4, String roadAddressLine5, int addressTextType, int startAuthority) {
        this.municipalityCode = municipalityCode;
        this.roadCode = roadCode;
        this.buildingNumber = trim(buildingNumber);
        this.houseNumber = trim(houseNumber);
        this.floor = floor;
        this.door = door;
        this.roadAddressLine1 = trim(roadAddressLine1);
        this.roadAddressLine2 = trim(roadAddressLine2);
        this.roadAddressLine3 = trim(roadAddressLine3);
        this.roadAddressLine4 = trim(roadAddressLine4);
        this.roadAddressLine5 = trim(roadAddressLine5);
        this.addressTextType = addressTextType;
        this.startAuthority = startAuthority;
    }

    @JsonIgnore
    public String getFieldName() {
        return TABLE_NAME;
    }


    public static final String DB_FIELD_MUNICIPALITY_CODE = "municipalityCode";
    public static final String IO_FIELD_MUNICIPALITY_CODE = "kommunekode";
    @Column(name = DB_FIELD_MUNICIPALITY_CODE)
    @JsonProperty(value = IO_FIELD_MUNICIPALITY_CODE)
    @XmlElement(name = IO_FIELD_MUNICIPALITY_CODE)
    private int municipalityCode;

    public int getMunicipalityCode() {
        return this.municipalityCode;
    }

    public void setMunicipalityCode(int municipalityCode) {
        this.municipalityCode = municipalityCode;
    }


    public static final String DB_FIELD_ROAD_CODE = "roadCode";
    public static final String IO_FIELD_ROAD_CODE = "vejkode";
    @Column(name = DB_FIELD_ROAD_CODE)
    @JsonProperty(value = IO_FIELD_ROAD_CODE)
    @XmlElement(name = IO_FIELD_ROAD_CODE)
    private int roadCode;

    public int getRoadCode() {
        return this.roadCode;
    }

    public void setRoadCode(int roadCode) {
        this.roadCode = roadCode;
    }


    public static final String DB_FIELD_BUILDING_NUMBER = "buildingNumber";
    public static final String IO_FIELD_BUILDING_NUMBER = "bnr";
    @Column(name = DB_FIELD_BUILDING_NUMBER)
    @JsonProperty(value = IO_FIELD_BUILDING_NUMBER)
    @XmlElement(name = IO_FIELD_BUILDING_NUMBER)
    private String buildingNumber;

    public String getBuildingNumber() {
        return this.buildingNumber;
    }

    public void setBuildingNumber(String buildingNumber) {
        this.buildingNumber = buildingNumber;
    }


    public static final String DB_FIELD_HOUSENUMBER = "houseNumber";
    public static final String IO_FIELD_HOUSENUMBER = "husnummer";
    @Column(name = DB_FIELD_HOUSENUMBER)
    @JsonProperty(value = IO_FIELD_HOUSENUMBER)
    @XmlElement(name = IO_FIELD_HOUSENUMBER)
    private String houseNumber;

    public String getHouseNumber() {
        return this.houseNumber;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    public static final String DB_FIELD_FLOOR = "floor";
    public static final String IO_FIELD_FLOOR = "etage";
    @Column(name = DB_FIELD_FLOOR)
    @JsonProperty(value = IO_FIELD_FLOOR)
    @XmlElement(name = IO_FIELD_FLOOR)
    private String floor;

    public String getFloor() {
        return this.floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }


    public static final String DB_FIELD_DOOR = "door";
    public static final String IO_FIELD_DOOR = "sidedør";
    @Column(name = DB_FIELD_DOOR)
    @JsonProperty(value = IO_FIELD_DOOR)
    @XmlElement(name = IO_FIELD_DOOR)
    private String door;

    public String getDoor() {
        return this.door;
    }

    public void setDoor(String door) {
        this.door = door;
    }


    public static final String DB_FIELD_ADDRESS_LINE1 = "roadAddressLine1";
    public static final String IO_FIELD_ADDRESS_LINE1 = "adresselinie1";
    @Column(name = DB_FIELD_ADDRESS_LINE1)
    @JsonProperty(value = IO_FIELD_ADDRESS_LINE1)
    @XmlElement(name = IO_FIELD_ADDRESS_LINE1)
    private String roadAddressLine1;

    public String getRoadAddressLine1() {
        return this.roadAddressLine1;
    }

    public void setRoadAddressLine1(String roadAddressLine1) {
        if (roadAddressLine1 != null) {
            this.roadAddressLine1 = roadAddressLine1;
        }
    }


    public static final String DB_FIELD_ADDRESS_LINE2 = "roadAddressLine2";
    public static final String IO_FIELD_ADDRESS_LINE2 = "adresselinie2";
    @Column(name = DB_FIELD_ADDRESS_LINE2)
    @JsonProperty(value = IO_FIELD_ADDRESS_LINE2)
    @XmlElement(name = IO_FIELD_ADDRESS_LINE2)
    private String roadAddressLine2;

    public String getRoadAddressLine2() {
        return this.roadAddressLine2;
    }

    public void setRoadAddressLine2(String roadAddressLine2) {
        if (roadAddressLine2 != null) {
            this.roadAddressLine2 = roadAddressLine2;
        }
    }


    public static final String DB_FIELD_ADDRESS_LINE3 = "roadAddressLine3";
    public static final String IO_FIELD_ADDRESS_LINE3 = "adresselinie3";
    @Column(name = DB_FIELD_ADDRESS_LINE3)
    @JsonProperty(value = IO_FIELD_ADDRESS_LINE3)
    @XmlElement(name = IO_FIELD_ADDRESS_LINE3)
    private String roadAddressLine3;

    public String getRoadAddressLine3() {
        return this.roadAddressLine3;
    }

    public void setRoadAddressLine3(String roadAddressLine3) {
        if (roadAddressLine3 != null) {
            this.roadAddressLine3 = roadAddressLine3;
        }
    }


    public static final String DB_FIELD_ADDRESS_LINE4 = "roadAddressLine4";
    public static final String IO_FIELD_ADDRESS_LINE4 = "adresselinie4";
    @Column(name = DB_FIELD_ADDRESS_LINE4)
    @JsonProperty(value = IO_FIELD_ADDRESS_LINE4)
    @XmlElement(name = IO_FIELD_ADDRESS_LINE4)
    private String roadAddressLine4;

    public String getRoadAddressLine4() {
        return this.roadAddressLine4;
    }

    public void setRoadAddressLine4(String roadAddressLine4) {
        if (roadAddressLine4 != null) {
            this.roadAddressLine4 = roadAddressLine4;
        }
    }


    public static final String DB_FIELD_ADDRESS_LINE5 = "roadAddressLine5";
    public static final String IO_FIELD_ADDRESS_LINE5 = "adresselinie5";
    @Column(name = DB_FIELD_ADDRESS_LINE5)
    @JsonProperty(value = IO_FIELD_ADDRESS_LINE5)
    @XmlElement(name = IO_FIELD_ADDRESS_LINE5)
    private String roadAddressLine5;

    public String getRoadAddressLine5() {
        return this.roadAddressLine5;
    }

    public void setRoadAddressLine5(String roadAddressLine5) {
        if (roadAddressLine5 != null) {
            this.roadAddressLine5 = roadAddressLine5;
        }
    }


    public static final String DB_FIELD_ADDRESS_TEXT_TYPE = "addressTextType";
    public static final String IO_FIELD_ADDRESS_TEXT_TYPE = "adressetekststype";
    @Column(name = DB_FIELD_ADDRESS_TEXT_TYPE)
    @JsonProperty(value = IO_FIELD_ADDRESS_TEXT_TYPE)
    @XmlElement(name = IO_FIELD_ADDRESS_TEXT_TYPE)
    private int addressTextType = 0;

    public int getAddressTextType() {
        return this.addressTextType;
    }

    public void setAddressTextType(int addressTextType) {
        this.addressTextType = addressTextType;
    }

    public static final String DB_FIELD_START_AUTHORITY = "startAuthority";
    public static final String IO_FIELD_START_AUTHORITY = "startautoritet";
    @Column(name = DB_FIELD_START_AUTHORITY)
    @JsonProperty(value = IO_FIELD_START_AUTHORITY)
    @XmlElement(name = IO_FIELD_START_AUTHORITY)
    private int startAuthority = 0;

    public int getStartAuthority() {
        return this.startAuthority;
    }

    public void setStartAuthority(int startAuthority) {
        this.startAuthority = startAuthority;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = DB_FIELD_CORRECTION_OF)
    private final Set<AddressDataRecord> correctors = new HashSet<>();

    public Set<AddressDataRecord> getCorrectors() {
        return this.correctors;
    }


    @Override
    public boolean equalData(Object o) {
        return this.equalDataWithMunicipalityChange(o, true);
    }

    public boolean equalDataWithMunicipalityChange(Object o, boolean equateMunicipality) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equalData(o)) return false;
        AddressDataRecord that = (AddressDataRecord) o;

        if (!(
                (municipalityCode == that.municipalityCode || (equateMunicipality && this.oldMunicipalityCode() == that.oldMunicipalityCode())) &&
                        roadCode == that.roadCode &&
                        Objects.equals(buildingNumber, that.buildingNumber) &&
                        Objects.equals(houseNumber, that.houseNumber) &&
                        Objects.equals(floor, that.floor) &&
                        Objects.equals(door, that.door)
        )) {
            return false;
        }

        if (!this.isHistoric() && !that.isHistoric()) {
            return addressTextType == that.addressTextType &&
                    startAuthority == that.startAuthority &&
                    Objects.equals(roadAddressLine1, that.roadAddressLine1) &&
                    Objects.equals(roadAddressLine2, that.roadAddressLine2) &&
                    Objects.equals(roadAddressLine3, that.roadAddressLine3) &&
                    Objects.equals(roadAddressLine4, that.roadAddressLine4) &&
                    Objects.equals(roadAddressLine5, that.roadAddressLine5);
        }
        return true;
    }

    @Override
    public boolean hasData() {
        return
                municipalityCode != 0 ||
                        roadCode != 0 ||
                        addressTextType != 0 ||
                        startAuthority != 0 ||
                        stringNonEmpty(buildingNumber) ||
                        stringNonEmpty(houseNumber) ||
                        stringNonEmpty(floor) ||
                        stringNonEmpty(door) ||
                        stringNonEmpty(roadAddressLine1) ||
                        stringNonEmpty(roadAddressLine2) ||
                        stringNonEmpty(roadAddressLine3) ||
                        stringNonEmpty(roadAddressLine4) ||
                        stringNonEmpty(roadAddressLine5);
    }

    private int oldMunicipalityCode() {
        if (this.municipalityCode == 959 || this.municipalityCode == 960) {
            return 958;
        }
        return this.municipalityCode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), municipalityCode, roadCode, buildingNumber, houseNumber, floor, door, roadAddressLine1, roadAddressLine2, roadAddressLine3, roadAddressLine4, roadAddressLine5, addressTextType, startAuthority);
    }

    @Override
    public AddressDataRecord clone() {
        AddressDataRecord clone = new AddressDataRecord();
        clone.buildingNumber = this.buildingNumber;
        clone.municipalityCode = this.municipalityCode;
        clone.roadCode = this.roadCode;
        clone.floor = this.floor;
        clone.houseNumber = this.houseNumber;
        clone.door = this.door;
        clone.roadAddressLine1 = this.roadAddressLine1;
        clone.roadAddressLine2 = this.roadAddressLine2;
        clone.roadAddressLine3 = this.roadAddressLine3;
        clone.roadAddressLine4 = this.roadAddressLine4;
        clone.roadAddressLine5 = this.roadAddressLine5;
        clone.addressTextType = this.addressTextType;
        clone.startAuthority = this.startAuthority;
        CprBitemporalRecord.copy(this, clone);
        return clone;
    }


    @JsonIgnore
    @Override
    public List<BaseQuery> getAssoc() {
        PluginManager pluginManager = PluginManager.getInstance();
        ArrayList<BaseQuery> queries = new ArrayList<>();
        HashMap<String, String> map = new HashMap<>();
        map.put("municipalitycode", Integer.toString(this.municipalityCode));
        map.put("roadcode", Integer.toString(this.roadCode));

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
