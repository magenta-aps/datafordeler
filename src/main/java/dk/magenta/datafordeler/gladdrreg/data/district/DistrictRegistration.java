package dk.magenta.datafordeler.gladdrreg.data.district;

import dk.magenta.datafordeler.core.database.Registration;

import javax.persistence.Index;
import javax.persistence.Table;
import java.time.OffsetDateTime;

/**
 * Created by lars on 16-05-17.
 */
@javax.persistence.Entity
@Table(name="gladdrreg_district_registration", indexes = {
        @Index(name = "gladdrreg_district_entity", columnList = "entity_id"),
        @Index(name = "gladdrreg_district_registration_from", columnList = "registrationFrom"),
        @Index(name = "gladdrreg_district_registration_to", columnList = "registrationTo")
})
public class DistrictRegistration extends Registration<DistrictEntity, DistrictRegistration, DistrictEffect> {
    @Override
    protected DistrictEffect createEmptyEffect(OffsetDateTime effectFrom, OffsetDateTime effectTo) {
        return new DistrictEffect(this, effectFrom, effectTo);
    }
}
