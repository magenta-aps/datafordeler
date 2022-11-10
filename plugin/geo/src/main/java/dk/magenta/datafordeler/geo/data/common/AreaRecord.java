package dk.magenta.datafordeler.geo.data.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vividsolutions.jts.geom.*;
import dk.magenta.datafordeler.geo.GeoPlugin;
import dk.magenta.datafordeler.geo.data.GeoEntity;
import org.geojson.LngLatAlt;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@MappedSuperclass
public abstract class AreaRecord<E extends GeoEntity> extends GeoMonotemporalRecord<E> {

    public AreaRecord() {
    }

    public AreaRecord(double area, double circumference, MultiPolygon shape) {
        this.area = area;
        this.circumference = circumference;
        this.shape = shape;
    }

    public AreaRecord(double area, double circumference, org.geojson.MultiPolygon shape) {
        this.area = area;
        this.circumference = circumference;
        this.setShape(shape);
    }

    public static final String DB_FIELD_AREA = "area";
    public static final String IO_FIELD_AREA = "areal";
    @Column(name = DB_FIELD_AREA)
    @JsonProperty(IO_FIELD_AREA)
    private double area;

    public double getArea() {
        return this.area;
    }

    public AreaRecord setArea(double area) {
        this.area = area;
        return this;
    }


    public static final String DB_FIELD_CIRCUMFERENCE = "circumference";
    public static final String IO_FIELD_CIRCUMFERENCE = "omkreds";
    @Column(name = DB_FIELD_CIRCUMFERENCE)
    @JsonProperty(IO_FIELD_CIRCUMFERENCE)
    private double circumference;

    public double getCircumference() {
        return this.circumference;
    }

    public AreaRecord setCircumference(double circumference) {
        this.circumference = circumference;
        return this;
    }


    public static final String DB_FIELD_SHAPE = "shape";
    public static final String IO_FIELD_SHAPE = "form";
    @Column(name = DB_FIELD_SHAPE, columnDefinition = "varbinary(max)")
    @JsonIgnore
    private MultiPolygon shape;

    public MultiPolygon getShape() {
        return this.shape;
    }

    // Getter for shape as geoJSON?
    @JsonProperty
    public org.geojson.Geometry getGeoJson() {
        if (this.shape.getNumGeometries() == 1) {
            return AreaRecord.convert((Polygon) this.shape.getGeometryN(0));
        } else {
            return AreaRecord.convert(this.shape);
        }
    }


    public AreaRecord setShape(MultiPolygon shape) {
        this.shape = shape;
        return this;
    }

    public AreaRecord setShape(org.geojson.MultiPolygon shape) {
        return this.setShape(AreaRecord.convert(shape));
    }


    @Override
    public boolean equalData(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        if (!super.equalData(o)) return false;
        AreaRecord that = (AreaRecord) o;
        return Objects.equals(this.area, that.area) &&
                Objects.equals(this.circumference, that.circumference) &&
                (this.shape == null ? that.shape == null : this.shape.equalsExact(that.shape, 0.0001));
    }


    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), GeoPlugin.SRID);


    public static MultiPolygon convert(org.geojson.MultiPolygon original) {
        List<Polygon> list = original.getCoordinates().stream().map(AreaRecord::convertList).filter(Objects::nonNull).collect(Collectors.toList());
        return new MultiPolygon(
                list.toArray(new Polygon[list.size()]),
                geometryFactory
        );
    }

    public static org.geojson.MultiPolygon convert(MultiPolygon original) {
        org.geojson.MultiPolygon multiPolygon = new org.geojson.MultiPolygon();
        for (int i = 0; i < original.getNumGeometries(); i++) {
            multiPolygon.add(AreaRecord.convert((Polygon) original.getGeometryN(i)));
        }
        return multiPolygon;
    }


    public static Polygon convert(org.geojson.Polygon original) {
        List<LinearRing> list = original.getInteriorRings().stream().map(AreaRecord::convert).filter(Objects::nonNull).collect(Collectors.toList());
        return new Polygon(
                AreaRecord.convert(original.getExteriorRing()),
                list.toArray(new LinearRing[list.size()]),
                geometryFactory
        );
    }

    public static org.geojson.Polygon convert(Polygon original) {
        org.geojson.Polygon polygon = new org.geojson.Polygon(AreaRecord.convert((LinearRing) original.getExteriorRing()));
        for (int i = 0; i < original.getNumInteriorRing(); i++) {
            polygon.addInteriorRing(AreaRecord.convert((LinearRing) original.getInteriorRingN(i)));
        }
        return polygon;
    }


    public static Polygon convertList(List<List<LngLatAlt>> original) {
        if (original.isEmpty()) return null;
        List<LinearRing> list = original.subList(1, original.size()).stream().map(AreaRecord::convert).collect(Collectors.toList());
        return new Polygon(
                AreaRecord.convert(original.get(0)),
                list.toArray(new LinearRing[list.size()]),
                geometryFactory
        );
    }

    public static List<List<LngLatAlt>> convertList(Polygon original) {
        ArrayList<List<LngLatAlt>> list = new ArrayList<>();
        list.add(AreaRecord.convert((LinearRing) original.getExteriorRing()));
        for (int i = 0; i < original.getNumInteriorRing(); i++) {
            list.add(AreaRecord.convert((LinearRing) original.getInteriorRingN(i)));
        }
        return list;
    }


    public static LinearRing convert(List<LngLatAlt> original) {
        if (original == null) return null;
        List<Coordinate> list = original.stream().map(AreaRecord::convert).collect(Collectors.toList());
        return new LinearRing(
                geometryFactory.getCoordinateSequenceFactory().create(
                        list.toArray(new Coordinate[list.size()])
                ),
                geometryFactory
        );
    }

    public static List<LngLatAlt> convert(LinearRing original) {
        return Arrays.stream(original.getCoordinates()).map(AreaRecord::convert).collect(Collectors.toList());
    }


    public static Coordinate convert(LngLatAlt original) {
        return new Coordinate(original.getLongitude(), original.getLatitude());
    }

    public static LngLatAlt convert(Coordinate original) {
        return new LngLatAlt(original.x, original.y);
    }
}
