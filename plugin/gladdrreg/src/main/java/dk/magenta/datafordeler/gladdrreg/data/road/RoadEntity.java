package dk.magenta.datafordeler.gladdrreg.data.road;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dk.magenta.datafordeler.core.database.Entity;

import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * Created by lars on 16-05-17.
 */
@Entity(name = "GladdrregRoadEntity")
@Table(name = "gladdrreg_road_entity", indexes = {
        @Index(name = "gladdrreg_road_identification", columnList = "identification_id")
})
public class RoadEntity extends Entity<RoadEntity, RoadRegistration> {

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
    public static final String schema = "Road";

    @Override
    protected RoadRegistration createEmptyRegistration() {
        return new RoadRegistration();
    }
}
