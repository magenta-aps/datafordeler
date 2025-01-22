package dk.magenta.datafordeler.gladdrreg.data.municipality;

import dk.magenta.datafordeler.core.database.Effect;

import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/**
 * Created by lars on 16-05-17.
 */
@Entity
@Table(name = "gladdrreg_municipality_effect")
public class MunicipalityEffect extends Effect<MunicipalityRegistration, MunicipalityEffect, MunicipalityData> {
    public MunicipalityEffect() {

    }

    public MunicipalityEffect(MunicipalityRegistration registration, OffsetDateTime effectFrom, OffsetDateTime effectTo) {
        super(registration, effectFrom, effectTo);
    }
}
