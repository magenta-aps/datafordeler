package dk.magenta.datafordeler.gladdrreg.data.address;

import dk.magenta.datafordeler.core.database.Effect;

import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/**
 * Created by lars on 16-05-17.
 */
@javax.persistence.Entity
@Table(name = "gladdrreg_address_effect")
public class AddressEffect extends Effect<AddressRegistration, AddressEffect, AddressData> {
    public AddressEffect() {

    }

    public AddressEffect(AddressRegistration registration, OffsetDateTime effectFrom, OffsetDateTime effectTo) {
        super(registration, effectFrom, effectTo);
    }
}
