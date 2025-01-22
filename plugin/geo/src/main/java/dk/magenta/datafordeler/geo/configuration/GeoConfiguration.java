package dk.magenta.datafordeler.geo.configuration;

import dk.magenta.datafordeler.core.Environment;
import dk.magenta.datafordeler.core.configuration.Configuration;
import dk.magenta.datafordeler.geo.GeoPlugin;
import dk.magenta.datafordeler.geo.data.accessaddress.AccessAddressEntity;
import dk.magenta.datafordeler.geo.data.building.BuildingEntity;
import dk.magenta.datafordeler.geo.data.locality.GeoLocalityEntity;
import dk.magenta.datafordeler.geo.data.municipality.GeoMunicipalityEntity;
import dk.magenta.datafordeler.geo.data.postcode.PostcodeEntity;
import dk.magenta.datafordeler.geo.data.road.GeoRoadEntity;
import dk.magenta.datafordeler.geo.data.unitaddress.UnitAddressEntity;

import jakarta.persistence.*;
import java.nio.charset.Charset;

@Entity
@Table(name = "geo_config")
public class GeoConfiguration implements Configuration {

    public enum RegisterType {
        DISABLED(0),
        LOCAL_FILE(1),
        REMOTE_HTTP(2);

        private final int value;

        RegisterType(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

    public enum CharsetValue {
        UTF_8(0),
        ISO_8859_1(1);

        private final int value;

        CharsetValue(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

        public String asString() {
            return this.name().replace('_', '-');
        }

        public Charset asCharset() {
            return Charset.forName(this.asString());
        }
    }

    @Id
    @Column(name = "id")
    private final String plugin = GeoPlugin.class.getName();


    @Column
    private String pullCronSchedule = null;

    public String getPullCronSchedule() {
        return this.pullCronSchedule;
    }


    @Column
    @Enumerated(EnumType.ORDINAL)
    private CharsetValue charsetName = CharsetValue.UTF_8;

    public CharsetValue getCharsetName() {
        return this.charsetName;
    }

    public Charset getCharset() {
        return this.charsetName.asCharset();
    }

    public void setCharsetName(CharsetValue charsetName) {
        this.charsetName = charsetName;
    }


    @Column(length = 1024)
    private String tokenService = "";

    public String getTokenService() {
        return this.tokenService;
    }

    public void setTokenService(String tokenService) {
        this.tokenService = tokenService;
    }

    @Column
    private String username = "";

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Column
    private String password = "";

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    @Column
    @Enumerated(EnumType.ORDINAL)
    private RegisterType municipalityRegisterType = RegisterType.DISABLED;

    public RegisterType getMunicipalityRegisterType() {
        return this.municipalityRegisterType;
    }

    public void setMunicipalityRegisterType(RegisterType municipalityRegisterType) {
        this.municipalityRegisterType = municipalityRegisterType;
    }


    @Column(length = 1024)
    private String municipalityRegisterURL = "";

    public String getMunicipalityRegisterURL() {
        return this.municipalityRegisterURL;
    }

    public void setMunicipalityRegisterURL(String municipalityRegisterURL) {
        this.municipalityRegisterURL = municipalityRegisterURL;
    }

    @Column(length = 1024)
    private String municipalityDeletionRegisterURL = "";

    public String getMunicipalityDeletionRegisterURL() {
        return this.municipalityDeletionRegisterURL;
    }

    public void setMunicipalityDeletionRegisterURL(String municipalityDeletionRegisterURL) {
        this.municipalityDeletionRegisterURL = municipalityDeletionRegisterURL;
    }


    @Column
    @Enumerated(EnumType.ORDINAL)
    private RegisterType postcodeRegisterType = RegisterType.DISABLED;

    public RegisterType getPostcodeRegisterType() {
        return this.postcodeRegisterType;
    }

    public void setPostcodeRegisterType(RegisterType postcodeRegisterType) {
        this.postcodeRegisterType = postcodeRegisterType;
    }

    @Column(length = 1024)
    private String postcodeRegisterURL = "";

    public String getPostcodeRegisterURL() {
        return this.postcodeRegisterURL;
    }


    public void setPostcodeRegisterURL(String postcodeRegisterURL) {
        this.postcodeRegisterURL = postcodeRegisterURL;
    }

    @Column(length = 1024)
    private String postcodeDeletionRegisterURL = "";

    public String getPostcodeDeletionRegisterURL() {
        return this.postcodeDeletionRegisterURL;
    }

    public void setPostcodeDeletionRegisterURL(String postcodeDeletionRegisterURL) {
        this.postcodeDeletionRegisterURL = postcodeDeletionRegisterURL;
    }


    @Column
    @Enumerated(EnumType.ORDINAL)
    private RegisterType localityRegisterType = RegisterType.DISABLED;

    public RegisterType getLocalityRegisterType() {
        return this.localityRegisterType;
    }

    public void setLocalityRegisterType(RegisterType localityRegisterType) {
        this.localityRegisterType = localityRegisterType;
    }

    @Column(length = 1024)
    private String localityRegisterURL = "";

    public String getLocalityRegisterURL() {
        return this.localityRegisterURL;
    }


    public void setLocalityRegisterURL(String localityRegisterURL) {
        this.localityRegisterURL = localityRegisterURL;
    }

    @Column(length = 1024)
    private String localityDeletionRegisterURL = "";

    public String getLocalityDeletionRegisterURL() {
        return this.localityDeletionRegisterURL;
    }

    public void setLocalityDeletionRegisterURL(String localityDeletionRegisterURL) {
        this.localityDeletionRegisterURL = localityDeletionRegisterURL;
    }


    @Column
    @Enumerated(EnumType.ORDINAL)
    private RegisterType roadRegisterType = RegisterType.DISABLED;

    public RegisterType getRoadRegisterType() {
        return this.roadRegisterType;
    }

    public void setRoadRegisterType(RegisterType roadRegisterType) {
        this.roadRegisterType = roadRegisterType;
    }

    @Column(length = 1024)
    private String roadRegisterURL = "";

    public String getRoadRegisterURL() {
        return this.roadRegisterURL;
    }

    public void setRoadRegisterURL(String roadRegisterURL) {
        this.roadRegisterURL = roadRegisterURL;
    }

    @Column(length = 1024)
    private String roadDeletionRegisterURL = "";

    public String getRoadDeletionRegisterURL() {
        return this.roadDeletionRegisterURL;
    }

    public void setRoadDeletionRegisterURL(String roadDeletionRegisterURL) {
        this.roadDeletionRegisterURL = roadDeletionRegisterURL;
    }


    @Column
    @Enumerated(EnumType.ORDINAL)
    private RegisterType buildingRegisterType = RegisterType.DISABLED;

    public RegisterType getBuildingRegisterType() {
        return this.buildingRegisterType;
    }

    public void setBuildingRegisterType(RegisterType buildingRegisterType) {
        this.buildingRegisterType = buildingRegisterType;
    }

    @Column(length = 1024)
    private String buildingRegisterURL = "";


    public String getBuildingRegisterURL() {
        return this.buildingRegisterURL;
    }

    public void setBuildingRegisterURL(String buildingRegisterURL) {
        this.buildingRegisterURL = buildingRegisterURL;
    }

    @Column(length = 1024)
    private String buildingDeletionRegisterURL = "";

    public String getBuildingDeletionRegisterURL() {
        return this.buildingDeletionRegisterURL;
    }

    public void setBuildingDeletionRegisterURL(String buildingDeletionRegisterURL) {
        this.buildingDeletionRegisterURL = buildingDeletionRegisterURL;
    }


    @Column
    @Enumerated(EnumType.ORDINAL)
    private RegisterType accessAddressRegisterType = RegisterType.DISABLED;

    public RegisterType getAccessAddressRegisterType() {
        return this.accessAddressRegisterType;
    }

    public void setAccessAddressRegisterType(RegisterType accessAddressRegisterType) {
        this.accessAddressRegisterType = accessAddressRegisterType;
    }

    @Column(length = 1024)
    private String accessAddressRegisterURL = "";

    public String getAccessAddressRegisterURL() {
        return this.accessAddressRegisterURL;
    }

    public void setAccessAddressRegisterURL(String accessAddressRegisterURL) {
        this.accessAddressRegisterURL = accessAddressRegisterURL;
    }

    @Column(length = 1024)
    private String accessAddressDeletionRegisterURL = "";

    public String getAccessAddressDeletionRegisterURL() {
        return this.accessAddressDeletionRegisterURL;
    }

    public void setAccessAddressDeletionRegisterURL(String accessAddressDeletionRegisterURL) {
        this.accessAddressDeletionRegisterURL = accessAddressDeletionRegisterURL;
    }


    @Column
    @Enumerated(EnumType.ORDINAL)
    private RegisterType unitAddressRegisterType = RegisterType.DISABLED;

    public RegisterType getUnitAddressRegisterType() {
        return this.unitAddressRegisterType;
    }

    public void setUnitAddressRegisterType(RegisterType unitAddressRegisterType) {
        this.unitAddressRegisterType = unitAddressRegisterType;
    }

    @Column(length = 1024)
    private String unitAddressRegisterURL = "";


    public String getUnitAddressRegisterURL() {
        return this.unitAddressRegisterURL;
    }

    public void setUnitAddressRegisterURL(String unitAddressRegisterURL) {
        this.unitAddressRegisterURL = unitAddressRegisterURL;
    }

    @Column(length = 1024)
    private String unitAddressDeletionRegisterURL = "";

    public String getUnitAddressDeletionRegisterURL() {
        return this.unitAddressDeletionRegisterURL;
    }

    public void setUnitAddressDeletionRegisterURL(String unitAddressDeletionRegisterURL) {
        this.unitAddressDeletionRegisterURL = unitAddressDeletionRegisterURL;
    }

    @Transient
    private String localCopyFolder = Environment.getEnv("GEO_LOCAL_COPY_FOLDER", null);

    public RegisterType getRegisterType(String schema) {
        switch (schema) {
            case GeoMunicipalityEntity.schema:
                return this.municipalityRegisterType;
            case PostcodeEntity.schema:
                return this.postcodeRegisterType;
            case GeoLocalityEntity.schema:
                return this.localityRegisterType;
            case GeoRoadEntity.schema:
                return this.roadRegisterType;
            case BuildingEntity.schema:
                return this.buildingRegisterType;
            case AccessAddressEntity.schema:
                return this.accessAddressRegisterType;
            case UnitAddressEntity.schema:
                return this.unitAddressRegisterType;
        }
        return null;
    }

    public String getURL(String schema) {
        switch (schema) {
            case GeoMunicipalityEntity.schema:
                return this.municipalityRegisterURL;
            case PostcodeEntity.schema:
                return this.postcodeRegisterURL;
            case GeoLocalityEntity.schema:
                return this.localityRegisterURL;
            case GeoRoadEntity.schema:
                return this.roadRegisterURL;
            case BuildingEntity.schema:
                return this.buildingRegisterURL;
            case AccessAddressEntity.schema:
                return this.accessAddressRegisterURL;
            case UnitAddressEntity.schema:
                return this.unitAddressRegisterURL;
        }
        return null;
    }

    public String getDeletionURL(String schema) {
        switch (schema) {
            case GeoMunicipalityEntity.schema:
                return this.municipalityDeletionRegisterURL;
            case PostcodeEntity.schema:
                return this.postcodeDeletionRegisterURL;
            case GeoLocalityEntity.schema:
                return this.localityDeletionRegisterURL;
            case GeoRoadEntity.schema:
                return this.roadDeletionRegisterURL;
            case BuildingEntity.schema:
                return this.buildingDeletionRegisterURL;
            case AccessAddressEntity.schema:
                return this.accessAddressDeletionRegisterURL;
            case UnitAddressEntity.schema:
                return this.unitAddressDeletionRegisterURL;
        }
        return null;
    }

    public String getLocalCopyFolder() {
        return this.localCopyFolder;
    }
}
