package dk.magenta.datafordeler.geo.data.locality;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.geo.data.SumiffiikRawData;
import dk.magenta.datafordeler.geo.data.common.GeoMonotemporalRecord;
import org.geojson.MultiPolygon;
import org.geojson.Polygon;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LocalityRawData extends SumiffiikRawData {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public class LocalityRawProperties extends RawAreaProperties {

        @JsonProperty("Lokalitetsnavn")
        public String name;

        @JsonProperty("Lokalitetsnavn_forkortelse")
        public String abbreviation;

        @JsonProperty("Lokalitetskode")
        public String code;

        @JsonProperty("Kommunekode")
        public Integer municipality;

        @JsonProperty("Lokalitetsvejkode")
        public Integer localityRoadcode;

        @JsonProperty("Status")
        public Integer status;

        @JsonProperty("Location_type")
        public Integer type;

        @JsonProperty("Lokalitetsbetegnelse")
        public Integer betegnelse;
    }

    @JsonProperty
    public LocalityRawProperties properties;

    @JsonProperty("attributes")
    public void setAttributes(LocalityRawProperties attributes) {
        this.properties = attributes;
    }

    @Override
    public List<GeoMonotemporalRecord> getMonotemporalRecords() {
        ArrayList<GeoMonotemporalRecord> records = new ArrayList<>();

        records.add(
                new LocalityNameRecord(this.properties.name)
        );

        records.add(
                new LocalityAbbreviationRecord(this.properties.abbreviation)
        );

        records.add(
                new LocalityTypeRecord(this.properties.type)
        );

        records.add(
                new LocalityStatusRecord(this.properties.status)
        );

        records.add(
                new LocalityBetegnRecord(this.properties.betegnelse)
        );

        records.add(
                new LocalityMunicipalityRecord(this.properties.municipality)
        );

        records.add(
                new LocalityRoadcodeRecord(this.properties.localityRoadcode)
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
                    new LocalityShapeRecord(this.properties.area, this.properties.length, multiPolygon)
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
