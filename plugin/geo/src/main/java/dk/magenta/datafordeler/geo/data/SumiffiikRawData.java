package dk.magenta.datafordeler.geo.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.geojson.GeoJsonObject;

import java.util.UUID;

public abstract class SumiffiikRawData extends RawData {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public abstract class RawProperties {

        @JsonProperty("OBJECTID")
        public int objectId;

        @JsonProperty("GlobalID")
        public String globalID;

        @JsonIgnore
        public String sumiffiikId;

        @JsonProperty("Creator")
        public String creator;

        @JsonProperty("CreationDate")
        public long creationDate;

        @JsonProperty("Editor")
        public String editor;

        @JsonProperty("EditDate")
        public long editDate;

        @JsonIgnore
        public UUID getUUID() {
            return SumiffiikRawData.getSumiffiikAsUUID(this.globalID);
        }

    }

    public abstract class RawLineProperties extends RawProperties {

        @JsonProperty("SHAPE.STLength()")
        public double length;

    }

    public abstract class RawAreaProperties extends RawProperties {

        @JsonProperty("SHAPE.STArea()")
        public double area;

        @JsonProperty("SHAPE.STLength()")
        public double length;

    }

    public abstract RawProperties getProperties();

    @JsonProperty("geometry")
    public GeoJsonObject shape;

    public static UUID getSumiffiikAsUUID(String sumiffiikId) {
        if (sumiffiikId != null) {
            if (sumiffiikId.length() == 38 && sumiffiikId.charAt(0) == '{' && sumiffiikId.charAt(37) == '}') {
                sumiffiikId = sumiffiikId.substring(1, 37);
            }
            if (sumiffiikId.length() == 36) {
                return UUID.fromString(sumiffiikId);
            }
        }
        return null;
    }
}
