package dk.magenta.datafordeler.gladdrreg.data.municipality;

import dk.magenta.datafordeler.core.database.Registration;

import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/**
 * Created by lars on 16-05-17.
 */
@Entity
@Table(name = "gladdrreg_municipality_registration", indexes = {
        @Index(name = "gladdrreg_minicipality_entity", columnList = "entity_id"),
        @Index(name = "gladdrreg_municipality_registration_from", columnList = "registrationFrom"),
        @Index(name = "gladdrreg_municipality_registration_to", columnList = "registrationTo")
})
public class MunicipalityRegistration extends Registration<MunicipalityEntity, MunicipalityRegistration, MunicipalityEffect> {
    @Override
    protected MunicipalityEffect createEmptyEffect(OffsetDateTime effectFrom, OffsetDateTime effectTo) {
        return new MunicipalityEffect(this, effectFrom, effectTo);
    }
}
