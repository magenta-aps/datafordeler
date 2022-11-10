package dk.magenta.datafordeler.geo.data.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import dk.magenta.datafordeler.geo.GeoPlugin;
import dk.magenta.datafordeler.geo.data.GeoEntity;
import org.geojson.LngLatAlt;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class PointRecord<E extends GeoEntity> extends GeoMonotemporalRecord<E> {

    public PointRecord() {
    }

    public PointRecord(Point shape) {
        this.shape = shape;
    }

    public PointRecord(org.geojson.Point shape) {
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

    public PointRecord setLength(double length) {
        this.length = length;
        return this;
    }


    public static final String DB_FIELD_SHAPE = "shape";
    public static final String IO_FIELD_SHAPE = "form";
    @Column(name = DB_FIELD_SHAPE, columnDefinition = "varbinary(max)")
    @JsonIgnore
    private Point shape;

    public Point getShape() {
        return this.shape;
    }

    public PointRecord setShape(Point shape) {
        this.shape = shape;
        return this;
    }

    public PointRecord setShape(org.geojson.Point shape) {
        return this.setShape(PointRecord.convert(shape));
    }


    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), GeoPlugin.SRID);


    public static Point convert(org.geojson.Point original) {
        return new Point(
                geometryFactory.getCoordinateSequenceFactory().create(
                        new Coordinate[]{
                                PointRecord.convert(original.getCoordinates())
                        }
                ),
                geometryFactory
        );
    }

    public static org.geojson.Point convert(Point original) {
        return new org.geojson.Point(PointRecord.convert(original.getCoordinate()));
    }

    public static Coordinate convert(LngLatAlt original) {
        return new Coordinate(original.getLongitude(), original.getLatitude());
    }

    public static LngLatAlt convert(Coordinate original) {
        return new LngLatAlt(original.x, original.y);
    }
}
