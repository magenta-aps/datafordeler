package dk.magenta.datafordeler.gladdrreg.data.road;

import dk.magenta.datafordeler.core.database.Effect;

import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/**
 * Created by lars on 16-05-17.
 */
@Entity
@Table(name = "gladdrreg_road_effect")
public class RoadEffect extends Effect<RoadRegistration, RoadEffect, RoadData> {
    public RoadEffect() {

    }

    public RoadEffect(RoadRegistration registration, OffsetDateTime effectFrom, OffsetDateTime effectTo) {
        super(registration, effectFrom, effectTo);
    }
}
