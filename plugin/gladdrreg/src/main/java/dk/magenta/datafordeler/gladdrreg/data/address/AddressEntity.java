package dk.magenta.datafordeler.gladdrreg.data.address;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dk.magenta.datafordeler.core.database.Entity;

import javax.persistence.Index;
import javax.persistence.Table;

/**
 * Created by lars on 16-05-17.
 */
@javax.persistence.Entity
@Table(name = "gladdrreg_address_entity", indexes = {
        @Index(name = "gladdrreg_address_identification", columnList = "identification_id")
})
public class AddressEntity extends Entity<AddressEntity, AddressRegistration> {

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
    public static final String schema = "Address";

    @Override
    protected AddressRegistration createEmptyRegistration() {
        return new AddressRegistration();
    }
}
