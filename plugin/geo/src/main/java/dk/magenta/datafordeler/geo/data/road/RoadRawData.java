package dk.magenta.datafordeler.geo.data.road;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.geo.data.SumiffiikRawData;
import dk.magenta.datafordeler.geo.data.common.GeoMonotemporalRecord;
import org.geojson.LineString;
import org.geojson.MultiLineString;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RoadRawData extends SumiffiikRawData {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public class RoadRawProperties extends RawLineProperties {

        @JsonProperty("Vejnavn")
        public String name;

        @JsonProperty("Vejadresseringsnavn")
        public String addressingName;

        @JsonProperty("Vejkode")
        public void setCode(String code) {
            try {
                this.code = Integer.parseInt(code, 10);
            } catch (NumberFormatException | NullPointerException e) {
            }
        }

        public int code;

        @JsonProperty("Lokalitetskode")
        public String locality;

        @JsonProperty("Kommunekode")
        public Integer municipality;

        @JsonProperty("VejmidteSumiffik")
        public void setSumiffiikId(String sumiffiikId) {
            this.sumiffiikId = sumiffiikId;
        }
    }

    @JsonProperty
    public RoadRawProperties properties;

    @JsonProperty("attributes")
    public void setAttributes(RoadRawProperties attributes) {
        this.properties = attributes;
    }

    @Override
    public List<GeoMonotemporalRecord> getMonotemporalRecords() {
        ArrayList<GeoMonotemporalRecord> records = new ArrayList<>();

        records.add(
                new RoadNameRecord(this.properties.name, this.properties.addressingName)
        );

        records.add(
                new RoadLocalityRecord(this.properties.locality)
        );

        records.add(
                new RoadMunicipalityRecord(this.properties.municipality)
        );

        MultiLineString multiLineString = null;
        if (this.shape instanceof LineString) {
            LineString lineString = (LineString) this.shape;
            multiLineString = new MultiLineString();
            multiLineString.add(lineString.getCoordinates());
        } else if (this.shape instanceof MultiLineString) {
            multiLineString = (MultiLineString) this.shape;
        }
        if (multiLineString != null) {
            records.add(
                    new RoadShapeRecord(this.properties.length, multiLineString)
            );
        }

        for (GeoMonotemporalRecord record : records) {
            record.setEditor(this.properties.editor);
            record.setRegistrationFrom(this.properties.editDate);
        }

        return records;
    }

    @Override
    public RawProperties getProperties() {
        return this.properties;
    }

}
