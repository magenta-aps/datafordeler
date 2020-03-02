package dk.magenta.datafordeler.geo.data.postcode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.geo.data.SumiffiikRawData;
import dk.magenta.datafordeler.geo.data.common.GeoMonotemporalRecord;
import org.geojson.MultiPolygon;
import org.geojson.Polygon;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PostcodeRawData extends SumiffiikRawData {

    public class PostcodeRawProperties extends RawAreaProperties {

        @JsonProperty("OBJECTID")
        public int objectId;

        @JsonProperty("Postdistri")
        public String name;

        @JsonProperty("Postnummer")
        public int code;

    }

    @JsonProperty("properties")
    public PostcodeRawProperties properties;

    public PostcodeRawProperties getProperties() {
        return this.properties;
    }

    @JsonProperty("attributes")
    public void setAttributes(PostcodeRawProperties attributes) {
        this.properties = attributes;
    }

    @Override
    public List<GeoMonotemporalRecord> getMonotemporalRecords() {
        ArrayList<GeoMonotemporalRecord> records = new ArrayList<>();
        records.add(
                new PostcodeNameRecord(this.properties.name)
        );

        MultiPolygon multiPolygon = null;
        if (this.shape instanceof Polygon) {
            Polygon polygon = (Polygon) this.shape;
            multiPolygon = new MultiPolygon();
            multiPolygon.add(polygon);
        } else if (this.shape instanceof MultiPolygon) {
            multiPolygon = (MultiPolygon) this.shape;
        }
        if (multiPolygon != null) {
            records.add(
                    new PostcodeShapeRecord(this.properties.area, this.properties.length, multiPolygon)
            );
        }

        for (GeoMonotemporalRecord record : records) {
            record.setEditor(this.properties.editor);
            record.setRegistrationFrom(this.properties.editDate);
        }

        return records;
    }

}
