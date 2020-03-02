package dk.magenta.datafordeler.cvr.data.unversioned;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cvr.data.shared.AddressData;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Set;

/**
 * Storage of a company's address
 */
@Entity
@Table(name = "cvr_address", indexes = {
        @Index(name = "cvr_address_roadCode", columnList = Address.DB_FIELD_ROADCODE),
        @Index(name = "cvr_address_roadName", columnList = Address.DB_FIELD_ROADNAME),
        @Index(name = "cvr_address_municipality", columnList = Address.DB_FIELD_MUNICIPALITY + DatabaseEntry.REF),
        @Index(name = "cvr_address_postcode", columnList = Address.DB_FIELD_POSTCODE_REF + DatabaseEntry.REF)
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Address extends UnversionedEntity {

    public static final String DB_FIELD_ID = "addressId";
    public static final String IO_FIELD_ID = "adresseId";

    @JsonProperty(value = IO_FIELD_ID)
    @XmlElement(name = IO_FIELD_ID)
    @Column(name = DB_FIELD_ID)
    private String addressId;

    public String getAddressId() {
        return this.addressId;
    }

    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }


    @OneToMany(mappedBy = "address", cascade = javax.persistence.CascadeType.ALL)
    private Set<AddressData> addressData;


    //----------------------------------------------------

    public static final String DB_FIELD_ROADCODE = "roadCode";
    public static final String IO_FIELD_ROADCODE = "vejkode";

    @JsonProperty(value = IO_FIELD_ROADCODE)
    @XmlElement(name = IO_FIELD_ROADCODE)
    @Column(name = DB_FIELD_ROADCODE)
    private int roadCode;

    public int getRoadCode() {
        return this.roadCode;
    }

    public void setRoadCode(int roadCode) {
        this.roadCode = roadCode;
    }

    public void setRoadCode(String roadCode) {
        this.setRoadCode(Integer.parseInt(roadCode));
    }

    //----------------------------------------------------

    public static final String DB_FIELD_CITY = "supplementalCityName";
    public static final String IO_FIELD_CITY = "supplerendeBynavn";

    @JsonProperty(value = IO_FIELD_CITY)
    @XmlElement(name = IO_FIELD_CITY)
    @Column(name = DB_FIELD_CITY)
    private String supplementalCityName;

    public String getSupplementalCityName() {
        return this.supplementalCityName;
    }

    public void setSupplementalCityName(String supplementalCityName) {
        this.supplementalCityName = supplementalCityName;
    }

    //----------------------------------------------------

    public static final String DB_FIELD_ROADNAME = "roadName";
    public static final String IO_FIELD_ROADNAME = "vejnavn";

    @JsonProperty(value = IO_FIELD_ROADNAME)
    @XmlElement(name = IO_FIELD_ROADNAME)
    @Column(name = DB_FIELD_ROADNAME)
    private String roadName;

    public String getRoadName() {
        return this.roadName;
    }

    public void setRoadName(String roadName) {
        this.roadName = roadName;
    }

    //----------------------------------------------------

    public static final String DB_FIELD_HOUSE_FROM = "houseNumberFrom";
    public static final String IO_FIELD_HOUSE_FROM = "husnummerFra";

    @JsonProperty(value = IO_FIELD_HOUSE_FROM)
    @XmlElement(name = IO_FIELD_HOUSE_FROM)
    @Column(name = DB_FIELD_HOUSE_FROM)
    private String houseNumberFrom;

    public String getHouseNumberFrom() {
        return this.houseNumberFrom;
    }

    public void setHouseNumberFrom(String houseNumberFrom) {
        this.houseNumberFrom = houseNumberFrom;
    }

    //----------------------------------------------------

    public static final String DB_FIELD_HOUSE_TO = "houseNumberTo";
    public static final String IO_FIELD_HOUSE_TO = "husnummerTil";

    @JsonProperty(value = IO_FIELD_HOUSE_TO)
    @XmlElement(name = IO_FIELD_HOUSE_TO)
    @Column(name = DB_FIELD_HOUSE_TO)
    private String houseNumberTo;

    public String getHouseNumberTo() {
        return this.houseNumberTo;
    }

    public void setHouseNumberTo(String houseNumberTo) {
        this.houseNumberTo = houseNumberTo;
    }

    //----------------------------------------------------

    public static final String DB_FIELD_LETTER_FROM = "letterFrom";
    public static final String IO_FIELD_LETTER_FROM = "bogstavFra";

    @JsonProperty(value = IO_FIELD_LETTER_FROM)
    @XmlElement(name = IO_FIELD_LETTER_FROM)
    @Column(name = DB_FIELD_LETTER_FROM)
    private String letterFrom;

    public String getLetterFrom() {
        return this.letterFrom;
    }

    public void setLetterFrom(String letterFrom) {
        this.letterFrom = letterFrom;
    }

    //----------------------------------------------------

    public static final String DB_FIELD_LETTER_TO = "letterTo";
    public static final String IO_FIELD_LETTER_TO = "bogstavTil";

    @JsonProperty(value = IO_FIELD_LETTER_TO)
    @XmlElement(name = IO_FIELD_LETTER_TO)
    @Column(name = DB_FIELD_LETTER_TO)
    private String letterTo;

    public String getLetterTo() {
        return this.letterTo;
    }

    public void setLetterTo(String letterTo) {
        this.letterTo = letterTo;
    }

    //----------------------------------------------------

    public static final String DB_FIELD_FLOOR = "floor";
    public static final String IO_FIELD_FLOOR = "etagebetegnelse";

    @JsonProperty(value = IO_FIELD_FLOOR)
    @XmlElement(name = IO_FIELD_FLOOR)
    @Column(name = DB_FIELD_FLOOR)
    private String floor;

    public String getFloor() {
        return this.floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    //----------------------------------------------------

    public static final String DB_FIELD_DOOR = "door";
    public static final String IO_FIELD_DOOR = "dørbetegnelse";

    @JsonProperty(value = IO_FIELD_DOOR)
    @XmlElement(name = IO_FIELD_DOOR)
    @Column(name = DB_FIELD_DOOR)
    private String door;

    public String getDoor() {
        return this.door;
    }

    public void setDoor(String door) {
        this.door = door;
    }

    //----------------------------------------------------

    public static final String DB_FIELD_MUNICIPALITY = "municipality";
    public static final String IO_FIELD_MUNICIPALITY = "kommune";

    @JsonProperty(value = IO_FIELD_MUNICIPALITY)
    @XmlElement(name = IO_FIELD_MUNICIPALITY)
    @ManyToOne
    @Cascade(value = CascadeType.SAVE_UPDATE)
    @JoinColumn(name = DB_FIELD_MUNICIPALITY + DatabaseEntry.REF)
    private Municipality municipality;

    public Municipality getMunicipality() {
        return this.municipality;
    }

    public void setMunicipality(Municipality municipality) {
        this.municipality = municipality;
    }

    //----------------------------------------------------

    public static final String DB_FIELD_POSTCODE_REF = "post";
    public static final String IO_FIELD_POSTCODE_REF = "post";

    @JsonProperty(value = IO_FIELD_POSTCODE_REF)
    @XmlElement(name = IO_FIELD_POSTCODE_REF)
    @ManyToOne
    @Cascade(value = CascadeType.SAVE_UPDATE)
    @JoinColumn(name = DB_FIELD_POSTCODE_REF + DatabaseEntry.REF)
    private PostCode post;

    public PostCode getPost() {
        return this.post;
    }

    public void setPost(PostCode postCode) {
        this.post = postCode;
    }

    //----------------------------------------------------

    public static final String DB_FIELD_POSTCODE = "postnummer";
    public static final String IO_FIELD_POSTCODE = "postnummer";

    @JsonIgnore
    @XmlTransient
    private int postnummer;

    @JsonIgnore
    public int getPostnummer() {
        return this.postnummer;
    }

    @JsonProperty(value = IO_FIELD_POSTCODE)
    public void setPostnummer(int code) {
        this.postnummer = code;
    }

    //----------------------------------------------------

    public static final String DB_FIELD_POSTDISTRICT = "postdistrikt";
    public static final String IO_FIELD_POSTDISTRICT = "postdistrikt";

    @JsonIgnore
    @XmlTransient
    private String postdistrikt;

    @JsonIgnore
    public String getPostdistrikt() {
        return this.postdistrikt;
    }

    @JsonProperty(IO_FIELD_POSTDISTRICT)
    public void setPostdistrikt(String district) {
        this.postdistrikt = district;
    }

    //----------------------------------------------------

    public static final String DB_FIELD_POSTBOX = "postBox";
    public static final String IO_FIELD_POSTBOX = "postboks";

    @JsonProperty(value = IO_FIELD_POSTBOX)
    @XmlElement(name = IO_FIELD_POSTBOX)
    @Column(name = DB_FIELD_POSTBOX)
    private int postBox;

    public int getPostBox() {
        return this.postBox;
    }

    public void setPostBox(int postBox) {
        this.postBox = postBox;
    }

    //----------------------------------------------------

    public static final String DB_FIELD_CONAME = "coName";
    public static final String IO_FIELD_CONAME = "conavn";

    @JsonProperty(value = IO_FIELD_CONAME)
    @XmlElement(name = IO_FIELD_CONAME)
    @Column(name = DB_FIELD_CONAME)
    private String coName;

    public String getCoName() {
        return this.coName;
    }

    public void setCoName(String coName) {
        this.coName = coName;
    }

    //----------------------------------------------------

    public static final String DB_FIELD_COUNTRYCODE = "countryCode";
    public static final String IO_FIELD_COUNTRYCODE = "landekode";

    @JsonProperty(value = IO_FIELD_COUNTRYCODE)
    @XmlElement(name = IO_FIELD_COUNTRYCODE)
    @Column(name = DB_FIELD_COUNTRYCODE)
    private String countryCode;

    public String getCountryCode() {
        return this.countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    //----------------------------------------------------

    public static final String DB_FIELD_TEXT = "addressText";
    public static final String IO_FIELD_TEXT = "adresseFritekst";

    @JsonProperty(value = IO_FIELD_TEXT)
    @XmlElement(name = IO_FIELD_TEXT)
    @Column(name = DB_FIELD_TEXT)
    private String addressText;

    public String getAddressText() {
        return this.addressText;
    }

    public void setAddressText(String addressText) {
        this.addressText = addressText;
    }

    //----------------------------------------------------

    public static final String DB_FIELD_VALIDATED = "lastValidated";
    public static final String IO_FIELD_VALIDATED = "sidstValideret";

    @JsonProperty(value = IO_FIELD_VALIDATED)
    @XmlElement(name = IO_FIELD_VALIDATED)
    @Column(name = DB_FIELD_VALIDATED)
    private String lastValidated;

    public String getLastValidated() {
        return this.lastValidated;
    }

    public void setLastValidated(String lastValidated) {
        this.lastValidated = lastValidated;
    }

    //----------------------------------------------------

    public String getAddressFormatted() {
//        if (this.addressText != null) {
//            return this.addressText;
//        }
        StringBuilder out = new StringBuilder();

        if (this.roadName != null) {
            out.append(this.roadName);
        }

        if (this.houseNumberFrom != null) {
            out.append(" " + this.houseNumberFrom + emptyIfNull(this.letterFrom));
            if (this.houseNumberTo != null) {
                out.append("-");
                if (this.houseNumberTo.equals(this.houseNumberFrom)) {
                    out.append(emptyIfNull(this.letterTo));
                } else {
                    out.append(this.houseNumberTo + emptyIfNull(this.letterTo));
                }
            }

            if (this.floor != null) {
                out.append(", " + this.floor + ".");
                if (this.door != null) {
                    out.append(" " + this.door);
                }
            }

        }
        return out.toString();
    }

    private String emptyIfNull(String text) {
        if (text == null) return "";
        return text;
    }

}
