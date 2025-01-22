package dk.magenta.datafordeler.gladdrreg.data.locality;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dk.magenta.datafordeler.core.database.Entity;

import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * Created by lars on 16-05-17.
 */
@javax.persistence.Entity
@Table(name = "gladdrreg_locality_entity", indexes = {
        @Index(name = "gladdrreg_locality_identification", columnList = "identification_id")
})
public class LocalityEntity extends Entity<LocalityEntity, LocalityRegistration> {

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
    public static final String schema = "Locality";

    @Override
    protected LocalityRegistration createEmptyRegistration() {
        return new LocalityRegistration();
    }
}
