package dk.magenta.datafordeler.gladdrreg.data.locality;

import dk.magenta.datafordeler.core.database.Effect;

import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/**
 * Created by lars on 16-05-17.
 */
@Entity
@Table(name = "gladdrreg_locality_effect")
public class LocalityEffect extends Effect<LocalityRegistration, LocalityEffect, LocalityData> {
    public LocalityEffect() {

    }

    public LocalityEffect(LocalityRegistration registration, OffsetDateTime effectFrom, OffsetDateTime effectTo) {
        super(registration, effectFrom, effectTo);
    }
}
