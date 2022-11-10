package dk.magenta.datafordeler.geo.data.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vividsolutions.jts.geom.*;
import dk.magenta.datafordeler.geo.GeoPlugin;
import dk.magenta.datafordeler.geo.data.GeoEntity;
import org.geojson.LngLatAlt;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@MappedSuperclass
public abstract class LineRecord<E extends GeoEntity> extends GeoMonotemporalRecord<E> {

    public LineRecord() {
    }

    public LineRecord(double length, MultiLineString shape) {
        this.length = length;
        this.shape = shape;
    }

    public LineRecord(double length, org.geojson.MultiLineString shape) {
        this.length = length;
        this.setShape(shape);
    }

    public static final String DB_FIELD_LENGTH = "length";
    public static final String IO_FIELD_LENGTH = "længde";
    @Column(name = DB_FIELD_LENGTH)
    @JsonProperty(IO_FIELD_LENGTH)
    private double length;

    public double getLength() {
        return this.length;
    }

    public LineRecord setLength(double length) {
        this.length = length;
        return this;
    }


    public static final String DB_FIELD_SHAPE = "shape";
    public static final String IO_FIELD_SHAPE = "form";
    @Column(name = DB_FIELD_SHAPE, columnDefinition = "varbinary(max)")
    @JsonIgnore
    private MultiLineString shape;

    public MultiLineString getShape() {
        return this.shape;
    }

    public LineRecord setShape(MultiLineString shape) {
        this.shape = shape;
        return this;
    }

    public LineRecord setShape(org.geojson.MultiLineString shape) {
        return this.setShape(LineRecord.convert(shape));
    }

    // Getter for shape as geoJSON?
    @JsonProperty
    public org.geojson.Geometry getGeoJson() {
        return LineRecord.convert(this.shape);
    }


    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), GeoPlugin.SRID);


    public static MultiLineString convert(org.geojson.MultiLineString original) {
        List<LineString> list = original.getCoordinates().stream().map(LineRecord::convert).collect(Collectors.toList());
        return new MultiLineString(
                list.toArray(new LineString[list.size()]),
                geometryFactory
        );
    }

    public static org.geojson.MultiLineString convert(MultiLineString original) {
        org.geojson.MultiLineString multiLineString = new org.geojson.MultiLineString();
        for (int i = 0; i < original.getNumGeometries(); i++) {
            multiLineString.add(LineRecord.convert((LineString) original.getGeometryN(i)));
        }
        return multiLineString;
    }

    public static LineString convert(List<LngLatAlt> original) {
        List<Coordinate> list = original.stream().map(AreaRecord::convert).collect(Collectors.toList());
        return new LineString(
                geometryFactory.getCoordinateSequenceFactory().create(
                        list.toArray(new Coordinate[list.size()])
                ),
                geometryFactory
        );
    }

    public static List<LngLatAlt> convert(LineString original) {
        return Arrays.asList(original.getCoordinates()).stream().map(AreaRecord::convert).collect(Collectors.toList());
    }


    public static Coordinate convert(LngLatAlt original) {
        return new Coordinate(original.getLongitude(), original.getLatitude());
    }

    public static LngLatAlt convert(Coordinate original) {
        return new LngLatAlt(original.x, original.y);
    }
}
