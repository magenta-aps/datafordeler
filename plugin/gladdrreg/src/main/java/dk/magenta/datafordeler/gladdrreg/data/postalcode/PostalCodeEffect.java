package dk.magenta.datafordeler.gladdrreg.data.postalcode;

import dk.magenta.datafordeler.core.database.Effect;

import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/**
 * Created by lars on 16-05-17.
 */
@Entity
@Table(name = "gladdrreg_postalcode_effect")
public class PostalCodeEffect extends Effect<PostalCodeRegistration, PostalCodeEffect, PostalCodeData> {
    public PostalCodeEffect() {

    }

    public PostalCodeEffect(PostalCodeRegistration registration, OffsetDateTime effectFrom, OffsetDateTime effectTo) {
        super(registration, effectFrom, effectTo);
    }
}
