package dk.magenta.datafordeler.gladdrreg.data.postalcode;

import dk.magenta.datafordeler.core.database.Registration;

import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/**
 * Created by lars on 16-05-17.
 */
@Entity
@Table(name = "gladdrreg_postalcode_registration", indexes = {
        @Index(name = "gladdrreg_postalcode_entity", columnList = "entity_id"),
        @Index(name = "gladdrreg_postalcode_registration_from", columnList = "registrationFrom"),
        @Index(name = "gladdrreg_postalcode_registration_to", columnList = "registrationTo")
})
public class PostalCodeRegistration extends Registration<PostalCodeEntity, PostalCodeRegistration, PostalCodeEffect> {
    @Override
    protected PostalCodeEffect createEmptyEffect(OffsetDateTime effectFrom, OffsetDateTime effectTo) {
        return new PostalCodeEffect(this, effectFrom, effectTo);
    }
}
