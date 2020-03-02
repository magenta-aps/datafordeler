package dk.magenta.datafordeler.geo.data.municipality;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.geo.data.SumiffiikRawData;
import dk.magenta.datafordeler.geo.data.common.GeoMonotemporalRecord;
import org.geojson.MultiPolygon;
import org.geojson.Polygon;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MunicipalityRawData extends SumiffiikRawData {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public class MunicipalityRawProperties extends RawAreaProperties {

        @JsonProperty("Kommunenavn")
        public String name;

        @JsonProperty("Kommunekode")
        public int code;

        @JsonProperty("groupid")
        public void setSumiffiikId(String sumiffiikId) {
            this.sumiffiikId = sumiffiikId;
        }
    }

    @JsonProperty
    public MunicipalityRawProperties properties;


    @JsonProperty("attributes")
    public void setAttributes(MunicipalityRawProperties attributes) {
        this.properties = attributes;
    }

    @Override
    public List<GeoMonotemporalRecord> getMonotemporalRecords() {
        ArrayList<GeoMonotemporalRecord> records = new ArrayList<>();
        records.add(
                new MunicipalityNameRecord(this.properties.name)
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
                    new MunicipalityShapeRecord(this.properties.area, this.properties.length, multiPolygon)
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
