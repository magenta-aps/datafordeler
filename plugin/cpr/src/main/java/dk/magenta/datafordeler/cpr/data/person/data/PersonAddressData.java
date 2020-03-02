package dk.magenta.datafordeler.cpr.data.person.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.cpr.CprPlugin;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import java.util.HashMap;
import java.util.Map;

import static dk.magenta.datafordeler.cpr.data.person.data.PersonAddressData.DB_FIELD_MUNICIPALITY_CODE;

/**
 * Storage for data on a Person's address,
 * referenced by {@link dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData}
 */
@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_person_address", indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_person_address_municipality", columnList = DB_FIELD_MUNICIPALITY_CODE)
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PersonAddressData extends AuthorityDetailData {

    public static final String DB_FIELD_BUILDING_NUMBER = "buildingNumber";
    public static final String IO_FIELD_BUILDING_NUMBER = "bygningsnummer";
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



    public static final String DB_FIELD_CITY_NAME = "cityName";
    public static final String IO_FIELD_CITY_NAME = "bynavn";
    @Column(name = DB_FIELD_CITY_NAME)
    @JsonProperty(value = IO_FIELD_CITY_NAME)
    @XmlElement(name = IO_FIELD_CITY_NAME)
    private String cityName;

    public String getCityName() {
        return this.cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }



    public static final String DB_FIELD_MUNICIPALITY_CODE = "municipalityCode";
    public static final String IO_FIELD_MUNICIPALITY_CODE = "cprKommunekode";
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



    public static final String DB_FIELD_MUNICIPALITY_NAME = "municipalityName";
    public static final String IO_FIELD_MUNICIPALITY_NAME = "cprKommunenavn";
    @Column(name = DB_FIELD_MUNICIPALITY_NAME)
    @JsonProperty(value = IO_FIELD_MUNICIPALITY_NAME)
    @XmlElement(name = IO_FIELD_MUNICIPALITY_NAME)
    private String municipalityName;

    public String getMunicipalityName() {
        return this.municipalityName;
    }

    public void setMunicipalityName(String municipalityName) {
        this.municipalityName = municipalityName;
    }



    public static final String DB_FIELD_ROAD_CODE = "roadCode";
    public static final String IO_FIELD_ROAD_CODE = "cprVejkode";
    @Column(name = DB_FIELD_ROAD_CODE)
    @JsonProperty(value = IO_FIELD_ROAD_CODE)
    @XmlElement(name = IO_FIELD_ROAD_CODE)
    private int roadCode;

    public int getRoadCode() { return this.roadCode; }

    public void setRoadCode(int roadCode) {
        this.roadCode = roadCode;
    }



    public static final String DB_FIELD_DAR_ADDRESS = "darAddress";
    public static final String IO_FIELD_DAR_ADDRESS = "darAdresse";
    @Column(name = DB_FIELD_DAR_ADDRESS)
    @JsonProperty(value = IO_FIELD_DAR_ADDRESS)
    @XmlElement(name = IO_FIELD_DAR_ADDRESS)
    private String darAddress;

    public String getDarAddress() {
        return this.darAddress;
    }

    public void setDarAddress(String darAddress) {
        this.darAddress = darAddress;
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



    public static final String DB_FIELD_POSTAL_DISTRICT = "postalDistrict";
    public static final String IO_FIELD_POSTAL_DISTRICT = "postdistrikt";
    @Column(name = DB_FIELD_POSTAL_DISTRICT)
    @JsonProperty(value = IO_FIELD_POSTAL_DISTRICT)
    @XmlElement(name = IO_FIELD_POSTAL_DISTRICT)
    private String postalDistrict;

    public String getPostalDistrict() {
        return this.postalDistrict;
    }

    public void setPostalDistrict(String postalDistrict) {
        this.postalDistrict = postalDistrict;
    }



    public static final String DB_FIELD_POSTAL_CODE = "postalCode";
    public static final String IO_FIELD_POSTAL_CODE = "postnummer";
    @Column(name = DB_FIELD_POSTAL_CODE)
    @JsonProperty(value = IO_FIELD_POSTAL_CODE)
    @XmlElement(name = IO_FIELD_POSTAL_CODE)
    private String postalCode;

    public String getPostalCode() {
        return this.postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
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



    public static final String DB_FIELD_ROAD_ADDRESS_NAME = "roadAddressName";
    public static final String IO_FIELD_ROAD_ADDRESS_NAME = "vejadresseringsnavn";
    @Column(name = DB_FIELD_ROAD_ADDRESS_NAME)
    @JsonProperty(value = IO_FIELD_ROAD_ADDRESS_NAME)
    @XmlElement(name = IO_FIELD_ROAD_ADDRESS_NAME)
    private String roadAddressName;

    public String getRoadAddressName() {
        return this.roadAddressName;
    }

    public void setRoadAddressName(String roadAddressName) {
        this.roadAddressName = roadAddressName;
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




    //Ikke i grunddatamodellen

    @Transient
    private int adressetekststype = 0;

    public int getAdressetekststype() {
        return this.adressetekststype;
    }

    public void setAdressetekststype(int adressetekststype) {
        if (adressetekststype != 0) {
            this.adressetekststype = adressetekststype;
        }
    }


    @Transient
    private int startautoritet = 0;

    public int getStartautoritet() {
        return this.startautoritet;
    }

    public void setStartautoritet(int startautoritet) {
        if (startautoritet != 0) {
            this.startautoritet = startautoritet;
        }
    }


    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>(super.asMap());
        //CprAdresse
        map.put("buildingNumber", this.buildingNumber);
        map.put("cityName", this.cityName);
        map.put("municipalityCode", this.municipalityCode);
        map.put("municipalityName", this.municipalityName);
        map.put("roadCode", this.roadCode);
        map.put("darAddress", this.darAddress);
        map.put("floor", this.floor);
        map.put("houseNumber", this.houseNumber);
        map.put("postalDistrict", this.postalDistrict);
        map.put("postalCode", this.postalCode);
        map.put("door", this.door);
        map.put("roadAddressName", this.roadAddressName);

        //SimpelAdresse
        map.put("roadAddressLine1", this.roadAddressLine1);
        map.put("roadAddressLine2", this.roadAddressLine2);
        map.put("roadAddressLine3", this.roadAddressLine3);
        map.put("roadAddressLine4", this.roadAddressLine4);
        map.put("roadAddressLine5", this.roadAddressLine5);

        //Ikke i grunddatamodellen
        //map.put("adressetekststype", this.adressetekststype);
        //map.put("startautoritet", this.startautoritet);
        return map;
    }

    @Override
    protected PersonAddressData clone() {
        PersonAddressData clone = new PersonAddressData();
        clone.buildingNumber = this.buildingNumber;
        clone.cityName = this.cityName;
        clone.municipalityCode = this.municipalityCode;
        clone.municipalityName = this.municipalityName;
        clone.roadCode = this.roadCode;
        clone.darAddress = this.darAddress;
        clone.floor = this.floor;
        clone.houseNumber = this.houseNumber;
        clone.postalDistrict = this.postalDistrict;
        clone.postalCode = this.postalCode;
        clone.door = this.door;
        clone.roadAddressName = this.roadAddressName;
        clone.roadAddressLine1 = this.roadAddressLine1;
        clone.roadAddressLine2 = this.roadAddressLine2;
        clone.roadAddressLine3 = this.roadAddressLine3;
        clone.roadAddressLine4 = this.roadAddressLine4;
        clone.roadAddressLine5 = this.roadAddressLine5;
        clone.setAuthority(this.getAuthority());
        clone.setDafoUpdated(this.getDafoUpdated());
        return clone;
    }
}
